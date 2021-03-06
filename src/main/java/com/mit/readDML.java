package com.mit;

import com.mit.dataStructure.foreignKey;
import com.mit.dataStructure.table_info;
import gudusoft.gsqlparser.*;
import gudusoft.gsqlparser.nodes.TAlterTableOption;
import gudusoft.gsqlparser.nodes.TObjectNameList;
import gudusoft.gsqlparser.stmt.TAlterTableStatement;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by peijialing on 27/9/2017.
 */
public class readDML {
    public static TStatementList statementList = new TStatementList();
    public static int searchForTable(String tableName)  {
        //traverse; may add advanced search method
        int res = 0;
        try {
            for (int i = 0; i < AssistMainApp.tableList.size(); ++i) {
                if (AssistMainApp.tableList.get(i).tableName.equals(tableName)) {
                    res = i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    public static void dealWithDML(TAlterTableStatement alterStatement) {

        String tableName = alterStatement.getTableName().toString();
        int tableId = searchForTable(tableName);
        //identify corresponding db components
        identification.fillAlterInfo(alterStatement);
        //schema mapping in internal structure
        for(int i=0;i<alterStatement.getAlterTableOptionList().size();i++){
            TAlterTableOption alterOp = alterStatement.getAlterTableOptionList().getAlterTableOption(i);
            switch (alterOp.getOptionType()) {
                //todo: add more cases
                case AddColumn:
                    for (int j=0; j<alterOp.getColumnDefinitionList().size();++j) {
                        //map to new schema
                        AssistMainApp.tableList.get(tableId).columnNameList.add(alterOp.getColumnDefinitionList().getColumn(j).getColumnName().toString());


                    }
                    break;
                case DropColumn:
                    System.out.println("========debug=========");
                    TObjectNameList tmp = alterOp.getColumnNameList();
                    AssistMainApp.tableList.get(tableId).columnNameList.remove(alterOp.getColumnNameList().toString());
                    System.out.println("========debug=========");
                    break;
                case RenameColumn:

                    AssistMainApp.tableList.get(tableId).columnNameList.remove(alterOp.getColumnName().toString());
                    AssistMainApp.tableList.get(tableId).columnNameList.add(alterOp.getNewColumnName().toString());
                    ArrayList<String> primKeyList = AssistMainApp.tableList.get(tableId).primaryKey;
                    if (primKeyList.contains(alterOp.getColumnName().toString())){
                        primKeyList.remove(alterOp.getColumnName().toString());
                        primKeyList.add(alterOp.getNewColumnName().toString());
                    }
                    ArrayList<foreignKey> foreignKeyList = AssistMainApp.tableList.get(tableId).foreignKey;
                    for (int j=0; j<foreignKeyList.size();++j) {
                        if (foreignKeyList.get(j).keyName.contains(alterOp.getColumnName().toString())){
                            int index = foreignKeyList.get(j).keyName.indexOf(alterOp.getColumnName().toString());
                            foreignKeyList.get(j).keyName.set(index, alterOp.getNewColumnName().toString());
                            System.out.print("========debug===========");
                        }
                    }

                    break;

            }
            //ddlparser.printAlterTableOption(alterStatement.getAlterTableOptionList().getAlterTableOption(i));
        }
    }
    public static void main(String[] args) {
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
            statementList = sqlparser.sqlstatements;
            for(int i=0;i<sqlparser.sqlstatements.size();i++){
                TCustomSqlStatement stmt = sqlparser.sqlstatements.get(i);
                ESqlStatementType type = stmt.sqlstatementtype;
                ddlparser.analyzeStmt(stmt);
                ddlparser.analyzeAlterTableStmt((TAlterTableStatement) sqlparser.sqlstatements.get(i));
                dealWithDML((TAlterTableStatement) sqlparser.sqlstatements.get(i));
                System.out.println("");
            }
        }else{
            System.out.println(sqlparser.getErrormessage());
        }
    }
}
