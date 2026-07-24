<template>
    <div class="container">
        <a-form
            :form="seniorConfigForm"
            class="intent-form"
        >
            <div class="container-title">
                <div class="container-title-text">
                    回复模式
                </div>
            </div>
            <a-form-item
                size="small"
                class="senior-form-item"
            >
                <a-radio-group
                    name="radioGroup"
                    class="mode-radio-group"
                    v-model="mode"
                    size="small"
                    @change="updateLf()"
                >
                    <a-radio
                        :value="item.value"
                        v-for="item in modeOption"
                        :key="item.value"
                    >
                        <span>{{ item.label }}</span>
                    </a-radio>
                </a-radio-group>
            </a-form-item>
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
                        :click-node="clickNode"
                        :lf="lf"
                        @changeInputData="changeInputData"
                        ref="inputVarsForm"
                    ></InputComponents>
                </div>
            </div>
            <a-form-item
                size="small"
                class="senior-form-item"
                v-show="mode === 'template'"
            >
                <div class="message-answer">
                    <div class="container-title">
                        <div class="container-title-text">
                            <span class="required-tip">*</span> 消息模板
                        </div>
                    </div>
                    <div
                        class="demos-item"
                    >
                        <a-textarea
                            :placeholder="'请填写向用户发送的消息模板内容，通过插入 {{ 参数名 }} 可以引用对应的输出参数'"
                            :auto-size="{ minRows: 6 }"
                            @change="answerTemplateChange"
                            class="intention-input"
                            size="small"
                            :max-length="1000"
                            v-decorator="['answerTemplate', {
                                rules: [
                                    { required: true, message: '消息模版不能为空' }
                                ]
                            }]"
                        />
                    </div>
                </div>
            </a-form-item>
        </a-form>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs } from '@/views/flow/getArgs';
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
            // 模式选项
            modeOption: [
                {
                    label: '按模版配置格式返回文本',
                    value: 'template',
                },
                {
                    label: '直接返回参数值',
                    value: 'directVar',
                }
            ],
            // 参数类型选项
            varTypeOption, // 参数类型选项
            arrArgs: [], // 系统参数树
            seniorConfigForm: this.$form.createForm(this), // 表单实例
            mode: 'template', // 模式
            inputVars: [], // 输入变量
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            allFlatArgs: [],
            answerTemplate: '', // 模板消息
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
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            const inputVarsData = [
                {
                    varName: 'query',
                    varType: 'reference',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            const { mode, inputVars, answerTemplate } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({ inputVars, allFlatArgs: this.allFlatArgs });
            } else {
                this.inputVars = inputVarsData;
            }
            this.mode = mode || 'template';
            this.answerTemplate = answerTemplate || '';
            this.seniorConfigForm.setFieldsValue({
                answerTemplate: answerTemplate
            });
            this.initValidate();
        },
        answerTemplateChange(e) {
            this.answerTemplate = e.target.value;
            this.updateLf();
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
            console.log('updateLf1', this.inputVars);
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