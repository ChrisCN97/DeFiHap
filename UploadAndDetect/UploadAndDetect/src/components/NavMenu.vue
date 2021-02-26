<template>
  <div class="left-nav" style="height: 100%">
    <el-row class="tac" style="height: 100%">
      <el-col :span="24" style="height: 100%">
        <el-menu
          default-active="2"
          class="el-menu-vertical-demo"
          @open="handleOpen"
          @close="handleClose"
          router
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          style="height: 100%;"
        >
          <el-menu-item index="/detect" style="margin-top: 15px">
            <i class="el-icon-s-order"></i>
            <span slot="title">Detect and Fix</span>
          </el-menu-item>
          <el-menu-item index="/dic">
            <i class="el-icon-notebook-1"></i>
            <span slot="title">AP Dictionary</span>
          </el-menu-item>
          <el-menu-item @click="open">
            <i class="el-icon-setting"></i>
            <span slot="title">Configuration</span>
          </el-menu-item>
          <el-menu-item index="/about">
            <i class="el-icon-info"></i>
            <span slot="title">About</span>
          </el-menu-item>
        </el-menu>
      </el-col>
    </el-row>
  </div>
</template>
<script>
export default {
    data(){
        return {
          opens: ["detectAndFix","configuration"]
        }
    },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    open() {
      this.$alert('This is a demo version running in private hive environment, ' +
        'you may not change the configuration for safety reasons.\n' +
        'Please download the source code and deploy it for full function.', 'Warning', {
        confirmButtonText: 'OK'
      });
    },
  },
};
</script>
