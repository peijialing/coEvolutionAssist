package com.mit;
import com.mit.dataStructure.*;
import com.sun.org.apache.xpath.internal.operations.Variable;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by peijialing on 23/8/2017.
 */
public class rewriting {
    public static String replaceTableName(String oldSqlText)
    {

        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);

        sqlparser.sqltext = oldSqlText;
//        sqlparser.sqltext        = "select table1.col1, table2.col2\n" +
//                "from table1, table2\n" +
//                "where table1.foo > table2.foo";

        System.out.println("input sql:");
        System.out.println(sqlparser.sqltext);
        String newSqlText = new String();
        int ret = sqlparser.parse();
        if (ret == 0){

            TSelectSqlStatement select = (TSelectSqlStatement)sqlparser.sqlstatements.get(0);

            TTable t ;
            for(int i=0;i<select.tables.size();i++){
                t = select.tables.getTable(i);
                if (t.toString().compareToIgnoreCase("table2") == 0){
                    for(int j=0;j<t.getObjectNameReferences().size();j++){
                        if(t.getObjectNameReferences().getObjectName(j).getObjectToken().toString().equalsIgnoreCase("table2")){
                            t.getObjectNameReferences().getObjectName(j).getObjectToken().astext = "table3";
                        }
                    }
                    t.setString("(tableX join tableY using (id)) as table3");
                }
            }

            System.out.println("\noutput sql:");
            System.out.println(select.toString());
            newSqlText = select.toString();


        }else{
            System.out.println(sqlparser.getErrormessage());
        }
        return newSqlText;
    }

    public static ArrayList<Integer> searchRelatedQuery(ArrayList<TwoTuple<String, ArrayList<String>>> tableList, String fileName) {
        ArrayList<Integer> resArr = new ArrayList<Integer>();
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();

        try {
            br = new BufferedReader(new FileReader(fileName));
            int countLineNumber = 0;
            while ((line = br.readLine()) != null) {
                for (int i=0;i<tableList.size();++i) {
                    String tableName = tableList.get(i).getFirst();
                    if (line.contains(tableName)) {
                        resArr.add(countLineNumber);
                    }
                }
                countLineNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return resArr;
    }

    public static void rewrite(String fileName, String newFileName) {
        ArrayList<alter_info> alterInfoList = identification.alterInfoList;
        for (int i=0; i<alterInfoList.size();++i) {
            alter_info info = alterInfoList.get(i);
            ArrayList<Integer> changePositionArr = new ArrayList<Integer>();
            switch (info.type) {
                case AddColumn:
                    //do nothing;
                    break;
                case DropColumn:
                    break;
                case RenameColumn:
                    JoinPath jp = info.joinPath;
                    //changePositionArr = searchRelatedQuery(jp.tableAndColumns,fileName);
                    //rewrite
                    BufferedReader br = null;
                    String line = null;
                    String newLine = null;
                    StringBuffer buf = new StringBuffer();
                    try {
                        br = new BufferedReader(new FileReader(fileName));
                        while ((line = br.readLine()) != null) {
                            newLine = replaceTableName(line);
                            buf.append(newLine);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                br = null;
                            }
                        }
                    }
                    FileModify fileModifier = new FileModify();
                    fileModifier.write(newFileName,buf.toString());
                    break;

            }

        }
    }
}
