import { login, logout, getUserInfo } from '@/api/user'

const state = {
  token: localStorage.getItem('token'),
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
  roles: []
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  SET_ROLES(state, roles) {
    state.roles = roles
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = {}
    state.roles = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
}

const actions = {
  // 用户登录
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      // 模拟API调用
      setTimeout(() => {
        if (username === 'admin' && password === '123456') {
          const token = 'mock-token-' + Date.now()
          commit('SET_TOKEN', token)
          resolve()
        } else {
          reject(new Error('用户名或密码错误'))
        }
      }, 500)
      
      // 实际API调用
      // login({ username, password }).then(response => {
      //   const { data } = response
      //   commit('SET_TOKEN', data.token)
      //   resolve()
      // }).catch(error => {
      //   reject(error)
      // })
    })
  },
  
  // 获取用户信息
  getUserInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      // 模拟API调用
      setTimeout(() => {
        const userInfo = {
          id: 1,
          username: 'admin',
          nickname: '管理员',
          avatar: 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif',
          roles: ['admin']
        }
        commit('SET_USER_INFO', userInfo)
        commit('SET_ROLES', userInfo.roles)
        resolve(userInfo)
      }, 500)
      
      // 实际API调用
      // getUserInfo(state.token).then(response => {
      //   const { data } = response
      //   commit('SET_USER_INFO', data)
      //   commit('SET_ROLES', data.roles)
      //   resolve(data)
      // }).catch(error => {
      //   reject(error)
      // })
    })
  },
  
  // 用户登出
  logout({ commit }) {
    return new Promise((resolve, reject) => {
      // 模拟API调用
      setTimeout(() => {
        commit('CLEAR_USER')
        resolve()
      }, 500)
      
      // 实际API调用
      // logout().then(() => {
      //   commit('CLEAR_USER')
      //   resolve()
      // }).catch(error => {
      //   reject(error)
      // })
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}