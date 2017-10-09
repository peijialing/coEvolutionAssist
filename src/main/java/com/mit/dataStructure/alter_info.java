package com.mit.dataStructure;

import gudusoft.gsqlparser.EAlterTableOptionType;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TTable;

import java.util.ArrayList;

/**
 * Created by peijialing on 28/9/2017.
 */
public class alter_info {
    public alter_info() {
        joinPath = new JoinPath();
        replacedTableName = null;
        oriTableName = null;
        replacedCol = null;
        oriCol = null;


    }
    public EAlterTableOptionType type;
    public JoinPath joinPath;
    //rename a table
    public table_info oriTableName;
    public String replacedTableName;
    //rename a column
    public TObjectName oriCol;
    public TObjectName replacedCol;
}
