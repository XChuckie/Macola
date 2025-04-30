<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

// import { healthUsingGet } from '@/api/mainController'
// import { useLoginUserStore } from '@/stores/useLoginUserStore'
//
// healthUsingGet().then((res) => {
//   console.log(res)
// })
// 已经改为在权限校验文件中获取
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser() // 通过调用fetchLoginUser让全局共享对象loginUser的值变化
</script>
