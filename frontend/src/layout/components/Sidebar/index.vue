<template>
  <div class="sidebar-container">
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :unique-opened="false"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const store = useStore()

// 变量定义
const variables = {
  menuBg: '#304156',
  menuText: '#bfcbd9',
  menuActiveText: '#409EFF'
}

// 侧边栏状态
const sidebar = computed(() => store.state.app.sidebar)
const isCollapse = computed(() => !sidebar.value.opened)

// 路由
const routes = computed(() => {
  // 从router中获取路由配置
  const router = route.matched[0].parent || route.matched[0]
  const routes = router.children || []
  
  // 过滤掉隐藏的路由
  return routes.filter(route => {
    return !route.hidden
  })
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  // 如果设置了activeMenu，则使用activeMenu
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  background-color: #304156;
  
  .scrollbar-wrapper {
    overflow-x: hidden !important;
  }
  
  .el-scrollbar__view {
    height: 100%;
  }
  
  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
  }
}
</style>