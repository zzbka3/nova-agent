<template>
  <div class="dialog-title">
    <div class="node-title">
      <img :src="nodeIcon" class="node-icon" />
      <span>{{ nodeTitle }}</span>
    </div>
    <a-tooltip :title="descMap[getNodeType]" v-if="descMap[getNodeType]">
      <div class="dialog-desc">{{ descMap[getNodeType] }}</div>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { getNodeMap, nodeList, originalNode } from '../basics/flowConfig'
import { flowRequest } from '../common/request'
import { getAgentDetail } from '../apiList'
import {
  startIconSvg, endIconSvg, apiIconSvg, agentIconSvg, ifIconSvg,
  codeIconSvg, knowledgeIconSvg, llmIconSvg, messageIconSvg,
  memoryIconSvg, workflowIconSvg, text_processorIconSvg, intentIconSvg
} from '../common/icons'

const props = defineProps<{ clickNode: any }>()

const descMap: Record<string, string> = {
  START: '工作流运行的起点，开始节点支持定义此工作流所需的输入参数，包括用户输入的原始内容、用户与应用的对话历史和用户在应用对话中上传的文件变量，将会自动从用户输入中获取。',
  END: '工作流的最终节点，输出工作流运行后的最终结果。',
  IF: '连接多个下游分支节点，若设定条件成立则运行对应的条件分支，若均不成立则运行"否则"分支。',
  INTENT: '识别用户的输入意图，并分配到不同分支执行。',
  KNOWLEDGE: '根据输入的参数，在选定的知识库中检索相关片段并召回，返回切片列表。',
  API: '配置外部 API 服务，并调用该服务。',
  LLM: '调用大语言模型，根据输入参数和提示词生成回复',
  MESSAGE: '支持工作流运行过程中的消息输出。',
  CODE: '编写代码，处理输入输出变量来生成返回值。',
  TEXT_PROCESSOR: '对多个字符串变量的格式进行处理。',
  MEMORY: '用于写入或读取 Agent 中的记忆变量，节点与 Agent 中的记忆变量名称需要相同才能匹配。',
}

const iconSvgMap: Record<string, string> = {
  START: startIconSvg, END: endIconSvg, API: apiIconSvg,
  WORKFLOW_AGENT: agentIconSvg, IF: ifIconSvg, CODE: codeIconSvg,
  KNOWLEDGE: knowledgeIconSvg, LLM: llmIconSvg, MESSAGE: messageIconSvg,
  MEMORY: memoryIconSvg, WORKFLOW: workflowIconSvg,
  TEXT_PROCESSOR: text_processorIconSvg, INTENT: intentIconSvg,
}

const getNodeType = computed(() => props.clickNode?.type)
const propertiesData = computed(() => props.clickNode?.properties || {})

const nodeTitle = computed(() => {
  return propertiesData.value?.nodeName || (getNodeMap(getNodeType.value) as string) || '未知节点'
})

const nodeIcon = computed(() => {
  return iconSvgMap[getNodeType.value] || startIconSvg
})
</script>

<style lang="less" scoped>
@import url('../customCss/index.less');
.dialog-title {
  border-bottom: 1px solid #e8e9eb;
  padding-bottom: 16px;
  margin-bottom: 16px;
  .dialog-desc {
    margin-top: 8px;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    color: #84868c;
    word-break: break-all;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
  .node-title {
    display: flex; align-items: center;
    .node-icon { width: 24px; height: 24px; margin-right: 8px; }
    span { font-size: 16px; font-weight: 500; }
  }
}
</style>
