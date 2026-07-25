<template>
  <div>
    <dialogTitle :click-node="clickNode" />
    <startConfig       v-if="clickNode.type === 'START'"           :lf="lf" :click-node="clickNode" />
    <endConfig         v-if="clickNode.type === 'END'"             :lf="lf" :click-node="clickNode" />
    <ifConfig          v-if="clickNode.type === 'IF'"              :lf="lf" :click-node="clickNode" />
    <intentConfig      v-if="clickNode.type === 'INTENT'"          :lf="lf" :click-node="clickNode" />
    <knowledgeConfig   v-if="clickNode.type === 'KNOWLEDGE'"       :lf="lf" :click-node="clickNode" />
    <llmConfig         v-if="clickNode.type === 'LLM'"             :lf="lf" :click-node="clickNode" />
    <apiConfig         v-if="clickNode.type === 'API'"             :lf="lf" :click-node="clickNode" />
    <messageConfig     v-if="clickNode.type === 'MESSAGE'"         :lf="lf" :click-node="clickNode" />
    <workflowConfig    v-if="clickNode.type === 'WORKFLOW'"        :lf="lf" :click-node="clickNode" />
    <codeConfig        v-if="clickNode.type === 'CODE'"            :lf="lf" :click-node="clickNode" />
    <textProcessorConfig v-if="clickNode.type === 'TEXT_PROCESSOR'" :lf="lf" :click-node="clickNode" />
    <memoryConfig      v-if="clickNode.type === 'MEMORY'"          :lf="lf" :click-node="clickNode" />
    <workFlowAgentConfig v-if="clickNode.type === 'WORKFLOW_AGENT'" :lf="lf" :click-node="clickNode" />
  </div>
</template>

<script setup lang="ts">
import dialogTitle from './dialogTitle.vue'
import startConfig from './startConfig.vue'
import endConfig from './endConfig.vue'
import ifConfig from './ifConfig.vue'
import intentConfig from './intentConfig.vue'
import knowledgeConfig from './knowledgeConfig.vue'
import llmConfig from './llmConfig.vue'
import apiConfig from './apiConfig.vue'
import messageConfig from './messageConfig.vue'
import workflowConfig from './workflowConfig.vue'
import codeConfig from './codeConfig.vue'
import textProcessorConfig from './text_processorConfig.vue'
import memoryConfig from './memoryConfig.vue'
import workFlowAgentConfig from './workFlowAgentConfig.vue'

defineProps<{ clickNode: any; lf: any }>()
</script>
