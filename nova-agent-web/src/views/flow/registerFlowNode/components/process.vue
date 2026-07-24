<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`process_${getNodeId}`"
    >
        <nodeOperate
            v-if="isSelected"
            :model="model"
            :lf="lf"
            @editNode="handleEditNode"
        />
        <nodeTitle
            :ref="`nodeTitleRef_${getNodeId}`"
            :model="model"
            :node-name="getNodeName"
            :node-icon="require('../../image/workFlow.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <!-- 输出字段显示 -->
        <queryInfo
            v-show="allShow"
            info-title="输出"
            :info-data="inputVars"
            :arr-args="arrArgs"
            @updateNodeAttributes="updateNodeAttributes"
        />
        <nodeTemplate
            v-show="allShow"
            template-title="IM 反馈消息"
            :template-text="msg"
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
import validateProcessNode from '../../validateUtils/validateProcessNode';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import outputs from '../commonComponents/outputs.vue';
import queryInfo from '../commonComponents/queryInfo.vue';
import nodeTemplate from '../commonComponents/nodeTemplate.vue';

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
    components: {
        nodeOperate,
        nodeTitle,
        outputs,
        queryInfo,
        nodeTemplate
    },
    data() {
        return {
            isSelected: false, // 当前节点是否选中
            validateStatus: true, // 节点配置校验状态
            errResult: [], // 校验错误提示
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            inputVars: [], // 输入变量数据
            msg: '', // 回答模板
            allShow: true, // 是否全部展示
            arrArgs: []
        };
    },
    computed: {
        getNodeId() {
            const graph = this.model;
            return graph.id;
        },
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
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                console.log(nodeId, '开始校验nodeId');
                if (nodeId === this.getNodeId) {
                    this.updateNodeAttributes();
                    const data = validateProcessNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = data.validateStatus;
                    this.errResult = data?.errResult;
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
            const { validateStatus, errResult } = validateProcessNode({
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
                inputVars,
                msg = '',
            } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = inputVars;
            } else {
                this.inputVars = inputVarsData;
            }
            this.msg = msg || '';
        },
        /**
         * 切换全部折叠状态
         * @return {*}
         */
        toggleFoldAll(expand) {
            this.allShow = !this.allShow;
            this.updateNodeAttributes(expand);
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs[`nodeTitleRef_${this.getNodeId}`].editNodeName();
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
         updateNodeAttributes(expand) {
            console.log('updateNodeAttributes', 'object');
            this.$nextTick(() => {
                const clientHeight = this.$refs[`process_${this.getNodeId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getNodeId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({currentHeight: clientHeight, expand: expand});
                }
            });
        },
    },
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
</style>