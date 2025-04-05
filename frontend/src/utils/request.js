import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import store from '@/store'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // API的基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    if (store.state.user.token) {
      // 让每个请求携带token
      config.headers['Authorization'] = `Bearer ${store.state.user.token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 直接返回数据，由业务代码处理状态
    return res
  },
  async error => {
    console.error('请求错误:', error)
    
    // 处理401错误（未授权）
    if (error.response && error.response.status === 401) {
      // 尝试刷新令牌
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          // 尝试刷新令牌
          const response = await axios.post('/api/auth/refresh-token', { refreshToken })
          if (response.data.status === 'success') {
            // 更新令牌
            store.commit('user/SET_TOKEN', response.data.token)
            store.commit('user/SET_REFRESH_TOKEN', response.data.refreshToken)
            
            // 重试原始请求
            const config = error.config
            config.headers['Authorization'] = `Bearer ${response.data.token}`
            return axios(config)
          }
        } catch (refreshError) {
          console.error('刷新令牌失败:', refreshError)
          // 刷新令牌失败，需要重新登录
          ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            store.dispatch('user/logout').then(() => {
              location.reload() // 重新加载页面
            })
          })
        }
      } else {
        // 没有刷新令牌，直接提示登录
        ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          store.dispatch('user/logout').then(() => {
            location.reload() // 重新加载页面
          })
        })
      }
    }
    
    // 显示错误消息
    ElMessage({
      message: error.response?.data?.message || error.message || '请求失败',
      type: 'error',
      duration: 5 * 1000
    })
    
    return Promise.reject(error)
  }

)

export default service