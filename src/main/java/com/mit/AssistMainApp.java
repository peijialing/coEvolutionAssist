package com.mit;
import com.mit.dataStructure.AppCandidate;
import com.mit.dataStructure.Req;
import com.mit.rewriting;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.List;

/**
 * Created by peijialing on 23/8/2017.
 */
public class AssistMainApp {
    public static void main(String[] args) throws Exception {

        //whole structure
        //input dml sentence, parse it and recognize which case it is
        //change the query statement according to different use cases
        Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM tab1");
        Select selectStatement = (Select) stmt;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
    }
}
