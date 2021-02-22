package mysqlUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class MysqlUtil {
    public static boolean compareTwoTableRowNum(String leftTable, String rightTable) {
        int leftTableRowNum = 0;
        int rightTableRowNum = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("mysqlUrl");
            Connection connection = DriverManager.getConnection(url, props.getProperty("mysqlUsername"), props.getProperty("mysqlPassword"));
            PreparedStatement ps = connection.prepareStatement("select TBL_ID from TBLS where TBL_NAME=\"" + leftTable + "\"");
            ResultSet r = ps.executeQuery();
            String leftTableID = "";
            while(r.next()){
                leftTableID = r.getString(1);
            }
            ps = connection.prepareStatement("select TBL_ID from TBLS where TBL_NAME=\"" + rightTable + "\"");
            r = ps.executeQuery();
            String rightTableID = "";
            while(r.next()){
                rightTableID = r.getString(1);
            }
            ps = connection.prepareStatement("select PART_ID from PARTITIONS where TBL_ID=\"" + leftTableID + "\"");
            r = ps.executeQuery();
            // Determine whether the query result of the partition table is empty
            if(r.next()){
                // Get the total number of records in the partition table
                ps = connection.prepareStatement("select sum(PARAM_VALUE) from (select PART_ID from PARTITIONS where TBL_ID=\"" + leftTableID + "\") as partID join PARTITION_PARAMS on partID.PART_ID = PARTITION_PARAMS.PART_ID where PARAM_KEY = \"numRows\"");
                r = ps.executeQuery();
                while (r.next()) {
                    leftTableRowNum = Double.valueOf(r.getString(1)).intValue();
                }
            }
            else {
                // Get the total number of records in a non-partitioned table
                ps = connection.prepareStatement("select PARAM_VALUE from TABLE_PARAMS where TBL_ID=\"" + leftTableID + "\" and PARAM_KEY=\"numRows\"");
                r = ps.executeQuery();
                while (r.next()) {
                    leftTableRowNum = Integer.valueOf(r.getString(1));
                }
            }

            ps = connection.prepareStatement("select PART_ID from PARTITIONS where TBL_ID=\"" + rightTableID + "\"");
            r = ps.executeQuery();
            if(r.next()){
                // Get the total number of records in the partition table
                ps = connection.prepareStatement("select sum(PARAM_VALUE) from (select PART_ID from PARTITIONS where TBL_ID=\"" + rightTableID + "\") as partID join PARTITION_PARAMS on partID.PART_ID = PARTITION_PARAMS.PART_ID where PARAM_KEY = \"numRows\"");
                r = ps.executeQuery();
                while (r.next()) {
                    rightTableRowNum = Double.valueOf(r.getString(1)).intValue();
                }
            }
            else {
                ps = connection.prepareStatement("select PARAM_VALUE from TABLE_PARAMS where TBL_ID=\"" + rightTableID + "\" and PARAM_KEY=\"numRows\"");
                r = ps.executeQuery();
                while (r.next()) {
                    rightTableRowNum = Integer.valueOf(r.getString(1));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return(leftTableRowNum <= rightTableRowNum);
    }

    public static boolean compareTwoTableRowNumByHive(String leftTable,String rightTable){
        int leftTableRowNum = 0;
        int rightTableRowNum = 0;
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            //FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("url");
            Connection connection = DriverManager.getConnection(url,props.getProperty("username"),props.getProperty("password"));
            PreparedStatement ps=connection.prepareStatement("select count(*) from "+leftTable);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                leftTableRowNum = Integer.valueOf(rs.getString(1));
            }
            ps = connection.prepareStatement("select count(*) from "+rightTable);
            rs = ps.executeQuery();
            while(rs.next()){
                rightTableRowNum = Integer.valueOf(rs.getString(1));
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return(leftTableRowNum < rightTableRowNum);
    }

    public static boolean compareParamType(String leftParam,String rightParam){
        String[] table1 = leftParam.split("\\.");
        String[] table2 = rightParam.split("\\.");
        if(table1.length < 2 || table2.length < 2){
            return true;
        }
        String tableName1 = table1[0];
        String column1 = table1[1];
        String tableName2 = table2[0];
        String column2 = table2[1];
        String type1 = "";
        String type2 = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("mysqlUrl");
            Connection connection = DriverManager.getConnection(url,props.getProperty("mysqlUsername"),props.getProperty("mysqlPassword"));
            PreparedStatement ps=connection.prepareStatement("select TBL_ID from TBLS where TBL_NAME=\""+tableName1+"\"");
            ResultSet r=ps.executeQuery();
            String table1ID = "";
            while(r.next()){
                table1ID = r.getString(1);
            }
            String table2ID = "";
            ps = connection.prepareStatement("select TBL_ID from TBLS where TBL_NAME=\""+tableName2+"\"");
            r = ps.executeQuery();
            while(r.next()){
                table2ID = r.getString(1);
            }

            PreparedStatement ps2=connection.prepareStatement("select TYPE_NAME from COLUMNS_V2 where CD_ID=\""+table1ID+"\" and COLUMN_NAME=\""+column1+"\"");
            ResultSet r2=ps2.executeQuery();
            while(r2.next()){
                type1 = r2.getString(1);
            }
            ps2=connection.prepareStatement("select TYPE_NAME from COLUMNS_V2 where CD_ID=\""+table2ID+"\" and COLUMN_NAME=\""+column2+"\"");
            r2=ps2.executeQuery();
            while(r2.next()){
                type2 = r2.getString(1);
            }

            ps.close();
            ps2.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return(type1.equals(type2));
    }

    public static String hasSameTable(String tableName, HashSet<String> colName){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            //FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("mysqlUrl");
            Connection connection = DriverManager.getConnection(url,props.getProperty("mysqlUsername"),props.getProperty("mysqlPassword"));
            Statement ps2 = connection.createStatement();
            ResultSet r = ps2.executeQuery("select TBL_NAME, SD_ID from TBLS");
            Map<String, String> id2tableName = new HashMap<>();
            while(r.next()){
                if(tableName.equals(r.getString(1))){
                    return r.getString(1);
                }
                id2tableName.put(r.getString(2), r.getString(1));
            }
            PreparedStatement ps=connection.prepareStatement("select CD_ID, COLUMN_NAME from COLUMNS_V2");
            r=ps.executeQuery();
            Map<String, HashSet<String>> colNameDic = new HashMap<String, HashSet<String>>();
            while(r.next()){
                String key = r.getString(1);
                String value = r.getString(2);
                if(!colNameDic.containsKey(key)){
                    colNameDic.put(key, new HashSet<String>());
                }
                colNameDic.get(key).add(value);
            }
            for(String key : colNameDic.keySet()){
                if(colNameDic.get(key).equals(colName)){
                    return id2tableName.get(key);
                }
            }
            ps.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashSet<String> partitionCheck(String tableName, List<String> whereItemList) {
        FileInputStream in = null;
        Properties props = null;
        String url = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            in = new FileInputStream("src/main/resources/application.properties");
            props = new Properties();
            props.load(in);
            url = props.getProperty("mysqlUrl");
        } catch (Exception e){
            System.out.println("Fail to init hive jdbc.");
            e.printStackTrace();
            return null;
        }
        HashSet<String> partCol = new HashSet<String>();
        try (
                Connection connection = DriverManager.getConnection(url,props.getProperty("mysqlUsername"),props.getProperty("mysqlPassword"));
                Statement ps = connection.createStatement()
        ) {
            ResultSet r = ps.executeQuery("select TBL_ID from TBLS where TBL_NAME=\""+tableName+"\"");
            String tableID = "";
            while(r.next()){
                tableID = r.getString(1);
            }
            if(tableID.equals("")){
                return null;
            }
            r = ps.executeQuery("select PKEY_NAME from PARTITION_KEYS where TBL_ID=\""+tableID+"\"");
            while(r.next()){
                partCol.add(r.getString(1));
            }
            if(partCol.isEmpty()){
                return null;
            }
            for(String s: whereItemList){
                if(partCol.contains(s)){
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return partCol;
    }

    public static List<String> configurationCheck(){
        List<String> configSuggestions=new ArrayList<>();
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
 //           FileInputStream in = new FileInputStream("C:/Users/Lenovo/Documents/GitHub/DetectionAndFixOfHiveQLAntiPattern/StaticAnalysis/src/main/resources/application.properties");
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("url");
            Connection connection = DriverManager.getConnection(url,props.getProperty("username"),props.getProperty("password"));
            PreparedStatement ps=connection.prepareStatement("set");
            ResultSet rs=ps.executeQuery();
            Map<String, String> confDic = new HashMap<String, String>();
            while(rs.next()){
                String[] conf = rs.getString(1).split("=");
                if(conf.length<2){
                    continue;
                }
                confDic.put(conf[0], conf[1]);
            }
            if(!confDic.getOrDefault("hive.optimize.cp", "true").equals("true")){
                configSuggestions.add("Automatic line trimming is not enabled, please set: hive.optimize.cp=true");
            }
            if(!confDic.getOrDefault("hive.optimize.pruner", "true").equals("true")){
                 configSuggestions.add("Automatic partition trimming is not enabled, please set: hive.optimize.pruner=true");
            }
            if(!confDic.getOrDefault("mapred.compress.map", "true").equals("true")){
                 configSuggestions.add("Map output compression is not enabled, please set: mapred.compress.map.output=true");
            }
            if(!confDic.getOrDefault("mapred.output.compress", "true").equals("true")){
                 configSuggestions.add("Job output compression is not enabled, please set: mapred.output.compress=true");
            }
            if(!confDic.getOrDefault("mapred.output.compress", "true").equals("true")
                    && !confDic.getOrDefault("hive.optimize.bucketmapjoin", "true").equals("true")){
                 configSuggestions.add("Use bucket without connection, please set: hive.enforce.bucketing=true, " +
                        "hive.optimize.bucketmapjoin=true");
            }
            if(!confDic.getOrDefault("hive.exec.parallel", "true").equals("true")){
                 configSuggestions.add("Parallel execution is not enabled, please set: hive.exec.parallel=true");
            }
            if(!confDic.getOrDefault("hive.vectorized.execution.enabled", "true").equals("true")
                    && !confDic.getOrDefault("hive.vectorized.execution.reduce.enabled", "true").equals("true")){
                 configSuggestions.add("Vectorization is not enabled, please set: hive.vectorized.execution.enabled=true, " +
                        "hive.vectorized.execution.reduce.enabled=true");
            }
            if(!confDic.getOrDefault("hive.cbo.enable", "true").equals("true")){
                 configSuggestions.add("Cost Based Optimizer(CBO) is not enabled, please set: hive.cbo.enable=true");
            }
            if(!confDic.getOrDefault("hive.server2.thrift.min.worker.threads", "1").equals("1")
                    && !confDic.getOrDefault("hive.server2.thrift.max.worker.threads", "1").equals("1")){
                 configSuggestions.add("Task has been rejected by ExecutorService, please set: hive.server2.thrift.max.worker.threads=1, " +
                        "hive.server2.thrift.min.worker.threads=1");
            }
            if(!confDic.getOrDefault("hive.exec.dynamic.partition.mode", "nonstrict").equals("nonstrict")){
                 configSuggestions.add("Insert the partition table without setting dynamic partition" +
                         ", please set: hive.exec.dynamic.partition.mode=nonstrict");
            }
            if(!confDic.getOrDefault("hive.map.aggr", "true").equals("true")
                    && !confDic.getOrDefault("hive.groupby.mapaggr.checkinterval", "100000").equals("100000")){
                 configSuggestions.add("Map partial aggregation function is not enabled, please set: hive.map.aggr=true, " +
                        "hive.groupby.mapaggr.checkinterval=100000");
            }
            if(!confDic.getOrDefault("hive.groupby.skewindata", "true").equals("true")){
                 configSuggestions.add("Automatic load balancing when data skew is not enabled, " +
                         "please set: hive.groupby.skewindata=true");
            }
            if(!confDic.getOrDefault("hive.auto.convert.join", "true").equals("true")
                    && !confDic.getOrDefault("hive.mapjoin.smalltable.filesize", "25000000").equals("25000000")){
                 configSuggestions.add("Small table join large table, automatically try mapjoin is not enabled, " +
                         "please set: hive.auto.convert.join=true, hive.mapjoin.smalltable.filesize=25000000");
            }
            if(!confDic.getOrDefault("hive.merge.mapfiles", "true").equals("true")
                    && !confDic.getOrDefault("hive.merge.mapredfiles", "true").equals("true")){
                 configSuggestions.add("Automatic merging of small files is not enabled, please set: hive.merge.mapfiles=true, " +
                        "hive.merge.mapredfiles=true");
            }
            return configSuggestions;
        } catch (Exception e) {
            configSuggestions.add("Hive connection fails, pls check your hive configurations");
            e.printStackTrace();
            return configSuggestions;
        }
    }
}
