package com.mit;
import com.mit.dataStructure.*;
import com.sun.org.apache.xpath.internal.operations.Variable;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceToken;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TObjectNameList;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by peijialing on 23/8/2017.
 */
public class rewriting {
    public static String replaceTableName(String oldSqlText,String oldTableName, String newTableName)
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
                if (t.toString().compareToIgnoreCase(oldTableName) == 0){
                    for(int j=0;j<t.getObjectNameReferences().size();j++){
                        TObjectNameList tmpNameList = t.getObjectNameReferences();
                        TObjectName tmpName = tmpNameList.getObjectName(j);
                        TSourceToken tmpToken = tmpName.getObjectToken();
                        if (tmpToken==null)
                            continue;
                        else if(t.getObjectNameReferences().getObjectName(j).getObjectToken().toString().equalsIgnoreCase(oldTableName)){
                            t.getObjectNameReferences().getObjectName(j).getObjectToken().astext = newTableName;
                        }
                    }
                    t.setString(newTableName);
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
                case RenameTable:
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
                            newLine = replaceTableName(line,info.oriTableName,info.replacedTableName);
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
                case RenameColumn:
                    break;

            }

        }
    }
}
