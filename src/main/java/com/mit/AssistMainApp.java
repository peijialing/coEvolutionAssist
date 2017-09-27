package com.mit;
import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.Req;
import com.mit.dataStructure.table_info;
import com.mit.rewriting;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.mit.ddlparser;
/**
 * Created by peijialing on 23/8/2017.
 */
public class AssistMainApp {
    //for whole structure, pls refer to my google doc https://docs.google.com/document/d/19YpWNRyfE9EhLNTvxr3D8i-oomhn994B4P45AUSPyPc/edit
    public static ArrayList<table_info> tableList = new ArrayList<table_info>();
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
                        tableList.add(ddlparser.analyzeCreateTableStmt((TCreateTableSqlStatement) sqlparser.sqlstatements.get(i)));

                System.out.println("");
            }
        }else{
            System.out.println(sqlparser.getErrormessage());
        }
        //read dml and create a new schema in our internal representation
        String[] dmlFileName = new String[1];
        dmlFileName[0] = "/Users/peijialing/Desktop/test_alter.sql";
        readDML.main(dmlFileName);
    }


}
