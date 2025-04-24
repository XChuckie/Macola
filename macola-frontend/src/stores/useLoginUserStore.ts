import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 状态类定义：存储一类需要共享的数据
 */

export const useLoginUserStore = defineStore('loginUser', () => {
  // 定义初始值
  const loginUser = ref<any>({
    username: '未登录',
  })

  // 通过抓取来修改loginUser
  async function fetchLoginUser() {
    // todo 由于后端还没提供接口，暂时注释
    // setTimeout(() => {
    //   loginUser.value = { userName: '测试用户', id: 1 }
    // }, 3000)
    // const res = await getCurrentUser();
    // if (res.data.code === 0 && res.data.data) {
    //   loginUser.value = res.data.data;
    // }
  }

  // 通过设置来修改loginUser
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
