<!--
 * @Author: hewenquan
 * @Date: 2025-06-27 14:45:12
 * @LastEditTime: 2025-11-07 14:51:39
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/basics/nodePanel.vue
 * @Description: node panel 组件
-->
<template>
    <div class="node-panel flex-center">
        <div class="node-select flex-center">
            <a-popover
                v-model="popoverVisible"
                trigger="click"
                class="select-container-popover"
            >
                <div
                    slot="content"
                    class="select-container"
                >
                    <div
                        class="container-item"
                        v-for="item in nodeList"
                        :key="item.desc"
                    >
                        <div class="container-item-desc">
                            {{ item.desc }}
                        </div>
                        <div
                            v-for="child in item.children"
                            :key="child.type"
                            @click="selectNodeType(child)"
                            class="container-item-child"
                        >
                            <img
                                class="container-item-child-icon"
                                :src="child.icon"
                            />{{ child.text }}
                        </div>
                    </div>
                </div>
                + 节点
            </a-popover>
        </div>
        <div class="flex-center node-operator">
            <a-tooltip
                placement="topLeft"
                :title="shrink ? '展开节点' : '折叠节点'"
            >
                <div
                    class="operator-icon"
                    @click="triggerNode"
                >
                    <a-icon
                        :type="shrink ? 'arrows-alt' : 'shrink'"
                    />
                </div>
            </a-tooltip>
            <a-tooltip
                placement="topLeft"
                title="居中视图"
            >
                <img
                    class="operator-icon"
                    src="../image/center.png"
                    @click="viewCenter()"
                />
            </a-tooltip>
            <a-tooltip
                placement="topLeft"
                title="自动布局"
            >
                <img
                    class="operator-icon"
                    src="../image/layout.png"
                    @click="autoLayout"
                />
            </a-tooltip>
            <div class="scale-operate">
                <a-popover
                    v-model="scaleVisible"
                    trigger="click"
                    class="scale-container-popover"
                >
                    <div
                        slot="content"
                        class="scale-container"
                    >
                        <div
                            class="scale-item"
                            v-for="item in scaleList"
                            :key="item"
                            @click="setScale(item)"
                        >
                            {{ item * 100 }}%
                        </div>
                    </div>
                    {{ (scale * 100).toFixed(0) }}%
                    <a-icon
                        type="down"
                        class="scale-icon"
                    />
                </a-popover>
            </div>
        </div>
        <div
            :class="[
                'node-check flex-center',
                {
                    'node-check-active': openCheck
                }
            ]"
            @click="checkLf"
        >
            <img
                src="../image/check.png"
                class="check-icon"
                v-if="!openCheck"
            />
            <img
                v-else
                src="../image/checkNotAllow.png"
                class="check-icon"
            />
            调试
        </div>
        <a-drawer
            placement="right"
            :visible="openCheck"
            @close="closeCheckDialog"
            class="check-drawer"
            width="512"
            :mask="false"
            :destroy-on-close="true"
        >
            <check-conversation
                :conversation-id="conversationId"
                ref="checkConversation"
                :lf="lf"
                @clearMsg="clearMsg"
            />
        </a-drawer>
        <agentList
            :open-agent-select.sync="openAgentSelect"
            :lf="lf"
        />
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import { validateFlow, validateEdges } from '../validateUtils/lf';
import CheckConversation from './checkConversation.vue';
import { flowRequest } from '../common/request';
import { conversation, validate } from '../apiList';
import {
    codeDefaultInputVars,
    codeDefaultOutputVars,
    codeData,
    textProcessorOutPut,
    textProcessorInput,
    rewriteOutPut
} from './codeDealt';
import loadingCtrl from '@/utils/loading';
import { sleep } from '@/utils/common';
import agentList from './components/agentList.vue';
import { deleteTempOutputs } from '@/views/flow/getArgs';
export default {
    props: {
        lf: {
            type: LogicFlow,
            default: () => ({}),
            required: true
        },
        nodeList: {
            type: Array,
            default: () => []
        },
        // 暂存画布数据
        saveFlow: {
            type: Function,
            default: () => () => { }
        },
        // 初始化画布节点数据
        flowData: {
            type: Object,
            default: () => ({})
        }
    },
    components: {
        CheckConversation,
        agentList
    },
    data() {
        return {
            popoverVisible: false, // 控制 节点popover 的显示与隐藏
            scale: 1, // 画布缩放比例
            scaleVisible: false, // 控制 缩放popover 的显示与隐藏
            // 缩放比例列表
            scaleList: [
                2,
                1.5,
                1,
                0.7,
                0.5,
                0.3,
            ],
            openCheck: false, // 调试模式
            conversationId: '',
            shrink: false, // 是否折叠
            openAgentSelect: false, // 开启工作流配置选择
        };
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
    },
    mounted() {
        this.initLfEvent();
    },
    methods: {
        initLfEvent() {
            // 初始化缩放比例
            const { SCALE_X = 1 } = this.lf.getTransform();
            this.scale = SCALE_X.toFixed(2);
            // 如果缩放比例大于1，则重置为1
            if (this.scale > 1) {
                this.lf.zoom(1);
                this.lf.translateCenter();
                this.scale = 1;
            }
            if (this.flowData?.nodes?.length > 20) {
                this.lf.zoom(0.3);
                this.lf.translateCenter();
                this.scale =0.3;
            }
            // 监听画布缩放事件
            this.lf.on('graph:transform', (data) => {
                const { type, transform } = data;
                if (type === 'zoom') {
                    this.scale = transform.SCALE_X.toFixed(2);
                }
            });
        },
        /**
         * 将视图中心重置为默认状态
         *
         * 重置缩放和平移，将视图中心恢复到默认状态
         */
        viewCenter() {
            // 重置图形的缩放比例为默认
            // this.lf.resetZoom();
            // // 还原图形为初始位置。
            // this.lf.resetTranslate();
            // this.scale = 1;
            // 居中视图
            this.lf.translateCenter();
        },
        /**
         * @description: 自动化布局
         * @return {*}
         */
        autoLayout() {
            // 如果节点被折叠了，优先展开折叠
            if (this.shrink) {
                this.shrink = false;
                this.bus.$emit('triggerNode', this.shrink);
            }
            this.$nextTick(() => {
                this.lf.extension.dagre.layout({
                    align: '', // 节点居中排布
                });
            });
        },
        /**
         * 设置缩放比例
         *
         * @param scale 缩放比例
         */
        setScale(scale) {
            this.lf.zoom(scale);
            this.lf.translateCenter();
        },
        /**
         * @description: 选择节点
         * @param {*} nodeConfig
         * @return {*}
         */
        selectNodeType(nodeConfig) {
            this.popoverVisible = false;
            // 代码节点需要设置初始化数据
            if (nodeConfig.type === 'CODE') {
                this.lf.dnd.startDrag({
                    type: nodeConfig.type,
                    properties: {
                        inputVars: codeDefaultInputVars(),
                        outputVars: codeDefaultOutputVars(),
                        codeData,
                    }
                });
            } else if (nodeConfig.type === 'TEXT_PROCESSOR') {
                // 文本处理节点初始化数据
                this.lf.dnd.startDrag({
                    type: nodeConfig.type,
                    properties: {
                        inputVars: textProcessorInput(),
                        outputVars: textProcessorOutPut(),
                        mode: 'CONCAT'
                    }
                });
            } else if (nodeConfig.type === 'REWRITE') {
                // query 多轮改写节点初始化数据
                this.lf.dnd.startDrag({
                    type: nodeConfig.type,
                    properties: {
                        outputVars: rewriteOutPut(),
                        rewriteType: 1,
                        temperature: 0.0001
                    }
                });
            } else if (nodeConfig.type === 'WORKFLOW_AGENT') {
                // 工作流配置
                this.openAgentSelect = true;
            } else {
                this.lf.dnd.startDrag({
                    type: nodeConfig.type,
                });
            }
        },
        /**
         * 触发节点展开或折叠的方法
         *
         * 当调用此方法时，将切换 this.shrink 的布尔值。
         * 如果 this.shrink 为 true，则将其设置为 false，表示节点展开；
         * 如果 this.shrink 为 false，则将其设置为 true，表示节点折叠。
         */
        triggerNode() {
            this.shrink = !this.shrink;
            this.bus.$emit('triggerNode', this.shrink);
        },
        /**
         * @description: 画布校验
         * @return {*}
         */
        async checkLf() {
            if (this.openCheck) {
                return;
            }
            // 调试时清除上下文参数，强制检查一次
            deleteTempOutputs();
            loadingCtrl.show();
            await sleep(100);
            // 画布配置校验
            const flowConfig = await validateFlow({
                bus: this.bus,
                lf: this.lf
            }).catch(error => {
                loadingCtrl.hide();
                console.log(error);
                // 错误发生在某个节点时，需要将画布平移到该节点位置
                if (error) {
                    this.lf.focusOn({
                        id: error
                    });
                }
            });
            if (!flowConfig) {
                loadingCtrl.hide();
                return;
            }
            // 连线校验
            const edgesResult = validateEdges({ bus: this.bus, lf: this.lf });
            if (!edgesResult) {
                loadingCtrl.hide();
                return;
            }
            await this.saveFlow();
            // 再通过接口校验一遍
            const graphData = this.lf.getGraphData();
            const validateStatus = await flowRequest({
                url: validate,
                method: 'POST',
                data: {
                    config: JSON.stringify(graphData),
                    appId: this.appId
                }
            }).catch((error) => {
                console.log(error);
                loadingCtrl.hide();
                this.$message.error('校验失败，请检查agent配置');
            });
            if (!validateStatus) {
                loadingCtrl.hide();
                return;
            }
            // 校验通过后，开启调试模式
            // 有会话ID
            if (this.conversationId) {
                this.openCheck = true;
                this.$emit('openCheck', this.openCheck);
                loadingCtrl.hide();
            } else {
                // 没有会话ID，创建会话
                const res = await this.createConversation().catch(() => {
                    this.$message.error('调试失败，请稍后重试');
                });
                this.conversationId = res;
                this.openCheck = true;
                this.$emit('openCheck', this.openCheck);
                loadingCtrl.hide();
            }
        },
        /**
         * 创建一个新的会话
         *
         * @returns {Promise<any>} 返回一个Promise对象，成功时resolve会话数据，失败时reject
         */
        createConversation() {
            return new Promise((resolve, reject) => {
                flowRequest({
                    url: conversation,
                    method: 'get',
                    params: {
                        appId: this.appId
                    }
                }).then(res => {
                    resolve(res);
                }).catch(() => {
                    reject();
                });
            });
        },
        /**
         * 清除历史消息，新开一个会话
         *
         * @returns 无返回值
         */
        async clearMsg() {
            const res = await this.createConversation().catch(() => {
                this.$message.error('创建新会话失败，请稍后重试');
                this.closeCheckDialog();
            });
            this.conversationId = res;
        },
        closeCheckDialog() {
            this.$confirm({
                title: '退出调试提示',
                content: '工作流Agent正在调试中，离开后应用配置将更新，确定要退出调试吗？',
                okText: '确认',
                cancelText: '取消',
                onOk: () => {
                    this.openCheck = false;
                    this.$refs.checkConversation?.tmpSaveMsgList();
                    this.$refs.checkConversation?.clearTimeoutRunningInfo();
                    this.changeLinToDefaultColor();
                    // 关闭输出显示面板
                    this.bus.$emit('openOutputs');
                    // 通知父组件关闭调试模式
                    this.$emit('openCheck', this.openCheck);
                },
            });
        },
        /**
         * 将所有的的线条更改为默认颜色
         *
         * @param {Object} options - 配置项
         * @param {string} [options.lineType='EDGE_BEZIER'] - 线条类型，默认为 'EDGE_BEZIER'
         */
        changeLinToDefaultColor({ lineType = 'EDGE_BEZIER' } = {}) {
            const lf = this.lf;
            let outEdges = [];
            const { nodes } = lf.getGraphData();
            nodes.forEach(item => {
                const itemEdges = lf.getNodeOutgoingEdge(item.id);
                if (itemEdges.length > 0) {
                    outEdges = [...outEdges, ...itemEdges];
                }
            });
            outEdges.forEach(edge => {
                lf.changeEdgeType(edge.id, lineType);
            });
        }
    },
};
</script>
<style>
.ant-popover-inner-content {
    padding: 0;
    border-radius: 12px;
    background: #fff;
}
</style>
<style lang="less" scoped>
.flex-center{
    display: flex;
    align-items: center;
    justify-content: center;
}
.node-panel {
    position: absolute;
    bottom: 20px;
    left: 50%;
    height: 40px;
    transform: translateX(-50%);
    background-color: white;
    padding: 8px;
    box-shadow: 0 0 10px 1px rgb(228, 224, 219);
    border: 1px solid #d4d6d9;
    border-radius: 6px;
    z-index: 101;
    .node-select {
        margin-right: 8px;
        padding: 2px 8px;
        color: #fff;
        border-radius: 6px;
        background: #2468f2;
        font-size: 12px;
        font-weight: 500;
        line-height: 20px;
        cursor: pointer;
    }
    .node-operator {
        flex: 1;
        justify-content: space-between;
        margin-right: 8px;
        padding: 0 8px;
        border-right: 1px solid #e8e9eb;
        border-left: 1px solid #e8e9eb;
        .operator-icon {
            width: 24px;
            height: 24px;
            margin-right: 8px;
            cursor: pointer;
        }
    }
    .node-check {
        cursor: pointer;
        width: 56px;
        height: 24px;
        color: #fff;
        border-radius: 6px;
        background: #34c759;
        font-size: 12px;
        font-weight: 500;
        line-height: 20px;
        &:hover {
            background: #2eb250;
        }
        .check-icon {
            width: 14px;
            height: 12px;
            margin-right: 4px;
        }
    }
    .node-check-active {
        cursor: not-allowed;
        color: #b8babf;
        background: inherit;
        &:hover {
            background: inherit;
        }
    }
}
.select-container {
    display: flex;
    overflow-y: auto;
    flex-direction: column;
    box-sizing: border-box;
    width: 240px;
    height: fit-content;
    min-height: 300px;
    max-height: calc(100vh - 130px);
    padding: 12px 8px 8px;
    border-radius: 12px;
    background: #fff;
    box-shadow: 0 8px 20px #0000001a;
    .container-item {
        margin-bottom: 8px;
        .container-item-desc {
            padding-left: 6px;
            color: #84868c;
            font-size: 12px;
            font-weight: 500;
            line-height: 20px;
        }
        .container-item-child {
            box-sizing: border-box;
            margin-top: 2px;
            padding: 6px;
            border-radius: 8px;
            cursor: pointer;
            .container-item-child-icon {
                width: 20px;
                height: 20px;
                margin-right: 8px;
            }
        }
    }
}
.scale-operate {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 24px;
    text-align: center;
    color: #151b26;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    .scale-icon {
        font-size: 10px;
    }
    &:hover {
        border-radius: 6px;
        background: #f7f7f9;
    }
}
.scale-container {
    width: 122px;
    height: fit-content;
    padding: 8px;
    .scale-item {
        margin-bottom: 4px;
        padding: 5px 0px 5px 8px;
        cursor: pointer;
        border-radius: 6px;
        &:hover {
            background: #f7f7f9;
        }
    }
}
.check-drawer {
    /deep/ .ant-drawer-body {
        padding: 0;
    }
}
</style>