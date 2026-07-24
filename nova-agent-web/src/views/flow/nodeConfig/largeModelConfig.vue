<template>
    <div class="container">
        <div class="container-title">
            <div class="container-title-text">
                模型
            </div>
        </div>
        <div class="senior-config">
            <a-form
                :form="seniorConfigForm"
                class="intent-form"
            >
                <a-form-item
                    label="选择模型"
                    size="small"
                    :required="true"
                    :validate-status="modelWarning ? 'warning' : ''"
                    :help="modelWarning ? modelWarningText : modelHelpText"
                    class="senior-form-item"
                >
                    <a-select
                        size="small"
                        placeholder="请选择模型"
                        v-model="intentModel"
                        option-label-prop="label"
                        class="model-select intention-select"
                        @change="seniorConfigChange('model', $event)"
                        show-search
                    >
                        <a-select-opt-group
                            v-for="group in modelOption"
                            :key="group.modelCode"
                        >
                            <span
                                class="option-group-label"
                                slot="label"
                            >
                                <span>{{ group.modelName }}</span>
                            </span>
                            <a-select-option
                                class="option-box"
                                v-for="option in group.children"
                                :key="option.modelCode"
                                :value="option.modelCode"
                                :label="option.modelName"
                            >
                                <span
                                    class="option-img-box"
                                    role="img"
                                    :aria-label="option.modelName"
                                >
                                    <img
                                        class="option-img"
                                        :src="option.icon"
                                    >
                                </span>
                                <a-tooltip
                                    :title="option.modelName"
                                >
                                    {{ option.modelName }}
                                </a-tooltip>
                            </a-select-option>
                        </a-select-opt-group>
                    </a-select>
                    <!-- <a-icon
                        type="reload"
                        class="reload-icon"
                    /> -->
                </a-form-item>
                <a-form-item
                    label="备选模型"
                    size="small"
                    class="senior-form-item"
                >
                    <a-select
                        size="small"
                        placeholder="请选择备选模型"
                        v-model="fallbackModelName"
                        option-label-prop="label"
                        class="model-select intention-select"
                        @change="seniorConfigChange('fallbackModelName', $event)"
                        :allow-clear="true"
                        show-search
                    >
                        <a-select-opt-group
                            v-for="group in modelOption"
                            :key="group.modelName"
                        >
                            <span
                                class="option-group-label"
                                slot="label"
                            >
                                <span>{{ group.modelName }}</span>
                            </span>
                            <a-select-option
                                class="option-box"
                                v-for="option in group.children"
                                :key="option.modelCode"
                                :value="option.modelCode"
                                :label="option.modelName"
                            >
                                <span
                                    class="option-img-box"
                                    role="img"
                                    :aria-label="option.modelName"
                                >
                                    <img
                                        class="option-img"
                                        :src="option.icon"
                                    >
                                </span>
                                <a-tooltip
                                    :title="option.modelName"
                                >
                                    {{ option.modelName }}
                                </a-tooltip>
                            </a-select-option>
                        </a-select-opt-group>
                    </a-select>
                </a-form-item>
                <a-form-item
                    size="small"
                    :required="true"
                    class="temperature-form-item senior-form-item"
                >
                    <template #label>
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
                    class="temperature-form-item senior-form-item"
                    :validate-status="maxOutputTokensWarning ? 'warning' : ''"
                    :help="maxOutputTokensWarning ? maxOutputTokensWarningText : ''"
                >
                    <template #label>
                        <span class="temperature-label">最大输出Token数</span>
                        <a-tooltip>
                            <template slot="title">
                                控制模型输出的Tokens长度上限，该参数影响模型效果，通常100Tokens约等于120个中文汉字
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
                                v-model="maxOutputTokens"
                                disabled
                                :min="1"
                                :max="2048"
                                :step="1"
                                @change="maxOutputTokensChange($event)"
                            />
                        </a-col>
                        <a-col :span="4">
                            <a-input-number
                                size="small"
                                v-model="maxOutputTokens"
                                disabled
                                :min="1"
                                :max="2048"
                                :step="1"
                                class="input-number-box"
                                @change="maxOutputTokensChange($event)"
                            />
                        </a-col>
                    </a-row>
                </a-form-item>
                <a-form-item
                    size="small"
                    class="temperature-form-item senior-form-item"
                >
                    <template #label>
                        <span class="temperature-label">对话历史</span>
                        <a-tooltip>
                            <template slot="title">
                                开启后，将会引用用户与应用的对话历史作为意图识别的输入信息（对话历史轮数由对话设置中的【参考对话轮数】决定）。
                            </template>
                            <a-icon
                                type="question-circle"
                                class="question-circle"
                            />
                        </a-tooltip>
                    </template>
                    <a-switch
                        v-model="talkHistory"
                        @change="updateLf()"
                        size="small"
                    />
                </a-form-item>
            </a-form>
        </div>
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
                    :is-dynamics="true"
                    :input-tooltip="inputTooltip"
                    :click-node="clickNode"
                    :lf="lf"
                    @changeInputData="changeInputData"
                    key="inputVars"
                    ref="inputVarsForm"
                ></InputComponents>
            </div>
        </div>

        <div class="container-title">
            <div class="container-title-text">
                提示词
            </div>
        </div>
        <!-- 提示词 -->
        <div
            class="senior-config"
        >
            <a-form
                :class="[
                    'intent-form tips-form',
                ]"
                :form="intentForm"
            >
                <a-form-item
                    size="small"
                    :required="true"
                    class="flex-form-item"
                >
                    <div class="demos-box">
                        <div class="demos-title">
                            <div>
                                <a-icon
                                    :type="expandedSystem ? 'caret-down' : 'caret-right'"
                                    @click.stop="expandedSystem = !expandedSystem"
                                />
                                <span class="title-text item-required">系统提示词</span>
                                <a-tooltip class="radio-tooltip">
                                    <template slot="title">
                                        设定模型的角色和行为模式，决定对话的整体基调和方向
                                    </template>
                                    <a-icon type="question-circle" />
                                </a-tooltip>
                            </div>
                        </div>
                        <div
                            class="demos-item"
                            v-if="expandedSystem"
                        >
                            <a-textarea
                                :placeholder="'输入大模型的系统提示词，使大模型实现对应功能，通过插入{{参数名}}可以引用对应的参数值'"
                                :auto-size="{ minRows: 6, maxRows: 12 }"
                                @change="systemPromptChange"
                                class="intention-input"
                                size="small"
                                :max-length="5000"
                                v-decorator="['systemPrompt', {
                                    rules: [
                                        { required: true, message: '系统提示词不能为空' }
                                    ]
                                }]"
                            />
                        </div>
                    </div>
                </a-form-item>
                <a-form-item
                    size="small"
                    :required="true"
                    class="flex-form-item"
                >
                    <div class="demos-box">
                        <div class="demos-title">
                            <div>
                                <a-icon
                                    :type="expandedUser ? 'caret-down' : 'caret-right'"
                                    @click.stop="expandedUser = !expandedUser"
                                />
                                <span class="title-text item-required">用户提示词</span>
                                <a-tooltip class="radio-tooltip">
                                    <template slot="title">
                                        用户输入的具体问题或请求，决定模型在当前对话中的具体回答内容
                                    </template>
                                    <a-icon type="question-circle" />
                                </a-tooltip>
                            </div>
                        </div>
                        <div
                            class="demos-item"
                            v-if="expandedUser"
                        >
                            <a-textarea
                                placeholder="请输入附加提示词，提高意图识别准确率"
                                :auto-size="{ minRows: 6, maxRows: 12 }"
                                @change="userTipsChange"
                                class="intention-input"
                                size="small"
                                :max-length="5000"
                                v-decorator="['userPrompt', {
                                    rules: [
                                        { required: true, message: '用户提示词不能为空' }
                                    ]
                                }]"
                            />
                        </div>
                    </div>
                </a-form-item>
            </a-form>
        </div>
        <!-- 输出 -->
        <div
            class="input-vars"
            v-if="outputVars && outputVars.length > 0"
        >
            <div
                class="connect-config-item"
            >
                <InputComponents
                    :var-type-options="outPutsVarOptions"
                    :input-value-options="arrArgs"
                    :all-flat-args="allFlatArgs"
                    :input-data="outputVars"
                    :is-dynamics="true"
                    :is-output="true"
                    :click-node="clickNode"
                    :lf="lf"
                    @changeInputData="changeOutputData"
                    key="outputVars"
                    ref="outputVarsForm"
                    :is-output-desc="true"
                ></InputComponents>
            </div>
        </div>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption, modelOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs } from '@/views/flow/getArgs';
