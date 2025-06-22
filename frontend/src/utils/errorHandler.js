import { ElMessage } from 'element-plus'
import store from '@/store'

/**
 * 统一的错误处理工具类
 * 用于处理API请求错误和显示错误消息
 */
export default class ErrorHandler {
  /**
   * 处理API请求错误
   * @param {Error} error - 错误对象
   * @param {string} customMessage - 自定义错误消息
   */
  static handleApiError(error, customMessage = '') {
    // 设置加载状态为false
    store.commit('SET_LOADING', false)

    // 获取错误信息
    const errorMessage = error.response?.data?.message || 
                        error.message || 
                        customMessage || 
                        '操作失败'

    // 根据错误状态码处理不同类型的错误
    const status = error.response?.status
    switch (status) {
      case 400:
        ElMessage.error(`请求参数错误: ${errorMessage}`)
        break
      case 401:
        ElMessage.error('未授权，请重新登录')
        // 可以在这里处理登出逻辑
        break
      case 403:
        ElMessage.error('没有权限执行此操作')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        ElMessage.error(errorMessage)
    }

    // 记录错误日志
    console.error('API Error:', error)
  }

  /**
   * 显示成功消息
   * @param {string} message - 成功消息
   */
  static showSuccess(message) {
    ElMessage.success(message)
  }

  /**
   * 显示警告消息
   * @param {string} message - 警告消息
   */
  static showWarning(message) {
    ElMessage.warning(message)
  }

  /**
   * 显示错误消息
   * @param {string} message - 错误消息
   */
  static showError(message) {
    ElMessage.error(message)
  }
}