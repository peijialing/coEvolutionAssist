package com.mit.dataStructure;

import java.util.ArrayList;

/**
 * Created by peijialing on 29/9/2017.
 */
//all tables and columns needed to be modified, not the join path in paper
//<table_name, column_name_list>
public class JoinPath {
    public JoinPath() {
        tableAndColumns = new ArrayList<TwoTuple<String,ArrayList<String>>>();
    }
    public ArrayList<TwoTuple<String,ArrayList<String>>> tableAndColumns;

}
