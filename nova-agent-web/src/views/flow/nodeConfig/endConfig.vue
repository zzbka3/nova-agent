<template>
  <div class="container">
    <div class="container-title">
      <div class="container-title-text">回复模式</div>
    </div>
    <a-radio-group v-model:value="mode" size="small" class="mode-radio-group" @change="updateLf">
      <a-radio value="template">按模版配置格式返回文本</a-radio>
      <a-radio value="directVar">直接返回参数值</a-radio>
    </a-radio-group>

    <!-- Input Vars -->
    <div class="node-area-bg" style="margin-top: 16px" v-if="inputVars?.length">
      <div class="config-item-header">
        <span>参数名</span><span>类型</span><span>值</span>
      </div>
      <div v-for="(item, index) in inputVars" :key="index" class="config-item-row">
        <a-input v-model:value="item.varName" size="small" placeholder="参数名" style="width: 120px" @change="debounceUpdateLf" />
        <a-select v-model:value="item.varType" size="small" style="width: 100px" @change="debounceUpdateLf">
          <a-select-option value="reference">引用</a-select-option>
          <a-select-option value="String">String</a-select-option>
        </a-select>
        <a-select v-model:value="item.referenceVarName" size="small" style="width: 140px" placeholder="选择变量" @change="debounceUpdateLf" v-if="item.varType === 'reference'">
          <a-select-option v-for="arg in allFlatArgs" :key="arg.varName" :value="arg.varName">{{ arg.varName }}</a-select-option>
        </a-select>
        <a-input v-else v-model:value="item.varValue" size="small" placeholder="值" @change="debounceUpdateLf" />
      </div>
    </div>

    <!-- Template -->
    <div class="end-answer" v-show="mode === 'template'" style="margin-top: 16px">
      <div class="container-title"><div class="container-title-text"><span class="required-tip">*</span> 回复模板</div></div>
      <a-textarea
        v-model:value="answerTemplate"
        placeholder='可以根据参数名，在此定义返回结果的格式。 例如: 今天 {{output_location}} 的温度为 {{output_temperature}}'
        :auto-size="{ minRows: 6 }"
        :maxlength="1000"
        @change="debounceUpdateLf"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getAllFlatArgs } from '../getArgs'
import { debounce } from 'lodash-es'

const props = defineProps<{ clickNode: any; lf: any }>()

const mode = ref('template')
const inputVars = ref<any[]>([])
const answerTemplate = ref('')
const allFlatArgs = ref<any[]>([])

const propertiesData = computed(() => props.clickNode?.properties || {})

onMounted(() => init())
watch(propertiesData, () => init())

function init() {
  const { id } = props.clickNode
  allFlatArgs.value = getAllFlatArgs({ nodeId: id, lf: props.lf })
  const { mode: m, inputVars: iv, answerTemplate: at } = propertiesData.value
  mode.value = m || 'template'
  inputVars.value = iv?.length ? [...iv] : [{ varName: 'query', varType: 'reference', referenceVarName: '', varValue: '' }]
  answerTemplate.value = at || ''
}

function updateLf() {
  const { id } = props.clickNode
  const model = props.lf.getNodeModelById(id)
  model?.setProperties({
    mode: mode.value,
    inputVars: [...inputVars.value],
    answerTemplate: answerTemplate.value,
  })
}

const debounceUpdateLf = debounce(updateLf, 200)
</script>

<style lang="less" scoped>
.container { .container-title { color: #151b26; font-size: 14px; font-weight: 500; margin-bottom: 10px; } }
.mode-radio-group { display: flex; flex-direction: column; gap: 8px; }
.config-item-header { display: flex; gap: 8px; font-size: 12px; color: #84868c; margin-bottom: 8px; span { flex: 1; } }
.config-item-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
.required-tip { color: red; }
</style>
