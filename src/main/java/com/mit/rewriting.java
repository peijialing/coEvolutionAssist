package com.mit;
import com.mit.dataStructure.*;
import com.sun.org.apache.xpath.internal.operations.Variable;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceToken;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peijialing on 23/8/2017.
 */
public class rewriting {
    public static int MaintainLines = 0;
    public static String[] eliminatePrefix(String oldtext){
        String[] newText = null;
        if (oldtext.contains(".")){
            System.out.println(oldtext.split("\\.").length);
            newText = oldtext.split("\\.");
        }
        return newText;

    }
    public static String mergePrefix(String prefix, String colName){
        return prefix+"."+colName;
    }
    public static String replaceColName(String oldSqlText, JoinPath jp, TObjectName newCol){
        boolean changeFlag = false;
        TGSqlParser sqlParser = new TGSqlParser(EDbVendor.dbvoracle);
        String oldTableName = jp.tableAndColumns.get(0).first;
        String newSqlText = null;
        String oldColName = jp.tableAndColumns.get(0).second.get(0);
        sqlParser.sqltext = oldSqlText;
        System.out.println("inpupt sql:");
        System.out.println(sqlParser.sqltext);
        TObjectNameList columns = new TObjectNameList();
        int ret = sqlParser.parse();
        if (ret==0){
            TSelectSqlStatement select = (TSelectSqlStatement)sqlParser.sqlstatements.get(0);
            for(int i=0;i<select.tables.size();i++){
                TTable table = select.tables.getTable(i);
                String table_name = table.getName();
                System.out.println("Analyzing: "+ table_name +" <- "+ select.sqlstatementtype);
                for (int j=0; j < table.getLinkedColumns().size(); j++) {
                    TObjectName objectName = table.getLinkedColumns().getObjectName(j);
                    String currentColName = objectName.getColumnNameOnly();
                    if ((oldTableName.equalsIgnoreCase(table_name) || oldTableName.equalsIgnoreCase(table.getAliasName())) && oldColName.equalsIgnoreCase(currentColName)){
                        changeFlag = true;
                        objectName.setString(objectName.toString().split("\\.")[0]+"."+newCol);
                    }
                    String column_name = table_name +"."+ objectName.getColumnNameOnly().toLowerCase();
                    columns.addObjectName(objectName);
                    if (!objectName.isTableDetermined()) {
                        column_name = "?."+ objectName.getColumnNameOnly().toLowerCase();
                    }
                    System.out.println("Analyzing: "+ column_name +" in "+ select.sqlstatementtype +" "+ objectName.getLocation());
                }
            }

            //TResultColumnList columns = select.getResultColumnList();
//            for (int i=0; i<columns.size();++i){
//                System.out.println("========debug========");
//                System.out.println(columns.getObjectName(i).toString());
//                String prefix = eliminatePrefix(columns.getObjectName(i).toString())[0];
//                String colName = eliminatePrefix(columns.getObjectName(i).toString())[1];
//                if (colName.equalsIgnoreCase(oldColName)){
//                    changeFlag = true;
//                    columns.getObjectName(i).setString(mergePrefix(prefix,newColName));
//                }
//            }
            if (changeFlag) MaintainLines+=2;
            System.out.println("\noutput sql:");
            System.out.println(select.toString());
            newSqlText = select.toString();
        }
        else {
            System.out.println(sqlParser.getErrormessage());
        }
        return newSqlText;
    }
    public static String replaceTableName(String oldSqlText,table_info oldTableName, String newTableName)
    {
        boolean changeFlag = false;
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
                if (t.toString().compareToIgnoreCase(oldTableName.tableName) == 0){
                    changeFlag = true;
                    for(int j=0;j<t.getObjectNameReferences().size();j++){
                        TObjectNameList tmpNameList = t.getObjectNameReferences();
                        TObjectName tmpName = tmpNameList.getObjectName(j);
                        TSourceToken tmpToken = tmpName.getObjectToken();
                        String alias = oldTableName.tableName;
                        if (tmpToken==null)
                            continue;
                        else if(tmpToken.toString().equalsIgnoreCase(oldTableName.tableName) || tmpToken.toString().equalsIgnoreCase(t.getAliasName())){
                            t.getObjectNameReferences().getObjectName(j).getObjectToken().astext = newTableName;
                        }
                    }
                    t.setString(newTableName);
                }
            }
            if (changeFlag) MaintainLines += 2;
            System.out.println("\noutput sql:");
            System.out.println(select.toString());
            newSqlText = select.toString();


        }else{
            System.out.println(sqlparser.getErrormessage());
        }
        return newSqlText;
    }

    public static String dropColumn(String oldSqlText, TObjectName removeColName, TTable droppedColSourceTable){
        boolean changeFlag = false;
        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);
        sqlparser.sqltext = oldSqlText;
        System.out.println("input sql:");
        System.out.println(sqlparser.sqltext);
        String newSqlText = new String();
        String droppedColSourceTbName = droppedColSourceTable.getName();
        int ret = sqlparser.parse();
        if (ret == 0) {
            TSelectSqlStatement select = (TSelectSqlStatement) sqlparser.sqlstatements.get(0);
            //modify select objs
            TResultColumnList columns = select.getResultColumnList();

            for (int i=0; i<columns.size();++i){
                String colName = columns.getResultColumn(i).getColumnNameOnly();
                String tbName = columns.getResultColumn(i).getFieldAttr().getSourceTable().toString();
                String tbAlias = columns.getResultColumn(i).getFieldAttr().getSourceTable().getAliasName().toString();
                if ((tbName.equalsIgnoreCase(droppedColSourceTbName) || tbAlias.equalsIgnoreCase(droppedColSourceTbName)) && colName.equalsIgnoreCase(removeColName.toString())){
                    changeFlag = true;
                    columns.removeResultColumn(i);
                    break;
                }
            }
            //remove where conditions

            TWhereClause whereClause = select.getWhereClause();
            TExpression condition = whereClause.getCondition();
            ArrayList<TExpression>  expList = condition.getFlattedAndOrExprs();
            for (TExpression exp:expList){
                if (exp.toString().toLowerCase().contains(removeColName.toString().toLowerCase())){
                    exp.remove2();
                }
            }

            //remove join conditions
            TJoinList joinClauses = select.getJoins();

            for (int i=0;i<joinClauses.size();++i){
                TJoin joinClause = joinClauses.getJoin(i);
                TJoinItemList joinClauseJoinItems = joinClause.getJoinItems();
                for (int j=0;j<joinClauseJoinItems.size();++j){
                    TExpression con = joinClauseJoinItems.getJoinItem(j).getOnCondition();
                    ArrayList<TExpression> expListOfCon = con.getFlattedAndOrExprs();
                    if (expListOfCon==null){
                        continue;
                    }
                    for (TExpression expOfCon:expListOfCon){
                        if (expOfCon.toString().toLowerCase().contains(removeColName.toString().toLowerCase())){
                            expOfCon.remove2();
                        }
                    }
                }

            }



            if (changeFlag) MaintainLines += 2;
            System.out.println("\noutput sql:");
            System.out.println(select.toString());
            newSqlText = select.toString();
        } else{
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
            JoinPath jp = info.joinPath;
            MaintainLines = 0;
            switch (info.type) {
                case AddColumn:
                    //do nothing;
                    break;

                case RenameTable:

                    //changePositionArr = searchRelatedQuery(jp.tableAndColumns,fileName);
                    //rewrite
                    BufferedReader br = null;
                    String line = null;
                    String newLine = null;
                    StringBuffer buf = new StringBuffer();
                    try {
                        br = new BufferedReader(new FileReader(fileName));
                        while ((line = br.readLine()) != null) {
                            newLine = replaceTableName(line,info.oriTableName,info.replacedTableName)+"\n";
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

                    br = null;
                    line = null;
                    newLine = null;
                    buf = new StringBuffer();
                    try {
                        br = new BufferedReader(new FileReader(fileName));
                        while ((line = br.readLine()) != null) {
                            newLine = replaceColName(line,jp,info.replacedCol)+"\n";
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
                    fileModifier = new FileModify();
                    fileModifier.write(newFileName,buf.toString());
                    break;
                case DropColumn:
                    br = null;
                    line = null;
                    newLine = null;
                    buf = new StringBuffer();
                    try {
                        br = new BufferedReader(new FileReader(fileName));
                        while ((line = br.readLine()) != null) {
                            newLine = dropColumn(line,info.droppedCol,info.droppedColSouceTable)+"\n";
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
                    fileModifier = new FileModify();
                    fileModifier.write(newFileName,buf.toString());
                    break;
                case ChangeColumn:
                    break;
            }

        }
    }
}
