import { createStore } from 'vuex'
import user from './modules/user'
import app from './modules/app'

export default createStore({
  modules: {
    user,
    app
  },
  // 全局状态
  state: {
    // 全局加载状态
    loading: false,
    // 系统信息
    systemInfo: {}
  },
  // 修改状态的方法
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_SYSTEM_INFO(state, info) {
      state.systemInfo = info
    }
  },
import request from '@/utils/request' // 导入封装的axios实例

  // 异步操作
  actions: {
    // 获取系统信息
    async getSystemInfo({ commit }) {
      try {
        // 使用封装的axios实例调用后端API
        const response = await request({
          url: '/api/system/info',
          method: 'get'
        })

        // axios 封装通常会将后端返回的 data 直接放在 response.data 中
        // 需要根据 request.js 的具体实现调整
        // 假设 request.js 成功时直接返回 data，失败时抛出错误
        const systemInfo = response // 或者 response.data，取决于 request.js

        // 假设后端接口成功时返回的数据包含 success 字段
        if (systemInfo && systemInfo.success) { 
          commit('SET_SYSTEM_INFO', systemInfo);
          return systemInfo;
        } else {
          // 如果 request.js 没有在拦截器中处理错误消息，这里可以处理
          const message = systemInfo?.message || '获取系统信息失败';
          console.error('获取系统信息失败:', message);
          throw new Error(message);
        }
      } catch (error) {
        // 错误可能已在 request.js 的拦截器中处理
        console.error('调用系统信息API时出错:', error);
        commit('SET_SYSTEM_INFO', { error: true, message: error.message || '请求失败' }); 
        throw error; // 重新抛出错误，以便调用处可以处理
      }
    }
  },
  // 计算属性
  getters: {
    loading: state => state.loading,
    systemInfo: state => state.systemInfo
  }
})