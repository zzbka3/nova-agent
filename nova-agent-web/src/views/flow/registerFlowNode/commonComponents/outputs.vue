<template>
  <div class="outputs-wrapper">
    <div class="outputs-container">
      <div class="outputs-wrapper">
        <div class="status-wrapper">
          <img class="status-icon" :src="statusIcon" />
          <span class="status-text">{{ statusText }}</span>
          <span v-if="costTime" class="success-tag">{{ costTime }}</span>
          <span v-if="usedTokens" class="success-tag">{{ usedTokens }}</span>
        </div>
        <div class="trigger" @click="showOutputsDetail = !showOutputsDetail">
          {{ showOutputsDetail ? '收起' : '展开' }}
          <UpOutlined v-if="showOutputsDetail" class="trigger-icon" />
          <DownOutlined v-else class="trigger-icon" />
        </div>
      </div>
      <div class="outputs-detail" v-show="showOutputsDetail">
        <div class="inputs-container" v-if="inputVars">
          <div class="inputs-title">
            <span class="inputs-trigger" @click="showInputsDetail = !showInputsDetail">
              <CaretDownOutlined v-if="showInputsDetail" />
              <CaretRightOutlined v-else />
              输入
            </span>
            <CopyOutlined class="copy-icon" @click="copy(JSON.stringify(inputVars))" />
          </div>
          <div v-show="showInputsDetail" class="json-view">
            <pre>{{ JSON.stringify(inputVars, null, 2) }}</pre>
          </div>
        </div>
        <div class="inputs-container" v-if="outputVars">
          <div class="inputs-title">
            <span @click="showOutPutsDetail = !showOutPutsDetail" class="inputs-trigger">
              <CaretDownOutlined v-if="showOutPutsDetail" />
              <CaretRightOutlined v-else />
              输出
            </span>
            <CopyOutlined class="copy-icon" @click="copy(JSON.stringify(outputVars))" />
          </div>
          <div v-show="showOutPutsDetail" class="json-view">
            <pre>{{ JSON.stringify(outputVars, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { UpOutlined, DownOutlined, CaretDownOutlined, CaretRightOutlined, CopyOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const props = defineProps<{ outputsData: any }>()

const showOutputsDetail = ref(false)
const showInputsDetail = ref(false)
const showOutPutsDetail = ref(false)

const status = computed(() => props.outputsData?.status)
const statusText = computed(() => {
  const map: Record<string, string> = { init: '初始化', finish: '运行成功', exception: '运行失败', running: '运行中' }
  return map[status.value] || '未知状态'
})
const statusIcon = computed(() => {
  const map: Record<string, string> = { init: 'runFinish', finish: 'runFinish', exception: 'runError', running: 'running' }
  return ''
})
const costTime = computed(() => {
  const t = props.outputsData?.costTime
  return t ? `${t / 1000}s` : null
})
const usedTokens = computed(() => {
  const t = props.outputsData?.usedTokens
  return t ? `${t} Tokens` : null
})
const inputVars = computed(() => {
  const vars = props.outputsData?.inputVars
  if (!vars) return null
  try {
    const parsed = typeof vars === 'string' ? JSON.parse(vars) : vars
    if (Array.isArray(parsed) && parsed.length) {
      const result: Record<string, any> = {}
      parsed.forEach((item: any) => {
        result[item.varName || item.referenceVarName] = item.varValue
      })
      return result
    }
  } catch { return null }
  return null
})
const outputVars = computed(() => {
  const vars = props.outputsData?.outputVars
  if (!vars) return null
  try {
    const parsed = typeof vars === 'string' ? JSON.parse(vars) : vars
    if (Array.isArray(parsed) && parsed.length) {
      const result: Record<string, any> = {}
      parsed.forEach((item: any) => {
        result[item.varName || item.referenceVarName] = item.varValue
      })
      return result
    }
  } catch { return null }
  return null
})

function copy(text: string) {
  navigator.clipboard.writeText(text).then(() => message.success('复制成功'))
}
</script>

<style lang="less" scoped>
.outputs-wrapper { position: relative; }
.outputs-container {
  position: fixed; top: calc(100% + 10px); left: 0; width: 100%; background: #303540; color: #fff; border-radius: 12px; padding: 12px;
  .outputs-detail {
    .inputs-container { margin-top: 10px;
      .inputs-trigger { cursor: pointer; }
      .copy-icon { cursor: pointer; }
      .json-view { margin-top: 8px; background: rgba(255,255,255,0.05); border-radius: 6px; padding: 8px;
        pre { color: #fff; font-size: 12px; margin: 0; white-space: pre-wrap; word-break: break-all; }
      }
    }
  }
  .outputs-wrapper { display: flex; align-items: center; justify-content: space-between; height: 36px;
    .status-wrapper { display: flex; align-items: center; }
    .status-icon { width: 16px; height: 16px; }
    .status-text { padding: 0 8px; color: #fff; font-size: 14px; font-weight: 500; line-height: 24px; }
    .success-tag { color: #30bf13; background: #30bf1333; padding: 0 8px; border-radius: 6px; line-height: 20px; margin-right: 10px; }
    .trigger { cursor: pointer; .trigger-icon { width: 16px; height: 16px; } }
  }
}
</style>
