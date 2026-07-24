<template>
  <div class="node-header">
    <span class="node-icon">{{ icon }}</span>
    <span class="node-title">{{ title }}</span>
    <span v-if="subTitle" class="node-subtitle">{{ subTitle }}</span>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  title: { type: String, default: '未命名节点' },
  subTitle: { type: String, default: '' },
})
</script>

<style scoped>
.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 24px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 10px;
}
.node-icon {
  font-size: 16px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.node-title {
  color: #151b26;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  flex: 1;
}
.node-subtitle {
  font-size: 11px;
  color: #ff9326;
  background: #fff4e6;
  padding: 0 6px;
  border-radius: 4px;
  height: 20px;
  line-height: 20px;
  flex-shrink: 0;
}
</style>
