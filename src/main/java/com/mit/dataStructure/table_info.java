package com.mit.dataStructure;

import java.util.ArrayList;
import com.mit.dataStructure.TwoTuple;
/**
 * Created by peijialing on 21/9/2017.
 */
public class table_info {
    public table_info() {
        columnNameList = new ArrayList<String>();
        primaryKey = new ArrayList<String>();
        foreignKey = new ArrayList<foreignKey>();
    }
    public ArrayList<String> columnNameList;
    public ArrayList<String> primaryKey;
    public ArrayList<foreignKey> foreignKey;
}
