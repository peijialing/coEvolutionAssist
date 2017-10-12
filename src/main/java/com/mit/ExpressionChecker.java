
package com.mit;

import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TStatementList;
import gudusoft.gsqlparser.nodes.IExpressionVisitor;
import gudusoft.gsqlparser.nodes.TCaseExpression;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TExpressionList;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TOrderByItemList;
import gudusoft.gsqlparser.nodes.TParseTreeNode;
import gudusoft.gsqlparser.nodes.TWhenClauseItem;
import gudusoft.gsqlparser.nodes.TWhenClauseItemList;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionChecker implements IExpressionVisitor
{

	private Map<String, String> conditionMap;
	private removeCondition removeCondition;

	public ExpressionChecker( removeCondition removeCondition )
	{
		this.removeCondition = removeCondition;
	}

	public void checkExpression( TExpression expr,
			Map<String, String> conditionMap )
	{
		this.conditionMap = conditionMap;
		expr.inOrderTraverse( this );
	}

	boolean is_compare_condition( EExpressionType t )
	{
		return ( ( t == EExpressionType.simple_comparison_t )
				|| ( t == EExpressionType.group_comparison_t )  );
	}

	public boolean exprVisit( TParseTreeNode pnode, boolean pIsLeafNode )
	{
		TExpression expression = (TExpression) pnode;
		if ( is_compare_condition( expression.getExpressionType( ) ) )
		{
			TExpression leftExpr = (TExpression) expression.getLeftOperand( );
			TExpression rightExpr = (TExpression) expression.getRightOperand( );
			if ( leftExpr != null && !checkCondition( leftExpr ) )
			{
				expression.remove( );
			}
			if ( rightExpr != null && !checkCondition( rightExpr ) )
			{
				expression.remove( );
			}
		}
		if ( expression.getExpressionType( ) == EExpressionType.between_t
				&& !checkCondition( expression ) )
		{
			expression.remove( );
		}
		if ( expression.getExpressionType( ) == EExpressionType.pattern_matching_t
				&& !checkCondition( expression ) )
		{
			expression.remove( );
		}
		if ( expression.getExpressionType( ) == EExpressionType.in_t
				&& !checkCondition( expression ) )
		{
			expression.remove( );
		}
		if ( expression.getFunctionCall( ) != null )
		{
			TFunctionCall func = (TFunctionCall) expression.getFunctionCall( );
			checkFunctionCall( func, conditionMap );
		}
		if ( expression.getSubQuery( ) instanceof TCustomSqlStatement )
		{
			removeCondition.remove( (TCustomSqlStatement) expression.getSubQuery( ),
					conditionMap );
		}
		if ( expression.getCaseExpression( ) != null )
		{
			TCaseExpression expr = expression.getCaseExpression( );
			TExpression conditionExpr = expr.getInput_expr( );
			if ( conditionExpr != null )
			{
				if ( conditionExpr.getSubQuery( ) != null )
				{
					removeCondition.remove( conditionExpr.getSubQuery( ),
							conditionMap );
				}
			}
			TExpression defaultExpr = expr.getElse_expr( );
			if ( defaultExpr != null )
			{
				if ( defaultExpr.getSubQuery( ) != null )
				{
					removeCondition.remove( defaultExpr.getSubQuery( ),
							conditionMap );
				}
			}
			TStatementList defaultStatList = expr.getElse_statement_list( );
			if ( defaultStatList != null && defaultStatList.size( ) > 0 )
			{
				for ( int i = 0; i < defaultStatList.size( ); i++ )
				{
					removeCondition.remove( defaultStatList.get( i ),
							conditionMap );
				}
			}

			TWhenClauseItemList list = expr.getWhenClauseItemList( );
			if ( list != null && list.size( ) > 0 )
			{
				for ( int i = 0; i < list.size( ); i++ )
				{
					TWhenClauseItem item = list.getWhenClauseItem( i );
					if ( item.getComparison_expr( ) != null )
					{
						if ( item.getComparison_expr( ).getSubQuery( ) != null )
						{
							removeCondition.remove( item.getComparison_expr( )
									.getSubQuery( ), conditionMap );
						}
					}
					if ( item.getReturn_expr( ) != null )
					{
						if ( item.getReturn_expr( ).getSubQuery( ) != null )
						{
							removeCondition.remove( item.getReturn_expr( )
									.getSubQuery( ), conditionMap );
						}
					}
					TStatementList statList = expr.getElse_statement_list( );
					if ( statList != null && statList.size( ) > 0 )
					{
						for ( int j = 0; j < statList.size( ); j++ )
						{
							removeCondition.remove( statList.get( j ),
									conditionMap );
						}
					}
				}
			}
		}

		return true;
	}

	public void checkFunctionCall( TFunctionCall func,
			Map<String, String> conditionMap )
	{
		if ( func.getArgs( ) != null )
		{
			for ( int k = 0; k < func.getArgs( ).size( ); k++ )
			{
				TExpression expr = func.getArgs( ).getExpression( k );
				if ( expr.getSubQuery( ) != null )
				{
					removeCondition.remove( expr.getSubQuery( ), conditionMap );
				}
			}
		}
		if ( func.getAnalyticFunction( ) != null )
		{
			TExpressionList list = func.getAnalyticFunction( )
					.getPartitionBy_ExprList( );
			if ( list != null && list.size( ) > 0 )
			{
				for ( int i = 0; i < list.size( ); i++ )
				{
					TExpression expr = list.getExpression( i );
					if ( expr.getSubQuery( ) != null )
					{
						removeCondition.remove( expr.getSubQuery( ),
								conditionMap );
					}
				}
			}
			if ( func.getAnalyticFunction( ).getOrderBy( ) != null )
			{
				TOrderByItemList orderByItemList = func.getAnalyticFunction( )
						.getOrderBy( )
						.getItems( );
				if ( orderByItemList != null && orderByItemList.size( ) > 0 )
				{
					for ( int i = 0; i < orderByItemList.size( ); i++ )
					{
						TExpression sortKey = orderByItemList.getOrderByItem( i )
								.getSortKey( );
						if ( sortKey.getSubQuery( ) != null )
						{
							removeCondition.remove( sortKey.getSubQuery( ),
									conditionMap );
						}
					}
				}
			}
		}
	}

	private boolean checkCondition( TExpression expression )
	{
		String[] conditions = new String[0];
		if ( conditionMap != null && !conditionMap.isEmpty( ) )
		{
			conditions = conditionMap.keySet( ).toArray( new String[0] );
		}
		String expr = expression.toString( );
		if ( expr == null )
			return false;
		Pattern pattern = Pattern.compile( "\\$[^$]+\\$",
				Pattern.CASE_INSENSITIVE );
		Matcher matcher = pattern.matcher( expr );
		StringBuffer buffer = new StringBuffer( );
		while ( matcher.find( ) )
		{
			String condition = matcher.group( ).replaceAll( "\\$", "" ).trim( );
			boolean flag = false;
			for ( int i = 0; i < conditions.length; i++ )
			{
				if ( conditions[i].equalsIgnoreCase( condition )
						&& conditionMap.get( conditions[i] ) != null )
				{
					flag = true;
					matcher.appendReplacement( buffer,
							conditionMap.get( conditions[i] ) );
					break;
				}
			}
			if ( !flag )
				return false;
		}
		matcher.appendTail( buffer );
		if ( !expression.toString( ).equals( buffer.toString( ) ) )
			expression.setString( buffer.toString( ) );
		return true;
	}
}
