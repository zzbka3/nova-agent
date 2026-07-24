<template>
    <div class="text-container">
        <a-form
            :form="seniorConfigForm"
            class="intent-form"
        >
            <div class="text-container-title">
                <div class="text-container-title-text">
                    处理模式
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
                    :disabled="true"
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
            >
                <div class="message-answer">
                    <div class="text-container-title">
                        <div class="text-container-title-text">
                            字符串拼接
                        </div>
                    </div>
                    <div
                        class="demos-item"
                    >
                        <a-textarea
                            :placeholder="'请输⼊字符串拼接语句，可使⽤{{参数名}}的⽅式引⽤输⼊参数'"
                            :auto-size="{ minRows: 6 }"
                            @change="answerTemplateChange"
                            class="intention-input"
                            size="small"
                            :max-length="1000"
                            v-decorator="['template', {
                                rules: [
                                    { required: true, message: '请输入字符串拼接语句' }
                                ]
                            }]"
                        />
                    </div>
                </div>
            </a-form-item>
        </a-form>
        <varsTree
            :tree-data="outputVars"
            :show-common-area="false"
            title="输出"
        />
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
            // 模式选项
            modeOption: [
                {
                    label: '字符串拼接',
                    value: 'CONCAT',
                },
                {
                    label: '字符串分隔',
                    value: 'SPLIT',
                }
            ],
            // 参数类型选项
            varTypeOption, // 参数类型选项
            arrArgs: [], // 系统参数树
            seniorConfigForm: this.$form.createForm(this), // 表单实例
            mode: 'CONCAT', // 模式
            inputVars: [], // 输入变量
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            allFlatArgs: [],
            template: '', // 拼接模版
            outputVars: [],
            joiner: '', // 分割字段
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
                    varName: 'query',
                    varType: 'reference',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            const { mode, inputVars, template, outputVars = '' } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({inputVars, allFlatArgs: this.allFlatArgs});
            } else {
                this.inputVars = inputVarsData;
            }
            this.mode = mode || 'CONCAT';
            this.template = template || '';
            this.outputVars = outputVars;
            this.seniorConfigForm.setFieldsValue({
                template: template
            });
            this.initValidate();
        },
        answerTemplateChange(e) {
            this.template = e.target.value;
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
                template: this.template,
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
.text-container {
    .senior-form-item {
        margin-bottom: 0px;
    }
    .text-container-title {
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
    .message-answer {
        margin-top: 10px;
        .required-tip {
            color: red;
        }
    }
}
</style>