import axios from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const macolaAxios = axios.create({
  baseURL: 'http://localhost:8123',
  timeout: 5000,
  withCredentials: true,
})

// 全局请求拦截器
macolaAxios.interceptors.request.use(
  function (config) {
    // do somethind before request sented, e.g.: console.log(config.path)
    return config
  },
  function (error) {
    return error
  },
)

// 全局响应拦截器
macolaAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code == 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !response.request.responseURL.includes('user.login')
      ) {
        message.warning('请先登录')
        window.location.href = 'user/login?redirect=${window.location.href}'
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default macolaAxios;
