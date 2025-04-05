import request from '@/utils/request'

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - 包含旧密码和新密码
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}

/**
 * 获取用户列表
 * @param {Object} query - 查询参数
 * @returns {Promise}
 */
export function getUserList(query) {
  return request({
    url: '/user/list',
    method: 'get',
    params: query
  })
}