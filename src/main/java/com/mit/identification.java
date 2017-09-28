package com.mit;

import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.TwoTuple;
import com.mit.dataStructure.alter_info;
import gudusoft.gsqlparser.EAlterTableOptionType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TStatementList;
import gudusoft.gsqlparser.nodes.TAlterTableOption;
import gudusoft.gsqlparser.stmt.TAlterTableStatement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by peijialing on 23/8/2017.
 */
public class identification {
    public static ArrayList<alter_info> alterInfoList = new ArrayList<alter_info>();
    public static void fillAlterInfo(TAlterTableStatement stmt) {
        for (int i = 0; i < stmt.getAlterTableOptionList().size(); i++) {
            TAlterTableOption alterOp = stmt.getAlterTableOptionList().getAlterTableOption(i);
            String tableName = stmt.getTableName().toString();
            alter_info newInfo = new alter_info();
            newInfo.type = alterOp.getOptionType();
            switch (alterOp.getOptionType()) {
                case AddColumn:
                    //do nothing
                    break;
                case DropColumn:
                    //find join path
                    break;
                case RenameColumn:
                    //only have to add current table
                    ArrayList<String> columnList = new ArrayList<String>();
                    columnList.add(alterOp.getColumnName().toString());
                    newInfo.joinPath.tableAndColumns.add(new TwoTuple<String,ArrayList<String>> (tableName,columnList));

            }
            alterInfoList.add(newInfo);
        }
    }

    public static void main(TStatementList alterStmtList) {
        for (int i=0; i<alterStmtList.size(); ++i) {
            TCustomSqlStatement stmt = alterStmtList.get(i);

        }
    }
}
