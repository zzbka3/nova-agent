<template>
  <div class="start-config">
    <varsTree :tree-data="getArgs" title="输入" :show-expanded="true" />
    <mockValueConfig
      title="业务字段"
      :config-data="startMockVars"
      @updateConfigData="updateConfigData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import varsTree from '../registerFlowNode/commonComponents/varsTree.vue'
import mockValueConfig from './components/mockValueConfig.vue'

const props = defineProps<{ clickNode: any; lf: any }>()

const getArgs = computed(() => [{
  nodeId: '1', key: '1-system',
  children: [
    { varName: 'rawQuery', varType: 'String', varDesc: '原始查询' },
    { varName: 'conversationId', varType: 'String', varDesc: '会话ID' },
  ]
}]?.[0]?.children || [])

const propertiesData = computed(() => props.clickNode?.properties || {})
const startMockVars = ref<any[]>([])

onMounted(() => init())
watch(propertiesData, () => init())

function init() {
  const { inputVars } = propertiesData.value
  if (inputVars?.length) {
    startMockVars.value = [...inputVars]
  } else {
    startMockVars.value = [
      { id: '1_customVar1', varName: 'customVar1', varType: 'String', varValue: '' },
      { id: '1_customVar2', varName: 'customVar2', varType: 'Number', varValue: '' },
    ]
  }
}

function updateConfigData(mockConfig: any[]) {
  startMockVars.value = mockConfig
  const { id } = props.clickNode
  const model = props.lf.getNodeModelById(id)
  model?.setProperties({ inputVars: [...mockConfig] })
}
</script>

<style lang="less" scoped>
@import url('../customCss/index.less');
</style>
