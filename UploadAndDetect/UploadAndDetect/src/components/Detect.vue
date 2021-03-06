<template>
  <div>
    <el-alert
      title="Tips"
      type="info"
      :description=instrctionDescription
      show-icon
      style="white-space: pre-wrap;">
    </el-alert>

    <h1 style="margin-top: 40px">Please Enter the HiveQL Statement Below</h1>

    <el-input
      type="textarea"
      :autosize="{ minRows: 3, maxRows: 25 }"
      v-model="hiveQL"
    >
    </el-input>

    <div class="detect">
      <el-button type="primary" style="border-color: rgb(45 123 199); background-color: rgb(45 123 199)" v-on:click="detect">Detect</el-button>
    </div>

    <div style="clear: right">
      <el-row>
        <el-card class="box-card" shadow="never" style="width: 100%">
          <div slot="header" class="clearfix">
            <span>Detecting Result</span>
          </div>
          <el-alert
            title="Detecting result will be shown here."
            type="info"
            v-if="!isGetDRA && !isGetDRD && !DRALoading && !DRDLoading">
          </el-alert>
          <el-table
            v-if="isGetDRA && !isDRACorrect"
            v-loading="DRALoading"
            element-loading-text="Detecting, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            :data="fixSuggestions"
            style="width: 100%; margin-top: -10px"
          >
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="suggestion" label="Statement Anti-Pattern" >
            </el-table-column>
          </el-table>

          <div v-if="isGetDRA && isDRACorrect" class="text">This HiveQL statement is correct.</div>

          <div
            v-if="isGetDRD"
            v-loading="DRDLoading"
            element-loading-text="Checking data skew, this may cost 60 secs..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            <br>
            Data skew check：{{ dataImbalancedSuggest }}
            <br>
          </div>

          <el-table
            v-if="isGetConfigResult && !isConfigCorrect"
            v-loading="configFixLoading"
            element-loading-text="Checking, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            :data="configFixSuggestions"
            style="width: 100%; "
          >
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="suggestion" label="Configuration Anti-Pattern" >
            </el-table-column>
          </el-table>
          <div v-if="isGetConfigResult && isConfigCorrect" class="text">The configuration does not contain recorded anti-patterns.</div>

        </el-card>

        <el-card class="box-card" shadow="never" style="width: 100%; margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>Fix Suggestion</span>
          </div>
          <el-alert
            title="The fix suggestion will be shown here."
            type="info"
            v-if="!isGetFS && !isGetFR && !FSLoading && !FRLoading">
          </el-alert>
          <div
            v-if="isGetFS"
            v-loading="FSLoading"
            element-loading-text="Fixing, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            Fixed HiveQL：{{ fixedHiveql }}
          </div>
          <div
            v-if="isGetFR"
            v-loading="FRLoading"
            element-loading-text="Recommending reduce number, this may cost 60 secs..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            <br>
            {{ recommendReduceNum }}
            <br>
          </div>
        </el-card>

      </el-row>
    </div>
  </div>
</template>

