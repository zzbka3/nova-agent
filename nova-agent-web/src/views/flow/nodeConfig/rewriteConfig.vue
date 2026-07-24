<template>
    <div class="text-container">
        <a-form
            :form="seniorConfigForm"
            class="intent-form"
        >
            <!-- 输入 -->
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
                        :is-dynamics="false"
                        :click-node="clickNode"
                        :lf="lf"
                        @changeInputData="changeInputData"
                        ref="inputVarsForm"
                    ></InputComponents>
                </div>
            </div>
            <div class="config-area">
                <div class="text-container-title">
                    <div class="text-container-title-text">
                        改写配置
                    </div>
                </div>
                <a-form-item
                    size="small"
                    :required="true"
                    class="senior-form-item"
                >
                    <template
                        #label
                    >
                        <span class="temperature-label">多样性</span>
                        <a-tooltip>
                            <template slot="title">
                                控制模型输出的多样性，数值越高输出内容的差异性越大。推荐设置0.0001，可保证输出的效果稳定（设置为0可能导致模型响应变慢）
                            </template>
                            <a-icon
                                type="question-circle"
                                class="question-circle"
                            />
                        </a-tooltip>
                    </template>
                    <a-row>
                        <a-col :span="12">
                            <a-slider
                                class="temperature-slider"
                                size="small"
                                v-model="temperature"
                                :min="0"
                                :max="1"
                                :step="0.0001"
                                @change="updateLf()"
                            />
                        </a-col>
                        <a-col :span="4">
                            <a-input-number
                                size="small"
                                v-model="temperature"
                                :min="0"
                                :max="1"
                                :step="0.0001"
                                class="input-number-box"
                                @change="updateLf()"
                            />
                        </a-col>
                    </a-row>
                </a-form-item>
                <a-form-item
                    size="small"
                    :required="true"
                    class="senior-form-item"
                >
                    <template
                        #label
                    >
                        <span class="temperature-label">rewrite_type</span>
                        <a-tooltip>
                            <template slot="title">
                                控制改写query时参考的历史对话内容
                            </template>
                            <a-icon
                                type="question-circle"
                                class="question-circle"
                            />
                        </a-tooltip>
                    </template>
                    <a-select
                        size="small"
                        placeholder="请选择rewrite_type"
                        v-model="rewriteType"
                        option-label-prop="label"
                    >
                        <a-select-option
                            v-for="option in rewriteTypeOptions"
                            :key="option.label"
                            :value="option.value"
                            :label="option.label"
                        >
                            {{ option.label }}
                        </a-select-option>
                    </a-select>
                </a-form-item>
            </div>
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
            varTypeOption, // 参数类型选项
            arrArgs: [], // 系统参数树
            seniorConfigForm: this.$form.createForm(this), // 表单实例
            inputVars: [], // 输入变量
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            allFlatArgs: [],
            outputVars: [],
            temperature: 0, // 多样性
            rewriteType: 0, // 改写类型
            // 改写类型枚举
            rewriteTypeOptions: [
                { label: '仅参考历史问题', value: 0 },
                { label: '参考历史问答', value: 1 },
            ],
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
            const { temperature = 0, rewriteType = 1, inputVars, outputVars = '' } = this.propertiesData;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({ inputVars, allFlatArgs: this.allFlatArgs });
            } else {
                this.inputVars = inputVarsData;
            }
            this.outputVars = outputVars;
            this.temperature = temperature;
            this.rewriteType = rewriteType;
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
    /deep/ .senior-form-item {
        margin-bottom: 15px;
        display: flex;
        .ant-form-item-label {
            width: 40%;
            line-height: 24px;
        }
        .ant-form-item-control-wrapper {
            width: 70%;
            .ant-form-item-control {
                line-height: 24px;
                .ant-form-explain {
                    font-size: 12px;
                }
            }
        }
        /* form 标签 */
        label {
            display: flex;
            align-items: center;
            .temperature-label {
                position: relative;
                margin: 0 0px 0 2px
            }
            .question-circle {
                font-size: 12px;
                margin-left: 4px;
            }
        }
        .temperature-slider {
            margin: 6px;
        }
        .input-number-box {
            margin-left: 10px;
            font-size: 12px;
        }
    }
    .config-area {
        margin: 10px 0;
        padding: 10px 0;
        border-top: 1px solid #e8e9eb;
        border-bottom: 1px solid #e8e9eb;
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
}
</style>