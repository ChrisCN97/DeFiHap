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
          <el-table
            v-if="isGetDetectResult"
            v-loading="fixLoading"
            element-loading-text="Detecting, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            :data="fixSuggestions"
            style="width: 100%; margin-top: -10px"
          >
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="suggestion" label="Anti-Pattern" >
            </el-table-column>
          </el-table>

          <div v-if="isCorrect" class="text">This HiveQL is correct.</div>

          <div
            v-if="isGetJoinResult"
            v-loading="joinLoading"
            element-loading-text="Checking data skew, this may cost 60 secs..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            <br>
            Data skew check：{{ dataImbalancedSuggest }}
            <br>
          </div>
        </el-card>

        <el-card class="box-card" shadow="never" style="width: 100%; margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>Fix Suggestion</span>
          </div>
          <div
            v-if="isGetFixResult"
            v-loading="fixLoading"
            element-loading-text="Fixing, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            Fixed HiveQL：{{ fixedHiveql }}
          </div>
          <div
            v-if="isGetJoinResult"
            v-loading="joinLoading"
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

        <el-card class="box-card" shadow="never" style="width: 100%; margin-top: 10px">
          <div slot="header" class="clearfix">
            <span>Configuration Check</span>
          </div>
          <el-button
            type="primary"
            style="border-color: rgb(45 123 199); background-color: rgb(45 123 199)"
            v-on:click="configDetect"
            v-if="!configFixLoading && !isGetConfigResult">
              Configuration Check
          </el-button>
          <el-table
            v-if="isGetConfigResult"
            v-loading="configFixLoading"
            element-loading-text="Checking, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            :data="configFixSuggestions"
            style="width: 100%; margin-top: -10px"
          >
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="suggestion" label="Anti-Pattern" >
            </el-table-column>
          </el-table>
          <div v-if="isConfigCorrect" class="text">The configuration does not contain recorded anti-patterns.</div>
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
      instrctionDescription: "You can use HAPDF to detect and fix anti-patterns hidden in HiveQL statement. " +
        "After you input a Hive statement, the detecting result and fix suggestion will be showed. " +
        "You can also click \"Configuration Check\" to find anti-patterns in your hive configuration.",
      hiveQL: "select t1.name,avg(t1.score),t1.age from t1 group by t1.name; -- example HiveQL statement",
      fixedHiveql: "",
      dataImbalancedSuggest: "",
      recommendReduceNum: "",
      fixSuggestions:[
        // {
        //   id: "-1",
        //   suggestion: "test",
        // }
       ],
      configFixSuggestions: [],
      isGetDetectResult: false,
      isGetFixResult: false,
      isGetJoinResult: false,
      isGetConfigResult: false,
      isCorrect: false,
      isConfigCorrect: false,
      fixLoading: false,
      joinLoading: true,
      configFixLoading: false,
      api1url: this.common.api1url,
      api2url: this.common.api2url,
      t1_name: " ",
      t1_key: " ",
      t2_name: " ",
      t2_key: " ",
    };
  },
  methods: {
    detect() {
      var _this = this; //save this
      // clear all history detection
      _this.isGetFixResult = false;
      _this.isGetJoinResult = false;
      _this.isGetDetectResult = false;
      _this.isCorrect = false;
      _this.fixLoading = true;
      _this.joinLoading = true;
      _this.fixedHiveql = "";
      _this.fixSuggestions = [];
      _this.dataImbalancedSuggest = "";
      _this.recommendReduceNum = "";
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
          _this.fixLoading = false ;
          console.log(response.data); // Print the requested data
          _this.fixedHiveql = response.data.fixedHiveql;
          for(var i=0;i<response.data.fixedSuggestions.length;i++){
            console.log(response.data.fixedSuggestions[i]);
            _this.fixSuggestions.push({"id":i+1,"suggestion":response.data.fixedSuggestions[i]});
          }
          if(response.data.fixedSuggestions[0]==="Correct HQL."){
            _this.isCorrect = true;
          }else{
            _this.isGetDetectResult = true;
          }
          if (_this.fixedHiveql != null && _this.fixedHiveql !== ""){
            _this.isGetFixResult = true;
          }
          console.log(response.data.fixedHiveql);
          console.log(response.data.joinParams);
          if (response.data.joinParams){
            _this.t1_name = response.data.joinParams[0];
            _this.t1_key = response.data.joinParams[1];
            _this.t2_name = response.data.joinParams[2];
            _this.t2_key = response.data.joinParams[3];
            _this.join_detect();
          }
        });
    },
    join_detect(){
      var _this = this;
      _this.isGetJoinResult = true;
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
        _this.joinLoading = false;
        console.log(response.data); // Print the requested data
        _this.dataImbalancedSuggest = response.data.dataImbalancedSuggest;
        _this.recommendReduceNum = response.data.recommendReduceNum;
        if(_this.dataImbalancedSuggest === ""){
          _this.dataImbalancedSuggest = "Data skew does not exist.";
        }
      });
    },
    configDetect() {
      var _this = this;
      _this.isGetConfigResult = false;
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
          _this.configFixLoading = false;
          if(response.data.length===0){
            _this.isConfigCorrect = true;
          }else{
            _this.isGetConfigResult = true;
          }
        });
    },
  },
};
</script>

<style scoped>
.detect {
  float: right;
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
