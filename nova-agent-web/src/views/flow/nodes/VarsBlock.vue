<template>
  <div class="var-block">
    <div class="var-block-title" @click="expanded = !expanded">
      <span class="arrow">{{ expanded ? '▼' : '▶' }}</span>
      {{ label }}
    </div>
    <template v-if="expanded">
      <div v-if="rows.length === 0" class="var-empty">暂无配置</div>
      <div v-for="(row, i) in rows" :key="i" class="var-row">
        <span class="var-name">
          <span class="var-name-text">{{ row.varName || '未命名' }}</span>
          <span v-if="row.varType" class="var-type">{{ row.varType }}</span>
        </span>
        <span class="var-value">{{ row.displayValue || '未选择' }}</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  label: { type: String, default: '输入' },
  rows: { type: Array, default: () => [] },
})

const expanded = ref(true)
</script>

<style scoped>
.var-block {
  font-size: 12px;
  color: #5c5f66;
  border-radius: 8px;
  background: #f9f9fb;
  padding: 10px 12px;
  margin-top: 8px;
  line-height: 20px;
}
.var-block-title {
  color: #151b26;
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  user-select: none;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.arrow { font-size: 10px; color: #999; }
.var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  min-height: 22px;
}
.var-name {
  width: 55%;
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.var-name-text {
  color: #151b26;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 100px;
}
.var-type {
  padding: 0 5px;
  border-radius: 4px;
  background: #e8e9eb;
  font-size: 11px;
  color: #5c5f66;
  flex-shrink: 0;
}
.var-value {
  width: 45%;
  padding: 0 6px;
  border: 1px solid #e8e9eb;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 11px;
  min-width: 0;
}
.var-empty {
  color: #bbb;
  padding: 2px 0;
}
</style>
