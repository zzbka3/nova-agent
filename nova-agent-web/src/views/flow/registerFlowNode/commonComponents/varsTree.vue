<template>
  <div :class="['vars-tree', { 'common-area': showCommonArea }]">
    <div class="common-title" @click="expandVars">
      <CaretDownOutlined v-if="expandedVars" />
      <CaretRightOutlined v-else />
      {{ title }}
    </div>
    <a-tree
      v-if="treeData?.length"
      v-show="expandedVars"
      :tree-data="treeData"
      :field-names="fieldNames"
      @expand="onExpand"
      @click.stop
    >
      <template #title="scopeTree">
        <div class="args-item">
          <span class="args-item-name">{{ scopeTree.varNameAbbr || scopeTree.varName }}</span>
          <span class="args-item-type">{{ scopeTree.originalVarType || scopeTree.varType }}</span>
          <a-tooltip v-if="showVarValue">
            <template #title>{{ scopeTree.varValue || '暂未配置' }}</template>
            <span class="args-item-value">：{{ scopeTree.varValue || '暂未配置' }}</span>
          </a-tooltip>
        </div>
      </template>
    </a-tree>
    <div v-else v-show="expandedVars" class="not-tree-data">暂无配置</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'

const props = withDefaults(defineProps<{
  showCommonArea?: boolean
  showExpanded?: boolean
  title?: string
  treeData?: any[]
  fieldNames?: { children: string; title: string; key: string }
  showVarValue?: boolean
}>(), {
  showCommonArea: true,
  showExpanded: true,
  title: '输出',
  treeData: () => [],
  fieldNames: () => ({ children: 'children', title: 'varName', key: 'varName' }),
  showVarValue: false,
})

const emit = defineEmits(['updateNodeAttributes'])
const expandedVars = ref(props.showExpanded)

watch(() => props.showExpanded, (val) => { expandedVars.value = val })

function onExpand(_expandedKeys: any, { expanded }: { expanded: boolean }) {
  setTimeout(() => emit('updateNodeAttributes', expanded), 200)
}

function expandVars() {
  expandedVars.value = !expandedVars.value
  emit('updateNodeAttributes', expandedVars.value)
}
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.common-area {
  padding: 12px 0 !important;
  .common-title { padding-left: 12px; }
}
.vars-tree {
  margin-top: 10px;
  /deep/ .ant-tree-node-content-wrapper { width: calc(100% - 24px); }
  .args-item { display: flex; align-content: center; width: 100%; }
  .args-item-name { color: #876300; overflow: hidden; max-width: 160px; white-space: nowrap; text-overflow: ellipsis; flex-shrink: 0; }
  .args-item-type { margin-left: 4px; flex-shrink: 0; padding: 0 5px; white-space: nowrap; border-radius: 4px; background-color: #e8e9eb; }
  .args-item-value { color: #000; overflow: hidden; display: inline-block; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
}
</style>
