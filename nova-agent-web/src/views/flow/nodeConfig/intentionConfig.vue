<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-02 13:32:51
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @LastEditTime: 2026-07-16 14:08:43
 * @FilePath: /metis-front/src/views/flow/nodeConfig/intentionConfig.vue
 * @Description: 意图组件
-->
<template>
    <div class="container">
        <div class="container-title">
            <div class="container-title-text">
                模式
            </div>
        </div>
        <!-- 模式单选 -->
        <div class="mode-radio">
            <a-radio-group
                name="radioGroup"
                class="mode-radio-group"
                v-model="mode"
                @change="modeRadioChange"
            >
                <a-radio
                    :value="item.value"
                    v-for="item in modeOption"
                    :key="item.value"
                >
                    <span>{{ item.label }}</span>
                    <a-tooltip class="radio-tooltip">
                        <template slot="title">
                            {{ item.descText }}
                        </template>
                        <a-icon type="question-circle" />
                    </a-tooltip>
                </a-radio>
            </a-radio-group>
        </div>
        <a-divider />
        <!-- 输入 -->
        <div class="input-vars">
            <a-form :form="inputVarsForm">
                <a-form-item>
                    <div
                        class="connect-config-item"
                        v-if="inputVars.length > 0"
                    >
                        <InputComponents
                            :var-type-options="varTypeOptions"
                            :input-value-options="arrArgs"
                            :all-flat-args="allFlatArgs"
                            :input-data="inputVars"
                            :input-tooltip="inputTooltip"
                            :click-node="clickNode"
                            :lf="lf"
                            @changeInputData="changeInputData"
                        ></InputComponents>
                    </div>
                </a-form-item>
            </a-form>
        </div>
        <div class="container-title">
            <div class="container-title-text">
                意图配置
            </div>
        </div>
        <!-- 意图配置 -->
        <div
            class="intent-items"
            v-for="(item, index) in intentItems"
            :key="item.intentItemsIndex"
        >
            <div class="intent-items-title">
                <div class="title-text">
                    <a-icon
                        :type="intentItemsShow[index] ? 'caret-down' : 'caret-right'"
                        @click.stop="intentItemsToggleFold(index)"
                    />
                    {{ item.intentName }}
                </div>
                <div class="del-icon">
                    <a-icon
                        type="minus-circle"
                        @click.stop="delIntentItems(index)"
                    />
                </div>
            </div>
            <a-form
                :class="[
                    'intent-form',
                    { 'intent-form-hide': !intentItemsShow[index] }
                ]"
                :form="intentForm"
            >
                <a-form-item
                    label="意图名称"
                    size="small"
                    :required="true"
                    class="flex-form-item"
                >
                    <div class="input-name-box">
                        <a-input
                            placeholder="请输入意图名称"
                            size="small"
                            :max-length="20"
                            class="input-name intention-input"
                            show-count
                            v-decorator="[`intentName${index}`, {
                                rules: [
                                    { required: true, message: '请输入意图名称' }
                                ]
                            }]"
                            @input="inputVarsChange(index, 'intentName', $event, 'intentItems')"
                        />
                        <div class="input-counter">
                            {{ item.intentName.length }}/20
                        </div>
                    </div>
                </a-form-item>
                <a-form-item
                    label="意图描述"
                    size="small"
                    :required="true"
                    class="flex-form-item"
                >
                    <a-textarea
                        placeholder="请描述意图的含义、使用场景，或提供例句，便于大模型更好识别该意图"
                        size="small"
                        class="input-name intention-input"
                        :rows="4"
                        v-decorator="[`intentDesc${index}`, {
                            rules: [
                                { required: true, message: '请输入意图描述' }
                            ]
                        }]"
                        :max-length="1000"
                        @input="inputVarsChange(index, 'intentDesc', $event, 'intentItems')"
                    />
                </a-form-item>
                <a-form-item
                    size="small"
                    v-show="mode === 'accurate'"
                >
                    <div class="demos-box">
                        <div class="demos-title">
                            <div>
                                <span class="title-text">意图例句</span>
                                <a-tooltip class="radio-tooltip">
                                    <template slot="title">
                                        意图配置可以是表达意图的一句话，包括但不局限于意图的具体名称。
                                    </template>
                                    <a-icon type="question-circle" />
                                </a-tooltip>
                            </div>
                            <div>
                                <span>{{ item.demos.length }}/10</span>
                                <a-icon
                                    type="plus"
                                    class="add-icon"
                                    @click.stop="addDemos(index)"
                                />
                            </div>
                        </div>
                        <div
                            class="demos-item"
                            v-for="(demo, demoIndex) in item.demos"
                            :key="demo.indexId"
                        >
                            <a-input
                                placeholder="请输入意图例句"
                                class="intention-input"
                                v-model="demo.demo"
                                @input="updateLf()"
                                size="small"
                            />
                            <a-icon
                                type="minus-circle"
                                class="del-icon"
                                @click.stop="delDemos(index, demoIndex)"
                            />
                        </div>
                    </div>
                </a-form-item>
                <a-form-item
                    size="small"
                    v-show="mode === 'accurate'"
                >
                    <div class="demos-box">
                        <div class="demos-title">
                            <div>
                                <span class="title-text">参数抽取</span>
                                <a-tooltip class="radio-tooltip">
                                    <template slot="title">
                                        配置识别到该意图时需要抽取的参数，大模型将根据参数描述抽取。
                                    </template>
                                    <a-icon type="question-circle" />
                                </a-tooltip>
                            </div>
                            <div>
                                <span>{{ item.extractVars.length }}/10</span>
                                <a-icon
                                    type="plus"
                                    class="add-icon"
                                    @click.stop="addExtractVars(index)"
                                />
                            </div>
                        </div>
                        <div>
                            <div
                                class="item-title"
                                v-if="item.extractVars.length > 0"
                            >
                                <div class="item-varName">
                                    参数名
                                </div>
                                <div class="item-varType">
                                    类型
                                </div>
                                <div class="item-desc">
                                    描述
                                </div>
                            </div>
                            <div
                                class="demos-item"
                                v-for="(extract, extractIndex) in item.extractVars"
                                :key="extract.id"
                            >
                                <div class="item-container">
                                    <div class="item-box">
                                        <a-form-item
                                            size="small"
                                            class="extract-form-item"
                                        >
                                            <a-input
                                                placeholder="请输入参数名"
                                                class="intention-input"
                                                @change="extractChange(index, extractIndex, $event, 'varName')"
                                                size="small"
                                                v-decorator="[`extractVarName${index}_${extractIndex}`, {
                                                    rules: [
                                                        { required: true, message: '请输入参数名' },
                                                        {
                                                            pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
                                                            message: '只能输入字母、数字、下划线，并以字母开头'
                                                        },
                                                        { validator: validateUniqueVarName }
                                                    ]
                                                }]"
                                            />
                                        </a-form-item>
                                    </div>
                                    <div class="item-box">
                                        <a-form-item
                                            size="small"
                                            class="extract-form-item"
                                        >
                                            <a-select
                                                size="small"
                                                class="intention-select"
                                                :value="extract.varType"
                                                @change="extractChange(index, extractIndex, $event, 'varType')"
                                            >
                                                <a-select-option
                                                    v-for="option in varTypeOption"
                                                    :key="option.value"
                                                >
                                                    {{ option.label }}
                                                </a-select-option>
                                            </a-select>
                                        </a-form-item>
                                    </div>
                                    <div class="item-box">
                                        <a-form-item
                                            size="small"
                                            class="extract-form-item"
                                        >
                                            <a-input
                                                placeholder="请输入描述"
                                                class="intention-input"
                                                @input="extractChange(index, extractIndex, $event, 'desc')"
                                                size="small"
                                                v-decorator="[`extractDesc${index}_${extractIndex}`, {
                                                    rules: [
                                                        { required: true, message: '意图抽取参数描述不能为空' }
                                                    ]
                                                }]"
                                            />
                                        </a-form-item>
                                    </div>
                                </div>
                                <a-icon
                                    type="minus-circle"
                                    class="del-icon"
                                    @click.stop="delExtractVars(index, extractIndex)"
                                />
                            </div>
                        </div>
                    </div>
                </a-form-item>
            </a-form>
        </div>
        <div class="add-btn-box">
            <a-button
                class="add-btn"
                icon="plus"
                size="small"
                @click="addIntentItems"
            >
                添加意图
            </a-button>
        </div>
        <div class="container-title">
            <div class="container-title-text">
                高级配置
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
                        option-label-prop="label"
                        v-model="intentModel"
                        class="model-select intention-select"
                        @change="seniorConfigChange('intentModel', $event)"
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
                                <a-checkbox
                                    v-if="group.modelName === '更多'"
                                    class="option-checkbox"
                                >
                                    已有专享
                                </a-checkbox>
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
                                {{ option.modelName }}
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
                        option-label-prop="label"
                        v-model="fallbackModelName"
                        class="model-select intention-select"
                        @change="fallbackModelChange"
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
                                <a-checkbox
                                    v-if="group.modelName === '更多'"
                                    class="option-checkbox"
                                >
                                    已有专享
                                </a-checkbox>
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
                                {{ option.modelName }}
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
                                :disabled="!intentModel"
                                :min="2"
                                :max="10000"
                                :step="1"
                                @change="maxOutputTokensChange($event)"
                            />
                        </a-col>
                        <a-col :span="4">
                            <a-input-number
                                size="small"
                                v-model="maxOutputTokens"
                                :disabled="!intentModel"
                                :min="2"
                                :max="100000"
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
                <a-form-item
                    class="temperature-form-item additionalPrompt"
                    size="small"
                >
                    <div class="demos-box">
                        <div class="demos-title">
                            <div>
                                <span class="title-text">附加提示词</span>
                                <a-tooltip class="radio-tooltip">
                                    <template slot="title">
                                        输入的内容会被追加到意图识别的提示词中
                                    </template>
                                    <a-icon type="question-circle" />
                                </a-tooltip>
                            </div>
                        </div>
                        <div class="demos-item">
                            <a-textarea
                                v-model="additionalPrompt"
                                placeholder="请输入附加提示词，提高意图识别准确率"
                                :auto-size="{ minRows: 4 }"
                                @change="updateLf()"
                                class="intention-input"
                                size="small"
                            />
                        </div>
                    </div>
                </a-form-item>
            </a-form>
        </div>
        <div class="container-title">
            <div class="container-title-text">
                输出
            </div>
            <a-tooltip>
                <template slot="title">
                    意图识别节点匹配输出的结果。
                </template>
                <a-icon type="question-circle" />
            </a-tooltip>
        </div>
        <div class="export-box">
            <div class="export-title">
                参数名
            </div>
            <div
                class="export-item"
                v-for="(item, index) in outputList"
                :key="index"
            >
                <div class="export-var-name">
                    <a-tooltip v-if="item.varDesc">
                        <template slot="title">
                            {{ item.varDesc }}
                        </template>
                        {{ item.varName }}
                    </a-tooltip>
                    <div v-else>
                        {{ item.varName }}
                    </div>
                </div>
                <div class="export-var-type">
                    {{ item.varType }}
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { varTypeOption, modeOption } from '@/views/flow/common/commonData';
import { getAllArgs, getAllFlatArgs, deleteTempOutputs } from '@/views/flow/getArgs';
import { processTreeData, uniqueValue } from '@/views/flow/common/common';
import InputComponents from './components/InputComponents';
import { getModelList } from '@/views/flow/common/modelList';
import {
    updateReferenceVarNameById,
    deleteReferenceVarNameById
} from '@/views/flow/basics/lfEvent.js';
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
            modeOption,
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
            inputVarsForm: this.$form.createForm(this), // 表单实例
            intentForm: this.$form.createForm(this), // 表单实例
            seniorConfigForm: this.$form.createForm(this), // 表单实例
            mode: '', // 模式
            inputVars: [], // 输入变量
            intentItems: [], // 意图项
            intentModel: '', // 模型
            temperature: 0, // 多样性
            maxOutputTokens: 0, // 最大输出Tokens数
            talkHistory: false, // 对话历史
            additionalPrompt: '', // 附加提示词
            modelOption: [], // 模型选项
            modelWarning: false, // 模型警告信息
            maxOutputTokensWarning: false, // 最大输出Tokens数警告信息
            modelWarningText: '当前选择的模型不可用，请重新选择',
            maxOutputTokensWarningText: '设定值过小，可能导致工具调用失败或输出内容被截断',
            modelHelpText: '免费调试中，如需扩容或提升稳定性，请切换为专享资源',
            outputList: [], // 输出列表
            extractVarsIndex: [], // 提取变量索引
            demoIndexId: [], // 示例索引
            varTypeOptions: [ // 参数类型选项
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            modelType: '',
            allFlatArgs: [], // 全部可选参数
            inputTooltip: '输入给意图识别节点的参数，节点将根据输入的Query进行意图识别和匹配。',
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
         * @description: 初始化函数
         * @return {*}
         */
        async init() {
            const { id } = this.clickNode;
            // 获取系统参数树（全部可选参数）
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            // eslint-disable-next-line max-len
            const { mode, inputVars, intentItems, model, temperature, maxOutputTokens, talkHistory, additionalPrompt, outputVars, modelType, modelServer, fallbackModelName, fallbackModelServer } = this.propertiesData;
            this.mode = mode || 'speed';
            this.intentItems = intentItems;
            this.intentModel = model || '';
            this.temperature = temperature || 0.0001;
            this.maxOutputTokens = maxOutputTokens || 1024;
            this.talkHistory = talkHistory === 1 ? true : false;
            this.additionalPrompt = additionalPrompt || '';
            this.outputList = outputVars || [];
            this.modelType = modelType || '';
            this.modelServer = modelServer || '';
            this.fallbackModelName = fallbackModelName || undefined;
            this.fallbackModelServer = fallbackModelServer || '';

            this.modelWarning = !this.intentModel;
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
            this.$nextTick(() => {
                if (inputVars && inputVars.length > 0) {
                    this.inputVars = inputVars;
                } else {
                    this.inputVars = inputVarsData;
                }
                this.intentItems.forEach((item, index) => {
                    this.$set(this.intentItemsShow, index, true);
                    this.intentForm.setFieldsValue({
                        [`intentName${index}`]: item.intentName,
                        [`intentDesc${index}`]: item.intentDesc,
                    });
                    item.extractVars.forEach((extractVar, extractIndex) => {
                        this.intentForm.setFieldsValue({
                            [`extractVarName${index}_${extractIndex}`]: extractVar.varName,
                            [`extractDesc${index}_${extractIndex}`]: extractVar.desc,
                        });
                        item.extractVarsIndex = extractIndex + 1;
                    });
                    item.demos = item.demos.map((demo, demoIndex) => ({
                        demo: demo,
                        demoIndex: demoIndex + 1,
                    }));
                    this.extractVarsIndex.push(item.extractVars.length);
                    this.demoIndexId.push(item.demos.length);
                });
                // }, 500);
            });
            const modelList = await getModelList(true);
            this.modelOption = modelList;
        },
        /**
         * @description: 模式单选框变化
         * @return {*}
         */
        modeRadioChange() {
            this.updateLf();
        },
        /**
         * @description: 输入变量校验方法
         * @param {object} rule 校验规则
         * @param {array} value 输入变量值
         * @param {function} callback 回调函数
         * @return {*}
         */
        checkInputForm(rule, value, callback) {
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
            this.$set(this[node], index, {
                ...this[node][index],
                [key]: newVal
            });
            this.updateLf();
        },
        /**
         * @description: 高级配置变化
         * @param {string} key 字段键
         * @param {string} value 字段值
         * @return {*}
         */
        seniorConfigChange(key, value) {
            this.$set(this, key, value);
            if (key === 'intentModel') {
                const modelData = this.getModelData(value);
                this.modelWarning = !value;
                this.$set(this, 'modelServer', modelData?.modelServer || '');
                this.$set(this, 'modelType', modelData?.modelType || '');
            }
            this.updateLf();
        },
        /**
         * @description: 备选模型变化
         * @param {string} value 模型编码
         * @return {*}
         */
        fallbackModelChange(value) {
            const modelData = this.getModelData(value);
            this.fallbackModelName = value || undefined;
            this.fallbackModelServer = modelData?.modelServer || '';
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
        /**
         * @description: 示例输入框变化
         * @param {number} index 意图项索引
         * @param {number} demoIndex 示例索引
         * @param {string} value 示例值
         * @return {*}
         */
        demoChange(index, demoIndex, value) {
            this.$set(this.intentItems[index].demos[demoIndex], 'demo', value.target.value);
            this.updateLf();
        },
        /**
         * @description: 参数抽取输入框变化
         * @param {number} index 意图项索引
         * @param {number} extractIndex 参数抽取索引
         * @param {string} key 参数键
         * @param {string} value 参数值
         * @return {*}
         */
        extractChange(index, extractIndex, value, key) {
            const newVal = value.target ? value.target.value : value;
            this.$set(this.intentItems[index].extractVars[extractIndex], key, newVal);
            const data = this.intentItems[index].extractVars[extractIndex];
            // eslint-disable-next-line max-len
            const outputIndex = this.outputList.findIndex(item => item.id && item.id === data.id);
            if (outputIndex > -1) {
                this.$set(this.outputList[outputIndex], key, newVal);
            }
            // 参数名变化更新，参数类型变化删除
            if (key === 'varName') {
                updateReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: data.id,
                    updateVarName: newVal
                });
            } else if (key === 'varType') {
                if (data.id) {
                    deleteReferenceVarNameById({
                        nodeId: this.clickNode?.id,
                        lf: this.lf,
                        varNameId: data.id
                    });
                }
            }
            deleteTempOutputs();
            this.updateLf();
        },
        /**
         * @description: 最大输出token数变化
         * @param {number} value 令牌数值
         * @return {*}
         */
        maxOutputTokensChange(value) {
            this.maxOutputTokensWarning = value < 1024;
            this.updateLf();
        },
        /**
         * @description: 更新逻辑流图
         * @return {*}
         */
        updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            const intentItems = JSON.parse(JSON.stringify(this.intentItems));
            intentItems.forEach(item => {
                item.demos = item.demos.map(demo => demo.demo);
            });
            edgeModel.setProperties({
                model: this.intentModel,
                inputVars: this.inputVars,
                intentItems: intentItems,
                temperature: this.temperature,
                maxOutputTokens: this.maxOutputTokens,
                talkHistory: this.talkHistory ? 1 : 0,
                additionalPrompt: this.additionalPrompt,
                mode: this.mode,
                outputVars: this.outputList,
                modelType: this.modelType,
                modelServer: this.modelServer,
                fallbackModelName: this.fallbackModelName || '',
                fallbackModelServer: this.fallbackModelServer || '',
            });
            setTimeout(() => {
                edgeModel.updatePath();
            }, 100);
        },
        /**
         * @description: 切换意图项折叠状态
         * @param {number} index 意图项索引
         * @return {*}
         */
        intentItemsToggleFold(index) {
            this.$set(this.intentItemsShow, index, !this.intentItemsShow[index]);
        },
        /**
         * @description: 删除意图项
         * @param {number} index 意图项索引
         * @return {*}
         */
        delIntentItems(index) {
            if (this.intentItems.length <= 1) return;
            // 如果锚点已经有链接线
            const { targetNodes = []} = this.intentItems[index];
            if (targetNodes && targetNodes.length) {
                targetNodes.forEach(item => {
                    if (item.edgeId) {
                        this.lf.deleteEdge(item.edgeId);
                    }
                });
            }
            // 精确模式下，删除意图时，先删除参数抽取中的变量，并从输出中删除
            if (this.mode === 'accurate') {
                const arr = this.intentItems[index]?.extractVars || [];
                arr.forEach(ele => {
                    if (ele.id) {
                        deleteReferenceVarNameById({
                            nodeId: this.clickNode?.id,
                            lf: this.lf,
                            varNameId: ele.id
                        });
                    }
                    deleteTempOutputs();
                    const outputIndex = this.outputList.findIndex(item => item.id === ele.id);
                    this.outputList.splice(outputIndex, 1);
                });
            }
            this.intentItems.splice(index, 1);
            this.$nextTick(() => {
                this.intentItems.forEach((item, index) => {
                    this.intentForm.setFieldsValue({
                        [`intentName${index}`]: item.intentName,
                        [`intentDesc${index}`]: item.intentDesc,
                    });
                    item.extractVars.forEach((extractVar, extractIndex) => {
                        this.intentForm.setFieldsValue({
                            [`extractVarName${index}_${extractIndex}`]: extractVar.varName,
                            [`extractDesc${index}_${extractIndex}`]: extractVar.desc,
                        });
                    });
                });
            });
            this.updateLf();
        },
        /**
         * @description: 添加意图项
         * @return {*}
         */
        addIntentItems() {
            const intentItemsIndex = this.intentItems[this.intentItems.length - 1].id + 1;
            this.intentItems.push({
                intentName: '意图' + intentItemsIndex,
                intentDesc: '',
                demos: [],
                extractVars: [],
                id: intentItemsIndex,
                intentItemsIndex: intentItemsIndex,
            });
            this.$set(this.intentItemsShow, this.intentItems.length - 1, true);
            this.updataForm();
            this.updateLf();
        },
        /**
         * @description: 更新表单数据
         * @return {*}
         */
        updataForm() {
            this.$nextTick(() => {
                this.intentItems.forEach((item, index) => {
                    this.intentForm.setFieldsValue({
                        [`intentName${index}`]: item.intentName,
                        [`intentDesc${index}`]: item.intentDesc,
                    });
                    item.extractVars.forEach((extractItem, extractIndex) => {
                        this.intentForm.setFieldsValue({
                            [`extractVarName${index}_${extractIndex}`]: extractItem.varName,
                            [`extractDesc${index}_${extractIndex}`]: extractItem.desc,
                        });
                    });
                });
            });
        },
        /**
         * @description: 添加示例
         * @param {number} index 意图项索引
         * @return {*}
         */
        addDemos(index) {
            const length = this.intentItems[index].demos.length;
            if (length >= 10) {
                return;
            }
            this.intentItems[index].demos.push({
                demo: '',
                demoIndex: length
            });
            this.updateLf();
        },
        /**
         * @description: 删除示例
         * @param {number} index 意图项索引
         * @param {number} demoIndex 示例索引
         * @return {*}
         */
        delDemos(index, demoIndex) {
            this.intentItems[index].demos.splice(demoIndex, 1);
            this.updateLf();
        },
        /**
         * @description: 添加参数抽取
         * @param {number} index 意图项索引
         * @return {*}
         */
        addExtractVars(index) {
            const length = this.intentItems[index].extractVars.length;
            if (length >= 10) {
                return;
            }
            this.$set(this.extractVarsIndex, index, (this.extractVarsIndex[index] || 0) + 1);
            this.intentItems[index].extractVars.push({
                varName: '',
                varType: 'String',
                id: uniqueValue(),
                desc: '',
                extractVarsIndex: this.extractVarsIndex[index]
            });
            const extractVarsIndex = this.intentItems[index].extractVars.length - 1;
            this.outputList.push(this.intentItems[index].extractVars[extractVarsIndex]);
            this.updateLf();
        },
        /**
         * @description: 删除参数抽取
         * @param {number} index 意图项索引
         * @param {number} extractIndex 参数抽取索引
         * @return {*}
         */
        delExtractVars(index, extractIndex) {
            const { id } = this.intentItems[index].extractVars[extractIndex] || {};
            if (id) {
                deleteReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id
                });
            }
            deleteTempOutputs();
            const data = this.intentItems[index].extractVars.splice(extractIndex, 1);
            const outputIndex = this.outputList.findIndex(item => item.id === data[0].id);
            this.outputList.splice(outputIndex, 1);
            this.$nextTick(() => {
                this.intentItems.forEach((item, index) => {
                    item.extractVars.forEach((extractItem, extractIndex) => {
                        this.intentForm.setFieldsValue({
                            [`extractVarName${index}_${extractIndex}`]: extractItem.varName,
                            [`extractDesc${index}_${extractIndex}`]: extractItem.desc,
                        });
                    });
                });
            });
            this.updateLf();
        },
        /**
         * @description: 参数抽取校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @return {*}
         */
        validateUniqueVarName(rule, value, callback) {
            const allNames = this.intentItems?.flatMap(item => item.extractVars?.map(v => v.varName)) || [];
            const duplicates = allNames.filter(name => name === value);
            if (duplicates.length > 1) {
                callback(new Error('参数名不能重复'));
            } else {
                callback();
            }
        },
        /**
         * @description: 更新输入数据
         * @param {array} value 输入数据
         * @return {*}
         */
        changeInputData(value) {
            this.$set(this, 'inputVars', value);
            this.updateLf();
        }
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
        padding: 12px;
        border-radius: 8px;
        background: #f9f9fb;

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
