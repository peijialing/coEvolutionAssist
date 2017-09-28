package com.mit.dataStructure;

import gudusoft.gsqlparser.EAlterTableOptionType;

import java.util.ArrayList;

/**
 * Created by peijialing on 28/9/2017.
 */
public class alter_info {
    public alter_info() {
        joinPath = new JoinPath();

    }
    public EAlterTableOptionType type;
    public JoinPath joinPath;

}
