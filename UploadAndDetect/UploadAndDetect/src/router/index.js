import Vue from 'vue'
import Router from 'vue-router'
import SetConfig from '@/components/SetConfig'
import Detect from '@/components/Detect'
import About from '@/components/About'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/detect'
    },
    {
      path:'/detect',
      name: 'Detect',
      component: Detect
    },
    {
      path:'/set',
      name:'Set',
      component:SetConfig
    },
    {
      path:'/about',
      name:'About',
      component:About
    },
  ]
})
