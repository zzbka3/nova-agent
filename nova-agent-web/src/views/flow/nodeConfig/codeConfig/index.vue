<!--
 * @Author: hewenquan
 * @Date: 2025-08-11 11:30:11
 * @LastEditTime: 2025-11-03 14:50:11
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/codeConfig/index.vue
 * @Description: 代码编辑器
-->
<template>
    <div class="code-config">
        <InputComponents
            v-if="inputVars && inputVars.length > 0"
            :var-type-options="varTypeOptions"
            :input-value-options="arrArgs"
            :all-flat-args="allFlatArgs"
            :input-data="inputVars"
            :is-dynamics="true"
            :is-output="false"
            :click-node="clickNode"
            :lf="lf"
            @changeInputData="changeInputData"
            ref="inputVarsForm"
        ></InputComponents>
        <div
            class="code-wrapper"
        >
            <div class="code-editor-title">
                代码&nbsp;
                <a-tooltip
                    title="使用输入参数中的变量，构建函数功能。需要通过 return 一个对象来输出结果。可参考示例代码编写一个函数。（运行环境: Python3； 预置 Package：NumPy；）"
                >
                    <a-icon type="question-circle-o" />
                </a-tooltip>
            </div>
            <myCodeMirror
                class="code-preview"
                :code-data.sync="codeData"
                :read-only="true"
                ref="myCodeMirror"
            />
            <div
                class="code-editor-btn"
                @click="showEditCode = true"
            >
                编辑代码
            </div>
        </div>
        <treeOutput
            v-if="outputVars && outputVars.length > 0"
            :var-type-options="varTypeOptions"
            :input-data="outputVars"
            :click-node="clickNode"
            :lf="lf"
            @valueChange="changeOutputData"
            ref="outputVarsForm"
        />
        <editCode
            v-if="showEditCode"
            :code-data.sync="codeData"
            @codeDataChange="codeDataChange"
            @close="closeEditCode"
            :input-vars="inputVars"
        />
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs, deleteTempOutputs } from '@/views/flow/getArgs';
import { processTreeData, checkReferenceVarName } from '@/views/flow/common/common';
import InputComponents from '../components/InputComponents';
import treeOutput from '../components/treeOutput';
import myCodeMirror from '@/views/flow/nodeConfig/components/myCodeMirror';
import editCode from './editCode.vue';
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
            outputVars: [], // 输出变量
            allFlatArgs: [],
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
                { label: 'Integer', value: 'Integer' },
                { label: 'Boolean', value: 'Boolean' },
                { label: 'Number', value: 'Number' },
                { label: 'Any', value: 'Any' },
            ],
            codeData: '', // 代码数据
            showEditCode: false, // 显示编辑代码
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
        myCodeMirror,
        editCode,
        treeOutput
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
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            const defaultVarsData = [
                {
                    varName: 'query',
                    varType: 'reference',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            const {
                inputVars = [],
                outputVars = [],
                codeData = ''
            } = this.propertiesData;
            // 输入
            if (inputVars && inputVars.length > 0) {
                // 检查引用值是否存在
                this.inputVars = checkReferenceVarName({inputVars, allFlatArgs: this.allFlatArgs});
            } else {
                this.inputVars = JSON.parse(JSON.stringify(defaultVarsData));
            }
            // 输入
            if (outputVars && outputVars.length > 0) {
                // 检查引用值是否存在
                this.outputVars = checkReferenceVarName({inputVars: outputVars, allFlatArgs: this.allFlatArgs});
            } else {
                this.outputVars = JSON.parse(JSON.stringify(defaultVarsData));
            }
            this.codeData = codeData;
            this.initValidate();
        },
        /**
         * 初始化验证函数
         *
         * 该函数在延迟100毫秒后执行，用于初始化表单验证。
         * 首先打印当前表单的值，然后验证表单字段，最后打印输入变量表单的验证结果。
         */
         initValidate() {
            setTimeout(() => {
                this.$refs.inputVarsForm?.inputForm?.validateFields();
                this.$refs.outputVarsForm?.treeOutputForm?.validateFields();
            }, 300);
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
                outputVars: this.outputVars,
                codeData: this.codeData
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
        // 监听输入变量变化
        changeInputData(value) {
            this.inputVars = value;
            this.updateLf();
        },
        // 监听输出变量变化
        changeOutputData(value) {
            this.outputVars = value;
            this.updateLf();
            deleteTempOutputs();
        },
        codeDataChange(codeData) {
            this.codeData = codeData;
            this.updateLf();
        },
        closeEditCode() {
            this.updateLf();
            this.showEditCode = false;
        }
    },
    mounted() {
        this.init();
    },
};
</script>

<style lang="less" scoped>
.code-config {
    .code-wrapper {
        border-top: 1px solid #e8e9eb;
        border-bottom: 1px solid #e8e9eb;
        padding: 10px 0;
        margin-bottom: 10px;
        .code-editor-title {
            margin-bottom: 8px;
            margin-right: 10px;
            font-weight: bold;
            color: #151b26;
            font-size: 14px;
        }
        .code-preview {
            width: 100%;
            height: 200px;
        }
        .code-editor-btn {
            margin-top: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 28px;
            color: #151b26;
            border: 1px solid #e8e9eb;
            font-size: 12px;
            border-radius: 6px;
            cursor: pointer;
            &:hover {
                color: #528EFF;
                border-color: #528EFF;
                background: rgb(255, 255, 255);
            }
        }
    }
}
</style>