// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App'
import router from './router'
import store from './store'
import common from './components/common'
import locale from 'element-ui/lib/locale/lang/en'

var axios = require('axios')
Vue.config.productionTip = false
Vue.use(ElementUI, {
  locale
})
// Quote axios and set the base URL
// axios.default.baseURL = 'https://localhost:8088'
// Bind API methods to the global
Vue.prototype.$axios = axios
Vue.prototype.common = common // Mount to the Vue instance

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
});
