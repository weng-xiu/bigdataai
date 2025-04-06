import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录信息 {username, password, captcha}
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取验证码
 * @returns {Promise}
 */
export function getCaptcha() {
  return request({
    url: '/auth/captcha',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 验证验证码
 * @param {string} captcha - 验证码
 * @returns {Promise}
 */
export function validateCaptcha(captcha) {
  return request({
    url: '/auth/validate-captcha',
    method: 'post',
    data: { captcha }
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {string} roleName - 角色名称
 * @returns {Promise}
 */
export function register(data, roleName = 'ROLE_USER') {
  return request({
    url: '/auth/register',
    method: 'post',
    data,
    params: { roleName }
  })
}

/**
 * 刷新令牌
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise}
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/auth/refresh-token',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 验证令牌
 * @param {string} token - JWT令牌
 * @returns {Promise}
 */
export function validateToken(token) {
  return request({
    url: '/auth/validate-token',
    method: 'post',
    data: { token }
  })
}

/**
 * 登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}