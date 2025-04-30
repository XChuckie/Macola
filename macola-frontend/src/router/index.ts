import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'
import { h } from 'vue'
import {
  HomeOutlined,
  DashboardOutlined,
  CodeSandboxOutlined,
  GithubOutlined,
} from '@ant-design/icons-vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
      meta: {
        icon: () => h(HomeOutlined),
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
      meta: {
        hiddenMenu: true, // 默认隐藏
      },
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
      meta: {
        hiddenMenu: true, // 默认隐藏
      },
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        icon: () => h(DashboardOutlined),
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/programNavigation-link',
      name: '编程导航',
      component: HomePage, // 提供组件，但不会实际使用
      meta: {
        icon: () => h(CodeSandboxOutlined),
        isExternal: true,
        externalLink: 'https://www.codefather.cn',
        externalTarget: '_blank',
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
    {
      path: '/owenGithub-link',
      name: 'Github',
      component: HomePage, // 提供组件，但不会实际使用
      meta: {
        icon: () => h(GithubOutlined),
        isExternal: true,
        externalLink: 'https://github.com/XChuckie',
        externalTarget: '_blank',
        access: ACCESS_ENUM.NOT_LOGIN,
      },
    },
  ],
})

export default router
