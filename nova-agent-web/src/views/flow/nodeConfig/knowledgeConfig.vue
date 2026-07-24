<template>
    <div class="connect-config">
        <a-form
            ref="inputForm"
            :form="inputForm"
            label-align="left"
            :style="{ width: '100%' }"
        >
            <a-form-item
                v-if="inputVars.length > 0"
            >
                <div
                    class="connect-config-item"
                >
                    <InputComponents
                        :var-type-options="varTypeOptions"
                        :input-value-options="inputValueOptions"
                        :all-flat-args="allFlatArgs"
                        :input-data="inputVars"
                        :input-tooltip="inputTooltip"
                        :click-node="clickNode"
                        :lf="lf"
                        @changeInputData="changeInputData"
                    ></InputComponents>
                </div>
            </a-form-item>
            <a-form-item>
                <div
                    class="connect-config-item"
                    :style="{ borderBottom: 'none', marginBottom: 0, pandingBottom: 0 }"
                >
                    <KnowledgeListComponents
                        :knowledge-data-list="knowledgeDataList"
                        v-decorator="[
                            'knowledgeListComponentsValue',
                            {
                                rules: [{ validator: checkKnowledgeList }],
                            },
                        ]"
                    ></KnowledgeListComponents>
                </div>
            </a-form-item>
            <a-form-item
                class="form-item-warp"
                :style="{
                    marginBottom: '12px',
                    borderRadius: '6px',
                }"
                :label-col="{
                    span: 6,
                }"
                :wrapper-col="{
                    span: 18,
                }"
            >
                <span slot="label">
                    检索策略&nbsp;
                    <a-tooltip title="按照指定的检索策略从知识库中寻找匹配的片段，不同的检索策略可以更有效地找到正确的信息，提高最终生成的答案的准确性和可用性">
                        <a-icon type="question-circle-o" />
                    </a-tooltip>
                </span>

                <a-radio-group
                    v-model="strategyValue"
                    @change="strategyChange"
                >
                    <a-radio
                        value="hybrid"
                    >
                        混合检索
                    </a-radio>
                    <a-radio
                        value="Semantic"
                    >
                        语义检索
                    </a-radio>
                </a-radio-group>
            </a-form-item>

            <a-form-item
                class="form-item-warp"
                :style="{
                    borderRadius: '6px 6px 0 0',
                }"
                :label-col="{
                    span: 8,
                }"
                :wrapper-col="{
                    span: 16,
                }"
            >
                <span slot="label">
                    重排序配置&nbsp;
                    <a-tooltip title="配置召回数量和匹配分，控制rerank模型重排序效果：开启rerank对召回切片重排序后，会按匹配分排序。关闭rerank会按检索策略默认排序">
                        <a-icon type="question-circle-o" />
                    </a-tooltip>
                </span>

                <a-switch
                    v-model="retakeValue"
                    @change="retakeChange"
                />
            </a-form-item>
            <a-form-item
                class="form-item-warp"
                :label-col="{
                    span: 8,
                }"
                :wrapper-col="{
                    span: 16,
                }"
            >
                <span slot="label">
                    重排序模型
                </span>

                <div class="flex-align-center">
                    <span class="value-type">
                        b
                    </span>
                    <span class="value-values">
                        bce-reranker-base
                    </span>
                    <span class="value-limit">
                        (限时免费)
                    </span>
                </div>
            </a-form-item>

            <a-form-item
                class="form-item-warp"
                :style="{
                    borderRadius: '6px 6px 0 0',
                }"
                :label-col="{
                    span: 8,
                }"
                :wrapper-col="{
                    span: 16,
                }"
            >
                <span slot="label">
                    召回数量&nbsp;
                    <a-tooltip title="从知识库中召回与输入Query匹配的片段个数，数量越大召回的片段越多">
                        <a-icon type="question-circle-o" />
                    </a-tooltip>
                </span>

                <a-row
                    :gutter="18"
                    type="flex"
                    align="middle"
                >
                    <a-col :span="16">
                        <a-slider
                            v-model="callNumValue"
                            :min="1"
                            :max="20"
                            :step="1"
                            @change="callNumChange"
                        />
                    </a-col>
                    <a-col :span="8">
                        <a-input-number
                            v-model="callNumValue"
                            :min="1"
                            :max="20"
                            :step="1"
                            @change="callNumChange"
                        />
                    </a-col>
                </a-row>
            </a-form-item>
            <a-form-item
                class="form-item-warp"
                :label-col="{
                    span: 8,
                }"
                :wrapper-col="{
                    span: 16,
                }"
            >
                <span slot="label">
                    匹配分&nbsp;
                    <a-tooltip title="在检索过程中，用来计算输入Query和知识库片段的相似度，高于匹配分数的片段将会被检索召回">
                        <a-icon type="question-circle-o" />
                    </a-tooltip>
                </span>

                <a-row
                    :gutter="18"
                    type="flex"
                    justify="space-between"
                    align="middle"
                >
                    <a-col :span="16">
                        <a-slider
                            v-model="matchNumValue"
                            :min="0.01"
                            :max="0.99"
                            :step="0.01"
                            @change="matchNumChange"
                        />
                    </a-col>
                    <a-col :span="8">
                        <a-input-number
                            v-model="matchNumValue"
                            :min="0.01"
                            :max="0.99"
                            :step="0.01"
                            @change="matchNumChange"
                        />
                    </a-col>
                </a-row>
            </a-form-item>
            <a-form-item
                class="form-item-warp"
                :style="{
                    borderRadius: '0 0 6px 6px',
                }"
                :label-col="{
                    span: 8,
                }"
                :wrapper-col="{
                    span: 16,
                }"
            >
                <span slot="label">
                    扩展上下文信息&nbsp;
                    <a-tooltip
                        title="开启后，将会用Small-to-Big策略对重排序后的切片扩展切片上下文，
                        大模型能够看到更多上下文内容，以提高检索的准确性，仅对文本和URL类数据生效。开启时，会增加模型处理的token数量，增加响应时长，建议将知识库的切片最大长度设置为400"
                    >
                        <a-icon type="question-circle-o" />
                    </a-tooltip>
                </span>
                <a-switch
                    v-model="extendValue"
                    @change="extendChange"
                />
            </a-form-item>
            <a-form-item>
                <varsTree
                    :tree-data="outputVars"
                    title="输出"
                />
            </a-form-item>
        </a-form>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import InputComponents from './components/InputComponents';
