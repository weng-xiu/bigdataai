<template>
  <div class="navbar">
    <hamburger class="hamburger-container" :is-active="sidebar.opened" @toggle-click="toggleSideBar" />
    
    <breadcrumb class="breadcrumb-container" />
    
    <div class="right-menu">
      <!-- 系统状态指示器 -->
      <el-tooltip content="系统状态" effect="dark" placement="bottom">
        <div class="right-menu-item">
          <el-badge :is-dot="true" :hidden="systemStatus === 'normal'">
            <el-icon :color="statusColor"><Monitor /></el-icon>
          </el-badge>
        </div>
      </el-tooltip>
      
      <!-- 全屏按钮 -->
      <el-tooltip content="全屏" effect="dark" placement="bottom">
        <div class="right-menu-item" @click="toggleFullScreen">
          <el-icon><FullScreen /></el-icon>
        </div>
      </el-tooltip>
      
      <!-- 用户头像下拉菜单 -->
      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="30" :src="userAvatar" />
          <span class="user-name">{{ userName }}</span>
          <el-icon class="el-icon-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="toUserInfo">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import Hamburger from './Hamburger.vue'
import Breadcrumb from './Breadcrumb.vue'

const store = useStore()
const router = useRouter()

// 侧边栏状态
const sidebar = computed(() => store.state.app.sidebar)

// 用户信息
const userInfo = computed(() => store.state.user.userInfo)
const userName = computed(() => userInfo.value.name || '用户')
const userAvatar = computed(() => userInfo.value.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 系统状态
const systemInfo = computed(() => store.state.systemInfo)
const systemStatus = computed(() => systemInfo.value.serverStatus || 'normal')
const statusColor = computed(() => {
  switch (systemStatus.value) {
    case 'warning': return '#E6A23C'
    case 'error': return '#F56C6C'
    default: return '#67C23A'
  }
})

// 切换侧边栏
const toggleSideBar = () => {
  store.dispatch('app/toggleSideBar')
}

// 切换全屏
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

// 跳转到用户信息页
const toUserInfo = () => {
  router.push('/user/profile')
}

// 退出登录
const logout = () => {
  store.dispatch('user/logout').then(() => {
    router.push('/login')
  })
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  
  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    padding: 0 15px;
    cursor: pointer;
    transition: background 0.3s;
    
    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }
  
  .breadcrumb-container {
    float: left;
    margin-left: 15px;
  }
  
  .right-menu {
    float: right;
    height: 100%;
    display: flex;
    align-items: center;
    padding-right: 15px;
    
    .right-menu-item {
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: middle;
      cursor: pointer;
      display: flex;
      align-items: center;
      
      &:hover {
        background: rgba(0, 0, 0, 0.025);
      }
    }
    
    .avatar-container {
      margin-right: 10px;
      
      .avatar-wrapper {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .user-name {
          margin: 0 5px;
          color: #606266;
        }
        
        .el-icon-arrow {
          font-size: 12px;
        }
      }
    }
  }
}
</style>