<script>
import Header from "@/components/Header.vue";
import NavMenu from "@/components/NavMenu.vue";
export default {
  name: "Detect",
  components: { Header, NavMenu },
  data() {
    return {
      instrctionDescription: "You can use this tool to detect and fix anti-patterns hidden in HiveQL statements. " +
        "After you input a Hive statement, the detecting result and fix suggestion will be shown.",
      hiveQL: "select t1.name,avg(t1.score),t1.age from t1 group by t1.name; -- example HiveQL statement",
      fixedHiveql: "",
      dataImbalancedSuggest: "",
      recommendReduceNum: "",
      fixSuggestions: null,
      configFixSuggestions: null,
      isGetDRA: false,
      isGetDRD: false,
      DRALoading: false,
      DRDLoading: false,
      isDRACorrect: false,
      isGetFS: false,
      isGetFR: false,
      FSLoading: false,
      FRLoading: false,
      isGetConfigResult: false,
      isConfigCorrect: false,
      configFixLoading: false,
      api1url: this.common.api1url,
      api2url: this.common.api2url,
      t1_name: " ",
      t1_key: " ",
      t2_name: " ",
      t2_key: " ",
    };
  },
  mounted: function (){
    this.hiveQL = this.$store.getters.getHiveQL;
    this.fixedHiveql = this.$store.getters.getFixedHiveql;
    this.dataImbalancedSuggest = this.$store.getters.getDataImbalancedSuggest;
    this.recommendReduceNum = this.$store.getters.getRecommendReduceNum;
    this.fixSuggestions = this.$store.getters.getFixSuggestions;
    this.configFixSuggestions = this.$store.getters.getConfigFixSuggestions;
    if(this.configFixSuggestions.length>0){
      this.isGetConfigResult = true;
      if(this.configFixSuggestions[0].suggestion==="correct"){
        this.isConfigCorrect = true;
      }
    }
    if(this.fixSuggestions.length>0){
      this.isGetDRA = true;
      if(this.fixSuggestions[0].suggestion==="Correct HQL."){
        this.isDRACorrect = true;
      }
    }
    if(this.dataImbalancedSuggest !== ""){
      this.isGetDRD = true;
    }
    if (this.fixedHiveql != null && this.fixedHiveql !== ""){
      this.isGetFS = true;
    }
    if (this.recommendReduceNum !== ""){
      this.isGetFR = true;
    }
  },
  methods: {
    detect() {
      this.$store.commit('setHiveQL', this.hiveQL);
      var _this = this; //save this
      // clear all history detection
      _this.isGetDRA = true;
      _this.isGetDRD = false;
      _this.DRALoading = true;
      _this.DRDLoading = false;
      _this.isDRACorrect = false;
      _this.isGetFS = true;
      _this.isGetFR = false;
      _this.FSLoading = true;
      _this.FRLoading = false;
      _this.fixedHiveql = "";
      _this.fixSuggestions = [];
      _this.dataImbalancedSuggest = "";
      _this.recommendReduceNum = "";
      _this.configDetect()
      if(this.hiveQL.replaceAll(" ", "").length===0){
        _this.isGetDRA = false;
        _this.isGetFS = false;
        _this.DRALoading = false;
        _this.FSLoading = false;
        this.$message({
          message: 'Please enter an effective HiveQL!',
          type: 'warning'
        });
      }else{
        _this
          .$axios({
            // Create interface
            method: "post", // Request type is get
            url: _this.api1url + "/astCheck", // Requested interface address
            data: {
              hiveql: this.hiveQL,
            },
          })
          .then(function (response) {
            // Request returned successfully
            console.log(response.data); // Print the requested data
            _this.fixedHiveql = response.data.fixedHiveql;
            _this.$store.commit('setFixedHiveql', _this.fixedHiveql);
            for(var i=0;i<response.data.fixedSuggestions.length;i++){
              console.log(response.data.fixedSuggestions[i]);
              _this.fixSuggestions.push({"id":i+1,"suggestion":response.data.fixedSuggestions[i]});
            }
            _this.$store.commit('setFixSuggestions', _this.fixSuggestions);
            if(response.data.fixedSuggestions[0]==="Correct HQL."){
              _this.isDRACorrect = true;
            }
            if (_this.fixedHiveql == null || _this.fixedHiveql === ""){
              _this.isGetFS = false;
            }
            console.log(response.data.fixedHiveql);
            console.log(response.data.joinParams);
            _this.DRALoading = false;
            _this.FSLoading = false;
            if (response.data.joinParams){
              _this.t1_name = response.data.joinParams[0];
              _this.t1_key = response.data.joinParams[1];
              _this.t2_name = response.data.joinParams[2];
              _this.t2_key = response.data.joinParams[3];
              _this.join_detect();
            }
          });
      }
    },
    join_detect(){
      var _this = this;
      _this.isGetDRD = true;
      _this.isGetFR = true;
      _this.DRDLoading = true;
      _this.FRLoading = true;
      _this
      .$axios({
        // create api
        method: "get",
        url: _this.api2url+'/join_check',
        params:{
          t1_name:_this.t1_name,
          t1_key:_this.t1_key,
          t2_name:_this.t2_name,
          t2_key:_this.t2_key
        }
      }).then(function(response){
        console.log(response.data); // Print the requested data
        _this.dataImbalancedSuggest = response.data.dataImbalancedSuggest;
        _this.recommendReduceNum = response.data.recommendReduceNum.split(": ")[1];
        console.log(_this.recommendReduceNum);
        if(_this.recommendReduceNum !== undefined){
          _this.recommendReduceNum = "The desired number of reducers: "+_this.recommendReduceNum;
          _this.$store.commit('setRecommendReduceNum', _this.recommendReduceNum);
        }
        if(_this.dataImbalancedSuggest === "" || _this.dataImbalancedSuggest === "Tables may be empty, can not check data skew. Please check HiveQL or database connection."){
          _this.isGetDRD = false;
        }else{
          _this.$store.commit('setDataImbalancedSuggest', _this.dataImbalancedSuggest);
        }
        _this.DRDLoading = false;
        _this.FRLoading = false;
      });
    },
    configDetect() {
      var _this = this;
      _this.isGetConfigResult = true;
      _this.configFixLoading = true;
      _this.isConfigCorrect = false;
      _this.configFixSuggestions = [];
      _this.$axios({
          // Create interface
          method: "get", // Request type is get
          url: _this.api1url + "/configCheck", // Requested interface address
        })
        .then(function (response) {
          // Request returned successfully
          for(var i=0;i<response.data.length;i++){
            console.log(response.data[i]);
            _this.configFixSuggestions.push({"id":i+1,"suggestion":response.data[i]});
          }
          if(response.data.length===0){
            _this.isConfigCorrect = true;
            _this.configFixSuggestions.push({"id":0,"suggestion": "correct"});
          }else{
            _this.isGetConfigResult = true;
          }
          _this.$store.commit('setConfigFixSuggestions', _this.configFixSuggestions);
          _this.configFixLoading = false;
        });
    },
  },
};
</script>

<style scoped>
.detect {
  text-align: center;
  margin-top: 10px;
  margin-bottom: 10px;
}
.text {
  margin-top:10px;
  margin-bottom:10px;
  font-size: 14px;
  color: #606266;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

.box-card {
  width: 1200px;
}
</style>
