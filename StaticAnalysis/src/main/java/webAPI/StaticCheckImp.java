package webAPI;

import myApplication.MergedTest;

import java.util.ArrayList;
import java.util.List;

public class StaticCheckImp {
    public static ReturnMessageEntity staticCheckRun(String hiveql){
        ReturnMessageEntity returnMessageEntity = MergedTest.astCheck(hiveql);
        return  returnMessageEntity;
    }

    public static ReturnMessageEntity testRun(String hiveql){
        List<String> fixedSuggestions=new ArrayList<>();
        fixedSuggestions.add("test");
        List<String>joinParams=new ArrayList<>();
        joinParams.add("a");
        joinParams.add("key");
        joinParams.add("b");
        joinParams.add("key");
        // If there is no join, joinParams is directly set to null, and there is no joinParams in json
        ReturnMessageEntity returnMessageEntity=new ReturnMessageEntity("select a from b",fixedSuggestions,null);
        return  returnMessageEntity;
    }
}
