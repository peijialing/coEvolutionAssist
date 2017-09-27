package com.mit;

import com.mit.dataStructure.table_info;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TAlterTableOption;
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
        for(int i=0;i<alterStatement.getAlterTableOptionList().size();i++){
            TAlterTableOption alterOp = alterStatement.getAlterTableOptionList().getAlterTableOption(i);
            switch (alterOp.getOptionType()) {
                //todo: add more cases
                case AddColumn:
                    for (int j=0; j<alterOp.getColumnDefinitionList().size();++j) {
                        AssistMainApp.tableList.get(tableId).columnNameList.add(alterOp.getColumnDefinitionList().getColumn(j).getColumnName().toString());

                    }
                    break;
                case DropColumn:
                    System.out.println("========debug=========");
                    AssistMainApp.tableList.get(tableId).columnNameList.remove(alterOp.getColumnName().toString());
                    System.out.println("========debug=========");

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
            for(int i=0;i<sqlparser.sqlstatements.size();i++){
                TCustomSqlStatement stmt = sqlparser.sqlstatements.get(i);
                ddlparser.analyzeAlterTableStmt((TAlterTableStatement) sqlparser.sqlstatements.get(i));
                dealWithDML((TAlterTableStatement) sqlparser.sqlstatements.get(i));
                System.out.println("");
            }
        }else{
            System.out.println(sqlparser.getErrormessage());
        }
    }
}
