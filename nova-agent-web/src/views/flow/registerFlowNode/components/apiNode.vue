<template>
  <div :class="['node-container', { 'node-selected': isSelected }]">
    <nodeTitle :node-name="getNodeName" :node-icon="apiIcon" :model="model" :lf="lf" :show-fold="true" :all-show="allShow" @toggle-fold-all="toggleFoldAll" />
    <queryInfo v-show="allShow" info-title="输入" :info-data="inputVars" :arr-args="arrArgs" @updateNodeAttributes="updateNodeAttributes" />
    <varsTree v-show="allShow" :tree-data="outputList" title="输出" @updateNodeAttributes="updateNodeAttributes" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import nodeTitle from '../commonComponents/nodeTitle.vue'
import queryInfo from '../commonComponents/queryInfo.vue'
import varsTree from '../commonComponents/varsTree.vue'
import { apiIconSvg } from '../../common/icons'
import { getAllArgs } from '../../getArgs'

const props = defineProps<{ model: any; lf: any }>()
const bus: any = inject('$bus')
const isSelected = ref(false)
const allShow = ref(true)
const arrArgs = ref<any[]>([])
const inputVars = ref<any[]>([])
const outputList = ref<any[]>([])
const apiIcon = apiIconSvg
const getNodeId = computed(() => props.model?.id)
const getNodeName = computed(() => props.model?.properties?.nodeName)

onMounted(() => {
  bus?.on('node:click', (args: any) => { isSelected.value = getNodeId.value === args?.data?.id })
  arrArgs.value = getAllArgs({ nodeId: getNodeId.value, lf: props.lf })
  const { inputVars: vars, outputVars: ov } = props.model?.properties || {}
  inputVars.value = vars?.length ? vars : [{ varName: '', varType: '' }]
  outputList.value = ov || []
})
function toggleFoldAll(expand: boolean) { allShow.value = expand; updateNodeAttributes(expand) }
function updateNodeAttributes(expand?: boolean) {
  setTimeout(() => {
    const el = document.querySelector(`.node-container`) as HTMLElement
    const edgeModel = props.lf.getNodeModelById(getNodeId.value)
    if (el?.clientHeight > 0) edgeModel?.setCustomAttributes({ currentHeight: el.clientHeight, expand })
  }, 0)
}
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-container { width: 100%; background: #fff; border-radius: 12px; padding: 12px; text-align: left; min-width: 400px; }
</style>
