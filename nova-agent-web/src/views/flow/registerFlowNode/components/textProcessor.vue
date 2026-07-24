<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`intention_${getId}`"
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
            :node-icon="require('../../image/text.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :err-result="errResult"
            :mode-text="mode === 'CONCAT' ? '拼接' : '分隔'"
            :lf="lf"
        />
        <!-- 输出字段显示 -->
        <queryInfo
            v-show="allShow"
            info-title="输入"
            :info-data="inputVars"
            :arr-args="arrArgs"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <varsTree
            v-show="allShow"
            :tree-data="outputVars"
            class="start-wrapper"
            title="输出"
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
import validateTextProcessorNode from '../../validateUtils/validateTextProcessorNode';
import nodeTitle from '../commonComponents/nodeTitle.vue';
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
            arrArgs: [], // 系统参数树
            inputVars: [], // 输入变量
            outputVars: [], // 输入变量
            validateStatus: true,
            errResult: [], // 校验错误提示
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            mode: '',
        };
    },
    computed: {
        // 获取节点ID
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
        }
    },
    mounted() {
        this.initBus();
        this.init();
    },
    methods: {
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getId === args?.data?.id;
            });
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getId) {
                    this.updateNodeAttributes();
                    const data = validateTextProcessorNode({
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
                    const filtered = data.nodes.filter(item => item.nodeId === this.getId);
                    this.showOutputs = filtered.length > 0;
                    this.outputsData = this.showOutputs ? filtered[0] : {};
                } else {
                    this.showOutputs = false;
                }
            });
        },
        init() {
            this.arrArgs = getAllArgs({ nodeId: this.getId, lf: this.lf });
            const {
                inputVars,
                outputVars,
                mode
            } = this.propertiesData;
            this.inputVars = inputVars || [];
            this.outputVars = outputVars || [];
            this.mode = mode;
        },
        /**
         * 切换全部折叠状态
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.updateNodeAttributes(allShow);
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
         updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`intention_${this.getId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({currentHeight: clientHeight, expand: expand});
                }
            });
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        },
        validateFlowChild() {
            const { validateStatus, errResult } = validateTextProcessorNode({
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
.node-icon {
    margin-left: 8px;
}
.mode-text {
    height: 20px;
    padding: 0 8px;
    color: #ff9326;
    border-radius: 4px;
    background: #fff4e6;
    font-size: 12px;
    line-height: 20px;
    margin-left: 5px;
}

.input-vars {
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
            width: 50%;
            display: flex;
            align-items: center;
            color: #151b26;
            font-weight: 500;

            .anticon {
                margin-right: 5px;
            }

            .text {
                overflow: hidden;
                max-width: 160px;
                white-space: nowrap;
                text-overflow: ellipsis;
                color: #5c5f66;
            }

            .type {
                padding: 0px 5px;
                text-align: center;
                border-radius: 4px;
                background: #e8e9eb;
                font-weight: 400;
                margin-left: 3px;
                color: #5c5f66;
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
    font-size: 14px;
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
        border-radius: 8px;

        .intent-item-name {
            margin-bottom: 4px;
            font-size: 12px;
            font-weight: 500;
            color: #84868c;
        }
        .intent-item-content {
            color: #5c5f66;
            word-break: break-all;
        }
    }

    .other-item {
        color: #84868c;
        padding: 8px;
    }
}

.warp-content-item {
    padding: 12px;
    color: #5c5f66;
    border-radius: 8px;
    background-color: #f9f9fb;
    line-height: 20px;
    margin-top: 8px;

    .warp-content-item-title {
        gap: 8px;
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