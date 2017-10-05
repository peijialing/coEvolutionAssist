package com.mit.dataStructure;

import gudusoft.gsqlparser.EAlterTableOptionType;

import java.util.ArrayList;

/**
 * Created by peijialing on 28/9/2017.
 */
public class alter_info {
    public alter_info() {
        joinPath = new JoinPath();
        replacedTableName = null;
        oriTableName = null;
        replacedColName = null;
        oriColName = null;

    }
    public EAlterTableOptionType type;
    public JoinPath joinPath;
    //rename a table
    public String oriTableName;
    public String replacedTableName;
    //rename a column
    public String oriColName;
    public String replacedColName;
}
