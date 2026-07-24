<!--
 * @Author: hewenquan
 * @Date: 2025-06-19 16:09:06
 * @LastEditTime: 2025-11-25 16:59:44
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/startNode.vue
 * @Description: ProgressNode
-->
<template>
    <div
        :class="[
            'node-container start-container',
            {'node-selected': isSelected}
        ]"
        :ref="`start_${getNodeId}`"
    >
        <nodeTitle
            :node-name="getNodeName"
            :node-icon="require('../../image/start.png')"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :model="model"
            :lf="lf"
        />
        <varsTree
            v-show="allShow"
            :tree-data="getArgs"
            class="start-wrapper"
            title="输入"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <varsTree
            v-show="allShow"
            :tree-data="startMockVars"
            class="start-wrapper"
            title="业务字段"
            :show-var-value="true"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <outputs
            :outputs-data="outputsData"
            v-if="showOutputs"
            class="start-outputs"
        />
    </div>
</template>
<script>
import { systemArgs } from '../../getArgs';
import outputs from '../commonComponents/outputs.vue';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import LogicFlow from '@logicflow/core';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import { startMockVars } from '@/views/flow/basics/codeDealt';
export default {
    props: {
        model: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    data() {
        return {
            // 当前节点是否被选中
            isSelected: false,
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            allShow: true, // 是否全部展示
            startMockVars: [], // 业务字段
        };
    },
    computed: {
        // 获取节点属性数据
        propertiesData() {
            return this.model.properties || {};
        },
        getArgs() {
            return systemArgs[0]?.children || [];
        },
        // 当前节点ID
        getNodeId() {
            return this.model?.id;
        },
        // 节点名称
        getNodeName() {
            return this.model?.properties?.nodeName;
        }
    },
    components: {
        outputs,
        nodeTitle,
        varsTree
    },
    mounted() {
        this.initBus();
        this.init();
    },
    methods: {
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            // 校验
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            // 处理输出面板
            this.bus.$on('openOutputs', (data) => {
                if (data && data?.nodes) {
                    const filtered = data.nodes.filter(item => item.nodeId === this.getNodeId);
                    this.showOutputs = filtered.length > 0;
                    this.outputsData = this.showOutputs ? filtered[0] : {};
                } else {
                    this.showOutputs = false;
                }
            });
        },
        init() {
            const { inputVars } = this.propertiesData;
            if (inputVars && inputVars.length) {
                this.startMockVars = inputVars;
            } else {
                this.startMockVars = startMockVars();
            }
        },
        /**
         * 校验流程子节点
         *
         * 调用该方法后，会触发'childValidateDone'事件，并传递节点ID和校验状态作为参数。
         *
         * @returns 无返回值
         */
        validateFlowChild() {
            this.bus.$emit('childValidateDone', {
                nodeId: this.getNodeId,
                validateStatus: true, // 校验是否通过
            });
        },
        toggleFoldAll(expand) {
            this.allShow = expand;
            this.updateNodeAttributes(expand);
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
         updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`start_${this.getNodeId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getNodeId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({currentHeight: clientHeight, expand: expand});
                }
            });
        },
    }
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.start-container {
    text-align: left;
    .start-title {
        margin-bottom: 4px;
    }
    .start-args {
        .start-args-item {
            margin-bottom: 4px;
            .args-item-name {
                color: #876300;
                overflow: hidden;
                max-width: 160px;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .args-item-type {
                margin-left: 4px;
                padding: 0 5px;
                white-space: nowrap;
                border-radius: 4px;
                background-color: #e8e9eb;
            }
        }
    }
}
.start-outputs {
    /deep/ .jse-main {
        .jse-tree-mode {
            .jse-key {
                max-width: 180px;
                word-break:break-all;
            }
            .jse-contents-outer {
                .jse-contents {
                    align-items: center;
                }
            }
        }
    }
}
</style>