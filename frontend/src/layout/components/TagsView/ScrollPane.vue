<template>
  <el-scrollbar
    ref="scrollContainer"
    class="scroll-container"
    :vertical="false"
    @wheel.prevent="handleScroll"
  >
    <slot />
  </el-scrollbar>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useEventListener } from '@vueuse/core'

const props = defineProps({
  // 可以添加需要的props
})

const emit = defineEmits(['scroll'])

// 滚动容器引用
const scrollContainer = ref(null)
const scrollWrapper = computed(() => scrollContainer.value?.$el.querySelector('.el-scrollbar__wrap'))

// 处理滚动事件
const handleScroll = (e) => {
  const eventDelta = e.wheelDelta || -e.deltaY * 40
  const $scrollWrapper = scrollWrapper.value
  $scrollWrapper.scrollLeft = $scrollWrapper.scrollLeft - eventDelta / 4
}

// 移动到目标标签
const moveToTarget = (currentTag) => {
  const $container = scrollContainer.value?.$el
  const $containerWidth = $container.offsetWidth
  const $scrollWrapper = scrollWrapper.value

  // 所有标签
  const $tags = $container.querySelectorAll('.tags-view-item')
  let firstTag = null
  let lastTag = null

  // 找到第一个和最后一个标签
  if ($tags.length > 0) {
    firstTag = $tags[0]
    lastTag = $tags[$tags.length - 1]
  }

  if (!firstTag || !lastTag) return

  // 目标标签
  let tagIndex = 0
  const tags = Array.from($tags)
  tags.forEach((el, i) => {
    if (el.dataset.path === currentTag.path) {
      tagIndex = i
    }
  })

  // 目标标签元素
  const targetTag = tags[tagIndex]
  if (!targetTag) return

  // 计算滚动位置
  const tagOffsetLeft = targetTag.offsetLeft
  const tagOffsetWidth = targetTag.offsetWidth

  // 如果标签在可视区域外，则滚动到标签位置
  if (tagOffsetLeft < $scrollWrapper.scrollLeft) {
    // 标签在左侧，滚动到标签位置
    $scrollWrapper.scrollLeft = tagOffsetLeft - 10
  } else if (tagOffsetLeft + tagOffsetWidth > $scrollWrapper.scrollLeft + $containerWidth) {
    // 标签在右侧，滚动到标签位置
    $scrollWrapper.scrollLeft = tagOffsetLeft + tagOffsetWidth - $containerWidth + 10
  }
}

// 暴露方法给父组件
defineExpose({
  moveToTarget
})

// 监听窗口大小变化
const handleResize = () => {
  moveToTarget()
}

onMounted(() => {
  useEventListener(window, 'resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  width: 100%;
  
  :deep(.el-scrollbar__bar) {
    bottom: 0px;
  }
  
  :deep(.el-scrollbar__wrap) {
    height: 49px;
  }
}
</style>