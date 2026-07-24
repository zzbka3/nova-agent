<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`knowledge_${getNodeId}`"
    >
        <nodeOperate
            v-if="isSelected"
            :model="model"
            :lf="lf"
            @editNode="handleEditNode"
        />
        <nodeTitle
            ref="nodeTitleRef"
            :model="model"
            :node-name="getNodeName"
            :node-icon="require('../../image/knowledge.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <div
            class="warp-content"
            v-if="allShow"
        >
            <!-- 输入 -->
            <queryInfo
                info-title="输入"
                :info-data="[inputParams]"
                :arr-args="arrArgs"
                @updateNodeAttributes="updateNodeAttributes"
            />
            <!-- 知识库 -->
            <div
                class="warp-content-item"
            >
                <div class="warp-content-item-title">
                    <a-icon
                        :type="expandedKnowledge ? 'caret-down' : 'caret-right'"
                        @click.stop="expandKnowledge"
                    />
                    <span class="warp-content-item-title-name">知识库</span>
                </div>
                <div v-if="knowledgeDataList.length > 0 && expandedKnowledge">
                    <div
                        class="warp-content-item-content"
                        v-for="item in knowledgeDataList"
                        :key="item.id"
                    >
                        <img src="@/assets/knowledge_file.png">
                        <span
                            class="knowledgeName"
                        > {{ item.knowledgeName || '' }}</span>
                    </div>
                </div>
            </div>
            <varsTree
                :tree-data="outputList"
                title="输出"
                @updateNodeAttributes="updateNodeAttributes"
            />
        </div>
        <outputs
            :outputs-data="outputsData"
            v-if="showOutputs"
        />
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import nodeOperate from '../commonComponents/nodeOperate.vue';
import validateKnowledgeNode from '../../validateUtils/validateKnowledgeNode';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import outputs from '../commonComponents/outputs.vue';
import { largeModelViews } from '../commonUtils';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import queryInfo from '../commonComponents/queryInfo.vue';
import { getAllArgs } from '@/views/flow/getArgs';
import { deepClone } from '@baidu/metis-js-util';

export default {
    props: {
        properties: {
            type: Object,
            default: (() => { })
        },
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
            knowledgeData: this.properties,
            configParmas: {},
            expanded: true,
            expandedInput: true,
            expandedOutput: true,
            expandedKnowledge: true,
            inputParams: {},
            knowledgeDataList: [],
            isSelected: false, // 是否选中当前节点
            validateStatus: true, // 校验状态
            errResult: [], // 校验错误提示
            outputList: [], // 输出字段集合
            expandedOutputParent: false,
            allShow: true, // 是否全部展开
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            arrArgs: [], // 参数列表
        };
    },
    computed: {
        // 获取节点ID
        getNodeId() {
            const graph = this.model;
            return graph.id;
        },
        // 获取节点名称
        getNodeName() {
            return this.model?.properties?.nodeName;
        }
    },
    watch: {
        knowledgeData: {
            handler(newVal) {
                if (newVal?.inputVars?.length > 0) {
                    this.inputParams = newVal?.inputVars[0] || {};
                } else {
                    this.inputParams = {};
                }
                this.knowledgeDataList = newVal?.knowledgeBaseId || [];
            },
            deep: true,
            immediate: true
        },
    },
    components: {
        nodeOperate,
        nodeTitle,
        outputs,
        varsTree,
        queryInfo
    },
    mounted() {
        this.init();
        this.initBus();
    },
    methods: {
        init() {
            if (this.knowledgeData?.inputVars?.length > 0) {
                this.inputParams = this.knowledgeData?.inputVars[0] || {};
            } else {
                this.inputParams = {};
            }
            this.knowledgeDataList = this.knowledgeData?.knowledgeBaseId || [];
            this.arrArgs = getAllArgs({ nodeId: this.getNodeId, lf: this.lf });
            let outputVars = deepClone(largeModelViews) || [];
            this.outputList = outputVars;
        },
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                console.log(nodeId, '开始校验nodeId');
                if (nodeId === this.getNodeId) {
                    this.updateNodeAttributes();
                    const data = validateKnowledgeNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.errResult = data.errResult;
                    this.validateStatus = data.validateStatus;
                }
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
        /**
         * 校验流程子节点
         *
         * 调用该方法后，会触发'childValidateDone'事件，并传递节点ID和校验状态作为参数。
         *
         * @returns 无返回值
         */
        validateFlowChild() {
            const { validateStatus, errResult } = validateKnowledgeNode({
                model: this.model,
                lf: this.lf
            });
            this.validateStatus = validateStatus;
            this.errResult = errResult;
            // 校验不通过时，执行相关操作
            if (!validateStatus) {
                console.log(errResult);
                // 清空已经选择的节点
            }
            this.bus.$emit('childValidateDone', {
                nodeId: this.getNodeId,
                validateStatus, // 校验是否通过
            });
        },
        expandKnowledge() {
            this.expandedKnowledge = !this.expandedKnowledge;
            this.updateNodeAttributes(this.expandedKnowledge);
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
        updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`knowledge_${this.getNodeId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getNodeId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({ currentHeight: clientHeight, expand: expand });
                }
            });
        },
        /**
         * @description: 展开/收起所有
         * @param {boolean} allShow
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.expandedKnowledge = allShow;
            this.expandedInput = allShow;
            this.expandedOutput = allShow;
            this.updateNodeAttributes(allShow);
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        }
    }
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-container {
    width: 100%;
    height: auto;
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    text-align: left;
    .warp-content {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-top: 10px;
    }
    .warp-content-item {
        padding: 14px;
        color: #5c5f66;
        border-radius: 8px;
        background-color: #f9f9fb;
        line-height: 20px;

        .warp-content-item-title {
            gap: 8px;
            display: flex;
            align-items: center;
            a-icon {
                cursor: pointer;
            }
            .warp-content-item-title-name {
                font-weight: bold;
                flex: 1;
                color: #151b26;
            }
        }
        .warp-content-item-content {
            display: flex;
            align-items: center;
            margin-top: 4px;
            gap: 8px;
            img {
                width: 15px;
                height: 15px;
            }
            .knowledgeName {
                color: #151B26;
                word-break: break-word;
                line-height: 1.67;
                font-size: 12px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                display: inline-block;
                max-width: 100%;
                margin-left: 6px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        }
    }
}
</style>