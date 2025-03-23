<template>
  <div class="hamburger-container" @click="toggleClick">
    <el-icon class="hamburger" :class="{'is-active': isActive}">
      <Fold v-if="isActive" />
      <Expand v-else />
    </el-icon>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  isActive: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggleClick'])

const toggleClick = () => {
  emit('toggleClick')
}
</script>

<style lang="scss" scoped>
.hamburger-container {
  padding: 0 15px;
  height: 100%;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.025);
  }
  
  .hamburger {
    font-size: 20px;
    transition: transform 0.3s;
    
    &.is-active {
      transform: rotate(180deg);
    }
  }
}
</style>