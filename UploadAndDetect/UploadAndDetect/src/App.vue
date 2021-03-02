<template>
  <div id="app">
    <el-container>
      <el-header class="header" height="70px">
        <vheader/>
      </el-header>
      <el-container :style="defaultHeight">
        <el-aside width="220px">
          <navmenu></navmenu>
        </el-aside>
        <el-main style="padding: 0 120px; margin-top: 20px">
<!--        <el-main>-->
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import NavMenu from './components/NavMenu.vue'
import Header from './components/Header'


export default {
  name: 'app',
  components:{
    'navmenu': NavMenu,
    'vheader': Header
  },
  data() {
    return {
        defaultHeight: {
            height: ""
        }
    };
},
methods: {
    // Define method, get height minus head and tail
    getHeight() {
        this.defaultHeight.height = window.innerHeight - 90 + "px";
    }
},
created() {
    // When the page is created, getHeight is executed once for assignment, and the resize event is bound by the way
    window.addEventListener("resize", this.getHeight);
    console.log("height");
    this.getHeight();
}

}

</script>


<style >
.header {
  background-color: #2970b6;
  color: #fff;
  line-height: 33px;
}
html,body,#app,.el-container{
  padding: 0;
  margin: 0;
  height: 100%;
}

</style>
