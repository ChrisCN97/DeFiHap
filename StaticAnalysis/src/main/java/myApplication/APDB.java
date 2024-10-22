package myApplication;

import java.util.*;

public class APDB {
    private Map<String, String > apDic;
    private Set<String> apFixed;
    private Set<String> apTryFixed;
    
    public APDB(){
        apDic = new HashMap<String, String>(){{
            // Change the short name, please search and replace it in the whole project
            put("Too many join", "Do not use too many 'Join' clauses.");
            put("Cal in where", "Do not calculate in 'where'.");
            put("Cal in join", "Do not calculate in 'join'.");
            put("Select *", "Be careful! Using \"select *\" will cause poor performance! Please select specific column.");
            put("Order by", "Be careful! Using \"order by\" will cause poor performance! Please use \"sort by\".");
            put("Having", "Be careful! Using \"having\" will cause poor performance! Please use \"where\".");
            put("Func in pred", "Do not invoke function in predication.");
            put("Col in group", "Warning! Column selected should be included in group by");
            put("Interval", "Be careful! Using \"interval\" in \"date_sub()\" will cause error!");
            put("Dif then else", "Be careful! Data type after \"then\" and \"else\" is different!");
            put("Agg with group", "Be careful! \"group by\" should be used with aggregate function!");
            put("Count distinct", "Be careful! Using \"count(distinct ...)\" may cause poor performance! Please use \"sum...group by\"");
            put("Less left join", "Please put the table containing less records on the left side of join. Or check database connection.");
            put("Similar table", "Creating table \"mrtest_50\" is similar to existed table \"mrtest_50\", please check again.");
            put("Use partition", "Warning! Please utilize partition in the query. Or check database connection.");
            // illegal input should be replaced separately in MergedTest.java
            put("Illegal input", "This HiveQL may be illegal, please check your input or the database connection.");
            put("Correct HQL", "Correct HQL.");
        }};

        // Record the APs that have been repaired. Only when these warnings are detected, will the repair statement be generated
        apFixed = new HashSet<String>(){{
            add("Less left join");
            add("Having");
            add("Interval");
            add("Select *");
            add("Col in group");
            add("Use partition");
        }};

        apTryFixed = new HashSet<String>();
    }

    public String getSug(String ap){
        apTryFixed.add(ap);
        String sug = apDic.get(ap);
        System.out.println(sug);
        return sug;
    }

    public String getSug(String ap, String s1, String s2){
        if(ap.equals("Similar table")){
            String sug = "Creating table \""+s1+"\" is similar to existed table \""+s2+"\", please check again.";
            System.out.println(sug);
            return sug;
        }
        String sug = "Ap does not exist.";
        System.out.println(sug);
        return sug;
    }

    // Determine whether all APs have been repaired
    public Boolean isAllFixed(){
        return apTryFixed.size()>0 && apFixed.containsAll(apTryFixed);
    }
}
