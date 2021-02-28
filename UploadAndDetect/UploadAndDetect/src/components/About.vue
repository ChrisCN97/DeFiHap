<template>
  <div>
    <el-card>
      <div slot="header">
        <span style="font-size: 18px">About us</span>
      </div>
      <span class="text">
        Anonymized Author
      </span>
    </el-card>

    <el-card style="margin-top: 10px">
      <div slot="header">
        <span style="font-size: 18px">About tool</span>
      </div>
      <span class="text">
        HAPDF is mainly divided into two phases: detection and fix. HAPDF takes the HiveQL statement
        and Hive configurations entered by the user as input. In the detection phase, HAPDF first converts the HiveQL
        statement into an abstract syntax tree (AST), then it traverses the AST to detect anti-patterns based on the
        preset detecting rules. In the fix phase, HAPDF constructs the HiveQL statement template according to the
        predefined statement generating gramma and replaces the wrong syntax or semantics with the correct ones based
        on the detection result. You can get the source code and installing guide in
        <a href="https://github.com/ChrisCN97/HAPDF" target="_blank">Github repository</a>.
      </span>
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
  margin-top:10px;
  margin-bottom:10px;
  font-size: 14px;
  color: #606266;
}
</style>
