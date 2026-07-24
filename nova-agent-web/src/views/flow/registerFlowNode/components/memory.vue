<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-09-04 16:15:11
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-24 16:49:10
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/memory.vue
 * @Description: 记忆变量 memory
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
        :ref="`memory_${getNodeId}`"
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
            :node-icon="require('../../image/memory.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :mode-text="mod === 'write' ? '写入' : '读取'"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <!-- 输入字段显示 -->
        <queryInfo
            v-show="mod === 'write'"
            info-title="写入变量"
            :info-data="inputVars"
            :arr-args="arrArgs"
            :input-expanded="inputVarsShows"
            @updateNodeAttributes="inputVarsShow"
        />
        <queryInfo
            v-show="mod === 'read'"
            info-title="读取变量"
            :info-data="readVars"
            :arr-args="memorySchemaList"
            :input-expanded="inputVarsShows"
            @updateNodeAttributes="inputVarsShow"
        />
        <varsTree
            v-show="allShow && mod === 'write'"
            :tree-data="outputVars"
            :replace-fields="replaceFields"
            title="输出"
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
import nodeTitle from '../commonComponents/nodeTitle.vue';
import { memorySchemaList } from '@/views/flow/common/modelList';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import { getAllArgs } from '@/views/flow/getArgs';
import queryInfo from '../commonComponents/queryInfo.vue';
import validateMemoryNode from '../../validateUtils/validateMemoryNode';
import outputs from '../commonComponents/outputs.vue';
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
        queryInfo,
        outputs
    },
    data() {
        return {
            isSelected: false, // 是否选中当前节点
            allShow: true, // 是否全部展开
            inputVarsShows: true,
            validateStatus: true, // 校验状态
            errResult: [], // 校验错误提示
            mod: 'write', // 模式
            inputVars: [], // 输入变量
            outputVars: [], // 输出变量
            readVars: [], // 读取变量
            replaceFields: {
                children: 'children',
                title: 'varName',
                key: 'id'
            },
            arrArgs: [], // 系统参数树
            memorySchemaList, // 记忆变量树状列表
            outputsData: {}, // 输出面板数据
            showOutputs: false, // 是否展示输出
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
    },
    methods: {
        /**
         * @description: bus监听
         * @return {*}
         */
        initBus() {
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getNodeId) {
                    const data = validateMemoryNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = data.validateStatus;
                    this.errResult = data.errResult;
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
            this.bus.$on('editNodeName', ({ isEditName = false, nodeName, nodeId }) => {
                // 节点名称编辑成功后的回调
                if (!isEditName) {
                    if (this.nodeNameMap[nodeId]) {
                        this.$set(this.nodeNameMap, nodeId, `${nodeName}/`);
                        this.$forceUpdate();
                    }
                }
            });
        },
        /**
         * @description: 初始化方法
         * @return {*}
         */
        init() {
            this.arrArgs = getAllArgs({ nodeId: this.getNodeId, lf: this.lf });
            const { mod = 'write', inputVars = [], outputVars = [], readVars = [] } = this.propertiesData;
            this.mod = mod;
            this.inputVars = inputVars;
            this.outputVars = outputVars;
            this.readVars = readVars;
        },
        /**
         * @description: 切换全部折叠
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.inputVarsShows = allShow;
            this.updateNodeAttributes(allShow);
        },
        /**
         * @description: 切换输入变量折叠
         * @return {*}
         */
        inputVarsShow(expand) {
            this.inputVarsShows = expand;
            this.updateNodeAttributes(expand);
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        },
        /**
         * @description: 更新节点高度和锚点的位置
         * @param {*} expand
         * @return {*}
         */
        updateNodeAttributes(expand) {
            this.$nextTick(() => {
                const clientHeight = this.$refs[`memory_${this.getNodeId}`]?.clientHeight;
                const edgeModel = this.lf.getNodeModelById(this.getNodeId);
                if (clientHeight > 0) {
                    edgeModel.setCustomAttributes({ currentHeight: clientHeight, expand: expand });
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
            const { validateStatus, errResult } = validateMemoryNode({
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
    },
    mounted() {
        this.initBus();
        this.init();
    },
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-container-hide {
    /deep/ .node-title {
        margin-bottom: 0px;
    }
}
</style>