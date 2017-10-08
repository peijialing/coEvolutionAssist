package com.mit;

/**
 * Created by peijialing on 23/9/2017.
 */

import com.mit.dataStructure.TwoTuple;
import com.mit.dataStructure.foreignKey;
import com.mit.dataStructure.table_info;
import gudusoft.gsqlparser.*;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.stmt.TAlterTableStatement;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.TCreateViewSqlStatement;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
* Parsing DDL
* 1. parsing create table.
*/

public class ddlparser {
    public static void main(String args[])
    {

        if (args.length != 1){
            System.out.println("Usage: java parsingDDL sqlfile.sql");
            return;
        }
        File file=new File(args[0]);
        if (!file.exists()){
            System.out.println("File not exists:"+args[0]);
            return;
        }

        EDbVendor dbVendor = EDbVendor.dbvoracle;
        String msg = "Please select SQL dialect: 1: SQL Server, 2: Oralce, 3: MySQL, 4: DB2, 5: PostGRESQL, 6: Teradta, default is 2: Oracle";
        System.out.println(msg);

        BufferedReader br=new   BufferedReader(new InputStreamReader(System.in));
        try{
            int db = Integer.parseInt(br.readLine());
            if (db == 1){
                dbVendor = EDbVendor.dbvmssql;
            }else if(db == 2){
                dbVendor = EDbVendor.dbvoracle;
            }else if(db == 3){
                dbVendor = EDbVendor.dbvmysql;
            }else if(db == 4){
                dbVendor = EDbVendor.dbvdb2;
            }else if(db == 5){
                dbVendor = EDbVendor.dbvpostgresql;
            }else if(db == 6){
                dbVendor = EDbVendor.dbvteradata;
            }
        }catch(IOException i) {
        }catch (NumberFormatException numberFormatException){
        }

        System.out.println("Selected SQL dialect: "+dbVendor.toString());

        TGSqlParser sqlparser = new TGSqlParser(dbVendor);

        sqlparser.sqlfilename  = args[0];

        int ret = sqlparser.parse();
        if (ret == 0){
            for(int i=0;i<sqlparser.sqlstatements.size();i++){
                analyzeStmt(sqlparser.sqlstatements.get(i));
                System.out.println("");
            }
        }else{
            System.out.println(sqlparser.getErrormessage());
        }
    }

    protected static void analyzeStmt(TCustomSqlStatement stmt){

        switch(stmt.sqlstatementtype){
            case sstupdate:
                analyzeUpdateStmt((TUpdateSqlStatement)stmt);
                break;
            case sstcreatetable:
                table_info newTable = analyzeCreateTableStmt((TCreateTableSqlStatement)stmt);
                break;
            case sstaltertable:
                analyzeAlterTableStmt((TAlterTableStatement) stmt);
                break;
            case sstcreateview:
                analyzeCreateViewStmt((TCreateViewSqlStatement)stmt);
                break;
            default:
                System.out.println(stmt.sqlstatementtype.toString());
        }

    }

