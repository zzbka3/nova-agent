<template>
    <div class="input-components-warp">
        <div class="connect-config-item">
            <div class="config-item-header">
                <span class="header-key">参数名</span>
                <span
                    class="header-type"
                    v-if="carrier.includes('body') || isResponse"
                >参数类型</span>
                <span
                    class="header-value"
                    v-if="!isResponse"
                >mock值</span>
                <span
                    class="header-operate"
                    v-if="carrier !== 'response-string'"
                >操作</span>
            </div>

            <a-form
                :form="inputForm"
            >
                <div
                    class="config-item-content-item flex-center"
                    v-for="(dataForm, index) in currInputData"
                    :key="dataForm.id"
                >
                    <a-form-item
                        size="small"
                        class="content-item-module intention-input key-cannotEdit"
                    >
                        <div class="key-item">
                            <a-icon
                                :type="dataForm.expandedVars ? 'caret-down' : 'caret-right'"
                                @click.stop="dataForm.expandedVars = !dataForm.expandedVars"
                                v-if="dataForm.type === 'Object'"
                            />

                            <div
                                class="response-string-default"
                                v-show="carrier === 'response-string'"
                            >
                                result
                            </div>
                            <a-input
                                placeholder="请输入参数名"
                                size="small"
                                @input="extractChange(index, $event, 'field', dataForm, true)"
                                v-decorator="[`extractVarName_${dataForm.id}`, {
                                    rules: [
                                        { required: true, message: '请输入参数名' },
                                        {
                                            pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
                                            message: '只能输入字母、数字、_、-，并以字母开头'
                                        },
                                        { validator: (rule, value, callback) =>
                                            validateUniqueVarName(rule, value, callback, currInputData) }
                                    ]
                                }]"
                                v-show="carrier !== 'response-string'"
                            />
                        </div>
                    </a-form-item>
                    <a-form-item
                        size="small"
                        class="content-item-module varType-select"
                        v-if="carrier.includes('body') || isResponse"
                    >
                        <div
                            class="response-string-default"
                            v-show="carrier === 'response-string'"
                        >
                            String
                        </div>
                        <a-select
                            size="small"
                            v-model="dataForm.type"
                            @change="inputTypeChange($event, dataForm)"
                            :disabled="formatType !== 'json' && !isResponse"
                            :dropdown-match-select-width="false"
                            v-show="carrier !== 'response-string'"
                        >
                            <a-select-option
                                v-for="item in varTypeOption()"
                                :key="item.value"
                            >
                                {{ item.label }}
                            </a-select-option>
                        </a-select>
                    </a-form-item>
                    <a-form-item
                        size="small"
                        class="content-item-module varValue"
                        v-if="!isResponse"
                    >
                        <a-input-number
                            placeholder="请输入参数值"
                            @change="extractChange(index, $event, 'mockValue', dataForm)"
                            size="small"
                            v-decorator="[`extractVarValue_${dataForm.id}`, {
                                rules: [
                                    { required: true, message: '请输入参数值' }
                                ]
                            }]"
                            :step="1"
                            v-if="dataForm.type === 'Number' || dataForm.type === 'Integer'"
                        />
                        <a-select
                            size="small"
                            placeholder="请选择参数值"
                            @change="extractChange(index, $event, 'mockValue', dataForm)"
                            :dropdown-match-select-width="false"
                            v-decorator="[`extractVarValue_${dataForm.id}`, {
                                rules: [
                                    { required: true, message: '请选择参数值' }
                                ]
                            }]"
                            v-else-if="dataForm.type === 'Boolean'"
                        >
                            <a-select-option
                                value="true"
                            >
                                True
                            </a-select-option>
                            <a-select-option
                                value="false"
                            >
                                False
                            </a-select-option>
                        </a-select>
                        <span
                            v-else-if="dataForm.type === 'Object'"
                        ></span>
                        <a-input
                            placeholder="请输入参数值"
                            @input="extractChange(index, $event, 'mockValue', dataForm)"
                            size="small"
                            v-decorator="[`extractVarValue_${dataForm.id}`, {
                                rules: [
                                    { required: true, message: '请输入参数值' }
                                ]
                            }]"
                            v-else
                        />
                    </a-form-item>
                    <div class="operate">
                        <a-icon
                            type="minus-circle"
                            class="del-icon"
                            @click.stop="delExtractChildVars(index, currInputData, true)"
                            v-if="currInputData.length > 1 && index !== currInputData.length - 1"
                        />
                        <a-icon
                            type="plus-circle"
                            class="del-icon add-icon"
                            @click.stop="addChildVars(dataForm)"
                            v-if="dataForm.type === 'Object'"
                        />
                    </div>
                    <div
                        class="varName-child-warp"
                        v-if="dataForm.children && dataForm.children.length > 0"
                    >
                        <div v-show="dataForm.expandedVars">
                            <div
                                class="config-item-content-item flex-center"
                                v-for="(dataFormChildren, indexChild) in dataForm.children"
                                :key="dataFormChildren.id"
                            >
                                <a-form-item
                                    size="small"
                                    class="content-item-module intention-input varName-child"
                                    :class="
                                        parentTypeString(dataFormChildren).includes('Array') ? 'varName-array' : ''
                                    "
                                >
                                    <div class="key-item">
                                        <a-icon
                                            :type="dataFormChildren.expandedVars ? 'caret-down' : 'caret-right'"
                                            @click.stop="dataFormChildren.expandedVars = !dataFormChildren.expandedVars"
                                            v-if="dataFormChildren.type === 'Object'"
                                        />
                                        <a-input
                                            placeholder="请输入参数名"
                                            size="small"
                                            @input="extractChange(indexChild, $event, 'field', dataFormChildren)"
                                            :disabled="isArrayAllBtnNotArrayObject(dataForm.type)"
                                            v-decorator="[`extractVarName_${dataFormChildren.id}`, {
                                                rules: [
                                                    { required: true, message: '请输入参数名' },
                                                    {
                                                        pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
                                                        message: '只能输入字母、数字、_、-，并以字母开头'
                                                    },
                                                    { validator: (rule, value, callback) =>
                                                        validateUniqueVarName(rule, value, callback, dataForm.children)
                                                    }
                                                ]
                                            }]"
                                        />
                                    </div>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="content-item-module varType-select"
                                >
                                    <a-select
                                        size="small"
                                        placeholder="请选择参数类型"
                                        v-model="dataFormChildren.type"
                                        :dropdown-match-select-width="false"
                                        :disabled="isArrayAllBtnNotArrayObject(dataForm.type)"
                                        @change="inputTypeChange($event, dataFormChildren, 'needChange')"
                                    >
                                        <a-select-option
                                            v-for="item in varTypeOption()"
                                            :key="item.value"
                                        >
                                            {{ item.label }}
                                        </a-select-option>
                                    </a-select>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="content-item-module varValue"
                                    v-if="!isResponse"
                                >
                                    <a-input-number
                                        placeholder="请输入参数值"
                                        @change="extractChange(indexChild, $event, 'mockValue', dataFormChildren)"
                                        size="small"
                                        :step="1"
                                        v-decorator="[`extractVarValue_${dataFormChildren.id}`, {
                                            rules: [
                                                { required: true, message: '请输入参数值' }
                                            ]
                                        }]"
                                        v-if="isInputNumberChildren(dataFormChildren.type, dataForm.type)"
                                    />
                                    <a-select
                                        size="small"
                                        placeholder="请选择参数值"
                                        @change="extractChange(
                                            indexChild, $event, 'mockValue', dataFormChildren
                                        )"
                                        v-decorator="[`extractVarValue_${dataFormChildren.id}`, {
                                            rules: [
                                                { required: true, message: '请选择参数值' }
                                            ]
                                        }]"
                                        v-else-if="
                                            isInputBooleanChildren(dataFormChildren.type, dataForm.type)
                                        "
                                    >
                                        <a-select-option
                                            value="true"
                                        >
                                            True
                                        </a-select-option>
                                        <a-select-option
                                            value="false"
                                        >
                                            False
                                        </a-select-option>
                                    </a-select>
                                    <span
                                        v-else-if="dataFormChildren.type === 'Object'"
                                    ></span>
                                    <a-input
                                        placeholder="请输入参数值"
                                        @input="extractChange(indexChild, $event, 'mockValue', dataFormChildren)"
                                        size="small"
                                        v-decorator="[`extractVarValue_${dataFormChildren.id}`, {
                                            rules: [
                                                { required: true, message: '请输入参数值' }
                                            ]
                                        }]"
                                        v-else
                                    />
                                </a-form-item>
                                <div class="operate">
                                    <a-icon
                                        type="minus-circle"
                                        class="del-icon"
                                        @click.stop="delExtractChildVars(indexChild, dataForm)"
                                        v-if="dataForm.children.length > 1"
                                    />
                                    <a-icon
                                        type="plus-circle"
                                        class="del-icon add-icon"
                                        @click.stop="addChildVars(dataFormChildren)"
                                        v-if="dataFormChildren.type === 'Object'"
                                    />
                                </div>
                                <div
                                    class="varName-child-warp"
                                    v-if="dataFormChildren.children && dataFormChildren.children.length > 0"
                                >
                                    <div v-show="dataFormChildren.expandedVars">
                                        <div
                                            class="config-item-content-item flex-center"
                                            v-for="(dataFormChildrenChildren, indexChildChild) in
                                                dataFormChildren.children"
                                            :key="dataFormChildrenChildren.id"
                                        >
                                            <a-form-item
                                                size="small"
                                                class="content-item-module intention-input varName-child-child"
                                                :class="parentTypeString(dataFormChildrenChildren) === 'Object' ?
                                                    'varName-array' : ''"
                                            >
                                                <a-input
                                                    placeholder="请输入参数名"
                                                    size="small"
                                                    @input="extractChange(
                                                        indexChildChild, $event, 'field', dataFormChildrenChildren
                                                    )"
                                                    v-decorator="[`extractVarName_${dataFormChildrenChildren.id}`, {
                                                        rules: [
                                                            { required: true, message: '请输入参数名' },
                                                            {
                                                                pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
                                                                message: '只能输入字母、数字、_、-，并以字母开头'
                                                            },
                                                            { validator: (rule, value, callback) =>
                                                                validateUniqueVarName(
                                                                    rule, value, callback, dataFormChildren.children
                                                                )
                                                            }
                                                        ]
                                                    }]"
                                                />
                                            </a-form-item>
                                            <a-form-item
                                                size="small"
                                                class="content-item-module varType-select"
                                            >
                                                <a-select
                                                    size="small"
                                                    placeholder="请选择参数类型"
                                                    v-model="dataFormChildrenChildren.type"
                                                    :dropdown-match-select-width="false"
                                                    @change="inputTypeChange(
                                                        $event, dataFormChildrenChildren
                                                    )"
                                                >
                                                    <a-select-option
                                                        v-for="item in varTypeApiBodyEasyOption"
                                                        :key="item.value"
                                                    >
                                                        {{ item.label }}
                                                    </a-select-option>
                                                </a-select>
                                            </a-form-item>
                                            <a-form-item
                                                size="small"
                                                class="content-item-module varValue"
                                                v-if="!isResponse"
                                            >
                                                <a-input-number
                                                    placeholder="请输入参数值"
                                                    @change="extractChange(
                                                        indexChildChild, $event, 'mockValue', dataFormChildrenChildren
                                                    )"
                                                    size="small"
                                                    :step="1"
                                                    v-decorator="[`extractVarValue_${dataFormChildrenChildren.id}`, {
                                                        rules: [
                                                            { required: true, message: '请输入参数值' }
                                                        ]
                                                    }]"
                                                    v-if="
                                                        isInputNumber(dataFormChildrenChildren.type)
                                                    "
                                                />
                                                <a-select
                                                    size="small"
                                                    placeholder="请选择参数值"
                                                    @change="extractChange(
                                                        indexChildChild, $event, 'mockValue', dataFormChildrenChildren
                                                    )"
                                                    v-decorator="[`extractVarValue_${dataFormChildrenChildren.id}`, {
                                                        rules: [
                                                            { required: true, message: '请选择参数值' }
                                                        ]
                                                    }]"
                                                    v-else-if="
                                                        isInputBoolean(dataFormChildrenChildren.type)
                                                    "
                                                >
                                                    <a-select-option
                                                        value="true"
                                                    >
                                                        True
                                                    </a-select-option>
                                                    <a-select-option
                                                        value="false"
                                                    >
                                                        False
                                                    </a-select-option>
                                                </a-select>
                                                <a-input
                                                    placeholder="请输入参数值"
                                                    @input="extractChange(
                                                        indexChildChild, $event, 'mockValue', dataFormChildrenChildren
                                                    )"
                                                    size="small"
                                                    v-decorator="[`extractVarValue_${dataFormChildrenChildren.id}`, {
                                                        rules: [
                                                            { required: true, message: '请输入参数值' }
                                                        ]
                                                    }]"
                                                    v-else
                                                />
                                            </a-form-item>
                                            <div class="operate">
                                                <a-icon
                                                    type="minus-circle"
                                                    class="del-icon"
                                                    @click.stop="delExtractChildVars(indexChildChild, dataFormChildren)"
                                                    v-if="dataFormChildren.children.length > 1"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </a-form>
        </div>
    </div>
