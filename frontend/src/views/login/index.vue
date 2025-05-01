<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="title-container">
        <h3 class="title">大数据系统登录</h3>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" autocomplete="on">
        <!-- 用户名/邮箱 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            type="text"
            autocomplete="on"
            prefix-icon="User"
            :disabled="loading" 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            placeholder="密码"
            :type="passwordVisible ? 'text' : 'password'"
            autocomplete="on"
            prefix-icon="Lock"
            show-password
            :disabled="loading"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <!-- 记住密码 -->
        <div class="remember-container">
          <el-checkbox v-model="loginForm.remember" :disabled="loading">记住密码</el-checkbox>
          <el-link type="primary" class="forget-password" href="#">忘记密码?</el-link>
        </div>
        
        <!-- 验证码 -->
        <el-form-item v-if="showCaptcha" prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              class="captcha-input"
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaUrl" alt="验证码" />
            </div>
          </div>
        </el-form-item>
        
        <!-- 登录按钮 -->
        <el-button :loading="loading" type="primary" class="login-button" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { validEmail } from '@/utils/validate'

const store = useStore()
const router = useRouter()

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 密码可见状态
const passwordVisible = ref(false)

// 是否显示验证码
const showCaptcha = ref(false)

// 验证码URL
const captchaUrl = ref('')

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: localStorage.getItem('remember') === 'true',
  captcha: ''
})

// 如果记住了用户名，则自动填充
onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.remember = true
  }
  // 页面加载时不主动检查验证码，由登录失败触发
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, trigger: 'blur', message: '请输入用户名或邮箱' },
    { validator: (rule, value, callback) => {
      // 支持用户名或邮箱登录
      if (value.includes('@')) {
        // 如果输入包含@，则验证邮箱格式
        if (!validEmail(value)) {
          callback(new Error('请输入正确的邮箱格式'))
        } else {
          callback()
        }
      } else if (value.length < 3) {
        // 简单的用户名长度验证
        callback(new Error('用户名长度不能少于3个字符'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '请输入密码' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ],
  captcha: [
    // 验证码只有在显示时才需要校验
    { validator: (rule, value, callback) => {
        if (showCaptcha.value && !value) {
          callback(new Error('请输入验证码'))
        } else if (showCaptcha.value && (value.length < 4 || value.length > 6)) {
          callback(new Error('验证码长度不正确'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
  ]
}

// 刷新验证码
const refreshCaptcha = () => {
  // 确保只在需要时才刷新
  captchaUrl.value = `/api/auth/captcha?t=${new Date().getTime()}`
}

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true

      // 构建登录请求参数
      const loginData = {
        username: loginForm.username,
        password: loginForm.password
      }

      // 如果显示验证码，添加验证码参数
      if (showCaptcha.value) {
        loginData.captcha = loginForm.captcha
      }

      store.dispatch('user/login', loginData)
        .then((user) => {
          ElMessage.success('登录成功')

          // 登录成功，处理“记住我”
          if (loginForm.remember) {
            localStorage.setItem('rememberedUsername', loginForm.username)
            localStorage.setItem('remember', 'true')
          } else {
            localStorage.removeItem('rememberedUsername')
            localStorage.removeItem('remember')
          }
          // 登录成功后隐藏验证码
          showCaptcha.value = false

          // 跳转到首页或其他页面
          router.push('/')
        })
        .catch(error => {
          // 处理登录失败
          const errorMessage = error.message || '登录失败，请稍后重试'
          ElMessage.error(errorMessage)

          // 检查后端返回是否需要显示验证码
          if (error.data && error.data.captchaRequired) {
            showCaptcha.value = true
            refreshCaptcha()
          } else {
            // 如果不是因为需要验证码而失败，且验证码当前是显示的，也刷新一下
            if (showCaptcha.value) {
              refreshCaptcha()
            }
          }
        })
        .finally(() => {
          loading.value = false
        })
    } else {
      console.log('表单验证失败')
      return false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNDQwIiBoZWlnaHQ9IjYwMCIgZmlsbD0ibm9uZSI+PHBhdGggZmlsbD0iIzE4OTBmZiIgZmlsbC1vcGFjaXR5PSIuMDUiIGQ9Ik0wIDBoMTQ0MHY2MDBIMHoiLz48cGF0aCBzdHJva2U9IiNmZmYiIHN0cm9rZS1vcGFjaXR5PSIuMiIgZD0iTTE0NDAuNSA1OTkuNUgwVjBoMTQ0MC41djU5OS41eiIvPjxnIHN0cm9rZT0iIzE4OTBmZiIgc3Ryb2tlLW9wYWNpdHk9Ii4yIj48cGF0aCBkPSJNMTQ0MC41IDQ0OS41SDBNMTAwLjUgNTk5LjVWME0yMDAuNSA1OTkuNVYwTTMwMC41IDU5OS41VjBNNDAwLjUgNTk5LjVWME01MDAuNSA1OTkuNVYwTTYwMC41IDU5OS41VjBNNzAwLjUgNTk5LjVWME04MDAuNSA1OTkuNVYwTTkwMC41IDU5OS41VjBNMTAwMC41IDU5OS41VjBNMTEwMC41IDU5OS41VjBNMTIwMC41IDU5OS41VjBNMTMwMC41IDU5OS41VjBNMTQwMC41IDU5OS41VjAiLz48L2c+PGcgc3Ryb2tlPSIjMTg5MGZmIiBzdHJva2Utb3BhY2l0eT0iLjIiPjxwYXRoIGQ9Ik0xNDQwLjUgMTQ5LjVIME0xNDQwLjUgMjk5LjVIMCIvPjwvZz48L3N2Zz4=');
  background-size: cover;
  
  .login-card {
    width: 400px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .title-container {
      text-align: center;
      margin-bottom: 30px;
      
      .title {
        font-size: 26px;
        color: #303133;
        margin: 0;
      }
    }
    
    .login-form {
      .el-input {
        height: 40px;
        
        input {
          height: 40px;
        }
      }
      
      .remember-container {
        margin-bottom: 20px;
        color: #606266;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .forget-password {
          font-size: 14px;
        }
      }
      
      .captcha-container {
        display: flex;
        align-items: center;
        
        .captcha-input {
          flex: 1;
        }
        
        .captcha-image {
          margin-left: 10px;
          cursor: pointer;
          height: 40px;
          width: 120px;
          overflow: hidden;
          
          img {
            height: 100%;
            width: 100%;
            object-fit: cover;
          }
        }
      }
      
      .login-button {
        width: 100%;
        margin-bottom: 10px;
        height: 40px;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    .login-card {
      width: 90%;
      max-width: 400px;
    }
  }
}
</style>