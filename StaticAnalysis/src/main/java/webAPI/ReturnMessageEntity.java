package webAPI;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnMessageEntity {
    @JSONField(name = "fixedHiveql")
    public String fixedHiveql;
    @JSONField(name = "fixedSuggestions")
    public List<String> fixedSuggestions;
    @JSONField(name = "joinParams")
    //Parameter order: t1_name,t1_joinkey,t2_name,t2_joinkey
    public List<String> joinParams;

    @JSONField(name = "configSuggestions")
    public List<String> configSuggestions;

    public ReturnMessageEntity(String fixedHiveql, List<String> fixedSuggestions, List<String> joinParams) {
        super();
        this.fixedHiveql = fixedHiveql;
        this.fixedSuggestions = fixedSuggestions;
        this.joinParams = joinParams;
        this.configSuggestions = new ArrayList<>();
    }

    public ReturnMessageEntity() {
        super();
        this.fixedHiveql =null;
        this.fixedSuggestions = new ArrayList<>();
        this.configSuggestions = new ArrayList<>();
        this.joinParams = null;
    }

    public void addSuggestion(String suggestion) {
        this.fixedSuggestions.add(suggestion);
    }

    public String getFixedHiveql() {
        return fixedHiveql;
    }

    public void setFixedHiveql(String fixedHiveql) {
        this.fixedHiveql = fixedHiveql;
    }

    public List<String> getFixedSuggestions() {
        return fixedSuggestions;
    }

    public void setFixedSuggestions(List<String> fixedSuggestions) {
        this.fixedSuggestions = fixedSuggestions;
    }

    public List<String> getJoinParams() {
        return joinParams;
    }

    public void setJoinParams(List<String> joinPrams) {
        this.joinParams = joinPrams;
    }

    public List<String> getConfigSuggestions() {
        return configSuggestions;
    }

    public void setConfigSuggestions(List<String> configSuggestions) {
        this.configSuggestions = configSuggestions;
    }

    public void deduplicate(){
        Set<String> t = new HashSet<>(fixedSuggestions);
        fixedSuggestions.clear();
        fixedSuggestions.addAll(t);
    }
}
