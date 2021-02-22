<template>
  <div>
    <div class="detect">
      <el-button type="primary" v-on:click="detect">Configuration Check</el-button>
    </div>
    <div style="clear: right" v-if="isStart">
      <el-row style="flex-direction: row; clear: right">
        <el-col :span="24">
          <el-card class="box-card">
            <div slot="header" class="clearfix">
              <span>Check Result</span>
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
          </el-card>
        </el-col>
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
      hiveQL: "",
      fixedHiveql: "",
      dataImbalancedSuggest: "",
      recommendReduceNum:"",
      fixSuggestions:[
        // {
        //   id: "-1",
        //   suggestion: "test",
        // }
       ],
      isStart: false,
      isGetFixResult: false,
      disableHiveQL: false,
      isGetJoinResult: false,
      fixLoading: true,
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
      _this.isStart = false;
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
          method: "get", // Request type is get
          url: _this.api1url + "/configCheck", // Requested interface address
        })
        .then(function (response) {
          // Request returned successfully
          _this.isStart = true;
          _this.fixLoading = false ;
          console.log(response.data); // Print the requested data
          for(var i=0;i<response.data.length;i++){
            console.log(response.data[i]);
            _this.fixSuggestions.push({"id":i+1,"suggestion":response.data[i]});
          }
        });
    },
  },
};
</script>

<style scoped>
.detect {
  float: left;
  margin-top: 10px;
  margin-bottom: 10px;
}
.text {
  margin-top:10px;
  margin-bottom:10px;
  font-size: 14px;
  color: #606266;
}

.item {
  margin-bottom: 18px;
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
