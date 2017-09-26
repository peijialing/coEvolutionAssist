package com.mit;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.List;

/**
 * Created by peijialing on 26/9/2017.
 */
public class testCase {
    public static void main(String[] args) throws Exception {
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM tab1, tab2");
        Select selectStatement = (Select) stmt;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        for (int i=0; i<tableList.size();++i) {
            System.out.println(tableList.get(i));
        }
    }
}
