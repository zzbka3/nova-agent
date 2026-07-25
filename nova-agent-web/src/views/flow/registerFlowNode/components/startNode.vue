<template>
  <div :class="['node-container start-container', { 'node-selected': isSelected }]" :ref="startRef">
    <nodeTitle
      :node-name="getNodeName"
      :node-icon="startIcon"
      :show-fold="true"
      @toggle-fold-all="toggleFoldAll"
      :all-show="allShow"
      :model="model"
      :lf="lf"
    />
    <varsTree
      v-show="allShow"
      :tree-data="getArgs"
      class="start-wrapper"
      title="输入"
      @updateNodeAttributes="updateNodeAttributes"
    />
    <varsTree
      v-show="allShow"
      :tree-data="startMockVars"
      class="start-wrapper"
      title="业务字段"
      :show-var-value="true"
      @updateNodeAttributes="updateNodeAttributes"
    />
    <outputs :outputs-data="outputsData" v-if="showOutputs" class="start-outputs" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import nodeTitle from '../commonComponents/nodeTitle.vue'
import varsTree from '../commonComponents/varsTree.vue'
import outputs from '../commonComponents/outputs.vue'
import { startIconSvg } from '../../common/icons'

const props = defineProps<{ model: any; lf: any }>()
const bus: any = inject('$bus')

const isSelected = ref(false)
const showOutputs = ref(false)
const outputsData = ref<any>({})
const allShow = ref(true)
const startMockVars = ref<any[]>([])
const startRef = ref()

const startIcon = startIconSvg

const getNodeId = computed(() => props.model?.id)
const getNodeName = computed(() => props.model?.properties?.nodeName)

const getArgs = computed(() => ([{
  nodeId: '1', key: '1-system',
  children: [
    { varName: 'rawQuery', varType: 'String', varDesc: '原始查询' },
    { varName: 'conversationId', varType: 'String', varDesc: '会话ID' },
  ]
}]?.[0]?.children || []))

onMounted(() => {
  bus?.on('node:click', (args: any) => {
    isSelected.value = getNodeId.value === args?.data?.id
  })
  bus?.on('openOutputs', (data: any) => {
    if (data?.nodes) {
      const filtered = data.nodes.filter((item: any) => item.nodeId === getNodeId.value)
      showOutputs.value = filtered.length > 0
      outputsData.value = showOutputs.value ? filtered[0] : {}
    } else {
      showOutputs.value = false
    }
  })
  init()
})

function init() {
  const { inputVars } = props.model?.properties || {}
  startMockVars.value = inputVars?.length ? inputVars : [
    { varName: 'customVar1', varType: 'String', varValue: 'value1' },
    { varName: 'customVar2', varType: 'Number', varValue: '42' },
  ]
}

function toggleFoldAll(expand: boolean) {
  allShow.value = expand
  updateNodeAttributes(expand)
}

function updateNodeAttributes(expand?: boolean) {
  setTimeout(() => {
    const el = document.querySelector(`.start-container`) as HTMLElement
    const clientHeight = el?.clientHeight
    const edgeModel = props.lf.getNodeModelById(getNodeId.value)
    if (clientHeight > 0) {
      edgeModel?.setCustomAttributes({ currentHeight: clientHeight, expand })
    }
  }, 0)
}
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
</style>