import KnowledgeListComponents from './components/KnowledgeListComponents';
import { processTreeData } from '@/views/flow/common/common';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import { largeModelViews } from '../registerFlowNode/commonUtils';

import {
    getAllArgs,
    getAllFlatArgs,
} from '@/views/flow/getArgs';
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
    components: {
        InputComponents,
        KnowledgeListComponents,
        varsTree
    },
    data() {
        return {
            configArr: [],
            configOutputArr: [
                {
                    value: 1
                }
            ],
            form: this.$form.createForm(this),
            inputForm: this.$form.createForm(this, { name: 'inputForm' }),
            isShowTextareaSystem: false,
            isShowTextareauser: false,
            systemIconType: 'caret-right',
            userIconType: 'caret-right',
            strategyOptions: [
                { label: '混合检索', value: 'Apple' },
                { label: '语义检索', value: 'Pear' },
            ],
            isShowKnowledgeList: false,
            callNum: 6,
            matchNum: 0.4,
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
                { label: 'Array<String>', value: 'ArrayString' },
            ],
            inputValueOptions: [],
            inputType: 'String',
            inputValue: '',
            isRetake: true,
            isExtend: false,
            knowledgeDataList: [],
            configParmas: {},
            replaceFields: { // 树形控件字段映射
                title: 'varName',
                value: 'varName',
                children: 'children'
            },
            retakeValue: false,
            extendValue: false,
            strategyValue: 'Semantic',
            callNumValue: 6,
            matchNumValue: 0.4,
            inputVars: [],
            allFlatArgs: [],
            outputVars: largeModelViews,
            expandedOutputParent: false,
            inputTooltip: '在知识库中进行匹配的片段，输入参数会被转为string输入。array<string>类型参数会依次进行检索查询，返回设置的召回数量的内容。',
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
    created() {

    },
    mounted() {
        console.log(this.clickNode);
        this.init();
    },
    methods: {

        init() {
            console.log('knowledge', this.clickNode);
            const {
                expandContextOpen = true,
                inputVars = null,
                knowledgeBaseId = [],
                reCallCount = 6,
                reSort = null,
                strategy = 'Semantic',
            } = this.propertiesData;
            // eslint-disable-next-line max-len
            console.log('properties:', inputVars, reSort, reCallCount, strategy, expandContextOpen, knowledgeBaseId);

            console.log('this.configParmas1', this.configParmas);
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
                this.knowledgeDataList = knowledgeBaseId;
                this.extendValue = expandContextOpen === 1 ? true : false;
                if (inputVars && inputVars.length > 0) {
                    this.inputVars = inputVars;
                } else {
                    this.inputVars = inputVarsData;
                }

                if (reSort) {
                    if (reSort?.open === 1) {
                        this.retakeValue = true;
                    } else {
                        this.retakeValue = false;
                    }
                    this.matchNumValue = reSort?.matchScore || 0.4;
                } else {
                    this.retakeValue = true;
                    this.matchNumValue = 0.4;
                }

                this.strategyValue = strategy;
                this.callNumValue = reCallCount;


                this.$set(this.configParmas, 'extendValue', this.extendValue);
                this.$set(this.configParmas, 'retakeValue', this.retakeValue);
                this.$set(this.configParmas, 'matchNumValue', this.matchNumValue);
                this.$set(this.configParmas, 'strategyValue', this.strategyValue);
                this.$set(this.configParmas, 'callNumValue', this.callNumValue);
                this.$set(this.configParmas, 'inputValue', this.inputValue);
                this.$set(this.configParmas, 'inputType', this.inputType);

                console.log('this.configParmas2', this.configParmas);
            });
            let arr = [
                {
                    key: 'query',
                    type: '',
                    value: '',
                }
            ];
            if (this.testArr && this.testArr.length > 0) {
                arr = this.testArr;
            }
            this.configArr = arr;
            const { id } = this.clickNode;
            let argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.inputValueOptions = processTreeData(argsMap, this.$createElement);

            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            console.log('this.inputValueOptions', this.inputValueOptions);
        },
        minus(item) {
            const { edgeId } = item || {};
            const filterArr = this.configArr.filter(sub => sub.value !== item.value);
            this.configArr = filterArr;
            if (edgeId) {
                this.lf.deleteEdge(edgeId);
            }
            this.updateLf();
        },
        add() {
            const length = this.configArr.length;
            const lastNumber = this.configArr[length - 1].value;
            this.configArr.push({
                value: lastNumber + 1
            });
            console.log(this.configArr, 'lastNumber + 1');
            this.updateLf();
        },
        updateLf() {
            const { id } = this.clickNode;
            console.log(this.lf, 'lf');
            const edgeModel = this.lf.getNodeModelById(id);

            const {
                strategyValue,
                matchNumValue,
                callNumValue,
                extendValue,
                knowledgeBaseId,
                retakeValue,
                currInputData
            } = this.configParmas;
            console.log('this.configParmas3', this.configParmas);
            edgeModel.setProperties({
                knowledgeBaseId, // 知识库ID
                strategy: strategyValue, // 检索模式：hybrid 混合检索、Semantic 语义检索、fullText 全文检索
                reSort: {
                    open: retakeValue ? 1 : 0, // 是否开启重排序配置
                    model: 'bce-reranker-base', //  重排序模型
                    matchScore: matchNumValue // 匹配分
                }, // 重排序配置
                reCallCount: callNumValue,            // 召回数量
                expandContextOpen: extendValue ? 1 : 0,       // 是否开启扩展上下文信息
                inputVars: currInputData,
                outputVars: this.outputVars
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
        textareaSystemOperate() {
            this.isShowTextareaSystem = !this.isShowTextareaSystem;
            this.systemIconType = this.isShowTextareaSystem ? 'caret-down' : 'caret-right';
        },
        textareauserOperate() {
            this.isShowTextareauser = !this.isShowTextareauser;
            this.userIconType = this.isShowTextareauser ? 'caret-down' : 'caret-right';
        },
        strategyChange(e) {
            let value = e.target.value;
            this.$set(this.configParmas, 'strategyValue', value);
            this.form.setFieldsValue({
                'strategyValue': value,
            });
            this.updateLf();
        },
        retakeChange(e) {
            let value = e;
            this.retakeValue = value;
            this.$set(this.configParmas, 'retakeValue', value);
            this.updateLf();
        },
        extendChange(e) {
            let value = e;
            this.extendValue = value;
            this.$set(this.configParmas, 'extendValue', value);
            this.updateLf();
        },
        callNumChange(e) {
            let value = e;
            this.$set(this.configParmas, 'callNumValue', value);
            this.updateLf();
        },
        matchNumChange(e) {
            let value = e;
            this.$set(this.configParmas, 'matchNumValue', value);
            this.updateLf();
        },
        checkInputForm(rule, value, callback) {
            this.$set(this.configParmas, 'currInputData', value);
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
        checkKnowledgeList(rule, value, callback) {
            console.log('checkKnowledgeList', value[0]);
            if (value?.length > 0) {
                console.log('成功');
                this.$set(this.configParmas, 'knowledgeBaseId', value);
                this.updateLf();
                callback();
                return;
            }
            console.log('失败');
            this.$set(this.configParmas, 'knowledgeBaseId', value);
            this.updateLf();
            callback('请添加知识库');
        },
        changeInputData(value) {
            this.$set(this.configParmas, 'currInputData', value);
            this.updateLf();
        }
    }
};
</script>
<style scoped lang="less">
.flex-center {
    display: flex;
    align-items: center;
}
i {
    font-size: 11.5px;
}
.connect-config {
    display: inline-block;
    width: 100%;
    font-size: 12px;

    /deep/ .ant-form {
        width: 100%;
    }
    .config-item-header {
        display: flex;
        align-items: center;
        color: #84868c;
        gap: 4px;
        margin-bottom: 4px;

        .header-key {
            width: 98px;
        }
        .header-type {
            flex: 0 0 102px;
        }
    }
    .config-item-content-item {
        display: flex;
        align-items: baseline;
        gap: 4px;
        color: #151b26;

        .config-item-content-key {
            width: 98px;
        }
        .config-item-content-key:after  {
            content: "*";
            color: #f33d3d;
            margin-inline-start: 2px;
        }
        .varType-select {
            flex: 0 0 102px;
            width: 102px
        }
        .varValue {
            width: 142px;
        }
    }
    .connect-config-item {
        display: block;
        padding: 10px 0;
        border-bottom: 1px solid #e8e9eb;

        .config-item-minus {
            margin-left: 10px;
        }
    }
    .config-item-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        font-size: 14px;
        font-weight: bold;

        span {
            margin-right: 10px;
            font-weight: bold;
            color: #151b26;
            font-size: 14px;
        }
    }
    .formLabelFullLine {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    /deep/ .ant-form-item {
        margin-bottom: 0;
    }
    /deep/ .ant-form-item-label > label::after {
        display: none;
    }
    .config-item-content-title {
        display: flex;
        align-items: center;
        color: #84868c;
        gap: 4px;
        margin-bottom: 4px;
    }
    .config-item-content {
        cursor: pointer;
        user-select: none;
        line-height: 22px;
        position: relative;
        word-break: break-word;
        margin-bottom: 4px;
        .config-item-content-item {
            .config-item-content-key {
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .config-item-content-value {
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

    .form-item-warp {
        padding: 0 12px;
        background: #f9f9fb;
        display: flex;
        align-items: center;
        margin-bottom: 0;

        /deep/ .ant-input-number {
            width: auto;
        }
        .value-type {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 16px;
            height: 16px;
            color: #144bcc;
            border-radius: 4px;
            background: #d4e5ff;
            font-size: 12px;
            font-weight: bold;
            margin-right: 4px;
        }
        .value-values {
            color: #5c5f66;
            font-size: 12px;
            font-weight: 500;
            line-height: 20px;
        }
        .value-limit {
            color: #84868c;
            font-size: 12px;
            font-weight: 400;
            line-height: 20px;
        }
    }

    /deep/ .ant-form label {
        font-size: 12px !important;
    }

    .config-item-plus {
        font-size: 16px;
        color: #2468f2;
        cursor: pointer;
    }

    .config-item-content-knowledge {
        display: flex;
        overflow: auto;
        flex-direction: column;
        box-sizing: border-box;
        max-height: 398px;
        padding: 0 1px;
        gap: 8px;

        .knowledge-item {
            position: relative;
            display: flex;
            align-items: center;
            flex: 1;
            justify-content: space-between;
            box-sizing: border-box;
            height: 32px;
            padding: 6px;
            border: 1px solid rgba(212, 214, 217, .3);
            border-radius: 6px;

            .item-left {
                flex: 1;
                min-width: 0;

                img {
                    width: 20px;
                    height: 20px;
                }
                span {
                    min-width: 0;
                    margin-left: 8px;
                    color: #151b26;
                    font-weight: 500;
                    line-height: 22px;
                }
            }
            img {
                width: 16px;
                height: 16px;
                cursor: pointer;
            }
        }
    }
    .output-child {
        padding-left: 16px;
    }
}
</style>
