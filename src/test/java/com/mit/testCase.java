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
        Statement stmt = CCJSqlParserUtil.parse("create table new_employees\n" +
                "(\n" +
                "\temployee_id  number primary key,\n" +
                "\tfirst_name  varchar2(15) null,\n" +
                "\tlast_name varchar2(15) check(last_name>10),\n" +
                "\thire_date date default sysdate,\n" +
                "\tdept_id number,\n" +
                "\tdept_name varchar2(100),\n" +
                "\tstart_date timestamp(7) references scott.dept(start_date),\n" +
                "\tend_date timestamp(7)   references dept.end_date on delete cascade,\n" +
                "\tcheck (start_date>end_date),\n" +
                "\tconstraint c_name unique(first_name,last_name),\n" +
                "\tforeign key(dept_id,dept_name) references dept(id,name)\n" +
                ");");
        Select selectStatement = (Select) stmt;

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        for (int i=0; i<tableList.size();++i) {
            System.out.println(tableList.get(i));
        }
    }
}
