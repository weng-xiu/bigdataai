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
  // 异步操作
  actions: {
    // 获取系统信息
    getSystemInfo({ commit }) {
      return new Promise((resolve, reject) => {
        // 这里应该调用API获取系统信息
        setTimeout(() => {
          const systemInfo = {
            version: '1.0.0',
            serverStatus: 'running',
            dataNodes: 5,
            totalStorage: '10TB',
            usedStorage: '4.5TB'
          }
          commit('SET_SYSTEM_INFO', systemInfo)
          resolve(systemInfo)
        }, 500)
      })
    }
  },
  // 计算属性
  getters: {
    loading: state => state.loading,
    systemInfo: state => state.systemInfo
  }
})