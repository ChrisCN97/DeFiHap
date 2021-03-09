<template>
  <div>
    <el-card>
      <div slot="header">
        <span style="font-size: 18px">About us</span>
      </div>
      <span class="text">
We are members of the Big Data and Software Engineering Lab (BDSE) , Shanghai Jiao Tong University, China.
You can contact us by e-mail：{mytkeroro, yssjtu, cuinan, tjsoulshe, bjshen, chenyt}@sjtu.edu.cn
      </span>
    </el-card>

    <el-card style="margin-top: 10px">
      <div slot="header">
        <span style="font-size: 18px">About tool</span>
      </div>
      <span class="text">DeFiHap is geared towards automatically detecting and fixing APs in HiveQL applications. Application developers can leverage DeFiHap to create more performant, maintainable and accurate bigdata applications. After detecting HiveQL APs, the desired fixes are generated for statement APs and configuration APs respectively, combining rule-based and performance tuning techniques.
You can get the source code and installing guide in <a href="https://github.com/ChrisCN97/HAPDF" target="_blank">Github repository</a>.</span>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "About"
}
</script>

<style scoped>
.text {
  font-size: 14px;
  color: #606266;
  white-space: pre-wrap;
}
</style>
