<template>
  <div :class="['node-container', { 'node-selected': isSelected }]" ref="nodeRef">
    <nodeTitle :node-name="getNodeName" :node-icon="icon" :model="model" :lf="lf" :show-fold="true" :all-show="allShow" @toggle-fold-all="toggleFoldAll" />
    <queryInfo v-show="allShow" info-title="输入" :info-data="inputVars" :arr-args="arrArgs" @updateNodeAttributes="updateNodeAttributes" />
    <varsTree v-show="allShow" :tree-data="outputList" title="输出" @updateNodeAttributes="updateNodeAttributes" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import nodeTitle from '../commonComponents/nodeTitle.vue'
import queryInfo from '../commonComponents/queryInfo.vue'
import varsTree from '../commonComponents/varsTree.vue'
import { llmIconSvg } from '../../common/icons'
import { getAllArgs } from '../../getArgs'

const props = defineProps<{ model: any; lf: any }>()
const bus: any = inject('$bus')
const isSelected = ref(false)
const allShow = ref(true)
const arrArgs = ref<any[]>([])
const inputVars = ref<any[]>([])
const outputList = ref<any[]>([])
const nodeRef = ref()
const icon = llmIconSvg
const getNodeId = computed(() => props.model?.id)
const getNodeName = computed(() => props.model?.properties?.nodeName)

onMounted(() => {
  bus?.on('node:click', (a: any) => { isSelected.value = getNodeId.value === a?.data?.id })
  arrArgs.value = getAllArgs({ nodeId: getNodeId.value, lf: props.lf })
  const p = props.model?.properties || {}
  inputVars.value = p.inputVars?.length ? p.inputVars : [{ varName: '', varType: '' }]
  outputList.value = p.outputVars || []
})

function toggleFoldAll(e: boolean) { allShow.value = e; updateNodeAttributes(e) }
function updateNodeAttributes(e?: boolean) {
  setTimeout(() => {
    const el = document.querySelector('.node-container') as HTMLElement
    const m = props.lf.getNodeModelById(getNodeId.value)
    if (el?.clientHeight > 0) m?.setCustomAttributes({ currentHeight: el.clientHeight, expand: e })
  }, 0)
}
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-container { width: 100%; background: #fff; border-radius: 12px; padding: 12px; text-align: left; min-width: 400px; }
</style>
