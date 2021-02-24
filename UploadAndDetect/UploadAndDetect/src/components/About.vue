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
        You can get the source code and installing guide in
        <a href="https://github.com/ChrisCN97/HAPDF" target="_blank">Github repository</a>
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
