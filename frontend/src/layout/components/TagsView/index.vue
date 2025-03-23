<template>
  <div class="tags-view-container">
    <scroll-pane class="tags-view-wrapper" ref="scrollPaneRef">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.path"
        :class="isActive(tag) ? 'active' : ''"
        :to="{ path: tag.path, query: tag.query, fullPath: tag.fullPath }"
        class="tags-view-item"
        @click.middle="!isAffix(tag) && closeSelectedTag(tag)"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        <el-icon v-if="tag.meta.icon" class="tag-icon"><component :is="tag.meta.icon" /></el-icon>
        {{ tag.meta.title }}
        <el-icon v-if="!isAffix(tag)" class="close-icon" @click.prevent.stop="closeSelectedTag(tag)"><Close /></el-icon>
      </router-link>
    </scroll-pane>
    <ul v-show="visible" :style="{left: left + 'px', top: top + 'px'}" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">刷新</li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">关闭</li>
      <li @click="closeOthersTags">关闭其他</li>
      <li @click="closeAllTags(selectedTag)">关闭所有</li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import ScrollPane from './ScrollPane.vue'

const route = useRoute()
const router = useRouter()
const store = useStore()
const scrollPaneRef = ref(null)

// 右键菜单相关
const visible = ref(false)
const top = ref(0)
const left = ref(0)
const selectedTag = ref({})

// 访问过的视图
const visitedViews = ref([
  // 示例数据，实际应从store中获取
  {
    path: '/dashboard',
    meta: { title: '首页', affix: true, icon: 'HomeFilled' }
  }
])

// 判断是否是当前激活的标签
const isActive = (tag) => {
  return tag.path === route.path
}

// 判断是否是固定的标签
const isAffix = (tag) => {
  return tag.meta && tag.meta.affix
}

// 添加标签
const addTags = () => {
  const { name, path, meta, fullPath, query } = route
  if (name) {
    // 这里应该调用store的action来添加标签
    // 暂时直接修改本地状态，后续可以改为store管理
    const isExists = visitedViews.value.some(v => v.path === path)
    if (!isExists) {
      visitedViews.value.push({
        name,
        path,
        meta: { ...meta },
        fullPath,
        query
      })
    }
  }
}

// 刷新选中的标签
const refreshSelectedTag = (tag) => {
  closeMenu()
  // 这里可以实现刷新当前路由的逻辑
  router.replace({ path: '/redirect' + tag.fullPath })
}

// 关闭选中的标签
const closeSelectedTag = (tag) => {
  closeMenu()
  // 这里应该调用store的action来关闭标签
  // 暂时直接修改本地状态，后续可以改为store管理
  visitedViews.value = visitedViews.value.filter(v => v.path !== tag.path)
  if (isActive(tag)) {
    toLastView(visitedViews.value, tag)
  }
}

// 关闭其他标签
const closeOthersTags = () => {
  closeMenu()
  router.push(selectedTag.value)
  // 过滤出固定标签和当前选中的标签
  visitedViews.value = visitedViews.value.filter(tag => {
    return isAffix(tag) || tag.path === selectedTag.value.path
  })
}

// 关闭所有标签
const closeAllTags = () => {
  closeMenu()
  // 过滤出固定标签
  visitedViews.value = visitedViews.value.filter(tag => isAffix(tag))
  // 如果没有固定标签，跳转到首页
  if (visitedViews.value.length === 0) {
    router.push('/')
  } else {
    router.push(visitedViews.value[0])
  }
}

// 打开右键菜单
const openMenu = (tag, e) => {
  const menuMinWidth = 105
  const offsetLeft = document.querySelector('.tags-view-container').getBoundingClientRect().left
  const offsetWidth = document.querySelector('.tags-view-container').offsetWidth
  const maxLeft = offsetWidth - menuMinWidth
  const l = e.clientX - offsetLeft + 15

  left.value = l > maxLeft ? maxLeft : l
  top.value = 30
  visible.value = true
  selectedTag.value = tag
}

// 关闭右键菜单
const closeMenu = () => {
  visible.value = false
}

// 跳转到最后一个标签
const toLastView = (visitedViews, view) => {
  const latestView = visitedViews.slice(-1)[0]
  if (latestView) {
    router.push(latestView.fullPath)
  } else {
    // 如果没有标签，跳转到首页
    if (view.name === 'Dashboard') {
      router.replace({ path: '/redirect' + view.fullPath })
    } else {
      router.push('/')
    }
  }
}

// 监听路由变化，添加标签
watch(() => route.path, () => {
  addTags()
  moveToCurrentTag()
}, { immediate: true })

// 移动到当前标签
const moveToCurrentTag = () => {
  nextTick(() => {
    for (const tag of visitedViews.value) {
      if (tag.path === route.path) {
        scrollPaneRef.value?.moveToTarget(tag)
        if (tag.fullPath !== route.fullPath) {
          // 更新标签的fullPath
          visitedViews.value = visitedViews.value.map(item => {
            if (item.path === tag.path) {
              item.fullPath = route.fullPath
              item.query = route.query
            }
            return item
          })
        }
        break
      }
    }
  })
}

// 点击页面其他地方关闭右键菜单
const handleClickOutside = (e) => {
  const menu = document.querySelector('.contextmenu')
  if (menu && !menu.contains(e.target)) {
    closeMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
  
  .tags-view-wrapper {
    .tags-view-item {
      display: inline-flex;
      align-items: center;
      position: relative;
      cursor: pointer;
      height: 26px;
      line-height: 26px;
      border: 1px solid #d8dce5;
      color: #495060;
      background: #fff;
      padding: 0 8px;
      font-size: 12px;
      margin-left: 5px;
      margin-top: 4px;
      border-radius: 3px;
      
      &:first-of-type {
        margin-left: 15px;
      }
      
      &.active {
        background-color: #42b983;
        color: #fff;
        border-color: #42b983;
        
        &::before {
          content: '';
          background: #fff;
          display: inline-block;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          position: relative;
          margin-