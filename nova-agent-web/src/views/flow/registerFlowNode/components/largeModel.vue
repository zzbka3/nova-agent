<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`largeModel_${getNodeId}`"
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
            :node-icon="require('../../image/largeModel.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <!-- 输入 -->
        <queryInfo
            v-show="allShow"
            info-title="输入"
            :info-data="inputVars"
            :arr-args="arrArgs"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <!-- 模型 -->
        <div
            class="intent-items"
            v-show="allShow"
        >
            <div class="intent-title">
                <div class="intent-title-content">
                    <a-icon
                        :type="expandTips ? 'caret-down' : 'caret-right'"
                        @click.stop="toggleTips"
                    />
                    <div class="title-text">
                        提示词
                    </div>
                </div>
                <div
                    class="model-box"
                    v-if="intentModel"
                >
                    <img
                        :src="getModelIcon"
                        class="model-img"
                    >
                    <span>{{ getModelName }}</span>
                </div>
            </div>
            <div
                class="intent-content"
                v-show="expandTips"
            >
                <div
                    class="intent-item other-item"
                    v-if="systemPrompt && systemPrompt.length > 0"
                >
                    <div class="intent-item-name">
                        系统提示词
                    </div>
                    <div class="intent-item-content">
                        {{ systemPrompt }}
                    </div>
                </div>
                <div
                    class="intent-item other-item"
                    v-if="userPrompt && userPrompt.length > 0"
                >
                    <div class="intent-item-name">
                        用户提示词
                    </div>
                    <div class="intent-item-content">
                        {{ userPrompt }}
                    </div>
                </div>
            </div>
        </div>
        <varsTree
            v-show="allShow"
            :tree-data="outputVars"
            class="start-wrapper"
            title="输出"
            :replace-fields="replaceFields"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <outputs
            :outputs-data="outputsData"
            v-if="showOutputs"
        />
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import nodeOperate from '../commonComponents/nodeOperate';
import { getAllArgs } from '@/views/flow/getArgs';
import { modelOption } from '@/views/flow/common/commonData';
import validateLargeModelNode from '../../validateUtils/validateLargeModelNode';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import { getModelList } from '@/views/flow/common/modelList';
import outputs from '../commonComponents/outputs.vue';
import queryInfo from '../commonComponents/queryInfo.vue';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';

export default {
    props: {
        name: {
            type: String,
            default: ''
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
    components: {
        nodeOperate,
        nodeTitle,
        outputs,
        queryInfo,
        varsTree
    },
    data() {
        return {
            isSelected: false, // 是否选中当前节点
            allShow: true, // 是否全部展开
            inputVarsShows: true,
            arrArgs: [], // 系统参数树
            mode: '', // 模式
            inputVars: [], // 输入变量
            intentModel: '', // 模型
            temperature: 0, // 多样性
            maxOutputTokens: 0, // 最大输出Tokens数
            talkHistory: false, // 对话历史
            additionalPrompt: '', // 附加提示词
            modelOption, // 模型选项
            expandedOutput: true,
            expandTips: true,
            systemPrompt: '',
            userPrompt: '',
            validateStatus: true,
            errResult: [], // 校验错误提示
            outputVars: [],
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            replaceFields: {
                children: 'children',
                title: 'varName',
                key: 'id'
            }
        };
    },
    computed: {
        // 获取节点ID
        getNodeId() {
            const graph = this.model;
            return graph.id;
        },
        // 获取节点数据
        propertiesData() {
            return this.model.properties || {};
        },
        // 获取节点名称
        getNodeName() {
            return this.model?.properties?.nodeName;
        },
        // 获取模型图标
        getModelIcon() {
            return this.modelOption.find(item => item.modelCode === this.intentModel)?.icon;
        },
        // 获取模型名称
        getModelName() {
            return this.modelOption.find(item => item.modelCode === this.intentModel)?.modelName;
        },
    },
    created() {
        this.getModelData();
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
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getNodeId) {
                    this.updateNodeAttributes();
                    const data = validateLargeModelNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = data.validateStatus;
                    this.errResult = data?.errResult;
                }
            });
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
            const { validateStatus, errResult } = validateLargeModelNode({
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
        init() {
            const inputVarsData = [
                {
                    varName: '',
                    varType: '',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            this.arrArgs = getAllArgs({ nodeId: this.getNodeId, lf: this.lf });
            const {
                mode,
                inputVars,
                model: intentModel,
                temperature,
                maxOutputTokens,
                talkHistory,
                additionalPrompt,
                systemPrompt,
                userPrompt,
                outputVars,
            } = this.propertiesData;
            this.mode = mode || 'speed';
            if (inputVars && inputVars.length > 0) {
                this.inputVars = inputVars;
            } else {
                this.inputVars = inputVarsData;
            }
            this.intentModel = intentModel || '';
            this.temperature = temperature || 0.0001;
            this.maxOutputTokens = maxOutputTokens || 1024;
            this.talkHistory = talkHistory === 1 ? true : false;
            this.additionalPrompt = additionalPrompt || '';
            this.systemPrompt = systemPrompt || '';
            this.userPrompt = userPrompt || '';
            this.outputVars = outputVars || [];

        },
        /**
         * 切换全部折叠状态
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.inputVarsShows = allShow;
            this.expandTips = allShow;
            this.expandedOutput = allShow;
            this.updateNodeAttributes(allShow);
        },
        toggleTips() {
            this.expandTips = !this.expandTips;
            this.updateNodeAttributes(this.expandTips);
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
         updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`largeModel_${this.getNodeId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getNodeId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({currentHeight: clientHeight, expand: expand});
                }
            });
        },
        /**
         * @description: 获取模型图标地址
         * @param {String} model - 模型名称
         * @return {*}
         */
        async getModelData() {
            this.modelOption = await getModelList();
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        }
    },
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
    min-width: 400px;
}

.intent-items {
    font-size: 14px;
    padding: 12px;
    color: #5c5f66;
    border-radius: 8px;
    background-color: #f9f9fb;
    line-height: 20px;
    margin-top: 8px;

    .intent-title {
        display: flex;
        justify-content: space-between;
        height: auto;
        margin-bottom: 4px;

        .intent-title-content {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .title-text {
            color: #151b26;
            font-weight: bold;
        }

        .model-box {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 12px;

            .model-img {
                width: 20px;
                height: 20px;
            }
        }
    }

    .intent-content {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    .intent-item {
        color: #151b26;
        font-size: 12px;
        background: #fff;
        font-weight: 400;
        line-height: 20px;

        .intent-item-name {
            margin-bottom: 4px;
            font-size: 12px;
            font-weight: 500;
            color: #84868c;
        }
        .intent-item-content {
            color: #5c5f66;
            word-break: break-all;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
        }
    }

    .other-item {
        color: #84868c;
        padding: 8px;
    }
}
</style>