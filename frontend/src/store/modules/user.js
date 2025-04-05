import { login, logout, refreshToken, validateToken } from '@/api/auth'
import { getUserInfo } from '@/api/user'

const state = {
  token: localStorage.getItem('token'),
  refreshToken: localStorage.getItem('refreshToken'),
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
  roles: [],
  permissions: []
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)
  },
  SET_REFRESH_TOKEN(state, refreshToken) {
    state.refreshToken = refreshToken
    localStorage.setItem('refreshToken', refreshToken)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  SET_ROLES(state, roles) {
    state.roles = roles
  },
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions
  },
  CLEAR_USER(state) {
    state.token = ''
    state.refreshToken = ''
    state.userInfo = {}
    state.roles = []
    state.permissions = []
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  // 用户登录
  login({ commit }, userInfo) {
    const { username, password, remember } = userInfo
    return new Promise((resolve, reject) => {
      login({ username, password })
        .then(response => {
          if (response.status === 'success') {
            const { token, refreshToken, user } = response
            commit('SET_TOKEN', token)
            commit('SET_REFRESH_TOKEN', refreshToken)
            commit('SET_USER_INFO', user)
            commit('SET_ROLES', user.roles || [])
            commit('SET_PERMISSIONS', user.permissions || [])
            
            // 如果选择了记住密码，可以在本地存储用户名
            if (remember) {
              localStorage.setItem('rememberedUsername', username)
            } else {
              localStorage.removeItem('rememberedUsername')
            }
            
            resolve(user)
          } else {
            reject(new Error(response.message || '登录失败'))
          }
        })
        .catch(error => {
          reject(error)
        })
    })
  },
  
  // 获取用户信息
  getUserInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getUserInfo()
        .then(response => {
          const { data } = response
          commit('SET_USER_INFO', data)
          commit('SET_ROLES', data.roles || [])
          commit('SET_PERMISSIONS', data.permissions || [])
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },
  
  // 用户登出
  logout({ commit }) {
    return new Promise((resolve, reject) => {
      logout()
        .then(() => {
          commit('CLEAR_USER')
          resolve()
        })
        .catch(error => {
          // 即使后端登出失败，也清除前端状态
          commit('CLEAR_USER')
          resolve()
        })
    })
  },
  
  // 刷新令牌
  refreshToken({ commit, state }) {
    return new Promise((resolve, reject) => {
      if (!state.refreshToken) {
        reject(new Error('没有刷新令牌'))
        return
      }
      
      refreshToken(state.refreshToken)
        .then(response => {
          if (response.status === 'success') {
            const { token, refreshToken, user } = response
            commit('SET_TOKEN', token)
            commit('SET_REFRESH_TOKEN', refreshToken)
            commit('SET_USER_INFO', user)
            commit('SET_ROLES', user.roles || [])
            commit('SET_PERMISSIONS', user.permissions || [])
            resolve(token)
          } else {
            reject(new Error(response.message || '刷新令牌失败'))
          }
        })
        .catch(error => {
          reject(error)
        })
    })
  },
  
  // 验证令牌
  validateToken({ commit, state, dispatch }) {
    return new Promise((resolve, reject) => {
      if (!state.token) {
        reject(new Error('没有令牌'))
        return
      }
      
      validateToken(state.token)
        .then(response => {
          if (response.valid) {
            // 如果令牌有效，更新用户信息
            if (response.user) {
              commit('SET_USER_INFO', response.user)
              commit('SET_ROLES', response.user.roles || [])
              commit('SET_PERMISSIONS', response.user.permissions || [])
            }
            resolve(true)
          } else {
            // 如果令牌无效，尝试使用刷新令牌
            dispatch('refreshToken')
              .then(() => resolve(true))
              .catch(() => {
                commit('CLEAR_USER')
                reject(new Error('令牌已过期，请重新登录'))
              })
          }
        })
        .catch(error => {
          // 验证失败，尝试刷新令牌
          dispatch('refreshToken')
            .then(() => resolve(true))
            .catch(() => {
              commit('CLEAR_USER')
              reject(error)
            })
        })
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}