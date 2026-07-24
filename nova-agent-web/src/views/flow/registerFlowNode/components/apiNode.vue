<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-02 13:11:56
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-24 16:48:51
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/apiNode.vue
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
        :ref="`apiNode_${getId}`"
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
            :node-icon="require('../../image/api.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :show-mode="false"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <div
            :class="{
                'input-vars-box': true,
                'input-vars-box-hide': !allShow
            }"
        >
            <queryInfo
                v-show="allShow"
                info-title="输入"
                :info-data="inputVars"
                :arr-args="arrArgs"
                @updateNodeAttributes="updateNodeAttributes"
            />

            <div class="warp-content-item">
                <!-- <div class="warp-content-item-title flex-between">
                    <div class="warp-content-item-title">
                        <a-icon
                            :type="expandedOutput ? 'caret-down' : 'caret-right'"
                            @click.stop="expandOutput"
                        />
                        <span class="warp-content-item-title-name">输出</span>
                    </div>
                </div>
                <div v-if="expandedOutput">
                    <div
                        class="warp-content-item-content"
                        v-for="(item, index) in outputList"
                        :key="index"
                    >
                        <span class="output-key">{{ item.varName }}</span>
                        <span class="output-type">{{ item.varType }}</span>
                    </div>
                </div> -->
                <varsTree
                    v-show="allShow"
                    :tree-data="outputVars"
                    title="输出"
                    @updateNodeAttributes="updateNodeAttributes"
                />
                <!-- <div v-else>
                    未添加输出参数
                </div> -->
            </div>
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
import validateApiNode from '@/views/flow/validateUtils/validateApiNode';
import outputs from '../commonComponents/outputs.vue';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import queryInfo from '../commonComponents/queryInfo.vue';

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
        varsTree,
        outputs,
        queryInfo
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
            expandedOutput: true, // 展开输出
            validateStatus: true, // 校验状态
            errResult: [], // 校验错误提示
            outputVars: [], // 输出参数
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
        };
    },
    computed: {
        // 获取节点id
        getId() {
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
    },
    methods: {
        /**
         * @description: bus监听
         * @return {*}
         */
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getId === args?.data?.id;
            });
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getId) {
                    this.updateNodeAttributes();
                    const { validateStatus, errResult } = validateApiNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = validateStatus;
                    this.errResult = errResult;
                }
            });
            // 校验
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            // 处理输出面板
            this.bus.$on('openOutputs', (data) => {
                if (data && data?.nodes) {
                    const filtered = data.nodes.filter(item => item.nodeId === this.getId);
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
            const { validateStatus, errResult } = validateApiNode({
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
                nodeId: this.getId,
                validateStatus, // 校验是否通过
            });
        },
        /**
         * @description: 初始化方法
         * @return {*}
         */
        init() {
            this.arrArgs = getAllArgs({ nodeId: this.getId, lf: this.lf });
            // eslint-disable-next-line max-len
            const { inputVars, readOnlyOutputs } = this.propertiesData;
            // console.log('api-inputVars:', inputVars, readOnlyOutputs);
            this.inputVars = inputVars || [];
            this.outputVars = readOnlyOutputs || [];
        },
        /**
         * @description: 切换全部折叠
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.inputVarsShows = allShow;
            this.expandedOutput = allShow;
            this.updateNodeAttributes(allShow);
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
        updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`apiNode_${this.getId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({ currentHeight: clientHeight, expand: expand });
                }
            });
        },
        /**
         * @description: 切换输入变量折叠
         * @return {*}
         */
        inputVarsShow() {
            this.inputVarsShows = !this.inputVarsShows;
            this.updateNodeAttributes(this.inputVarsShows);
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
.input-vars {
    width: 100%;
    font-size: 14px;
    padding: 12px 12px 5px;
    color: #5c5f66;
    border-radius: 8px;
    background-color: #f9f9fb;
    line-height: 20px;
    margin-top: 10px;

    .input-vars-item {
        display: flex;
        align-items: center;
        gap: 10px;
        height: auto;
        margin-bottom: 4px;

        .var-name {
            width: 47%;
            display: flex;
            align-items: center;
            color: #151b26;
            font-weight: 500;
            gap: 10px;

            .text {
                color: #5c5f66;
                font-weight: 300;
                max-width: 50%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;

                &::before {
                    content: "*";
                    color: #f33d3d;
                    margin-inline-start: 2px;
                }
            }

            .type {
                padding: 0px 5px;
                text-align: center;
                color: #5c5f66;
                border-radius: 4px;
                background: #e8e9eb;
                font-weight: 400;
                margin-left: 3px;
            }
        }

        .var-value {
            width: 50%;

            .var-value-box {
                display: flex;
                box-sizing: border-box;
                width: fit-content;
                max-width: calc(100% - 8px);
                padding: 0 4px;
                border: 1px solid #e8e9eb;
                border-radius: 4px;
                background-color: #fff;
            }
        }
    }

    .input-vars-item-hide {
        height: 0;
        overflow: hidden;
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

.warp-content-item {
    width: 100%;
    color: #5c5f66;
    border-radius: 8px;
    background-color: #f9f9fb;
    margin-top: 10px;

    .warp-content-item-title {
        gap: 10px;
        display: flex;
        align-items: center;

        a-icon {
            cursor: pointer;
        }

        img {
            width: 15px;
            height: 15px;
        }

        .warp-content-item-title-name {
            font-weight: bold;
            flex: 1;
            color: #151b26;
        }

        .warp-content-item-title-value {
            display: flex;
            flex: 1;
            min-width: 0;
        }
    }

    .warp-content-item-content {
        display: flex;
        align-items: center;
        margin-top: 4px;

        .warp-content-item-content-left {
            display: flex;
            flex: 1;
            min-width: 0;
        }

        .content-key {
            overflow: hidden;
            max-width: 160px;
            white-space: nowrap;
            text-overflow: ellipsis;
        }

        .required-tag:after {
            content: "*";
            color: #f33d3d;
            margin-inline-start: 2px;
        }

        .content-key-type {
            height: 20px;
            margin-left: 4px;
            padding: 0 5px;
            white-space: nowrap;
            border-radius: 4px;
            background-color: #e8e9eb;
        }

        .warp-content-item-content-right {
            display: flex;
            flex: 1;
            min-width: 0;

            .content-default {
                flex-shrink: 0;
                width: 14px;
            }

            .content-value {
                display: flex;
                box-sizing: border-box;
                width: fit-content;
                max-width: calc(100% - 8px);
                padding: 0 4px;
                border: 1px solid #e8e9eb;
                border-radius: 4px;
                background-color: #fff;
            }
        }

        .output-key {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }

        .output-type {
            display: flex;
            align-items: center;
            flex-shrink: 0;
            margin-left: 4px;
            padding: 0 5px;
            background-color: #e8e9eb;
            border-radius: 4px;
            white-space: nowrap;
            height: 20px;
            color: #5c5f66;
            line-height: 20px;
        }

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
        }
    }

    .warp-content-item-content-tips {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 8px;
        background: #fff;
        border-radius: 4px;
        font-size: 12px;
        margin: 4px 0 8px;

        .tips-title {
            margin-bottom: 4px;
            font-size: 12px;
            font-weight: bold;
            color: #84868c;
        }

        .tips-content {
            word-break: break-all;
            -webkit-line-clamp: 2;
            color: #5c5f66;
            display: -webkit-box;
            overflow: hidden;
            -webkit-box-orient: vertical;
            text-overflow: ellipsis;
        }
    }
}
</style>