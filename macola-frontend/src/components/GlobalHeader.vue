<template>
  <div id="globalheader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img src="../assets/logo.png" alt="logo" class="logo" />
            <div class="title">Macola</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="menus"
          @click="onMenuClick"
        />
      </a-col>
      <a-col flex="100px">
        <div class="user-login-state">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <ASpace>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </ASpace>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogoutUsingPost } from '@/api/userController'
import checkAccess from '@/access/checkAccess'
import router from '@/router/index'

const loginUserStore = useLoginUserStore()

// 将路由转换为菜单项
const menuToRouteItem = (route: any) => {
  return {
    key: route.path,
    label: route.meta.isExternal
      ? h('a', { href: route.meta.externalLink, target: route.meta.externalTarget }, route.name)
      : route.name,
    title: route.name,
    icon: route.meta.icon?.(),
  }
}
const menus = computed(() => {
  // compute确保菜单项在用户权限变化时能够自动更新
  // 通过(router as any).options.routes直接访问原始路由配置数组以保证原有顺序；而router.getRoutes()获得的路由无法和定义的顺序保持一致
  return (router as any).options.routes
    .filter((route: any) => {
      if (route.meta?.hiddenMenu) {
        return false
      }
      return checkAccess(loginUserStore.loginUser, route.meta?.access as string)
    })
    .map(menuToRouteItem)
})

// 使用 routerInstance 进行导航，避免与导入的路由实例冲突
const routerInstance = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
routerInstance.afterEach((to) => {
  current.value = [to.path]
})

// 路由跳转事件
const onMenuClick = ({ key }) => {
  routerInstance.push({
    path: key,
  })
}

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await routerInstance.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}
.logo {
  height: 35px;
}
.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}
</style>