    protected static void printConstraint(TConstraint constraint, Boolean outline){

        if (constraint.getConstraintName() != null){
            System.out.println("\t\tconstraint name:"+constraint.getConstraintName().toString());
        }

        switch(constraint.getConstraint_type()){
            case notnull:
                System.out.println("\t\tnot null");
                break;
            case primary_key:
                System.out.println("\t\tprimary key");
                if (outline){
                    String lcstr = "";
                    if (constraint.getColumnList() != null){
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            lcstr = lcstr+constraint.getColumnList().getElement(k).toString();
                        }
                        System.out.println("\t\tprimary key columns:"+lcstr);
                    }
                }
                break;
            case unique:
                System.out.println("\t\tunique key");
                if(outline){
                    String lcstr="";
                    if (constraint.getColumnList() != null){
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            lcstr = lcstr+constraint.getColumnList().getElement(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:"+lcstr);
                }
                break;
            case check:
                System.out.println("\t\tcheck:"+constraint.getCheckCondition().toString());
                break;
            case foreign_key:
            case reference:
                System.out.println("\t\tforeign key");
                if(outline){
                    String lcstr="";
                    if (constraint.getColumnList() != null){
                        System.out.println("colsize");
                        System.out.println(constraint.getColumnList().size());
                        constraint.getColumnList();
                        for(int k=0;k<constraint.getColumnList().size();k++){
                            if (k !=0 ){lcstr = lcstr+",";}
                            TPTNodeList key = constraint.getColumnList();
                            lcstr = lcstr+constraint.getColumnList().getElement(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:"+lcstr);
                }
                System.out.println("\t\treferenced table:"+constraint.getReferencedObject().toString());
                if (constraint.getReferencedColumnList() != null){
                    String lcstr="";
                    for(int k=0;k<constraint.getReferencedColumnList().size();k++){
                        if (k !=0 ){lcstr = lcstr+",";}
                        lcstr = lcstr+constraint.getReferencedColumnList().getObjectName(k).toString();
                    }
                    System.out.println("\t\treferenced columns:"+lcstr);
                }
                break;
            default:
                break;
        }
    }

    protected static void printObjectNameList(TObjectNameList objList){
        for(int i=0;i<objList.size();i++){
            System.out.println(objList.getObjectName(i).toString());
        }

    }
    protected static void printColumnDefinitionList(TColumnDefinitionList cdl){
        for(int i=0;i<cdl.size();i++){
            System.out.println(cdl.getColumn(i).getColumnName());
        }
    }
    protected static void printConstraintList(TConstraintList cnl){
        for(int i=0;i<cnl.size();i++){
            printConstraint(cnl.getConstraint(i),true);
        }
    }

    protected static void printAlterTableOption(TAlterTableOption ato){
        System.out.println(ato.getOptionType());
        switch (ato.getOptionType()){
            case AddColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case ModifyColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case AlterColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case DropColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case SetUnUsedColumn:  //oracle
                printObjectNameList(ato.getColumnNameList());
                break;
            case DropUnUsedColumn:
                break;
            case DropColumnsContinue:
                break;
            case RenameColumn:
                System.out.println("rename "+ato.getColumnName().toString()+" to "+ato.getNewColumnName().toString());
                break;
            case ChangeColumn:   //MySQL
                System.out.println(ato.getColumnName().toString());
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case RenameTable:   //MySQL
                System.out.println(ato.getColumnName().toString());
                break;
            case AddConstraint:
                printConstraintList(ato.getConstraintList());
                break;
            case AddConstraintIndex:    //MySQL
                if (ato.getColumnName() != null){
                    System.out.println(ato.getColumnName().toString());
                }
                printObjectNameList(ato.getColumnNameList());
                break;
            case AddConstraintPK:
            case AddConstraintUnique:
            case AddConstraintFK:
                if (ato.getConstraintName() != null){
                    System.out.println(ato.getConstraintName().toString());
                }
                printObjectNameList(ato.getColumnNameList());
                break;
            case ModifyConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case RenameConstraint:
                System.out.println("rename "+ato.getConstraintName().toString()+" to "+ato.getNewConstraintName().toString());
                break;
            case DropConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintPK:
                break;
            case DropConstraintFK:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintUnique:
                if (ato.getConstraintName() != null){ //db2
                    System.out.println(ato.getConstraintName());
                }

                if (ato.getColumnNameList() != null){//oracle
                    printObjectNameList(ato.getColumnNameList());
                }
                break;
            case DropConstraintCheck: //db2
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintPartitioningKey:
                break;
            case DropConstraintRestrict:
                break;
            case DropConstraintIndex:
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintKey:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintFK:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintCheck:
                System.out.println(ato.getConstraintName());
                break;
            case CheckConstraint:
                break;
            case OraclePhysicalAttrs:
            case toOracleLogClause:
            case OracleTableP:
            case MssqlEnableTrigger:
            case MySQLTableOptons:
            case Db2PartitioningKeyDef:
            case Db2RestrictOnDrop:
            case Db2Misc:
            case Unknown:
                break;
        }

    }

    protected static void analyzeCreateViewStmt(TCreateViewSqlStatement pStmt){
        TCreateViewSqlStatement createView = pStmt;
        System.out.println("View name:"+createView.getViewName().toString());
        TViewAliasClause aliasClause = createView.getViewAliasClause();
        for(int i=0;i<aliasClause.getViewAliasItemList().size();i++){
            System.out.println("View alias:"+aliasClause.getViewAliasItemList().getViewAliasItem(i).toString());
        }

        System.out.println("View subquery: \n"+ createView.getSubquery().toString() );
    }

    protected static void analyzeUpdateStmt(TUpdateSqlStatement pStmt){
        System.out.println("Table Name:"+pStmt.getTargetTable().toString());
        System.out.println("set clause:");
        for(int i=0;i<pStmt.getResultColumnList().size();i++){
            TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
            TExpression expression = resultColumn.getExpr();
            System.out.println("\tcolumn:"+expression.getLeftOperand().toString()+"\tvalue:"+expression.getRightOperand().toString());
        }
        if(pStmt.getWhereClause() != null){
            System.out.println("where clause:\n"+pStmt.getWhereClause().getCondition().toString());
        }
    }

    protected static void analyzeAlterTableStmt(TAlterTableStatement pStmt){
        System.out.println("Table Name:"+pStmt.getTableName().toString());
        System.out.println("Alter table options:");

        for(int i=0;i<pStmt.getAlterTableOptionList().size();i++){
            TAlterTableOption temp = pStmt.getAlterTableOptionList().getAlterTableOption(i);
            //printAlterTableOption(pStmt.getAlterTableOptionList().getAlterTableOption(i));
        }
    }

    protected static table_info analyzeCreateTableStmt(TCreateTableSqlStatement pStmt){
        System.out.println("Table Name:"+pStmt.getTargetTable().toString());
        String tableName = pStmt.getTargetTable().toString();
        System.out.println("Columns:");
        table_info table = new table_info();
        ArrayList<String> coloumnList = new ArrayList<String>();
        ArrayList<String> primaryKey = new ArrayList<String>();
        ArrayList<foreignKey> foreignKeyList = new ArrayList<foreignKey>();
        TColumnDefinition column;
        for(int i=0;i<pStmt.getColumnList().size();i++){
            column = pStmt.getColumnList().getColumn(i);
            coloumnList.add(column.getColumnName().toString());
            System.out.println("\tname:"+column.getColumnName().toString());
            System.out.println("\tdatetype:"+column.getDatatype().toString());
            if (column.getDefaultExpression() != null){
                System.out.println("\tdefault:"+column.getDefaultExpression().toString());
            }
            if (column.isNull()){
                System.out.println("\tnull: yes");
            }
            if (column.getConstraints() != null){
                System.out.println("\tinline constraints:");
                for(int j=0;j<column.getConstraints().size();j++){
                    TConstraint constraint = column.getConstraints().getConstraint(j);
                    System.out.println(column.getConstraints().getConstraint(j).getConstraint_type());
                    switch (column.getConstraints().getConstraint(j).getConstraint_type()){
                        case primary_key:
                            primaryKey.add(column.getColumnName().toString());
                            break;
                        case foreign_key:
                            System.out.println("\t\tforeign key");
                            foreignKey fk = new foreignKey();
                            if (constraint.getColumnList() != null){
                                for(int k=0;k<constraint.getColumnList().size();k++){
                                    fk.keyName.add( constraint.getColumnList().getElement(k).toString());
                                }
                            }
                            fk.RefTable = constraint.getReferencedObject().toString();
                            System.out.println("\t\treferenced table:"+constraint.getReferencedObject().toString());
                            if (constraint.getReferencedColumnList() != null){
                                for(int k=0;k<constraint.getReferencedColumnList().size();k++){

                                    fk.RefAttr.add(constraint.getReferencedColumnList().getObjectName(k).toString());
                                }
                            }
                            break;
                        case reference:
                            System.out.println("\t\tforeign key");
                            fk = new foreignKey();
                            if (constraint.getColumnList() != null){
                                for(int k=0;k<constraint.getColumnList().size();k++){
                                    fk.keyName.add( constraint.getColumnList().getElement(k).toString());
                                }
                            }
                            fk.RefTable = constraint.getReferencedObject().toString();
                            System.out.println("\t\treferenced table:"+constraint.getReferencedObject().toString());
                            if (constraint.getReferencedColumnList() != null){
                                for(int k=0;k<constraint.getReferencedColumnList().size();k++){

                                    fk.RefAttr.add(constraint.getReferencedColumnList().getObjectName(k).toString());
                                }
                            }
                            break;

                    }
                    printConstraint(column.getConstraints().getConstraint(j),false);
                }
            }
            System.out.println("");
        }


        if(pStmt.getTableConstraints().size() > 0){
            System.out.println("\toutline constraints:");
            for(int i=0;i<pStmt.getTableConstraints().size();i++){
                TConstraint constraint = pStmt.getTableConstraints().getConstraint(i);
                switch (constraint.getConstraint_type()){
                    case foreign_key:
                    case reference:
                        System.out.println("\t\tforeign key");
                        foreignKey fk = new foreignKey();
                        if (constraint.getColumnList() != null){
                            for(int k=0;k<constraint.getColumnList().size();k++){
                                //TPTNodeList<TColumnWithSortOrder> t = constraint.getColumnList();
                                String kName = constraint.getColumnList().elementAt(k).getColumnName().toString();
                                //tring keyName = constraint.getColumnList().getElement(k).toString();
                                fk.keyName.add(kName);
                            }
                        }
                        fk.RefTable = constraint.getReferencedObject().toString();
                        System.out.println("\t\treferenced table:"+constraint.getReferencedObject().toString());
                        if (constraint.getReferencedColumnList() != null){
                            for(int k=0;k<constraint.getReferencedColumnList().size();k++){

                                fk.RefAttr.add(constraint.getReferencedColumnList().getObjectName(k).toString());
                            }
                        }
                        foreignKeyList.add(fk);
                        break;
                }
                printConstraint(pStmt.getTableConstraints().getConstraint(i), true);
                System.out.println("");
            }
        }
        table.tableName = tableName;
        table.columnNameList = coloumnList;
        table.primaryKey = primaryKey;
        table.foreignKey = foreignKeyList;
        return table;
    }

}