<template>
  <div style="padding: 0 40px">
    <h1>HiveQL Anti-Patterns Detecting and Fixing</h1>

    <el-input
      type="textarea"
      :autosize="{ minRows: 5, maxRows: 25 }"
      :placeholder=defaultHiveQL
      v-model="hiveQL"
    >
    </el-input>

    <div class="detect">
      <el-button type="primary" style="border-color: rgb(45 123 199); background-color: rgb(45 123 199)" v-on:click="detect">Detect</el-button>
    </div>

    <div style="clear: right">
      <el-row style="flex-direction: row; clear: right">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <span>Detect Result</span>
          </div>
          <el-table
            v-loading="fixLoading"
            element-loading-text="Detecting, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            :data="fixSuggestions"
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80">
            </el-table-column>
            <el-table-column prop="suggestion" label="Anti-Pattern" >
            </el-table-column>
          </el-table>

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

        <el-card class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <span>Fix Suggestions</span>
          </div>
          <div
            v-if="isGetFixResult"
            v-loading="fixLoading"
            element-loading-text="Fixing, please wait..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="#606266"
            class="text"
          >
            <br>
            Fixed HiveQL：{{ fixedHiveql }}
            <br>
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
      hiveQL: "select t1.name,avg(t1.score),t1.age from t1 group by t1.name;",
      defaultHiveQL: "select t1.name,avg(t1.score),t1.age from t1 group by t1.name;",
      fixedHiveql: "",
      dataImbalancedSuggest: "",
      recommendReduceNum:"",
      fixSuggestions:[
        // {
        //   id: "-1",
        //   suggestion: "test",
        // }
       ],
      isGetFixResult: false,
      isGetJoinResult: false,
      fixLoading: false,
      joinLoading: true,
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
    }
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
