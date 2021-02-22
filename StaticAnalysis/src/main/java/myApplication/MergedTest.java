package myApplication;

import gen.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import mysqlUtils.MysqlUtil;
import otherUtils.SqlParseCheck;
import otherUtils.stringUtil;
import webAPI.ReturnMessageEntity;

import java.util.ArrayList;
import java.util.List;

public class MergedTest {
    public static ReturnMessageEntity astCheck(String s) {
        try {
            System.out.println("-HiveQL:"+s);
            System.out.println("-Suggestion:");
            s = stringUtil.proProcessing(s);
            if(!SqlParseCheck.sqlParseCheck(s)){
                ReturnMessageEntity returnMessageEntity = new ReturnMessageEntity();
                System.out.println("This HiveQL may be illegal, please check your input or the database connection.");
                returnMessageEntity.addSuggestion("This HiveQL may be illegal, please check your input or the database connection.");
                return returnMessageEntity;
            }
            s = stringUtil.join2innerJoin(s);
            // Create input byte stream
            ANTLRInputStream input = new ANTLRInputStream(s);
            // Build a lexical analyzer
            HplsqlLexer lexer = new HplsqlLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(HplsqlErrorListener.INSTANCE);
            // Store words in memory
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Building grammar parser
            HplsqlParser parser = new HplsqlParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(HplsqlErrorListener.INSTANCE);
            // Build parse tree
            ParseTree tree = parser.program();

            // Build tree walker
            ParseTreeWalker walker = new ParseTreeWalker();
            // The first parameter is the parser built before
//        walker.walk(new MergedListener(),tree);

            TestFixListener testFixListener = new TestFixListener();
            walker.walk(testFixListener, tree);
            testFixListener.returnMessageEntity.deduplicate();
            return testFixListener.returnMessageEntity;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> configCheck(){
        List<String> res = MysqlUtil.configurationCheck();
        System.out.println(res);
        return res;
    }

    public static void main(String[] args) throws Exception {
        // use select *
//        String s = "select * from a";

        // use order by
//        String s = "select b from a order by b";

        // Grouping by not using with aggregate functions
//        String s = "select pokes.col1,unique1.col2 from unique1 left join pokes on pokes.id = unique1.id;";
//        String s = "select t3.col1,t3.col2,sum(t3.col1) from (select t1.col1,t2.col2 from t1 join t2 on t1.id = t2.id group by t1.col1) as t3 group by t3.col1,t3.col2;";

        // Create multiple identical tables
//        String s = "CREATE TABLE tableD (bar int, foo float);";
//        String s = "create table mrtest_50 (a String, b int)";
//        String s = "create table mrtest_502 (name String, age int, city int)";

        // When conditions permit, the table with few entries is not placed on the left side of join,
        // or the table with many entries is placed on the right side
//        String s = "SELECT t1.name, t2.age FROM mrtest_10 as t1 JOIN mrtest_500 as t2 ON t1.city=t2.city;";
//        String s = "SELECT t1.name, t2.age FROM mrtest_500 as t1 JOIN mrtest_10 as t2 ON t1.city=t2.city;";  // AP
//        String s = "select p1.name from mrtest_500 p1 join mrtest_50 p2 on p1.city = p2.city where p1.city = 1;";  // AP

        // Use having for filtering https://blog.csdn.net/high2011/article/details/82686858
//        String s = "SELECT id, avg(age) avaAge from table001 group by id having id >='20180901';";
//        String s = "SELECT id from table001 having id >='20180901';";  // AP
        /*String s = "SELECT C.CustomerID, C.Name, Count(S.SalesID)\n" +
                "FROM Customers as C\n" +
                "   INNER JOIN Sales as S\n" +
                "   ON C.CustomerID = S.CustomerID\n" +
                "GROUP BY C.CustomerID, C.Name\n" +
                "HAVING S.LastSaleDate BETWEEN '1/1/2019' AND '12/31/2019';";*/

        // use interval in date_sub()
//        String s = "Select date_add('2020-9-16', interval '10' day) from a;";  // AP
//        String s = "Select mrtest_10.name, mrtest_500.age FROM mrtest_10 inner JOIN mrtest_500 on mrtest_10.age = mrtest_500.age group by mrtest_10.name;";  // AP
//        String s = "select date_sub('2020-9-16',10) From a;";
//        String s = "select '2020-9-16' - interval '10' day From a;";

        // No partition query used on a partitioned table
//        String s = "select name from partitiontable;";  // AP
        String s = "select name from partitiontable where name='cn';";  // AP
//        String s = "select name from partitiontable where city='changzhou';";
//        String s = "select name from partitiontable where city='changzhou' and name+1='cn';";

        // The selected column is not in the group by
//        String s = "select name, city, avg(age) from t group by name;";  // AP
//        String s = "select name, city, avg(age) from t group by city, name;";

        // subselect
//        String s = "select p1.name from mrtest_500 p1 join (select city from mrtest_50) p2 on p1.city = p2.city where p1.city = 1;";

        // Don't use join too much
//        String s = "select t1.name,t2.age from t1 inner join t2 on t1.id = t2.id;";

        // Syntax error statement
//        String s = "12345";
//        String s = "alalala";

//        astCheck(s);

//        System.out.println(tree.toStringTree(parser));

        // Configuration detection
        configCheck();
    }
}
