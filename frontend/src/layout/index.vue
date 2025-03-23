<template>
  <div class="app-wrapper" :class="{ 'mobile': device === 'mobile', 'hide-sidebar': !sidebar.opened }">
    <!-- 侧边栏 -->
    <sidebar class="sidebar-container" />
    
    <!-- 主要内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <navbar />
      
      <!-- 标签导航 -->
      <tags-view v-if="showTagsView" />
      
      <!-- 应用主体内容 -->
      <app-main />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'

import Navbar from './components/Navbar.vue'
import Sidebar from './components/Sidebar/index.vue'
import AppMain from './components/AppMain.vue'
import TagsView from './components/TagsView/index.vue'

const store = useStore()
const route = useRoute()

// 计算属性
const sidebar = computed(() => store.state.app.sidebar)
const device = computed(() => store.state.app.device)
const showTagsView = computed(() => true) // 可以从配置中读取

// 监听窗口大小变化
const watchWindowResize = () => {
  const width = document.documentElement.clientWidth
  if (width < 992) {
    store.dispatch('app/toggleDevice', 'mobile')
    store.dispatch('app/closeSideBar', { withoutAnimation: true })
  } else {
    store.dispatch('app/toggleDevice', 'desktop')
  }
}

// 监听路由变化
watch(() => route.path, () => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    store.dispatch('app/closeSideBar', { withoutAnimation: false })
  }
})

// 生命周期钩子
onMounted(() => {
  window.addEventListener('resize', watchWindowResize)
  watchWindowResize()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', watchWindowResize)
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
  
  &.mobile.hide-sidebar {
    .sidebar-container {
      transform: translate3d(-210px, 0, 0);
    }
  }
}

.sidebar-container {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  width: 210px;
  transition: width 0.28s, transform 0.28s;
  z-index: 1001;
  overflow: hidden;
  background-color: #304156;
}

.main-container {
  min-height: 100%;
  transition: margin-left 0.28s;
  margin-left: 210px;
  position: relative;
}

.hide-sidebar {
  .sidebar-container {
    width: 54px !important;
  }
  
  .main-container {
    margin-left: 54px;
  }
}

/* 移动端适配 */
.mobile {
  .main-container {
    margin-left: 0px;
  }
  
  .sidebar-container {
    transition: transform 0.28s;
    width: 210px !important;
  }
}
</style>