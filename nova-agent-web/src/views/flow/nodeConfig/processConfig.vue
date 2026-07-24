<template>
    <div class="process-container">
        <!-- 输出 -->
        <div
            class="input-vars"
            v-if="inputVars && inputVars.length > 0"
        >
            <div
                class="connect-config-item"
            >
                <InputComponents
                    :var-type-options="varTypeOptions"
                    :input-value-options="arrArgs"
                    :all-flat-args="allFlatArgs"
                    :input-data="inputVars"
                    :is-dynamics="true"
                    :is-output="true"
                    :custom-input="true"
                    :custom-input-config="workFlowVarsConfig"
                    :click-node="clickNode"
                    :lf="lf"
                    @changeInputData="changeInputData"
                ></InputComponents>
            </div>
        </div>

        <div
            class="input-vars"
        >
            <div class="container-title">
                <div class="container-title-text">
                    IM 反馈信息
                </div>
            </div>
            <div
                class="demos-item"
            >
                <a-textarea
                    v-model="msg"
                    placeholder="请输入在后台执行接的过程中，前端可以先反馈的提示信息"
                    :auto-size="{ minRows: 6 }"
                    @change="updateLf()"
                    class="intention-input"
                    size="small"
                    :max-length="1000"
                />
            </div>
        </div>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs, extraArgs } from '@/views/flow/getArgs';
import { processTreeData, checkReferenceVarName } from '@/views/flow/common/common';
import InputComponents from './components/InputComponents';
export default {
    props: {
        clickNode: {
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
            // 参数类型选项
            varTypeOption, // 参数类型选项
            arrArgs: [], // 系统参数树
            inputVars: [], // 输入变量
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
                { label: 'Integer', value: 'Integer' },
            ],
            allFlatArgs: [],
            msg: '', // 模板消息
            bailingVars: []
        };
    },
    computed: {
        // 监听点击节点数据
        propertiesData() {
            return this.clickNode.properties || {};
        }
    },
    watch: {
        // 监听点击节点数据
        propertiesData() {
            this.init();
        }
    },
    components: {
        InputComponents,
    },
    methods: {
        /**
         * @description: 初始化函数
         * @return {*}
         */
        init() {
            const { id } = this.clickNode;
            // 获取系统参数树（全部可选参数）
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.workFlowVarsConfig = processTreeData([extraArgs?.workFlowVarsConfig], this.$createElement);
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            const inputVarsData = [
                {
                    varName: null,
                    varType: 'reference',
                    varValue: null,
                    referenceNodeId: '',
                    referenceVarName: null,
                    referenceVarType: '',
                }
            ];
            const { inputVars, msg } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({inputVars, allFlatArgs: this.allFlatArgs});
            } else {
                this.inputVars = inputVarsData;
            }
            this.msg = msg || '';
        },
        /**
         * @description: 更新逻辑流图
         * @return {*}
         */
        updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            edgeModel.setProperties({
                inputVars: this.inputVars,
                msg: this.msg,
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
        changeInputData(value) {
            this.inputVars = value;
            this.updateLf();
        },

    },
    mounted() {
        this.init();
    },
};
</script>
<style lang="less" scoped>
.process-container {
    .container-title {
        color: #151b26;
        font-size: 14px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 5px;
        margin-bottom: 10px;
    }
}
</style>