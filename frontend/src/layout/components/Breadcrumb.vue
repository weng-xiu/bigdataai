<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="item.redirect === 'noRedirect' || index === levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 面包屑数据
const levelList = ref([])

// 生成面包屑数据
const getBreadcrumb = () => {
  // 过滤掉没有meta和title的路由
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  
  // 如果第一个不是首页，添加首页
  const first = matched[0]
  if (first && first.path !== '/dashboard') {
    matched = [{
      path: '/dashboard',
      meta: { title: '首页' }
    }].concat(matched)
  }
  
  levelList.value = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
}

// 处理链接点击
const handleLink = (item) => {
  const { path } = item
  router.push(path)
}

// 监听路由变化
watch(() => route.path, () => {
  getBreadcrumb()
}, { immediate: true })
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
  
  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
  
  .el-breadcrumb__inner a {
    color: #666;
    font-weight: normal;
    
    &:hover {
      color: #409EFF;
    }
  }
}

/* 面包屑动画 */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-active {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}
</style>