<template>
  <div class="container">
    <div class="node-area-bg">
      <div class="config-item-header">
        <span>参数名</span><span>类型</span><span>值</span>
      </div>
      <div v-for="(item, index) in inputVars" :key="index" class="config-item-row">
        <a-input v-model:value="item.varName" size="small" placeholder="参数名" style="width: 120px" @change="debounceUpdateLf" />
        <a-select v-model:value="item.varType" size="small" style="width: 100px" @change="debounceUpdateLf">
          <a-select-option value="reference">引用</a-select-option>
          <a-select-option value="String">String</a-select-option>
          <a-select-option value="Number">Number</a-select-option>
          <a-select-option value="Boolean">Boolean</a-select-option>
        </a-select>
        <a-select v-if="item.varType === 'reference'" v-model:value="item.referenceVarName" size="small" style="width: 140px" placeholder="选择变量" @change="debounceUpdateLf">
          <a-select-option v-for="arg in allFlatArgs" :key="arg.varName" :value="arg.varName">{{ arg.varName }}</a-select-option>
        </a-select>
        <a-input v-else v-model:value="item.varValue" size="small" placeholder="值" style="width: 120px" @change="debounceUpdateLf" />
        <a-button size="small" type="link" danger @click="removeVar(index)">删除</a-button>
      </div>
      <a-button type="dashed" size="small" block @click="addVar">+ 添加</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getAllFlatArgs } from '../getArgs'
import { debounce } from 'lodash-es'

const props = defineProps<{ clickNode: any; lf: any }>()

const inputVars = ref<any[]>([])
const allFlatArgs = ref<any[]>([])

const propertiesData = computed(() => props.clickNode?.properties || {})

onMounted(() => init())
watch(propertiesData, () => init())

function init() {
  const { id } = props.clickNode
  allFlatArgs.value = getAllFlatArgs({ nodeId: id, lf: props.lf })
  const { inputVars: iv } = propertiesData.value
  inputVars.value = iv?.length ? [...iv] : []
}

function addVar() { inputVars.value.push({ varName: '', varType: 'reference', referenceVarName: '', varValue: '' }); updateLf() }
function removeVar(i: number) { inputVars.value.splice(i, 1); updateLf() }

function updateLf() {
  const { id } = props.clickNode
  const model = props.lf.getNodeModelById(id)
  model?.setProperties({ inputVars: [...inputVars.value] })
}

const debounceUpdateLf = debounce(updateLf, 200)
</script>

<style lang="less" scoped>
.container { padding-top: 8px; }
.config-item-header { display: flex; gap: 8px; font-size: 12px; color: #84868c; margin-bottom: 8px; span { flex: 1; } }
.config-item-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
</style>
