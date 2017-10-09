package com.mit.dataStructure;

import java.util.ArrayList;
import com.mit.dataStructure.TwoTuple;
/**
 * Created by peijialing on 21/9/2017.
 */
public class table_info {
    public table_info() {
        tableName = new String();
        aliasName = null;
        columnNameList = new ArrayList<String>();
        primaryKey = new ArrayList<String>();
        foreignKey = new ArrayList<foreignKey>();
    }
    public String tableName;
    public String aliasName;
    public ArrayList<String> columnNameList;
    public ArrayList<String> primaryKey;
    public ArrayList<foreignKey> foreignKey;
}
