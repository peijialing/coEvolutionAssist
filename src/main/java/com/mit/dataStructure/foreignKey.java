package com.mit.dataStructure;

import java.util.ArrayList;

/**
 * Created by peijialing on 27/9/2017.
 */
public class foreignKey {
    public foreignKey() {
        keyName = new ArrayList<String>();
        RefTable = new String();
        RefAttr = new ArrayList<String>();
    }
    public ArrayList<String> keyName;
    public String RefTable;
    public ArrayList<String> RefAttr;
}
