

/**
 * Created by peijialing on 12/10/2017.
 */
package test;

        import gudusoft.gsqlparser.nodes.*;
        import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;
        import junit.framework.TestCase;
        import gudusoft.gsqlparser.TGSqlParser;
        import gudusoft.gsqlparser.EDbVendor;
        import gudusoft.gsqlparser.stmt.TSelectSqlStatement;

public class testModifySql extends TestCase {

    private TGSqlParser parser = null;

    protected void setUp() throws Exception {
        super.setUp();
        parser = new TGSqlParser(EDbVendor.dbvoracle);
    }

    protected void tearDown() throws Exception {
        parser = null;
        super.tearDown();
    }

    /**
     * <p> column: t1.f1 -> t1.f3 as f1,
     * <p> column: t2.f2 as f2 -> t2.f3
     */
    public void test2(){
        parser.sqltext = "select t1.f1, t2.f2 as f2 from table1 t1 left join table2 t2 on t1.f1 = t2.f2 ";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getResultColumnList().getResultColumn(0).setString("t1.f3 as f1");
        select.getResultColumnList().getResultColumn(1).setString("t2.f3");
        assertTrue(select.toString().equalsIgnoreCase("select t1.f3 as f1, t2.f3 from table1 t1 left join table2 t2 on t1.f1 = t2.f2 "));
        //System.out.println(select.joins.getJoin(0).toString());
    }


    /**
     * change expression in where condition:
     * t1.f2 = 2 -> t1.f2 > 2
     */
    public void test3(){
        parser.sqltext = "select t1.f1 from table1 t1 where t1.f2 = 2 ";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getWhereClause().getCondition().setString("t1.f2 > 2");
        assertTrue(select.toString().equalsIgnoreCase("select t1.f1 from table1 t1 where t1.f2 > 2 "));
        // System.out.println(select.toString());
    }

    /**
     * table2 -> "(tableX join tableY using (id)) as table2"
     */

    public void test5(){
        parser.sqltext = "select table1.col1, table2.col2\n" +
                "from table1, table2\n" +
                "where table1.foo > table2.foo";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);

        TTable t ;
        for(int i=0;i<select.tables.size();i++){
            t = select.tables.getTable(i);
            if (t.toString().compareToIgnoreCase("table2") == 0){
                t.setString("(tableX join tableY using (id)) as table2");
            }
        }

        //   System.out.println(select.toString());

