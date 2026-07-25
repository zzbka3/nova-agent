<template>
  <div class="flow-view">
    <NodePanel
      v-if="lf"
      :lf="lf"
      :node-list="nodeListConfig"
      :save-flow="saveFlow"
      ref="nodePanelRef"
      :flow-data="flowData"
      @openCheck="changeOpenCheck"
    />
    <!-- Canvas -->
    <div class="logic-flow-container" ref="containerRef"></div>
    <!-- Node config drawer -->
    <a-drawer
      placement="right"
      :open="nodeDialogVisible"
      @close="closeNodeConfigDialog"
      width="400"
      :mask="false"
      :destroy-on-close="true"
      class="node-config-drawer"
    >
      <div v-if="clickNode" class="node-config-placeholder">
        <h3>{{ clickNode?.properties?.nodeName || '节点配置' }}</h3>
        <p>节点配置面板（待完善）</p>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, inject, nextTick } from 'vue'
import '@logicflow/core/dist/index.css'
import '@logicflow/extension/lib/style/index.css'
import LogicFlow from '@logicflow/core'
import { MiniMap } from '@logicflow/extension'
import { Dagre } from '@logicflow/layout'
import NodePanel from './basics/nodePanel.vue'
import { defaultEdge, animationEdge } from './basics/BezierEdge'
import { nodeList as nodeListConfig } from './basics/flowConfig'
import { registerAllNodes } from './registerFlowNode'
import { customAnchorClickEvent, customBackEvent } from './basics/lfEvent'
import { getAgentDetail, saveAgent } from './apiList'
import { flowRequest } from './common/request'
import { deleteTempOutputs } from './getArgs'
import { message } from 'ant-design-vue'

const bus: any = inject('$bus')

const lf = ref<any>(null)
const clickNode = ref<any>(null)
const nodeDialogVisible = ref(false)
const nodeListConfigRef = ref(nodeListConfig)
const openCheck = ref(false)
const flowData = ref<any>({ nodes: [], edges: [] })
const agentData = ref<any>({})
const saveTimer = ref<any>(null)
const isEditName = ref(false)
const containerRef = ref()
const nodePanelRef = ref()

const props = defineProps<{ appId?: string }>()

const emit = defineEmits(['syncAgentData'])

const appId = computed(() => props.appId)

onMounted(() => {
  bus?.on('editNodeName', ({ isEditName: editing }: any) => {
    isEditName.value = editing
  })
  if (appId.value) {
    getAgentDetailData()
  } else {
    initLf()
  }
})

onBeforeUnmount(() => {
  clearSaveInterval()
})


function clearSaveInterval() {
  if (saveTimer.value) {
    clearTimeout(saveTimer.value)
    saveTimer.value = null
  }
}

async function getAgentDetailData() {
  if (!appId.value) return
  try {
    const data = await flowRequest({
      url: getAgentDetail,
      method: 'get',
      params: { appId: appId.value }
    })
    if (data?.config) {
      let { edges = [], nodes = [] } = JSON.parse(data.config) || {}
      if (edges.length) {
        edges = edges.map((item: any) => ({ ...item, type: 'EDGE_BEZIER' }))
      }
      flowData.value = { nodes, edges }
      delete data.config
      agentData.value = data
      emit('syncAgentData', agentData.value)
    }
    initLf()
    saveTimer.value = setTimeout(() => saveFlowTimer(), 5000)
  } catch {
    message.error('获取 agent 详情失败')
  }
}

function initLf() {
  if (lf.value) {
    lf.value.destroy()
  }
  const logicFlow = new LogicFlow({
    adjustEdge: false,
    plugins: [MiniMap, Dagre],
    pluginsOptions: {
      miniMap: { width: 200, height: 100, leftPosition: 5, bottomPosition: 5 },
    },
    container: containerRef.value,
    grid: {
      size: 18,
      visible: true,
      type: 'dot' as const,
      config: { color: '#e2e4ed', thickness: 1 },
    },
    keyboard: {
      enabled: true,
      shortcuts: [{
        keys: ['backspace'],
        callback: () => customBackEvent({ lf: logicFlow, isEditName: isEditName.value, bus })
      }]
    },
    guards: {
      beforeDelete: (data: any) => {
        if (openCheck.value) { message.error('调试模式不能删除'); return false }
        if (['START', 'END'].includes(data?.type)) { message.error('开始和结束节点不能删除'); return false }
        return true
      }
    },
    edgeTextDraggable: false,
    hoverOutline: false,
    edgeTextEdit: false,
    nodeTextEdit: false,
  })
  lf.value = logicFlow
  registerAllNodes(logicFlow)
  logicFlow.register(defaultEdge)
  logicFlow.register(animationEdge)
  logicFlow.setDefaultEdgeType('EDGE_BEZIER')
  logicFlow.render(flowData.value)
  registerLfEvents()
  logicFlow.fitView()
}