</template>

<script>
import { varTypeApiBodyOption, varTypeApiArrayOption, varTypeApiBodyEasyOption } from '../../common/commonData';
import { deepClone } from '@baidu/metis-js-util';
import { updateReferenceVarNameById, deleteReferenceVarNameById } from '@/views/flow/basics/lfEvent.js';
import LogicFlow from '@logicflow/core';
import { deleteTempOutputs } from '@/views/flow/getArgs';

export default {
    props: {
        inputData: {
            type: Array,
            default: () => ([])
        }, // 输入参数原始数据
        carrier: {
            type: String,
            default: ''
        },
        formatType: {
            type: String,
            default: ''
        },
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        },
    },
    data() {
        return {
            varTypeApiBodyOption,
            varTypeApiArrayOption,
            varTypeApiBodyEasyOption,
            currInputData: [
                {
                    field: '',
                    type: 'String',
                    varDesc: '',
                }
            ], // 当前输入参数
            inputForm: this.$form.createForm(this),
            maxIndex: new Date().getTime() || '', // 最大索引
        };
    },
    watch: {},
    computed: {
        isResponse() {
            return this.carrier === 'response-json' || this.carrier === 'response-string';
        }
    },
    components: {
        // apiParamsItemComponents
    },
    mounted() {
        this.init();
    },
    methods: {
        init() {
            if (this.inputData?.length > 0) {
                this.currInputData = deepClone(this.inputData);
            }

            this.currInputData.forEach((element) => {
                if (this.carrier === 'params' || this.carrier === 'headers') {
                    this.$set(element, 'type', 'String');
                }
            });

            setTimeout(async() => {
                this.$nextTick(() => {
                    this.deepAssignData(this.currInputData);
                });

            }, 0);
        },
        // 递归赋值
        deepAssignData(arr) {
            return arr.map((item) => {
                // 使用Vue.set确保新属性是响应式的
                this.inputForm.setFieldsValue({
                    [`extractVarName_${item.id}`]: item.field,
                });
                // 当前子对象不是数组对象、对象，赋值（数组对象、对象类型时，没有value，也就不存在extractVarValue_，赋值会报错）
                if (
                    item.type !== 'Object' && (item?.value || item?.mockValue)
                ) {

                    this.inputForm.setFieldsValue({
                        // eslint-disable-next-line max-len
                        [`extractVarValue_${item.id}`]: item.type.includes('Boolean') ? String(item.value || item.mockValue) : item.value || item.mockValue,
                    });
                }

                // 如果当前项有子数组，递归处理
                if (item.children && Array.isArray(item.children)) {
                    this.deepAssignData(item.children);
                }
                return item;
            });
        },

        isJSON(str) {
            try {
                JSON.parse(str);
                return true;
            } catch (e) {
                return false;
            }
        },
        onSearch() {},
        // 输入类型改变
        inputTypeChange(e, dataForm, changeTag) {
            // 参数类型改变需要通知其它节点同步更新
            if (dataForm?.id && this.clickNode?.id) {
                const { id } = dataForm || {};
                deleteReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id
                });
                deleteTempOutputs();
            }
            if (e === 'Number' || e === 'Integer') {
                this.$set(dataForm, 'children', null);
                setTimeout(() => {
                    this.$nextTick(() => {
                        this.inputForm.setFieldsValue({
                            [`extractVarValue_${dataForm.id}`]: 0
                        });
                    });
                }, 0);
            } else if (e === 'Boolean') {
                this.$set(dataForm, 'children', null);
                setTimeout(() => {
                    this.$nextTick(() => {
                        this.inputForm.setFieldsValue({
                            [`extractVarValue_${dataForm.id}`]: 'true'
                        });
                    });

                }, 0);

            } else if (e.includes('Array')) {
                this.$set(dataForm, 'children', null);
                setTimeout(() => {
                    this.$nextTick(() => {
                        this.inputForm.setFieldsValue({
                            [`extractVarValue_${dataForm.id}`]: '[]'
                        });
                    });

                }, 0);
            } else if (e === 'Object') {
                let objectData = [
                    {
                        field: '',
                        type: 'String',
                        id: `${dataForm.id}_${this.uniqueValue()}`,
                        parentType: 'Object',
                        mockValue: '',
                    }
                ];
                this.$set(dataForm, 'children', objectData);
                this.$set(dataForm, 'expandedVars', true);

            } else {
                this.$set(dataForm, 'children', null);
                setTimeout(() => {
                    this.$nextTick(() => {
                        this.inputForm.setFieldsValue({
                            [`extractVarValue_${dataForm.id}`]: ''
                        });
                    });

                }, 0);
            }
            let singleData = dataForm;
            if (changeTag === 'needChange') {
                singleData = null;
            }
            // 参数类型修改，需要抛出值做动态更新
            if (this.carrier.includes('body')) {
                this.change(singleData, 'varTypeChange');
            } else {
                this.change(singleData);
            }
        },
        // 输入值改变
        inputValueChange() {
            this.change();
        },
        // 文件数据改变更新
        change(singleData = undefined, optionMethod) {
            const value = this.currInputData;
            this.$emit('apiParamsChange', value, singleData, optionMethod);
        },
        /**
         * @description: 参数抽取输入框变化
         * @param {number} index 入参索引
         * @param {string} key 参数键
         * @param {string} value 参数值
         * @return {*}
         */
        extractChange(index, value, key, dataForm, isFirst = false) {
            const newVal = value.target ? value.target.value : value;
            this.$set(dataForm, key, newVal);
            // this.$set(this.currInputData[index], key, newVal);
            if (isFirst) {
                if (index === this.currInputData.length - 1) {
                    this.addInputParams();
                }

                this.change(dataForm);
            } else {
                this.change();
            }
            // 参数名改变需要通知其它节点同步更新
            if (key === 'field' && dataForm?.id && this.clickNode?.id) {
                const { id } = dataForm || {};
                updateReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id,
                    updateVarName: newVal
                });
                deleteTempOutputs();
            }
        },
        /**
         * @description: 子集参数抽取输入框变化（二三级通用）
         * @param {number} index 入参索引
         * @param {string} key 参数键
         * @param {string} value 参数值
         * @return {*}
         */
        extractChildChange(index, value, key, dataFormChildren) {
            const newVal = value?.target ? value?.target?.value : value;
            // this.$set(this.currInputData[index], key, newVal);
            this.$set(dataFormChildren, key, newVal);

            if (
                dataFormChildren.type.includes('Array') &&
                dataFormChildren.type !== 'ArrayObject' &&
                dataFormChildren?.children?.length > 0
            ) {
                this.$nextTick(() => {
                    dataFormChildren.children.forEach(element => {
                        this.inputForm.setFieldsValue({
                            [`extractVarName_${element.id}`]: `${newVal}<item>`,
                        });
                    });
                });

            }

            // if (index === dataForm.children.length - 1) {
            //     this.addChildVars(dataForm);
            // }
            this.change();
        },
        addInputParams() {
            let data = {
                field: '',
                type: 'String',
                mockValue: '',
                id: this.uniqueValue(),
            };

            this.currInputData.push(data);
        },
        /**
         * @description: 参数抽取校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @param {array} targetArr 需要校验的数组
         * @return {*}
         */
        validateUniqueVarName(rule, value, callback, targetArr) {
            const allNames = targetArr?.map(v => v.field) || [];
            const duplicates = allNames.filter(name => name === value);

            if (duplicates.length >= 1) {
                callback(new Error('参数名不能重复'));
            } else {
                callback();
            }
        },

        // 删除参数
        delExtractVars(index) {
            let delData = this.currInputData[index];
            this.currInputData.splice(index, 1);

            this.$nextTick(() => {
                this.currInputData.forEach((element) => {
                    this.inputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.field,
                        [`extractVarValue_${element.id}`]: element.value,
                    });
                    if (element.children?.length > 0) {
                        element.children.forEach((item) => {
                            this.inputForm.setFieldsValue({
                                [`extractVarName_${item.id}`]: item.field,
                                [`extractVarValue_${item.id}`]: item.value,
                            });
                        });
                    }
                });
            });
            this.change(delData, 'delete');
        },
        // 子级删除同级数据（二三级通用）
        delExtractChildVars(index, dataForm, isFirst = false) {
            let varId = '';
            let delData = {};
            if (isFirst) {
                delData = dataForm[index] || {};
                varId = delData?.id;
                dataForm.splice(index, 1);
            } else {
                varId = dataForm.children[index]?.id;
                dataForm.children.splice(index, 1);
            }
            deleteReferenceVarNameById({
                nodeId: this.clickNode?.id,
                lf: this.lf,
                varNameId: varId
            });
            this.$nextTick(() => {
                if (dataForm.children?.length > 0) {
                    dataForm.children.forEach((item) => {
                        this.inputForm.setFieldsValue({
                            [`extractVarName_${item.id}`]: item.field,
                            [`extractVarValue_${item.id}`]: item.mockValue,
                        });
                    });
                }
            });
            if (isFirst) {
                this.change(delData, 'delete');
            } else {
                this.change();
            }

        },
        // 参数类型不同给予不同的选项option(暂时屏蔽传参判断)
        varTypeOption() {
            let options = varTypeApiBodyOption;
            // if (data.parentType && data.parentType === 'Array') {
            //     options = varTypeApiArrayOption;
            // }

            return options;
        },
        // 父级类型是否是Array
        parentTypeString(data) {
            let str = data.parentType || '';
            return str;
        },
        // 子级Array类型和Object类型下新增同级数据（二三级通用）
        addChildVars(dataForm) {
            let item = {
                field: '',
                type: 'String',
                id: this.uniqueValue(),
                parentType: dataForm.type,
                mockValue: '',
            };
            dataForm.children.push(item);
            this.$nextTick(() => {
                this.inputForm.setFieldsValue({
                    [`extractVarName_${item.id}`]: `${item.field}`,
                    [`extractVarValue_${item.id}`]: '',
                });
            });
        },
        // 判断是否是Number或者Integer
        isInputNumber(type) {
            return type === 'ArrayNumber' || type === 'ArrayInteger' || type === 'Number' || type === 'Integer';
        },
        // 判断是否是Boolean
        isInputBoolean(type) {
            return type === 'ArrayBoolean' || type === 'Boolean';
        },
        isInputNumberChildren(type, parentType) {
            return (type === 'Number' || type === 'Integer') ||
            (parentType === 'ArrayNumber' || parentType === 'ArrayInteger');
        },
        isInputBooleanChildren(type, parentType) {
            return type === 'Boolean'|| parentType === 'ArrayBoolean';
        },

        // 判断是否是Array或者object
        isInputArrayAndObject(type) {
            return type === 'Object' || type === 'ArrayObject';
        },
        isArrayAllBtnNotArrayObject(type) {
            return type.includes('Array') && type !== 'ArrayObject';
        },
        // 生成唯一值
        uniqueValue() {
            return Date.now().toString(36) + Math.random().toString(36);
        },

    }
};
</script>

