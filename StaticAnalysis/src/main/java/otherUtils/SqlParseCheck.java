package otherUtils;

import org.apache.hive.service.cli.HiveSQLException;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

/*
* Check whether Sql meets basic grammatical rules
* */
public class SqlParseCheck {
    public static boolean sqlParseCheck(String sql){
        FileInputStream in = null;
        Properties props = null;
        try{
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            in = new FileInputStream("src/main/resources/application.properties");
            props = new Properties();
            props.load(in);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(props == null){
            return false;
        }
        try(
                Connection connection = DriverManager.getConnection(props.getProperty("url"),props.getProperty("username"),props.getProperty("password"));
                Statement ps=connection.createStatement()
        )
        {
            ps.execute("explain " + sql);
        } catch (HiveSQLException e){
            // HiveQL fails to parse and reports ParseException, ErrorCode is 40000
            // Semantic errors may also be reported
//            e.printStackTrace();
            if(e.getErrorCode() == 40000){
                // It may also be rejected for security reasons
                return e.toString().contains("SemanticException Cartesian products are disabled for safety reasons.");
            }
        } catch (SQLException e){
            // There may also be network connection issues
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(sqlParseCheck("select t1.city from t1 join t2"));
    }
}
