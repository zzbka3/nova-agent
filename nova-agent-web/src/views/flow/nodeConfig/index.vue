<!--
 * @Author: hewenquan
 * @Date: 2025-06-17 11:24:08
 * @LastEditTime: 2025-10-27 17:03:45
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/index.vue
 * @Description: Control
-->
<template>
    <div>
        <dialogTitle
            :click-node="clickNode"
        />
        <startConfig
            v-if="clickNode.type === 'START'"
            :lf="lf"
            :click-node="clickNode"
        />
        <conditionalConfig
            v-if="clickNode.type === 'IF'"
            :lf="lf"
            :click-node="clickNode"
        />
        <largeModelConfig
            v-if="clickNode.type === 'LLM'"
            :lf="lf"
            :click-node="clickNode"
        />
        <knowledgeConfig
            v-if="clickNode.type === 'KNOWLEDGE'"
            :lf="lf"
            :click-node="clickNode"
        />
        <intentionConfig
            v-if="clickNode.type === 'INTENT'"
            :lf="lf"
            :click-node="clickNode"
        />
        <apiConfig
            v-if="clickNode.type === 'API'"
            :lf="lf"
            :click-node="clickNode"
        />
        <messageConfig
            v-if="clickNode.type === 'MESSAGE'"
            :lf="lf"
            :click-node="clickNode"
        />
        <processConfig
            v-if="clickNode.type === 'WORKFLOW'"
            :lf="lf"
            :click-node="clickNode"
        />
        <endConfig
            v-if="clickNode.type === 'END'"
            :lf="lf"
            :click-node="clickNode"
        />
        <codeConfig
            v-if="clickNode.type === 'CODE'"
            :lf="lf"
            :click-node="clickNode"
        />
        <textProcessor
            v-if="clickNode.type === 'TEXT_PROCESSOR'"
            :lf="lf"
            :click-node="clickNode"
        />
        <rewriteConfig
            v-if="clickNode.type === 'REWRITE'"
            :lf="lf"
            :click-node="clickNode"
        />
        <memoryConfig
            v-if="clickNode.type === 'MEMORY'"
            :lf="lf"
            :click-node="clickNode"
        />
        <workFlowAgentConfig
            v-if="clickNode.type === 'WORKFLOW_AGENT'"
            :lf="lf"
            :click-node="clickNode"
        />
    </div>
</template>
<script>
import dialogTitle from './dialogTitle.vue';
import startConfig from './startConfig';
import conditionalConfig from './conditionalConfig';
import intentionConfig from './intentionConfig.vue';
import LogicFlow from '@logicflow/core';
import knowledgeConfig from './knowledgeConfig';
import largeModelConfig from './largeModelConfig';
import apiConfig from './apiConfig/index.vue';
import messageConfig from './messageConfig';
import processConfig from './processConfig';
import endConfig from './endConfig.vue';
import textProcessor from './textProcessor.vue';
import rewriteConfig from './rewriteConfig.vue';
import codeConfig from './codeConfig/index.vue';
import memoryConfig from './memoryConfig.vue';
import workFlowAgentConfig from './workFlowAgentConfig.vue';
export default {
    props: {
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    components: {
        dialogTitle,
        startConfig,
        conditionalConfig,
        intentionConfig,
        knowledgeConfig,
        largeModelConfig,
        apiConfig,
        messageConfig,
        processConfig,
        endConfig,
        codeConfig,
        textProcessor,
        rewriteConfig,
        memoryConfig,
        workFlowAgentConfig
    }
};
</script>
