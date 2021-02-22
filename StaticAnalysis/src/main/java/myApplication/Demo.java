package myApplication;

public class Demo {
    public static void main(String[] args){
        // use select *
        String s1 = "select * from a";
        // group by not used with aggregate functions
        String s2 = "select t3.name, t3.age from  t3 group by t3.name,t3.age;";

        String s4="SELECT n.name, a.age FROM mrtest_5k n JOIN mrtest_500 a ON n.city=a.city;";

  //      MergedTest.astCheck(s1);

 //       MergedTest.astCheck(s2);

 //       MergedTest.astCheck(s4);

        // Configuration detection
        MergedTest.configCheck();
    }
}
