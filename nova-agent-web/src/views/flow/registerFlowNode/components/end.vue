<!--
 * @Author: hewenquan
 * @Date: 2025-06-19 16:09:06
 * @LastEditTime: 2025-10-24 16:49:00
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/end.vue
 * @Description: ProgressNode
-->
<template>
    <div
        :class="[
            'node-container end-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`end_${getNodeId}`"
    >
        <nodeTitle
            :node-name="getNodeName"
            :node-icon="require('../../image/end.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            :model="model"
            :all-show="allShow"
            @toggleFoldAll="toggleFoldAll"
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
            template-title="回复模版"
            :mode="mode"
            :template-text="answerTemplate"
        />
        <!-- 回复模版 -->
        <outputs
            :outputs-data="outputsData"
            v-if="showOutputs"
        />
    </div>
</template>
<script>
import nodeTitle from '../commonComponents/nodeTitle.vue';
import queryInfo from '../commonComponents/queryInfo.vue';
import nodeTemplate from '../commonComponents/nodeTemplate.vue';
import validateEndNode from '../../validateUtils/validateEndNode';
import LogicFlow from '@logicflow/core';
import outputs from '../commonComponents/outputs.vue';
import { getAllArgs } from '@/views/flow/getArgs';
export default {
    data() {
        return {
            isSelected: false, // 当前节点是否选中
            validateStatus: true, // 节点配置校验状态
            errResult: [], // 校验错误提示
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
            inputVars: [], // 输入变量数据
            answerTemplate: '', // 回答模板
            mode: 'directVar', // template 模板，directVar 直接输出参数
            allShow: true, // 是否全部展示
            arrArgs: [], // 参数列表
        };
    },
    props: {
        model: {
            type: Object,
            default: () => ({}),
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    components: {
        nodeTitle,
        outputs,
        queryInfo,
        nodeTemplate
    },
    mounted() {
        this.initBus();
        this.init();
    },
    computed: {
        // 当前节点ID
        getNodeId() {
            return this.model?.id;
        },
        // 获取节点名称
        getNodeName() {
            return this.model?.properties?.nodeName;
        },
        // 获取节点属性数据
        propertiesData() {
            return this.model.properties || {};
        },
    },
    methods: {
        initBus() {
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            // 校验
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$once('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getNodeId) {
                    this.updateNodeAttributes();
                    const { validateStatus, errResult } = validateEndNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = validateStatus;
                    this.errResult = errResult;
                    console.log(3);
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
                answerTemplate = '',
                mode = ''
            } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = inputVars;
            } else {
                this.inputVars = inputVarsData;
            }
            this.answerTemplate = answerTemplate || '';
            this.mode = mode;
        },
        /**
         * 校验流程子节点
         *
         * 调用该方法后，会触发'childValidateDone'事件，并传递节点ID和校验状态作为参数。
         *
         * @returns 无返回值
         */
         validateFlowChild() {
            const { validateStatus, errResult } = validateEndNode({
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
            console.log(this.getNodeId, errResult, validateStatus, 'validateEnd');
            this.bus.$emit('childValidateDone', {
                nodeId: this.getNodeId,
                validateStatus, // 校验是否通过
                errResult
            });
        },
        /**
         * 切换折叠/展开所有内容
         */
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
                const clientHeight = this.$refs[`end_${this.getNodeId}`]?.clientHeight;
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
</style>