import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUserUsingGet } from '@/api/userController'

/**
 * 状态类定义：存储一类需要共享的数据
 */

export const useLoginUserStore = defineStore('loginUser', () => {
  // 定义初始值
  const loginUser = ref<API.LoginUserV0>({
    userName: '未登录',
  })

  /**
   * 远程获取用户登录态信息
   */
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()

    if (res.data.code == 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  // 通过设置来修改loginUser
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
