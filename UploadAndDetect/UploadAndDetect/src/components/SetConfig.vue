<template>
  <div>
    <h1>Set Configurations</h1>
    <el-form
      ref="form"
      :model="formInline"
      class="demo-form-inline"
      label-width="200px"
      style="width: 800px"
    >
      <el-form-item label="MetaBase Address">
        <el-input v-model="formInline.mysqlUrl"></el-input>
      </el-form-item>
      <el-form-item label="MetaBase Username">
        <el-input v-model="formInline.mysqlUsername"></el-input>
      </el-form-item>
      <el-form-item label="MetaBase Password">
        <el-input v-model="formInline.mysqlPassword"></el-input>
      </el-form-item>
      <el-form-item label="Hive Address">
        <el-input v-model="formInline.url"></el-input>
      </el-form-item>
      <el-form-item label="Hive Username">
        <el-input v-model="formInline.username"></el-input>
      </el-form-item>
      <el-form-item label="Hive Password">
        <el-input v-model="formInline.password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">Save</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import qs from "qs";
export default {
  data() {
    return {
      formInline: {
        mysqlUrl: " ",
        mysqlUsername: " ",
        mysqlPassword: " ",
        url: " ",
        username: " ",
        password: "",
      },
      api2url: this.common.api2url,
    };
  },
  mounted: function () {
    this.getConfig(); // Function to be triggered
  },
  methods: {
    onSubmit() {
      console.log("submit!");
      const _this = this;
      _this
        .$axios({
          // create api
          method: "post",
          url: _this.api2url + "/configSet",
          data: {
            mysqlUrl: _this.formInline.mysqlUrl,
            mysqlUsername: _this.formInline.mysqlUsername,
            mysqlPassword: _this.formInline.mysqlPassword,
            url: _this.formInline.url,
            username: _this.formInline.username,
            password: _this.formInline.password,
          },
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          transformRequest: function (obj) {
            var str = [];
            for (var p in obj) {
              str.push(
                encodeURIComponent(p) + "=" + encodeURIComponent(obj[p])
              );
            }
            return str.join("&");
          },
        })
        .then(function (response) {
          console.log(response.data);
          if (response.status == 200) {
            console.log("save successfully");
            _this.$message({
              message: "Save successfully",
              type: "success",
            });
          } else {
            _this.$message({
              message: "Save failed",
              type: "error",
            });
          }
        });
    },
    getConfig() {
      console.log("View Config, ready to get configures");
      const _this = this;
      _this
        .$axios({
          method: "get",
          url: _this.api2url + "/configGet",
        })
        .then(function (response) {
          console.log("get data");
          console.log(response.data); // print data
          _this.formInline.mysqlUrl = response.data.mysqlUrl;
          _this.formInline.mysqlUsername = response.data.mysqlUsername;
          _this.formInline.mysqlPassword = response.data.mysqlPassword;
          _this.formInline.url = response.data.url;
          _this.formInline.username = response.data.username;
          _this.formInline.password = response.data.password;
        });
    },
  },
};
</script>
