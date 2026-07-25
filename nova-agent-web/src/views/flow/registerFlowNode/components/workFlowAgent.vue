<template>
  <div :class="['node-container', { 'node-selected': isSelected, 'node-error': !validateStatus }]" :ref="agentRef">
    <nodeOperate v-if="isSelected" :model="model" :lf="lf" @editNode="handleEditNode" />
    <nodeTitle
      ref="nodeTitleRef"
      :model="model"
      :node-name="getNodeName"
      :node-icon="agentIcon"
      :validate-status="validateStatus"
      :show-fold="true"
      @toggle-fold-all="toggleFoldAll"
      :all-show="allShow"
      :err-result="errResult"
      :lf="lf"
    />
    <queryInfo
      v-show="allShow"
      info-title="输入"
      :info-data="inputVars"
      :arr-args="arrArgs"
      @updateNodeAttributes="updateNodeAttributes"
    />
    <varsTree
      :tree-data="outputList"
      title="输出"
      @updateNodeAttributes="updateNodeAttributes"
    />
    <outputs :outputs-data="outputsData" v-if="showOutputs" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import nodeOperate from '../commonComponents/nodeOperate.vue'
import nodeTitle from '../commonComponents/nodeTitle.vue'
import queryInfo from '../commonComponents/queryInfo.vue'
import varsTree from '../commonComponents/varsTree.vue'
import outputs from '../commonComponents/outputs.vue'
import { agentIconSvg } from '../../common/icons'
import { getAllArgs } from '../../getArgs'

const props = defineProps<{ name?: string; model: any; lf: any }>()
const bus: any = inject('$bus')

const isSelected = ref(false)
const allShow = ref(true)
const arrArgs = ref<any[]>([])
const inputVars = ref<any[]>([])
const validateStatus = ref(true)
const errResult = ref<string[]>([])
const showOutputs = ref(false)
const outputsData = ref<any>({})
const outputList = ref<any[]>([])
const agentRef = ref()
const nodeTitleRef = ref()
const agentIcon = agentIconSvg

const getId = computed(() => props.model?.id)
const getNodeName = computed(() => props.model?.properties?.nodeName)

onMounted(() => {
  bus?.on('node:click', (args: any) => {
    isSelected.value = getId.value === args?.data?.id
  })
  bus?.on('openOutputs', (data: any) => {
    if (data?.nodes) {
      const filtered = data.nodes.filter((item: any) => item.nodeId === getId.value)
      showOutputs.value = filtered.length > 0
      outputsData.value = showOutputs.value ? filtered[0] : {}
    } else {
      showOutputs.value = false
    }
  })
  init()
})

function init() {
  arrArgs.value = getAllArgs({ nodeId: getId.value, lf: props.lf })
  const { inputVars: vars, outputVars: outVars } = props.model?.properties || {}
  inputVars.value = vars?.length ? vars : [{ varName: '', varType: '' }]
  outputList.value = outVars || []
}

function toggleFoldAll(allShowVal: boolean) {
  allShow.value = allShowVal
  updateNodeAttributes(allShowVal)
}

function updateNodeAttributes(expand?: boolean) {
  setTimeout(() => {
    const el = document.querySelector(`.node-container`) as HTMLElement
    const clientHeight = el?.clientHeight
    const edgeModel = props.lf.getNodeModelById(getId.value)
    if (clientHeight > 0) {
      edgeModel?.setCustomAttributes({ currentHeight: clientHeight, expand })
    }
  }, 0)
}

function handleEditNode() {
  nodeTitleRef.value?.editNodeName()
}
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-container {
  width: 100%; height: auto; background: #fff; border-radius: 12px; padding: 15px; text-align: left; min-width: 400px;
}
</style>
