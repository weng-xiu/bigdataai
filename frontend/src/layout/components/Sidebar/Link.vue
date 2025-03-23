<template>
  <component :is="type" v-bind="linkProps(to)">
    <slot />
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'

const props = defineProps({
  to: {
    type: String,
    required: true
  }
})

// 根据链接类型确定渲染的组件类型
const type = computed(() => {
  if (isExternal(props.to)) {
    return 'a'
  }
  return 'router-link'
})

// 根据链接类型生成不同的属性
const linkProps = (to) => {
  if (isExternal(to)) {
    return {
      href: to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: to
  }
}
</script>