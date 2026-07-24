<template>
    <div class="container">
        <a-form
            :form="seniorConfigForm"
            class="intent-form"
        >
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
                        :click-node="clickNode"
                        :lf="lf"
                        @changeInputData="changeInputData"
                        ref="inputVarsForm"
                    ></InputComponents>
                </div>
            </div>
            <varsTree
                :tree-data="outputList"
                title="输出"
            />
        </a-form>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs } from '@/views/flow/getArgs';
import { processTreeData, checkReferenceVarName } from '@/views/flow/common/common';
import InputComponents from './components/InputComponents';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
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
            seniorConfigForm: this.$form.createForm(this), // 表单实例
            inputVars: [], // 输入变量
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            allFlatArgs: [],
            outputList: []
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
        varsTree
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
            const inputVarsData = [
                {
                    varName: 'rawQuery',
                    varType: 'reference',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            const { inputVars, outputVars } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({ inputVars, allFlatArgs: this.allFlatArgs });
            } else {
                this.inputVars = inputVarsData;
            }
            this.outputList = outputVars;
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
                this.seniorConfigForm?.validateFields();
                this.$refs.inputVarsForm?.inputForm?.validateFields();
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
                mode: this.mode,
                answerTemplate: this.answerTemplate,
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
.container {
    .container-title {
        color: #151b26;
        font-size: 14px;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 5px;
        margin-bottom: 10px;
    }

    .mode-radio-group {
        .ant-radio-wrapper {
            font-size: 12px;
            box-sizing: border-box;

            .radio-tooltip {
                margin-left: 5px;
            }
        }
    }

    .intention-input {
        font-size: 12px;
    }

    .intent-form {
        height: auto;
        margin-top: 10px;

        /deep/ .ant-form-item {
            margin-bottom: 10px;

            .ant-form-item-control {
                line-height: 24px;

                .ant-form-explain {
                    font-size: 12px;
                }
            }
        }
    }
    .message-answer {
        margin-top: 10px;
        .required-tip {
            color: red;
        }
    }
}
</style>