<template>
    <div class="flow-view">
        <node-panel
            v-if="lf"
            :lf="lf"
            :node-list="nodeList"
            @openCheck="changeOpenCheck"
            :save-flow="saveFlow"
            ref="nodePanel"
            :flow-data="flowData"
        ></node-panel>
        <!-- 画布 -->
        <div
            class="logic-flow-container"
            ref="container"
        ></div>
        <a-drawer
            placement="right"
            :visible="nodeDialogVisible"
            @close="closeNodeConfigDialog"
            width="400"
            :mask="false"
            :destroy-on-close="true"
            class="node-config-drawer"
        >
            <nodeConfig
                v-if="clickNode"
                :click-node="clickNode"
                :key="clickNode.id"
                :lf="lf"
            ></nodeConfig>
        </a-drawer>
        <basisConfig
            @closeBasisConfig="saveFlow"
            :agent-data="agentData"
            @change="changeAgentData"
            @updataMemory="updataMemory"
            @deleteMemory="deleteMemory"
            :open-check="openCheck"
        />
    </div>
</template>
<script>
import '@logicflow/core/dist/index.css';
import '@logicflow/extension/lib/style/index.css';
import LogicFlow from '@logicflow/core';
import { MiniMap } from '@logicflow/extension';
import NodePanel from './basics/nodePanel.vue';
import { Dagre } from '@logicflow/layout';
import { defaultEdge, animationEdge } from './basics/BezierEdge';
import { nodeList } from '@/views/flow/basics/flowConfig.js';
import nodeConfig from './nodeConfig/index.vue';
import basisConfig from './basics/basisConfig.vue';
import {
    registerStart,
    registerEnd,
    registerConditional,
    registerIntention,
    registerKnowledge,
    registerLargeModel,
    registerApi,
    registerMessage,
    registerProcess,
    registerCode,
    registerTextProcessor,
    registerRewrite,
    registerMemory,
    registerWorkflowAgent
} from './registerFlowNode';
import { customAnchorClickEvent, customBackEvent } from './basics/lfEvent';
import { getAgentDetail, saveAgent } from './apiList';
import { flowRequest } from './common/request';
import loadingCtrl from '@/utils/loading';
import { setMemorySchemaList } from './common/modelList';
import { updateReferenceVarNameById, deleteReferenceVarNameById } from '@/views/flow/basics/lfEvent.js';
import { deleteTempOutputs } from '@/views/flow/getArgs';
export default {
    components: {
        NodePanel,
        nodeConfig,
        basisConfig
    },
    data() {
        return {
            lf: null,
            clickNode: null, // 当前点击的节点数据
            nodeDialogVisible: false, // 节点配置
            // moveData: {},
            nodeList,
            openCheck: false, // 是否开启调试模式
            flowData: {}, // 画布数据
            agentData: {}, // agent 详情数据
            saveTimer: null, // 保存定时器
            isEditName: false, // 是否编辑节点名称
        };
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
        productLine() {
            return this.$route.params.productLine;
        },
    },
    beforeDestroy() {
        // 移除所有监听器
        this.bus.$off();
        this.clearSaveInterval();
        // this.lf?.destroy();
    },
    created() {
        this.getAgentDetail();
        this.bus.$on('editNodeName', ({ isEditName = false }) => {
            this.isEditName = isEditName;
        });
    },
    methods: {
        /**
         * @description: 清除保存定时器
         * @return {*}
         */
        clearSaveInterval() {
            this.saveTimer = null;
            clearTimeout(this.saveTimer);
        },
        // 获取 agent 详情
        async getAgentDetail() {
            if (!this.appId) {
                this.$router.back();
                return;
            }
            loadingCtrl.show();
            const data = await flowRequest({
                url: getAgentDetail,
                method: 'get',
                params: {
                    appId: this.appId
                }
            }).catch(() => {
                loadingCtrl.hide();
                this.$message.error('获取 agent 详情失败, 请重试');
            });
            if (data && data.config) {
                try {
                    let { edges = [], nodes = [] } = JSON.parse(data?.config) || {};
                    // 将高亮的线处理为正常模式
                    if (edges.length) {
                        edges = edges.map(item => {
                            item.type = 'EDGE_BEZIER';
                            return item;
                        });
                    }
                    // 将API输入参数为Array/Object的转换成string
                    let dealGraphNodes = [];
                    nodes.forEach(item => {
                        const { type, properties } = item || {};
                        if (type === 'API') {
                            let { inputVars = [], inputVarsAll = [] } = properties || {};
                            inputVars.map(sub => {
                                const { varType = '', varValue } = sub || {};
                                if (varType.includes('Array') && varValue && Array.isArray(varValue)) {
                                    sub.varValue = JSON.stringify(varValue);
                                }
                                if (varType.includes('Object') && varValue && typeof varValue === 'object') {
                                    sub.varValue = JSON.stringify(varValue);
                                }
                                if (varType === 'Boolean') {
                                    sub.varValue = sub.varValue === true ? 'true' : 'false';
                                }
                            });
                            inputVarsAll.map(sub => {
                                const { varType = '', varValue } = sub || {};
                                if (varType.includes('Array') && varValue && Array.isArray(varValue)) {
                                    sub.varValue = JSON.stringify(varValue);
                                }
                                if (varType.includes('Object') && varValue && typeof varValue === 'object') {
                                    sub.varValue = JSON.stringify(varValue);
                                }
                                if (varType === 'Boolean') {
                                    sub.varValue = sub.varValue === true ? 'true' : 'false';
                                }
                            });
                            dealGraphNodes.push(item);
                        } else {
                            dealGraphNodes.push(item);
                        }
                    });
                    this.flowData = {
                        nodes: dealGraphNodes,
                        edges
                    };
                } catch (error) {
                    console.log(error);
                    loadingCtrl.hide();
                }
                delete data.config;
                this.agentData = data;
                const { memorySchema = '' } = this.agentData;
                setMemorySchemaList(memorySchema ? JSON.parse(memorySchema) : []);
                this.$emit('syncAgentData', this.agentData);
                this.$_initLf();
                this.saveTimer = setTimeout(() => {
                    this.saveFlowTimer();
                }, 5000);
            }
        },
        /**
         * @description: 初始化画布
         * @return {*}
         */
        $_initLf() {
            if (this.lf) {
                this.lf.destroy();
            }
            // 画布配置
            const lf = new LogicFlow({
                adjustEdge: false,
                // 插件注册
                plugins: [
                    MiniMap,
                    Dagre
                ],
                // 插件配置
                pluginsOptions: {
                    miniMap: {
                        width: 200,
                        height: 100,
                        leftPosition: 5,
                        bottomPosition: 5,
                    },
                },
                // 画布容器节点
                container: this.$refs.container,
                // 画布背景配置
                grid: {
                    size: 18, // 点的密集程度
                    visible: true,
                    type: 'dot', // 'dot' | 'mesh'
                    config: {
                        color: '#e2e4ed', // 点的颜色
                        thickness: 1, // 点的大小
                    },
                },
                // 键盘快捷键配置
                keyboard: {
                    enabled: true,
                    shortcuts: [
                        {
                            keys: ['backspace'],
                            callback: () => {
                                customBackEvent({ lf, isEditName: this.isEditName, bus: this.bus });
                            }
                        }
                    ]
                },
                guards: {
                    // 删除节点和线前的回调函数
                    beforeDelete: (data) => {
                        if (this.openCheck) {
                            this.$message.error('调试模式不能删除');
                            return false;
                        }
                        if (['START', 'END'].includes(data?.type)) {
                            this.$message.error('开始和结束节点不能删除');
                            // 阻止删除
                            return false;
                        }
                        return true;
                    }
                },
                edgeTextDraggable: false, // 节点文本拖拽
                hoverOutline: false,
                // 连接线文本编辑配置
                edgeTextEdit: false,
                // 节点文本编辑配置
                nodeTextEdit: false,
            });
            this.lf = lf;
            this.$_registerNode();
            loadingCtrl.hide();
        },
        /**
         * @description: 注册自定义节点
         * @return {*}
         */
        $_registerNode() {
            this.lf.register(defaultEdge);
            this.lf.register(animationEdge);
            // 设置全局默认dege样式
            this.lf.setDefaultEdgeType('EDGE_BEZIER');
            registerStart(this.lf);
            registerEnd(this.lf);
            registerConditional(this.lf);
            registerIntention(this.lf);
            registerKnowledge(this.lf);
            registerLargeModel(this.lf);
            registerApi(this.lf);
            registerMessage(this.lf);
            registerProcess(this.lf);
            registerCode(this.lf);
            registerTextProcessor(this.lf);
            registerRewrite(this.lf);
            registerMemory(this.lf);
            registerWorkflowAgent(this.lf);
            // 渲染数据
            this.lf.render(this.flowData);
            this.$_LfEvent();
            this.lf.fitView();
            this.$nextTick(() => {
                // this.lf.extension.miniMap.show(); // 小地图
            });
        },
        /**
         * @description: 注册一系列的画布回调事件
         * @return {*}
         */
        $_LfEvent() {
            // 单击
            this.lf.on('node:click', (args) => {
                console.log(args, 'node:click');
                this.bus.$emit('node:click', args);
            });
            // 双击 编辑节点配置
            this.lf.on('node:dbclick', (args) => {
                if (this.openCheck) {
                    this.$refs.nodePanel?.closeCheckDialog();
                    return;
                }
                // 如果已经有节点在编辑，则告知其需要校验
                if (this.nodeDialogVisible) {
                    const { id } = this.clickNode || {};
                    if (id) {
                        this.bus.$emit('validateConfigById', { nodeId: id });
                    }
                }
                this.clickNode = args?.data || {};
                // this.bus.$emit('node:click', args);
                this.nodeDialogVisible = true;
            });
            // this.lf.on('edge:click', ({ data }) => {
            //     console.log('edge:click1', data);
            //     // this.clickNode = data;
            // });
            // this.lf.on('element:click', () => {
            //     // this.hideAddPanel();
            //     console.log('element:click');
            // });
            // 连线添加处理
            this.lf.on('edge:add', ({ data }) => {
                deleteTempOutputs();
                this.addEdgeCallback({ data });
            });
            // 锚点连线拖动连线成功
            this.lf.on('anchor:drop', ({ data, edgeModel }) => {
                const { meta = {} } = data || {};
                if (meta?.anchorIndex > -1) {
                    this.lf.getEdgeModelById(edgeModel?.id).setProperties({
                        anchorIndex: meta?.anchorIndex
                    });
                }
            });
            // this.lf.on('node:mousemove', ({ data, e }) => {
            //     e.preventDefault();
            //     // console.log('node:mousemove');
            //     this.moveData = data;
            // });
            // this.lf.on('node:dragstart', ({ e }) => {
            //     e.stopPropagation();
            //     console.log('node:dragstart', e);
            // });
            // 画布单击
            this.lf.on('blank:click', () => {
                console.log('blank:click');
                this.setNodeZIndex();
                this.closeNodeConfigDialog();
                this.clickNode = null;
                this.bus.$emit('node:click', null);
            });
            // 不允许连线时的处理，错误处理
            this.lf.on('connection:not-allowed', (data) => {
                this.$message.error(data.msg);
            });
            // 鼠标进入边，连线显示 关闭 按钮
            this.lf.on('edge:mouseenter', ({ data }) => {
                if (this.openCheck) {
                    return;
                }
                const edgeModel = this.lf.getEdgeModelById(data.id);
                edgeModel.setProperties({
                    showAddMark: true
                });
            });
            // 鼠标离开
            this.lf.on('edge:mouseleave', ({ data }) => {
                const edgeModel = this.lf.getEdgeModelById(data.id);
                edgeModel.setProperties({
                    showAddMark: false
                });
            });
            // 自定义事件，删除连线确认弹窗
            this.lf.on('custom:anchorClick', ({ edge }) => {
                customAnchorClickEvent({ edge, lf: this.lf, bus: this.bus });
            });
            // 删除节点事件
            this.lf.on('node:delete', ({ data }) => {
                if (this.nodeDialogVisible && this.clickNode.id === data.id) {
                    this.nodeDialogVisible = false;
                    this.clickNode = null;
                }
            });
        },
        /**
         * @description: 设置 'IF', 'INTENT' 类型的节点 zIndex 属性为 0
         * @return {*}
         */
        setNodeZIndex() {
            if (!this.clickNode) {
                return;
            }
            const { id, type } = this.clickNode || {};
            if (!['IF', 'INTENT'].includes(type)) {
                return;
            }
            const sourceNodeMode = this.lf.getNodeModelById(id);
            sourceNodeMode.zIndex = 0;
        },
        /**
         * @description: 锚点的添加单独处理
         * @param {*} data
         * @return {*}
         */
        addEdgeCallback({ data }) {
            const { sourceAnchorId, sourceNodeId, targetNodeId, id } = data || {};
            console.log(data);
            if (sourceAnchorId) {
                const splitSourceAnchorId = sourceAnchorId.split('__');
                if (splitSourceAnchorId.length === 3) {
                    const sourceNodeMode = this.lf.getNodeModelById(sourceNodeId);
                    const { type = '' } = sourceNodeMode || {};
                    // 分支器组件
                    if (type === 'IF') {
                        const { conditionList = [], defaultTargetNodes = [] } = sourceNodeMode.getProperties();
                        if (splitSourceAnchorId[1] === '-1') {
                            // 兜底分支
                            sourceNodeMode.setProperties({
                                defaultTargetNodes: [
                                    ...defaultTargetNodes,
                                    {
                                        nodeId: targetNodeId,
                                        edgeId: id
                                    }
                                ]
                            });
                        } else {
                            // 正常分支
                            // eslint-disable-next-line max-len
                            const targetIndex = conditionList.findIndex(item => item.conditionIndex === +splitSourceAnchorId[1]);
                            if (targetIndex > -1) {
                                // const { targetNodes = []} = conditionList[targetIndex];
                                conditionList[targetIndex].targetNodes = [
                                    // ...targetNodes,
                                    {
                                        nodeId: targetNodeId,
                                        edgeId: id
                                    }
                                ];
                                sourceNodeMode.setProperties({
                                    conditionList
                                });
                            }
                        }
                        console.log(sourceNodeMode.getProperties(), 'sourceNodeMode.getProperties()');
                    }
                    if (type === 'INTENT') {
                        const { intentItems = [], defaultTargetNodes = [] } = sourceNodeMode.getProperties();
                        if (splitSourceAnchorId[1] === '-1') {
                            sourceNodeMode.setProperties({
                                defaultTargetNodes: [
                                    ...defaultTargetNodes,
                                    {
                                        nodeId: targetNodeId,
                                        edgeId: id
                                    }
                                ]
                            });
                        } else {
                            // 正常分支
                            // eslint-disable-next-line max-len
                            const targetIndex = intentItems.findIndex(item => item.intentItemsIndex === +splitSourceAnchorId[1]);
                            if (targetIndex > -1) {
                                const { targetNodes = [] } = intentItems[targetIndex];
                                intentItems[targetIndex].targetNodes = [
                                    ...targetNodes,
                                    {
                                        nodeId: targetNodeId,
                                        edgeId: id
                                    }
                                ];
                                sourceNodeMode.setProperties({
                                    intentItems
                                });
                            }
                        }
                    }
                }
            }
        },
        /**
         * @description: 获取画布数据
         * @return {*}
         */
        $_catData() {
            const graphData = this.lf.getGraphData();
            console.log(JSON.stringify(graphData));
            return graphData;
        },
        /**
         * @description: 关闭节点编辑弹窗
         * @return {*}
         */
        closeNodeConfigDialog() {
            this.nodeDialogVisible = false;
            const { id } = this.clickNode || {};
            if (id) {
                this.bus.$emit('validateConfigById', { nodeId: id });
            }
        },
        /**
         * @description: 设置连接线高亮
         * @return {*}
         */
        changeLineColor() {
            const { nodes } = this.lf.getGraphData();
            let outEdges = [];
            nodes.forEach(item => {
                console.log(item.id, 'item.id');
                const itemEdges = this.lf.getNodeOutgoingEdge(item.id);
                if (itemEdges.length > 0) {
                    outEdges = [...outEdges, ...itemEdges];
                }
            });
            outEdges.forEach(edge => {
                this.lf.changeEdgeType(edge.id, 'EDGE_BEZIER_A');
            });
        },
        // 调试模式开关
        changeOpenCheck(openCheck) {
            this.openCheck = openCheck;
            this.nodeDialogVisible = false;
            this.clickNode = null;
            this.bus.$emit('node:click', null);
        },
        /**
         * @description: 保存画布轮训
         * @return {*}
         */
        saveFlowTimer() {
            // 画布数据有改动，需要保存
            const graphData = this.lf.getGraphData();
            if (JSON.stringify(graphData) !== JSON.stringify(this.flowData)) {
                this.flowData = graphData;
                this.saveFlow();
            }
            if (this.saveTimer) {
                this.clearSaveInterval();
            }
            // 每10s保存一次
            setTimeout(() => {
                this.saveFlowTimer();
            }, 10 * 1000);
        },
        /**
         * @description: 保存画布
         * @return {*}
         */
        saveFlow(status = 'draft') {
            // 调试状态下不保存 或者 没有appId不保存
            if (this.openCheck || !this.appId) {
                return;
            }
            // appid 不一致，不保存
            if (this.agentData?.appId !== this.appId) {
                // this.$message.error(`appId： ${this.appId} 不一致，保存失败`);
                return;
            }
            return new Promise((resolve, reject) => {
                const { edges, nodes = [] } = this.lf.getGraphData();
                let dealGraphNodes = [];
                const {
                    name = '',
                    remark = '',
                    openingLines = '',
                    referenceTurns = '',
                    suggestedQuestion = '',
                    memorySchema = '',
                } = this.agentData || {};
                // 遍历节点，处理API数组类型的变量值
                nodes.forEach(item => {
                    const { type, properties } = item || {};
                    if (type === 'API') {
                        let { inputVars = [], inputVarsAll = [] } = properties || {};
                        inputVars.map(sub => {
                            if (sub.varType.includes('Array') && sub.varType !== 'ArrayObject' && sub.varValue) {
                                sub.varValue = JSON.parse(sub.varValue);
                            } else if (sub.varType.includes('Object') && sub.varValue) {
                                sub.varValue = this.tryFixAndParse(sub.varValue);
                            // eslint-disable-next-line max-len
                            } else if (sub.varType === 'Boolean' && (sub.varValue === 'true' || sub.varValue === 'false')) {
                                sub.varValue = sub.varValue.toLowerCase() === 'true';
                            }
                        });
                        inputVarsAll.map(sub => {
                            if (sub.varType.includes('Array') && sub.varType !== 'ArrayObject' && sub.varValue) {
                                sub.varValue = JSON.parse(sub.varValue);
                            } else if (sub.varType.includes('Object') && sub.varValue) {
                                sub.varValue = this.tryFixAndParse(sub.varValue);
                            // eslint-disable-next-line max-len
                            } else if (sub.varType === 'Boolean' && (sub.varValue === 'true' || sub.varValue === 'false')) {
                                sub.varValue = sub.varValue.toLowerCase() === 'true';
                            }
                        });
                        dealGraphNodes.push(item);
                    } else {
                        dealGraphNodes.push(item);
                    }
                });
                const postData = {
                    name,
                    remark,
                    openingLines,
                    referenceTurns,
                    suggestedQuestion: suggestedQuestion,
                    appId: this.appId,
                    config: JSON.stringify({
                        edges,
                        nodes: dealGraphNodes
                    }),
                    status,
                    memorySchema,
                };
                flowRequest({
                    url: saveAgent,
                    method: 'post',
                    data: postData
                }).then(res => {
                    resolve(res);
                }).catch((error) => {
                    reject(error);
                });
            });
        },
        /**
         * @description: 更新agent数据
         * @param {object} data
         * @return {*}
         */
        changeAgentData(data) {
            this.agentData = data;
            const { memorySchema = '' } = this.agentData;
            setMemorySchemaList(memorySchema ? JSON.parse(memorySchema) : []);
            this.$emit('syncAgentData', this.agentData);
        },
        /**
         * @description: 修改记忆变量数据
         * @param {object} data
         * @return {*}
         */
        updataMemory(data) {
            const { id, varName } = data || {};
            updateReferenceVarNameById({
                nodeId: '3',
                lf: this.lf,
                varNameId: id,
                updateVarName: varName
            });
            deleteTempOutputs();
        },
        /**
         * @description: 删除记忆变量数据
         * @param {object} data
         * @return {*}
         */
        deleteMemory(data) {
            const { id } = data[0] || {};
            deleteReferenceVarNameById({
                nodeId: '3',
                lf: this.lf,
                varNameId: id
            });
            deleteTempOutputs();
        },
        /**
         * @description: 自动修复object字符串并解析
         * @param {object} str 字符串内容
         * @return {*}
         */
        tryFixAndParse(str) {
            try {
                // 先尝试直接解析
                return JSON.parse(str);
            } catch (error) {
                try {
                    // 尝试修复：给键名添加双引号
                    const fixed = str.replace(/(\w+):/g, '"$1":');
                    return JSON.parse(fixed);
                } catch (secondError) {
                    return;
                }
            }
        }

    }
};
</script>
<style lang="less" scoped>
.flow-view {
    position: relative;
    width: 100%;
    height: 100%;
    .logic-flow-container {
        width: 100%;
        height: 100%;
        outline: none;
    }
    ::v-deep {
        foreignObject {
            overflow: unset;
        }
        .lf-dnd-item {
          margin: 0;
          padding: 10px;
          display: flex;
          align-items: center;
          justify-content: flex-start;
        }
        .lf-dnd-shape {
          background-size: 30px;
          margin: 0 10px 0 0;
        }
        .lf-control {
          right: 0px;
          box-shadow: none;
          filter: drop-shadow(2px 2px 6px rgba(0, 0, 0, 0.1));
        }
        .lf-node {
          filter: drop-shadow(0px 1px 2px rgba(0, 0, 0, 0.2));
        }
        .lf-graph {
          background: rgb(248, 249, 252);
        }
        .lf-edge-append {
          pointer-events: none;
        }
        .lf-mini-map {
          background: #ebedf1;
          border: none;
        }
        .lf-minimap-viewport {
          background: rgba(48, 48, 48, 0.1);
        }
    }
}
.node-config-drawer {
    /deep/ .ant-drawer-content {
        transform: none !important; /* 禁用 transform 影响 */
        overflow: visible !important;
    }
}
</style>