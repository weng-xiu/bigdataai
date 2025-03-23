<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="title-container">
        <h3 class="title">大数据系统登录</h3>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" autocomplete="on">
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            type="text"
            autocomplete="on"
            prefix-icon="User"
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
          />
        </el-form-item>
        
        <!-- 记住密码 -->
        <div class="remember-container">
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
        </div>
        
        <!-- 登录按钮 -->
        <el-button :loading="loading" type="primary" class="login-button" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { validUsername } from '@/utils/validate'

const store = useStore()
const router = useRouter()

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 密码可见状态
const passwordVisible = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: 'admin',
  password: '123456',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, trigger: 'blur', message: '请输入用户名' },
    { validator: (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error('请输入正确的用户名'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '请输入密码' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      store.dispatch('user/login', loginForm)
        .then(() => {
          ElMessage.success('登录成功')
          router.push({ path: '/' })
        })
        .catch(error => {
          ElMessage.error(error.message || '登录失败，请重试')
        })
        .finally(() => {
          loading.value = false
        })
    } else {
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