function registerLfEvents() {
  const logicFlow = lf.value
  logicFlow.on('node:click', (args: any) => {
    bus?.emit('node:click', args)
  })
  logicFlow.on('node:dbclick', (args: any) => {
    if (openCheck.value) return
    clickNode.value = args?.data || {}
    nodeDialogVisible.value = true
  })
  logicFlow.on('edge:add', ({ data }: any) => {
    deleteTempOutputs()
  })
  logicFlow.on('blank:click', () => {
    closeNodeConfigDialog()
    bus?.emit('node:click', null)
  })
  logicFlow.on('connection:not-allowed', (data: any) => {
    message.error(data.msg)
  })
  logicFlow.on('edge:mouseenter', ({ data }: any) => {
    if (openCheck.value) return
    logicFlow.getEdgeModelById(data.id)?.setProperties({ showAddMark: true })
  })
  logicFlow.on('edge:mouseleave', ({ data }: any) => {
    logicFlow.getEdgeModelById(data.id)?.setProperties({ showAddMark: false })
  })
  logicFlow.on('custom:anchorClick', ({ edge }: any) => {
    customAnchorClickEvent({ edge, lf: logicFlow, bus })
  })
  logicFlow.on('node:delete', ({ data }: any) => {
    if (nodeDialogVisible.value && clickNode.value?.id === data.id) {
      nodeDialogVisible.value = false
      clickNode.value = null
    }
  })
}

function closeNodeConfigDialog() {
  nodeDialogVisible.value = false
  clickNode.value = null
}

function changeOpenCheck(val: boolean) {
  openCheck.value = val
  nodeDialogVisible.value = false
  clickNode.value = null
  bus?.emit('node:click', null)
}

function saveFlowTimer() {
  const graphData = lf.value?.getGraphData()
  if (graphData && JSON.stringify(graphData) !== JSON.stringify(flowData.value)) {
    flowData.value = graphData
    saveFlow()
  }
  clearSaveInterval()
  saveTimer.value = setTimeout(() => saveFlowTimer(), 10000)
}

async function saveFlow(status = 'draft') {
  if (openCheck.value || !appId.value) return
  const { edges, nodes = [] } = lf.value?.getGraphData() || {}
  const { name = '', remark = '', memorySchema = '' } = agentData.value || {}
  const postData = {
    name, remark,
    appId: appId.value,
    config: JSON.stringify({ edges, nodes }),
    status,
    memorySchema,
  }
  return flowRequest({ url: saveAgent, method: 'post', data: postData })
}

defineExpose({ lf, saveFlow, clearSaveInterval })
</script>

<style lang="less" scoped>
.flow-view {
  position: relative; width: 100%; height: 100%;
  .logic-flow-container { width: 100%; height: 100%; outline: none; }
  :deep(.lf-dnd-item) { margin: 0; padding: 10px; display: flex; align-items: center; }
  :deep(.lf-control) { right: 0; box-shadow: none; filter: drop-shadow(2px 2px 6px rgba(0,0,0,0.1)); }
  :deep(.lf-node) { filter: drop-shadow(0px 1px 2px rgba(0,0,0,0.2)); }
  :deep(.lf-graph) { background: rgb(248, 249, 252); }
  :deep(.lf-edge-append) { pointer-events: none; }
  :deep(.lf-mini-map) { background: #ebedf1; border: none; }
  :deep(.lf-minimap-viewport) { background: rgba(48,48,48,0.1); }
}
.node-config-drawer {
  :deep(.ant-drawer-content) { overflow: visible !important; }
}
.node-config-placeholder {
  padding: 20px;
  h3 { margin-bottom: 12px; }
  p { color: #84868c; }
}
</style>
