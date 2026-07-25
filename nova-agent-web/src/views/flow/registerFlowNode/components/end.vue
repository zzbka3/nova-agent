<template>
  <div :class="['node-container end-container', { 'node-selected': isSelected, 'node-error': !validateStatus }]" :ref="endRef">
    <nodeTitle
      :node-name="getNodeName"
      :node-icon="endIcon"
      :validate-status="validateStatus"
      :show-fold="true"
      :model="model"
      :all-show="allShow"
      @toggle-fold-all="toggleFoldAll"
      :err-result="errResult"
      :lf="lf"
    />
    <queryInfo
      v-show="allShow"
      info-title="输出"
      :info-data="inputVars"
      :arr-args="arrArgs"
      @updateNodeAttributes="updateNodeAttributes"
    />
    <outputs :outputs-data="outputsData" v-if="showOutputs" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import nodeTitle from '../commonComponents/nodeTitle.vue'
import queryInfo from '../commonComponents/queryInfo.vue'
import outputs from '../commonComponents/outputs.vue'
import { getAllArgs } from '../../getArgs'

const props = defineProps<{ model: any; lf: any }>()
const bus: any = inject('$bus')

const isSelected = ref(false)
const validateStatus = ref(true)
const errResult = ref<string[]>([])
const showOutputs = ref(false)
const outputsData = ref<any>({})
const inputVars = ref<any[]>([])
const allShow = ref(true)
const arrArgs = ref<any[]>([])
const endRef = ref()
const endIcon = ref('')

const getNodeId = computed(() => props.model?.id)
const getNodeName = computed(() => props.model?.properties?.nodeName)

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
  arrArgs.value = getAllArgs({ nodeId: getNodeId.value, lf: props.lf })
  const { inputVars: vars } = props.model?.properties || {}
  inputVars.value = vars?.length ? vars : [{ varName: '', varType: '', varValue: '' }]
}

function toggleFoldAll(expand: boolean) {
  allShow.value = expand
  updateNodeAttributes(expand)
}

function updateNodeAttributes(expand?: boolean) {
  setTimeout(() => {
    const el = document.querySelector(`.end-container`) as HTMLElement
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