<style lang="less" scoped>
.flex-center {
    display: flex;
    align-items: center;
}
.input-components-warp {
    .connect-config-item {
        display: block;
        padding: 0;

        .config-item-minus {
            margin-left: 10px;
        }
    }
    /deep/ .ant-form-item {
        font-size: 12px;
        margin-bottom: 0 !important;
    }

    .config-item-header {
        display: flex;
        align-items: center;
        background: #f7f7f9;
        gap: 4px;
        margin-bottom: 4px;
        border-radius: 4px 4px 0 0;

        span {
            height: 32px;
            position: relative;
            padding-left: 12px;
            box-sizing: border-box;
            height: 100%;
            font-size: 12px;
            line-height: 32px;
            color: #5c5f66;
        }

        .header-key {
            flex: 0 0 45%;
        }
        .header-key::after {
            content: "";
            position: absolute;
            display: block;
            top: 0;
            right: 0;
            width: 1px;
            height: 100%;
            background: #fff;
        }
        .header-type {
            flex: 0 0 17.5%;
        }
        .header-type::after {
            content: "";
            position: absolute;
            display: block;
            top: 0;
            right: 0;
            width: 1px;
            height: 100%;
            background: #fff;
        }
        .header-value {
            flex: 0 0 27.5%;
        }
        .header-value::after {
            content: "";
            position: absolute;
            display: block;
            top: 0;
            right: 0;
            width: 1px;
            height: 100%;
            background: #fff;
        }
        .header-operate {
            flex: 1;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding-left: 0;

        }
    }

    .config-item-content-item {
        display: flex;
        align-items: flex-start;
        gap: 4px;
        color: #151b26;
        border-top: 1px solid #e8e9eb;
        word-break: break-word;
        flex-wrap: wrap;

        .key-item {
            display: inline-flex;
            align-items: center;
        }
        .content-item-module {
            position: relative;
            padding-left: 6px;
            box-sizing: border-box;
            height: 100%;
            font-size: 12px !important;
            color: #5c5f66;
            display: flex;
        }
        .intention-input {
            width: 45%;
        }
        .varType-select {
            width: 17.5%;
        }
        /deep/ .varValue {
            flex: 0 0 27.5%;

        }
        .operate {
            flex: 1;
            text-align: center;
            padding-top: 12px;

            .del-icon {
                margin: auto;
                color: #151b26;
                font-size: 14px;
                display: block;
                margin-bottom: 5px;

            }
            .add-icon {
            }

        }

        /deep/ .ant-input {
            border: none;
        }
        /deep/ .ant-select-selection {
            border: none;
        }
        .varName-child {
            padding-left: 20px;
        }
        .varName-array {
            /deep/ .ant-input {
                border: 1px solid #d9d9d9;
            }

        }
        .varName-child-child {
            padding-left: 40px;
        }

    }

    /deep/ .ant-select-selection-selected-value {
        .arg-type {
            display: none;
        }
    }

    .varName-child-warp {
        width: 100%;
        .intention-input {
            // width: calc(45% - 20px);
        }
    }
    .response-string-default {
        font-size: 12px;
        color: #5c5f66;
        padding-left: 8px;

    }
}
/deep/ .tree-select-dropdown {
    left: -57px !important;
    li {
        margin: 2px 0;
    }
    .ant-select-tree-title {
        font-size: 12px;
    }
}
.config-item-plus {
    font-size: 16px;
    cursor: pointer;
}
.config-item-content-key::after {
    content: '*';
    color: #f33d3d;
}

</style>