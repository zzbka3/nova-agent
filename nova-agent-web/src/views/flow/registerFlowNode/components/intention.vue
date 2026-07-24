<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-02 13:11:56
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-24 16:49:02
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/intention.vue
 * @Description: 意图节点组件
-->
<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus,
                'node-container-hide': !allShow
            }
        ]"
        :ref="`intention_${getNodeId}`"
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
            :node-icon="require('../../image/intention.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :mode-text="mode === 'speed' ? '极速' : '精确'"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <div
            :class="{
                'input-vars-box': true,
            }"
        >
            <!-- 输入字段显示 -->
            <queryInfo
                info-title="输入"
                :info-data="inputVars"
                :arr-args="arrArgs"
                :input-expanded="inputVarsShows"
                @updateNodeAttributes="inputVarsShow"
            />
            <!-- 意图 -->
            <div class="intent-items">
                <div class="intent-title">
                    <div class="title-text">
                        意图
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
                <div>
                    <div
                        class="intent-item"
                        v-for="(item) in intentItems"
                        :key="item.intentItemsIndex"
                    >
                        <div class="intent-name">
                            <div class="item-index">
                                {{ item.id }}
                            </div>
                            <div class="item-name">
                                {{ item.intentName }}
                            </div>
                        </div>
                    </div>
                    <div class="intent-item other-item">
                        <div class="intent-name">
                            <div class="item-index">
                                -1
                            </div>
                            <div class="item-name">
                                其他意图
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <varsTree
                v-show="allShow"
                :tree-data="outputList"
                :replace-fields="replaceFields"
                title="输出"
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
import nodeOperate from '../commonComponents/nodeOperate';
import { getAllArgs } from '@/views/flow/getArgs';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import validateIntentionNode from '@/views/flow/validateUtils/validateIntentionNode';
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
            intentItems: [], // 意图项
            intentModel: '', // 模型
            temperature: 0, // 多样性
            maxOutputTokens: 0, // 最大输出Tokens数
            talkHistory: false, // 对话历史
            additionalPrompt: '', // 附加提示词
            modelOption: [], // 模型选项
            outputList: [],
            expandedOutput: true, // 展开输出
            validateStatus: true, // 校验状态
            errResult: [], // 校验错误提示
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            replaceFields: {
                children: 'children',
                title: 'varName',
                key: 'id'
            },
        };
    },
    computed: {
        // 获取节点id
        getNodeId() {
            const graph = this.model;
            return graph.id;
        },
        // 获取节点属性数据
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
    methods: {
        /**
         * @description: bus监听
         * @return {*}
         */
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getNodeId) {
                    const data = validateIntentionNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = data.validateStatus;
                    this.errResult = data?.errResult;
                }
            });
            this.bus.$on('removeSessionStorage', () => {
                sessionStorage.removeItem(`${this.getNodeId}allShow`);
                sessionStorage.removeItem(`${this.getNodeId}inputVarsShows`);
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
            const { validateStatus, errResult } = validateIntentionNode({
                model: this.model,
                lf: this.lf
            });
            this.validateStatus = validateStatus;
            this.errResult = errResult;
            // 校验不通过时，执行相关操作
            if (!validateStatus) {
                console.log(errResult, '意图识别');
                // 清空已经选择的节点
            }
            this.bus.$emit('childValidateDone', {
                nodeId: this.getNodeId,
                validateStatus, // 校验是否通过
            });
        },
        /**
         * @description: 初始化方法
         * @return {*}
         */
        init() {
            this.arrArgs = getAllArgs({ nodeId: this.getNodeId, lf: this.lf });
            // eslint-disable-next-line max-len
            const { mode, inputVars, intentItems, model: intentModel, temperature, maxOutputTokens, talkHistory, additionalPrompt, outputVars, modelType } = this.propertiesData;
            this.mode = mode || 'speed';
            this.intentItems = intentItems;
            this.intentModel = intentModel || '';
            this.temperature = temperature || 0.0001;
            this.maxOutputTokens = maxOutputTokens || 1024;
            this.talkHistory = talkHistory === 1 ? true : false;
            this.outputList = outputVars || [];
            this.additionalPrompt = additionalPrompt || '';
            this.modelType = modelType || '';
            const inputVarsData = [
                {
                    varName: 'query',
                    varType: 'String',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            if (inputVars && inputVars.length > 0) {
                this.inputVars = inputVars;
            } else {
                this.inputVars = inputVarsData;
            }
            this.$nextTick(() => {
                this.setCustomAnchors();
            });
            this.model.setProperties({
                model: this.intentModel,
                inputVars: this.inputVars,
                intentItems: this.intentItems,
                temperature: this.temperature,
                maxOutputTokens: this.maxOutputTokens,
                talkHistory: this.talkHistory ? 1 : 0,
                additionalPrompt: this.additionalPrompt,
                mode: this.mode,
            });
            const showVal = sessionStorage.getItem(`${this.getNodeId}allShow`);
            const inputVarsShows = sessionStorage.getItem(`${this.getNodeId}inputVarsShows`);
            this.allShow = showVal === null ? true : showVal === 'true';
            this.inputVarsShows = inputVarsShows === null ? true : inputVarsShows === 'true';
            this.expandedOutput = showVal === null ? true : showVal === 'true';
        },
        /**
         * @description: 设置自定义锚点
         * @return {*}
         */
        setCustomAnchors() {
            const ref = this.$refs[`intention_${this.getNodeId}`];
            const items = ref.getElementsByClassName('intent-item');
            const customAnchors = [];
            Array.from(items).forEach((item, index) => {
                const top = item.offsetTop;
                const height = item.clientHeight;
                const { intentItemsIndex = -1 } = this.intentItems[index] || {};
                customAnchors.push({
                    indexId: intentItemsIndex,
                    height: top + height / 2
                });
            });
            this.model.setProperties({
                customAnchors: customAnchors,
            });
            setTimeout(() => {
                this.model.updatePath();
            }, 50);
        },
        /**
         * @description: 切换全部折叠
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.inputVarsShows = allShow;
            this.expandedOutput = allShow;
            sessionStorage.setItem(`${this.getNodeId}allShow`, allShow);
            sessionStorage.setItem(`${this.getNodeId}inputVarsShows`, allShow);
            this.$nextTick(() => {
                this.setCustomAnchors();
            });
        },
        /**
         * @description: 切换输入变量折叠
         * @return {*}
         */
        inputVarsShow(expand) {
            this.inputVarsShows = expand;
            sessionStorage.setItem(`${this.getNodeId}inputVarsShows`, this.inputVarsShows);
            this.$nextTick(() => {
                this.setCustomAnchors();
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
         * @description: 展开输出变量
         * @return {*}
         */
        expandOutput() {
            this.expandedOutput = !this.expandedOutput;
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        }
    },
    created() {
        this.getModelData();
    },
    mounted() {
        this.initBus();
        this.init();
    },
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.input-vars-box {
    display: block;
}
.input-vars-box-hide {
    display: none;
}
.node-container-hide {
    /deep/ .node-title {
        margin-bottom: 0px;
    }
}

.intent-items {
    width: 100%;
    font-size: 12px;
    padding: 12px;
    color: #5c5f66;
    border-radius: 8px;
    background-color: #f9f9fb;
    line-height: 20px;
    margin-top: 8px;

    .intent-title {
        display: flex;
        align-items: center;
        gap: 10px;
        height: auto;
        margin-bottom: 4px;

        .title-text {
            color: #151b26;
            font-weight: 500;
        }

        .model-box {
            display: flex;
            align-items: center;

            .model-img {
                width: 20px;
                height: 20px;
            }
        }
    }

    .intent-item {
        color: #151b26;
        font-size: 12px;
        background: #fff;
        font-weight: 400;
        line-height: 20px;
        margin-bottom: 4px;

        .intent-name {
            display: flex;
            align-items: center;
            padding: 4px 6px;
            border-radius: 4px;

            .item-index {
                width: 20px;
                color: #84868c;
            }
        }
    }

    .other-item {
        color: #84868c;
    }
}

</style>