import { processTreeData } from '@/views/flow/common/common';
import InputComponents from './components/InputComponents';
import { getModelList } from '@/views/flow/common/modelList';

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
            intentionData: {}, // 意图数据
            arrArgs: [], // 系统参数树
            replaceFields: { // 树形控件字段映射
                title: 'title',
                value: 'key',
                label: 'label',
                children: 'children'
            },
            intentItemsShow: [], // 意图项显示状态
            intentForm: this.$form.createForm(this), // 表单实例
            seniorConfigForm: this.$form.createForm(this, { name: 'largeModel' }), // 表单实例
            mode: '', // 模式
            inputVars: [], // 输入变量
            model: '', // 模型
            temperature: 0, // 多样性
            maxOutputTokens: 2048, // 最大输出Tokens数
            talkHistory: false, // 对话历史
            additionalPrompt: '', // 附加提示词
            modelOption, // 模型选项
            modelWarning: false, // 模型警告信息
            maxOutputTokensWarning: false, // 最大输出Tokens数警告信息
            modelWarningText: '当前选择的模型不可用，请重新选择',
            maxOutputTokensWarningText: '设定值过小，可能导致工具调用失败或输出内容被截断',
            modelHelpText: '免费调试中，如需扩容或提升稳定性，请切换为专享资源',
            intentItemsIndex: 1, // 意图项索引
            extractVarsIndex: [], // 提取变量索引
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            outPutsVarOptions: [
                { label: 'String', value: 'String' },
                { label: 'Integer', value: 'Integer' },
                { label: 'Boolean', value: 'Boolean' },
                { label: 'Number', value: 'Number' },
                { label: 'ArrayString', value: 'ArrayString' },
                { label: 'ArrayInteger', value: 'ArrayInteger' },
                { label: 'ArrayBoolean', value: 'ArrayBoolean' },
                { label: 'ArrayNumber', value: 'ArrayNumber' },
                { label: 'ArrayObject', value: 'ArrayObject' },
                { label: 'Object', value: 'Object' },
                { label: 'Any', value: 'Any' },
            ],
            allFlatArgs: [],
            expandedSystem: true, // 是否展开系统提示词
            expandedUser: true, // 是否展开用户提示词
            systemPrompt: '', // 系统提示词
            outputVars: [], // 输出列表
            inputTooltip: '输入给大模型的参数，可在下方提示词中引用。所有输入参数会被转为string输入。',
            intentModel: null,
            modelType: '',
            modelServer: '', // 模型服务
            fallbackModelName: undefined, // 备选模型
            fallbackModelServer: '', // 备选模型服务
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
         * 初始化验证函数
         *
         * 该函数在延迟100毫秒后执行，用于初始化表单验证。
         * 首先打印当前表单的值，然后验证表单字段，最后打印输入变量表单的验证结果。
         */
        initValidate() {
            setTimeout(() => {
                this.intentForm?.validateFields();
                // this.$refs.inputVarsForm?.inputForm?.validateFields();
                // this.$refs.outputVarsForm?.inputForm?.validateFields();
            }, 300);
        },
        /**
         * @description: 初始化函数
         * @return {*}
         */
        async init() {
            const { id } = this.clickNode;
            // 获取系统参数树（全部可选参数）
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            const inputVarsData = [
                {
                    varName: 'query',
                    varType: 'String',
                    varValue: '',
                    referenceNodeId: '',
                    referenceVarName: '',
                    referenceVarType: '',
                }
            ];
            // eslint-disable-next-line max-len
            const { mode, inputVars, model, modelType, temperature, maxOutputTokens, talkHistory, additionalPrompt, outputVars, userPrompt, systemPrompt, modelServer, fallbackModelName, fallbackModelServer } = this.propertiesData;
            this.mode = mode || 'speed';
            if (inputVars && inputVars.length > 0) {
                this.inputVars = inputVars;
            } else {
                this.inputVars = JSON.parse(JSON.stringify(inputVarsData));
            }
            if (outputVars && outputVars.length > 0) {
                this.outputVars = outputVars;
            } else {
                this.outputVars = JSON.parse(JSON.stringify(inputVarsData));
            }
            this.intentModel = model || undefined;
            this.temperature = temperature || 0.0001;
            this.maxOutputTokens = maxOutputTokens || 2048;
            this.talkHistory = talkHistory === 1 ? true : false;
            this.additionalPrompt = additionalPrompt || '';
            this.modelWarning = !this.intentModel;
            this.systemPrompt = systemPrompt || '';
            this.modelType = modelType;
            this.modelServer = modelServer || '';
            this.fallbackModelName = fallbackModelName || undefined;
            this.fallbackModelServer = fallbackModelServer || '';
            const modelList = await getModelList(true);
            this.modelOption = modelList;
            this.intentForm.setFieldsValue({
                userPrompt: userPrompt,
                systemPrompt: systemPrompt
            });
        },
        /**
         * @description: 模式单选框变化
         * @return {*}
         */
        modeRadioChange() {
            this.updateLf();
        },
        checkInputForm(rule, value, callback) {
            console.log('updateLf2', value);
            this.$set(this, 'inputVars', value);
            this.updateLf();

            if (value?.length > 0) {
                let checkSuccess = true;
                value.forEach(element => {
                    if (!element.varValue || element.varValue?.length === 0) {
                        checkSuccess = false;
                        callback('请输入参数值');
                        this.updateLf();
                        return;
                    }
                });
                if (checkSuccess) {
                    this.updateLf();
                    callback();
                }
                this.updateLf();
                return;
            }
            callback('请输入参数值');
        },
        /**
         * @description: 输入框变化
         * @param {number} index 意图项索引
         * @param {string} key 字段键
         * @param {string} value 字段值
         * @return {*}
         */
        inputVarsChange(index, key, value, node) {
            let newVal = value;
            if (key === 'intentName' || key === 'intentDesc') {
                newVal = value.target.value;
            }
            console.log('inputVarsChange', index, key, value);
            this.$set(this[node], index, {
                ...this[node][index],
                [key]: newVal
            });
            this.updateLf();
        },
        seniorConfigChange(key, value) {
            this.$set(this, key, value);
            if (key === 'model') {
                const modelData = this.getModelData(value);
                this.modelWarning = !value;
                this.$set(this, 'modelServer', modelData?.modelServer || '');
                this.$set(this, 'modelType', modelData?.modelType || '');
            }
            if (key === 'fallbackModelName') {
                const modelData = this.getModelData(value);
                this.$set(this, 'fallbackModelServer', modelData?.modelServer || '');
            }
            this.updateLf();
        },
        /**
         * @description: 获取模型数据
         * @param {String} model - 模型名称
         * @return {*}
         */
        getModelData(model) {
            if (!model) return '';
            for (const group of this.modelOption) {
                if (group.children && Array.isArray(group.children)) {
                    const match = group.children.find(item => item.modelCode === model);
                    if (match) return match || '';
                }
            }
        },

        maxOutputTokensChange(value) {
            this.maxOutputTokensWarning = value < 2048;
            this.updateLf();
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
                model: this.intentModel,
                inputVars: this.inputVars,
                temperature: this.temperature,
                maxOutputTokens: this.maxOutputTokens,
                talkHistory: this.talkHistory ? 1 : 0,
                additionalPrompt: this.additionalPrompt,
                systemPrompt: this.systemPrompt,
                userPrompt: this.userPrompt,
                outputVars: this.outputVars,
                modelType: this.modelType,
                modelServer: this.modelServer,
                fallbackModelName: this.fallbackModelName || '',
                fallbackModelServer: this.fallbackModelServer || '',
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },

        userTipsChange(e) {
            console.log('userTipsChange', e);
            this.userPrompt = e.target.value;
            this.updateLf();
        },
        systemPromptChange(e) {
            console.log('userTipsChange', e);
            this.systemPrompt = e.target.value;
            this.updateLf();
        },
        changeInputData(value) {
            this.inputVars = value;
            this.updateLf();
        },
        changeOutputData(value) {
            this.outputVars = value;
            this.updateLf();
        }

    },
    mounted() {
        this.init();
        this.initValidate();
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

    .mode-radio {
        .mode-radio-group {
            .ant-radio-wrapper {
                margin-right: 75px;
                font-size: 12px;
                box-sizing: border-box;

                .radio-tooltip {
                    margin-left: 5px;
                }
            }
        }
    }

    .input-vars {
        font-size: 12px;

        .input-vars-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            color: #5c5f66;
            gap: 5px;

            .var-name {
                width: 30%;

                .text {
                    color: #000;
                    font-weight: 400;
                }

                .type {
                    padding: 2px 5px;
                    text-align: center;
                    color: #5c5f66;
                    border-radius: 4px;
                    background: #e8e9eb;
                    font-size: 12px;
                    font-weight: 400;
                    line-height: 20px;
                    margin-left: 3px;
                }
            }

            .var-type {
                width: 30%;

                .var-type-select {
                    width: 100%;
                }
            }

            .var-value {
                width: 40%;

                .var-value-select {
                    font-size: 12px;
                    width: 100%;

                    .arg-type {
                        display: none;
                    }
                }

                /deep/ .tree-select-dropdown {
                    left: -120px !important;

                    li {
                        margin: 2px 0;
                    }

                    .ant-select-tree-title {
                        font-size: 12px;
                    }
                }

                /deep/ .input-vars-form-item {
                    margin-bottom: 0px;

                    .ant-form-explain {
                        font-size: 12px;
                    }
                }
            }
        }
    }

    .intent-items {
        margin-bottom: 12px;

        .intent-items-title {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .intent-form-hide {
            height: 0;
            overflow: hidden;
            margin-top: 0px;
        }
    }

    .add-btn-box {
        .add-btn {
            width: 100%;
            height: 28px;
            border: 1px solid #e8e9eb;
            border-radius: 6px;
            font-size: 12px;
            margin-bottom: 10px;

            &:hover {
                color: #2468f2;
                border-color: #2468f2;
            }
        }
    }

    .intention-input {
        font-size: 12px;
    }

    .intention-select {
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

        /deep/ .senior-form-item {
            display: flex;

            .ant-form-item-label {
                width: 40%;
            }

            .ant-form-item-control-wrapper {
                width: 70%;
            }
        }

        /deep/ .flex-form-item {
            display: flex;

            .ant-form-item-label {
                width: 25%;
            }

            .ant-form-item-control-wrapper {
                width: 75%;
            }
        }

        /deep/ .ant-form-item-label {
            line-height: 24px;
            text-align: left;

            .ant-form-item-required {
                display: flex;
                align-items: center;
                font-size: 12px;
                color: #5c5f66;
                font-weight: 400;

                &::after {
                    content: '*';
                    color: #f5222d;
                }

                &::before {
                    display: none;
                }
            }

            label {
                font-size: 12px;
                color: #5c5f66;
                font-weight: 400;
            }
        }

        .temperature-form-item {
            /deep/ .ant-form-item-required {
                .temperature-label {
                    position: relative;
                    margin: 0 8px 0 2px;

                    &::after {
                        content: '*';
                        display: flex;
                        align-items: center;
                        font-size: 12px;
                        color: #f5222d;
                        font-weight: 400;
                        position: absolute;
                        top: -0.5px;
                        right: -7px;
                    }
                }

                .question-circle {
                    font-size: 12px;
                    margin-left: 4px;
                }

                &::after {
                    display: none;
                }

                &::before {
                    display: none;
                }
            }

            /deep/ label {
                display: flex;
                align-items: center;

                .temperature-label {
                    position: relative;
                    margin: 0 8px 0 2px
                }

                .question-circle {
                    font-size: 12px;
                    margin-left: 4px;
                }

                &::after {
                    display: none;
                }

                &::before {
                    display: none;
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

        .input-name-box {
            position: relative;
            display: inline-block;
            width: 100%;

            .input-name {
                padding-right: 40px;
            }

            .input-counter {
                position: absolute;
                bottom: 0;
                right: 8px;
                font-size: 12px;
                color: #999;
                line-height: 24px;
                pointer-events: none;
            }
        }

        .model-select {
            width: 88%;
        }

        .reload-icon {
            margin-left: 12px;
            font-size: 14px;
            color: #151b26;
            cursor: pointer;
            display: inline-block;
            font-weight: 400;
        }
    }

    .demos-box {
        font-size: 12px;
        color: #5c5f66;
        font-weight: 400;

        .demos-title {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .title-text {
                margin-right: 5px;
            }

            .add-icon {
                margin-left: 5px;
            }
        }

        .demos-item {
            display: flex;
            margin-top: 5px;
            gap: 4px;

            .del-icon {
                color: #151b26;
                font-size: 14px;
                margin-top: 5px;
            }

            .item-box {
                width: 33%;

                .extract-form-item {
                    margin-bottom: 0px;
                }
            }

            .item-container {
                display: flex;
                gap: 4px;
            }
        }

        .item-title {
            display: flex;
            align-items: center;
            gap: 7px;

            .item-varName {
                width: 30%;
            }

            .item-varType {
                width: 30%;
            }

            .item-desc {
                width: 30%;
            }
        }
    }

    .export-box {
        font-size: 12px;

        .export-title {
            margin-bottom: 4px;
            color: #84868c;
            font-weight: 400;
            line-height: 20px;
        }

        .export-item {
            display: flex;
            margin-bottom: 4px;
            color: #84868c;
            font-weight: 400;
            line-height: 20px;
            cursor: pointer;

            .export-var-type {
                display: flex;
                align-items: center;
                flex-shrink: 0;
                margin-left: 4px;
                padding: 0 5px;
                background-color: #e8e9eb;
                border-radius: 4px;
                white-space: nowrap;
                height: 20px;
                color: #5c5f66;
                line-height: 20px;
            }
        }
    }
    .tips-form {
        /deep/ .ant-form-item-control-wrapper {
            width: 100% !important;
        }
    }
    .item-required::after {
        content: '*';
        color: #f5222d;
        margin-inline-start: 2px;
    }
}
</style>
<style lang="less">
.option-group-label {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.option-checkbox {
    font-size: 12px;
}

.option-box {
    display: flex;
    align-items: center;
    font-size: 12px;

    .option-img-box {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 5px;

        .option-img {
            width: 16px;
            height: 16px;
        }
    }
}
</style>
