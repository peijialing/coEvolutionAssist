
package com.mit;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceToken;
import gudusoft.gsqlparser.TSourceTokenList;
import gudusoft.gsqlparser.nodes.TCTE;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TTableList;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class removeCondition
{

	private String result;

	public removeCondition( File sqlFile, EDbVendor vendor,
			Map<String, String> conditionMap )
	{
		TGSqlParser sqlparser = new TGSqlParser( vendor );
		sqlparser.setSqlfilename( sqlFile.getAbsolutePath( ) );
		remove( sqlparser, conditionMap );
	}

	public removeCondition( String sql, EDbVendor vendor,
			Map<String, String> conditionMap )
	{
		TGSqlParser sqlparser = new TGSqlParser( vendor );
		sqlparser.setSqltext( sql );
		remove( sqlparser, conditionMap );
	}

	void remove( TGSqlParser sqlparser, Map<String, String> conditionMap )
	{
		int i = sqlparser.parse( );
		if ( i == 0 )
		{
			TCustomSqlStatement stat = sqlparser.sqlstatements.get( 0 );
			result = stat.toString( );
			getParserString( stat, conditionMap );
			if ( result != null )
			{
				result = replaceCondition( result, conditionMap );
			}
		}
		else
			System.err.println( sqlparser.getErrormessage( ) );
	}

	String remove( TCustomSqlStatement stat, Map<String, String> conditionMap )
	{
		String clauseCondition = null;
		if ( stat.getResultColumnList( ) != null )
		{
			for ( int j = 0; j < stat.getResultColumnList( ).size( ); j++ )
			{
				TResultColumn column = stat.getResultColumnList( )
						.getResultColumn( j );
				if ( column.getExpr( ) != null
						&& column.getExpr( ).getSubQuery( ) instanceof TCustomSqlStatement )
				{
					TCustomSqlStatement query = (TCustomSqlStatement) column.getExpr( )
							.getSubQuery( );
					if ( getParserString( query, conditionMap ) )
						return null;
				}
			}
		}
		if ( stat.getCteList( ) != null )
		{
			for ( int i = 0; i < stat.getCteList( ).size( ); i++ )
			{
				TCTE cte = stat.getCteList( ).getCTE( i );
				if ( cte.getSubquery( ) != null )
				{
					if ( getParserString( cte.getSubquery( ), conditionMap ) )
						return null;
				}
				if ( cte.getInsertStmt( ) != null )
				{
					if ( getParserString( cte.getInsertStmt( ), conditionMap ) )
						return null;
				}
				if ( cte.getUpdateStmt( ) != null )
				{
					if ( getParserString( cte.getUpdateStmt( ), conditionMap ) )
						return null;
				}
				if ( cte.getPreparableStmt( ) != null )
				{
					if ( getParserString( cte.getPreparableStmt( ),
							conditionMap ) )
						return null;
				}
				if ( cte.getDeleteStmt( ) != null )
				{
					if ( getParserString( cte.getDeleteStmt( ), conditionMap ) )
						return null;
				}
			}
		}
		if ( stat.getStatements( ) != null && stat.getStatements( ).size( ) > 0 )
		{
			for ( int i = 0; i < stat.getStatements( ).size( ); i++ )
			{
				if ( getParserString( stat.getStatements( ).get( i ),
						conditionMap ) )
					return null;
			}
		}
		if ( stat.getReturningClause( ) != null )
		{
			if ( stat.getReturningClause( ).getColumnValueList( ) != null )
			{
				for ( int i = 0; i < stat.getReturningClause( )
						.getColumnValueList( )
						.size( ); i++ )
				{
					if ( stat.getReturningClause( )
							.getColumnValueList( )
							.getExpression( i )
							.getSubQuery( ) != null )
					{
						if ( getParserString( stat.getReturningClause( )
								.getColumnValueList( )
								.getExpression( i )
								.getSubQuery( ), conditionMap ) )
							return null;
					}
				}
			}
			if ( stat.getReturningClause( ).getVariableList( ) != null )
			{
				for ( int i = 0; i < stat.getReturningClause( )
						.getVariableList( )
						.size( ); i++ )
				{
					if ( stat.getReturningClause( )
							.getVariableList( )
							.getExpression( i )
							.getSubQuery( ) != null )
					{
						if ( getParserString( stat.getReturningClause( )
								.getVariableList( )
								.getExpression( i )
								.getSubQuery( ), conditionMap ) )
							return null;
					}
				}
			}
		}
		if ( stat instanceof TSelectSqlStatement )
		{
			TTableList list = ( (TSelectSqlStatement) stat ).tables;
			for ( int i = 0; i < list.size( ); i++ )
			{
				TTable table = list.getTable( i );
				if ( table.getSubquery( ) != null )
				{
					if ( getParserString( table.getSubquery( ), conditionMap ) )
						return null;
				}
				if ( table.getFuncCall( ) != null )
				{
					ExpressionChecker w = new ExpressionChecker( this );
					w.checkFunctionCall( table.getFuncCall( ), conditionMap );
				}
			}
		}

		if ( stat.getWhereClause( ) != null
				&& stat.getWhereClause( ).getCondition( ) != null )
		{
			String oldString = stat.toString( );
			StringBuffer buffer = new StringBuffer( );
			TExpression whereExpression = stat.getWhereClause( ).getCondition( );
			ExpressionChecker w = new ExpressionChecker( this );
			w.checkExpression( whereExpression, conditionMap );
			String newString = stat.toString( );
			if ( !oldString.equals( newString ) )
			{
				if ( whereExpression != null )
				{
					if ( whereExpression.toString( ) != null )
					{
						String prefix = getStmtPrefix( stat,
								stat.getWhereClause( ).getStartToken( ) );
						clauseCondition = whereExpression.toString( ).trim( );
						whereExpression.remove( );
						String suffix = getStmtSuffix( stat,
								stat.getWhereClause( ).getEndToken( ),
								false );
						buffer.append( prefix )
								.append( stat.getWhereClause( ).toString( ) )
								.append( clauseCondition )
								.append( suffix );
					}
					else
					{
						String prefix = getStmtPrefix( stat,
								stat.getWhereClause( ).getStartToken( ) );
						String suffix = getStmtSuffix( stat,
								stat.getWhereClause( ).getEndToken( ),
								true );
						buffer.append( prefix ).append( suffix );
					}
				}
				return buffer.toString( );
			}
		}
		if ( ( stat instanceof TSelectSqlStatement )
				&& ( (TSelectSqlStatement) stat ).getGroupByClause( ) != null
				&& ( (TSelectSqlStatement) stat ).getGroupByClause( )
						.getHavingClause( ) != null )
		{
			String oldString = stat.toString( );
			StringBuffer buffer = new StringBuffer( );
			TExpression havingExpression = ( (TSelectSqlStatement) stat ).getGroupByClause( )
					.getHavingClause( );
			ExpressionChecker w = new ExpressionChecker( this );
			w.checkExpression( havingExpression, conditionMap );
			String newString = stat.toString( );
			if ( !oldString.equals( newString ) )
			{
				if ( havingExpression != null )
				{
					if ( havingExpression.toString( ) != null )
					{
						String prefix = getStmtPrefix( stat,
								( (TSelectSqlStatement) stat ).getGroupByClause( )
										.getHavingClause( )
										.getStartToken( ) );
						clauseCondition = havingExpression.toString( ).trim( );
						havingExpression.remove( );

						if ( havingExpression.toString( ) != null )
						{
							String suffix = getStmtSuffix( stat,
									( (TSelectSqlStatement) stat ).getGroupByClause( )
											.getHavingClause( )
											.getEndToken( ),
									false );
							buffer.append( prefix )
									.append( ( (TSelectSqlStatement) stat ).getGroupByClause( )
											.getHavingClause( )
											.toString( ) )
									.append( clauseCondition )
									.append( suffix );
						}
						else
						{
							String suffix = getStmtSuffix( stat,
									( (TSelectSqlStatement) stat ).getGroupByClause( )
											.getHAVING( ),
									false );
							buffer.append( prefix )
									.append( clauseCondition )
									.append( suffix );
						}
					}
					else
					{
						String prefix = getStmtPrefix( stat,
								( (TSelectSqlStatement) stat ).getGroupByClause( )
										.getHAVING( ) );
						String suffix = getStmtSuffix( stat,
								( (TSelectSqlStatement) stat ).getGroupByClause( )
										.getHAVING( ),
								true );
						buffer.append( prefix ).append( suffix );
					}
				}
				return buffer.toString( );
			}
		}
		return stat.toString( );

	}

	private String getStmtSuffix( TCustomSqlStatement stat,
			TSourceToken clauseToken, boolean removeSpaces )
	{
		TSourceToken startToken = clauseToken;
		TSourceToken endToken = stat.getEndToken( );
		TSourceTokenList tokenList = startToken.container;
		StringBuffer suffixBuffer = new StringBuffer( );
		boolean flag = false;
		for ( int i = 0; i < tokenList.size( ); i++ )
		{
			TSourceToken token = tokenList.get( i );
			if ( !flag )
			{
				if ( token == startToken )
				{
					flag = true;
					// Remove the white space token, replace the where token
					// with the suffix token.
					if ( removeSpaces )
					{
						while ( ++i < tokenList.size( ) )
						{
							token = tokenList.get( i );
							String tokenText = token.toString( );
							if ( tokenText.trim( ).length( ) == 0 )
							{
								if ( token == endToken )
								{
									return suffixBuffer.toString( );
								}
								continue;
							}
							else
							{
								suffixBuffer.append( tokenText );
								if ( token == endToken )
								{
									return suffixBuffer.toString( );
								}
								break;
							}
						}
					}
				}
				continue;
			}
			suffixBuffer.append( token.toString( ) );
			if ( token == endToken )
			{
				break;
			}
		}
		return suffixBuffer.toString( );
	}

	private String getStmtPrefix( TCustomSqlStatement stat,
			TSourceToken clauseToken )
	{
		TSourceToken startToken = stat.getStartToken( );
		TSourceToken endToken = clauseToken;
		TSourceTokenList tokenList = startToken.container;
		StringBuffer prefixBuffer = new StringBuffer( );
		boolean flag = false;
		for ( int i = 0; i < tokenList.size( ); i++ )
		{
			TSourceToken token = tokenList.get( i );
			if ( !flag )
			{
				if ( token == startToken )
					flag = true;
				else
					continue;
			}
			if ( token == endToken )
				break;
			prefixBuffer.append( token.toString( ) );
		}
		String prefix = prefixBuffer.toString( );
		return prefix;
	}

	private boolean getParserString( TCustomSqlStatement query,
			Map<String, String> conditionMap )
	{
		String oldString = query.toString( );
		String newString = remove( query, conditionMap );
		if ( newString == null )
			return true;
		if ( newString != null && !oldString.equals( newString ) )
		{
			result = result.replace( oldString, newString );
			TGSqlParser parser = query.getGsqlparser( );
			parser.setSqltext( result );
			remove( parser, conditionMap );
			return true;
		}
		return false;
	}

	public String getRemoveResult( )
	{
		return result.toString( );
	}

	public static void main( String[] args )
	{
		String sql = "SELECT SUM (d.amt) \r\n"
				+ "FROM   summit.cntrb_detail d \r\n"
				+ "WHERE"
				+ " d.id = summit.mstr.id \r\n"
				+ "AND d.system_gift_type IN ( 'OG', 'PLP', 'PGP' ) \r\n"
				// + " d.fund_coll_attrb IN ( '$Institute$' ) \r\n"
				+ "AND d.fund_coll_attrb IN ( '$Institute$' ) \r\n"
				+ "AND d.fund_acct IN ( '$Fund$' ) \r\n"
				+ "AND d.cntrb_date >= '$From_Date$' \r\n"
				+ "AND d.cntrb_date <= '$Thru_Date$' \r\n"
				+ "GROUP  BY d.id; ";
		Map<String, String> conditionMap = new HashMap<String, String>( );
		conditionMap.put( "Institute", "ShanXi University" );
		conditionMap.put( "Fund", "Eclipse.org" );
		
//		File sql = new File( "C:\\test.sql" );
//		Map<String, String> conditionMap = new HashMap<String, String>( );
//		conditionMap.put( "Institute", "ShanXi University" );
//		conditionMap.put( "Fund", "Eclipse.org" );
//		conditionMap.put( "ContributionTypes", "types" );
//		conditionMap.put( "IncludeRelatedGiving", "relation" );
//		conditionMap.put( "GivingFromAmount", "amount1" );
//		conditionMap.put( "GivingThruAmount", "amount2" );

		removeCondition remove = new removeCondition( sql,
				EDbVendor.dbvmssql,
				conditionMap );
		System.out.println( remove.getRemoveResult( ) );
	}

	private String replaceCondition( String content,
			Map<String, String> conditionMap )
	{
		String[] conditions = new String[0];
		if ( conditionMap != null && !conditionMap.isEmpty( ) )
		{
			conditions = conditionMap.keySet( ).toArray( new String[0] );
		}
		Pattern pattern = Pattern.compile( "\\$[^$]+\\$",
				Pattern.CASE_INSENSITIVE );
		Matcher matcher = pattern.matcher( content );
		StringBuffer buffer = new StringBuffer( );
		while ( matcher.find( ) )
		{
			String condition = matcher.group( ).replaceAll( "\\$", "" ).trim( );
			for ( int i = 0; i < conditions.length; i++ )
			{
				if ( conditions[i].equalsIgnoreCase( condition )
						&& conditionMap.get( conditions[i] ) != null )
				{
					matcher.appendReplacement( buffer,
							conditionMap.get( conditions[i] ) );
					break;
				}
			}
		}
		matcher.appendTail( buffer );
		return buffer.toString( );
	}
}