        assertTrue(select.toString().equalsIgnoreCase("select table1.col1, table2.col2\n" +
                "from table1, (tableX join tableY using (id)) as table2\n" +
                "where table1.foo > table2.foo"));
    }

    /**
     * <p> table2 -> "(tableX join tableY using (id)) as table3"
     * <p> table2.col2 -> table3.col2
     * <p> table2.foo -> table3.foo
     */
    public void test6(){
        parser.sqltext = "select table1.col1, table2.col2\n" +
                "from table1, table2\n" +
                "where table1.foo > table2.foo";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);

        TTable t ;
        for(int i=0;i<select.tables.size();i++){
            t = select.tables.getTable(i);
            if (t.toString().compareToIgnoreCase("table2") == 0){
                for(int j=0;j<t.getObjectNameReferences().size();j++){
                    if(t.getObjectNameReferences().getObjectName(j).getObjectToken().toString().equalsIgnoreCase("table2")){
                        t.getObjectNameReferences().getObjectName(j).getObjectToken().astext = "table3";
                    }
                }
                t.setString("(tableX join tableY using (id)) as table3");
            }
        }

        //System.out.println(select.toString());

        assertTrue(select.toString().equalsIgnoreCase("select table1.col1, table3.col2\n" +
                "from table1, (tableX join tableY using (id)) as table3\n" +
                "where table1.foo > table3.foo"));

    }

    public void testRemoveResultColumn(){
        parser.sqltext = "SELECT A as A_Alias, B AS B_Alias FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TResultColumnList columns = select.getResultColumnList();
        columns.removeResultColumn(1);
        // System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT A as A_Alias FROM TABLE_X"));
        columns.addResultColumn("x");
        assertTrue(select.toString().equalsIgnoreCase("SELECT A as A_Alias,x FROM TABLE_X"));
    }

    public void testAddResultColumn(){
        parser.sqltext = "SELECT A as A_Alias, B AS B_Alias FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TResultColumnList columns = select.getResultColumnList();
        columns.addResultColumn("d as d_alias");

        assertTrue(select.toString().equalsIgnoreCase("SELECT A as A_Alias, B AS B_Alias,d as d_alias FROM TABLE_X"));
    }

    public void testReplaceColumn(){
        parser.sqltext = "SELECT * FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TResultColumnList columns = select.getResultColumnList();
        if (columns.getResultColumn(0).toString().equalsIgnoreCase("*")){
            columns.getResultColumn(0).setString("TABLE_X.*");
        }
        //System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT TABLE_X.* FROM TABLE_X"));
    }

    public void testRemoveTable1(){
        parser.sqltext = "SELECT * FROM t1,t2 where t1.f1=t2.f2";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TJoinList joinList = select.joins;
        // let's remove t1 and where clause

        joinList.removeJoin(0);
        select.getWhereClause().setString(" ");

        assertTrue(select.toString().trim().equalsIgnoreCase("SELECT * FROM t2"));
    }

    public void testRemoveTable2(){
        parser.sqltext = "SELECT * FROM t1,t2 where t1.f1=t2.f2";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TJoinList joinList = select.joins;
        // let's remove t2 and where clause

        joinList.removeJoin(1);
        select.getWhereClause().setString(" ");

        assertTrue(select.toString().trim().equalsIgnoreCase("SELECT * FROM t1"));
    }

    // SELECT * FROM t1,t2 where t1.f1=t2.f2
    // covert to
    // SELECT * FROM t1 left join t2 on t1.f1=t2.f2
    // this includes following steps
    // 1. remove t2
    // 2. replace t1 with t1 left join t2 on t1.f1=t2.f2
    // 3. remove where clause

    // for a detailed demo about how to  Rewrite Oracle propriety joins to ANSI SQL compliant joins.
    // http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/rewrite-oracle-propriety-joins-to-ansi-sql-compliant-joins/

    // Rewrite SQL Server proprietary joins to ANSI SQL compliant joins.
    // http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/rewrite-sql-server-propriety-joins-to-ansi-sql-compliant-joins/

    public void testRemoveTable3(){
        parser.sqltext = "SELECT * FROM t1,t2 where t1.f1=t2.f2";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TJoinList joinList = select.joins;
        // let's remove t2 and where clause
        joinList.removeJoin(1);

        //replace t1 with t1 left join,t2 on t1.f1=t2.f2

        joinList.getJoin(0).setString("t1 left join t2 on t1.f1=t2.f2");
        // remove where clause
        select.getWhereClause().setString(" ");

        // System.out.println(select.toString());
        assertTrue(select.toString().trim().equalsIgnoreCase("SELECT * FROM t1 left join t2 on t1.f1=t2.f2"));
    }


    public void testAddWhereClause(){
        parser.sqltext = "SELECT * FROM TABLE_X where f > 0";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addWhereClause("c>1");

        // System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where f > 0 and c>1"));
    }

    public void testAddWhereClause2(){
        parser.sqltext = "SELECT * FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addWhereClause("c>1");

        // System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where c>1"));
    }

    public void testAddWhereClause3(){
        parser.sqltext = "SELECT * FROM TABLE_X group by a";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addWhereClause("c>1");
        //System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where c>1 group by a"));
    }

    public void testRemoveWhereClause(){
        parser.sqltext = "SELECT * FROM TABLE_X where a>1 order by a";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getWhereClause().setString(" ");
        System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X  order by a"));
    }

    public void testAddNewOrderBy(){
        parser.sqltext = "SELECT * FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addOrderBy("a desc");
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X order by a desc"));

        parser.sqltext = "SELECT * FROM TABLE_X where a>1";
        assertTrue(parser.parse() == 0);
        select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addOrderBy("a desc");
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where a>1 order by a desc"));

        parser.sqltext = "SELECT * FROM TABLE_X where a>1 group by a having count(*) > 1";
        assertTrue(parser.parse() == 0);
        select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addOrderBy("a asc");
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where a>1 group by a having count(*) > 1 order by a asc"));

        parser.sqltext = "SELECT * FROM TABLE_X where a>1 group by a having count(*) > 1 order by c desc";
        assertTrue(parser.parse() == 0);
        select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addOrderBy("a asc");
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where a>1 group by a having count(*) > 1 order by c desc,a asc"));

        parser.sqltext = "SELECT * FROM TABLE_X";
        assertTrue(parser.parse() == 0);
        select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.addWhereClause("a>1 and b>2") ;
        select.addOrderBy("a desc");
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X where a>1 and b>2 order by a desc"));

        // System.out.println(select.toString());
    }

    public void testAddOrderBy(){
        parser.sqltext = "SELECT * FROM TABLE_X order by a";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getOrderbyClause().addOrderByItem("b");

        //System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X order by a,b"));
    }

    public void testRemoveOrderBy(){
        parser.sqltext = "SELECT * FROM TABLE_X order by a,b";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getOrderbyClause().removeOrderByItem(1);
        //System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X order by a"));

        select.getOrderbyClause().removeOrderByItem(0);
        assertTrue(select.toString().trim().equalsIgnoreCase("SELECT * FROM TABLE_X"));

//        System.out.println(select.toString());
    }

    public void testReplaceOrderBy1(){
        parser.sqltext = "SELECT * FROM TABLE_X order by a";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getOrderbyClause().getItems().getOrderByItem(0).setString("b asc,c desc");
        //select.getOrderbyClause().addOrderByItem("c");

        //System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X order by b asc,c desc"));
    }

    public void testReplaceOrderBy2(){
        parser.sqltext = "SELECT * FROM TABLE_X order by a";
        assertTrue(parser.parse() == 0);
        TSelectSqlStatement select = (TSelectSqlStatement)parser.sqlstatements.get(0);
        select.getOrderbyClause().getItems().getOrderByItem(0).setString("b asc");
        select.getOrderbyClause().addOrderByItem("c desc");
        select.getOrderbyClause().addOrderByItem("d desc");

        // System.out.println(select.toString());
        assertTrue(select.toString().equalsIgnoreCase("SELECT * FROM TABLE_X order by b asc,c desc,d desc"));
    }

    public void testRemoveSetClauseInUpdate(){
        parser.sqltext = "UPDATE BLA SET A=2, B=3 WHERE X=5";
        assertTrue(parser.parse() == 0);

        TUpdateSqlStatement updateSqlStatement = (TUpdateSqlStatement)parser.sqlstatements.get(0);
        TResultColumnList setClauses = updateSqlStatement.getResultColumnList();
        setClauses.removeResultColumn(1); // the second set expression
        assertTrue(updateSqlStatement.toString().equalsIgnoreCase("UPDATE BLA SET A=2 WHERE X=5"));
    }

    public void testModifyJoin(){
        parser.sqltext = "select * from t1 inner join t2 on t1.col1 = t2.col2";
        assertTrue(parser.parse() == 0);

        TSelectSqlStatement selectSqlStatement = (TSelectSqlStatement)parser.sqlstatements.get(0);
        //System.out.println(selectSqlStatement.joins.getJoin(0).toString());

        selectSqlStatement.joins.getJoin(0).setString("t2 left join t1 on t1.col3 = t2.col5");
        assertTrue(selectSqlStatement.toString().equalsIgnoreCase("select * from t2 left join t1 on t1.col3 = t2.col5"));
    }

    public void testModifyTable(){
        parser.sqltext = "select * from t1";
        assertTrue(parser.parse() == 0);

        TTable table = parser.sqlstatements.get(0).tables.getTable(0);
        table.setString("newt");
        assertTrue(parser.sqlstatements.get(0).toString().equalsIgnoreCase("select * from newt"));
        //System.out.println(selectSqlStatement.joins.getJoin(0).toString());

    }

    public void testRemoveHavingClause(){
        parser.sqltext = "SELECT\n" +
                "c.ID AS \"SMS.ID\"\n" +
                "FROM\n" +
                "SUMMIT.cntrb_detail c\n" +
                "where\n" +
                "c.cntrb_date >='$GivingFromDate$'\n" +
                "and c.cntrb_date<='$GivingThruDate$'\n" +
                "group by c.id\n" +
                "having sum(c.amt) >= '$GivingFromAmount$' and sum(c.amt) <= '$GivingThruAmount$'";
        assertTrue(parser.parse() == 0);

        TSelectSqlStatement selectSqlStatement = (TSelectSqlStatement)parser.sqlstatements.get(0);
        TGroupBy groupBy = selectSqlStatement.getGroupByClause();
        TExpression having = groupBy.getHavingClause();
        having.setString(" ");
        groupBy.getHAVING().setString(" ");
        assertTrue(selectSqlStatement.toString().trim().equalsIgnoreCase("SELECT\n" +
                "c.ID AS \"SMS.ID\"\n" +
                "FROM\n" +
                "SUMMIT.cntrb_detail c\n" +
                "where\n" +
                "c.cntrb_date >='$GivingFromDate$'\n" +
                "and c.cntrb_date<='$GivingThruDate$'\n" +
                "group by c.id"));
        //System.out.println(selectSqlStatement.toString());
    }

}