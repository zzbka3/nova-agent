<template>
    <div class="input-components-warp">
        <div class="connect-config-item">
            <div class="config-item-title">
                <div class="flex-center">
                    <span>
                        {{ getText() }}
                    </span>
                </div>
                <div
                    @click="addInputParams"
                    v-if="isDynamics"
                >
                    <a-icon
                        class="config-item-plus"
                        type="plus"
                    />
                </div>
            </div>
            <div class="config-item-header">
                <span class="header-key">{{ headerKey ? headerKey : '参数名' }}</span>
                <span class="header-type">类型</span>
                <span class="header-value">{{ headerValue ? headerValue : isOutputDesc ? '描述' : '值' }}</span>
            </div>
            <a-form
                :form="inputForm"
            >
                <div
                    class="config-item-content-item flex-center"
                    v-for="(dataForm, index) in currInputData"
                    :key="dataForm.id"
                >
                    <!-- 参数名 -->
                    <a-form-item
                        size="small"
                        class="intention-input key-cannotEdit"
                    >
                        <div class="config-item-content-key-warp">
                            <a-tooltip
                                :title="dataForm.varName"
                            >
                                <span
                                    class="config-item-content-key"
                                    v-show="!isDynamics"
                                >
                                    {{ dataForm.varName }}
                                </span>
                            </a-tooltip>
                            <span
                                class="originalVarType"
                                v-show="dataForm.originalVarType && !customInput"
                            >{{ dataForm.originalVarType }}</span>
                        </div>
                        <template>
                            <!-- 自定义选择参数 -->
                            <a-tree-select
                                v-if="isDynamics && customInput"
                                size="small"
                                :tree-data="customInputConfig"
                                placeholder="请选择引用值"
                                tree-default-expand-all
                                :replace-fields="replaceFields"
                                v-model="dataForm.varName"
                                @change="selectCustomValue(index, $event, dataForm)"
                                :get-popup-container="(trigger) => trigger.parentNode"
                                :dropdown-style="{
                                    maxHeight: '300px',
                                    overflow: 'auto',
                                    fontSize: '12px',
                                    width: '260px',
                                }"
                            >
                            </a-tree-select>

                            <a-input
                                placeholder="请输入参数名"
                                size="small"
                                @change="extractChange(index, $event, 'varName')"
                                v-decorator="[`extractVarName_${dataForm.id}`, {
                                    rules: [
                                        { required: true, message: '请输入参数名'},
                                        {
                                            pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
                                            message: '只能输入字母、数字、_、-，并以字母开头'
                                        },
                                        { validator: validateUniqueVarName }
                                    ]
                                }]"
                                v-if="isDynamics && !customInput"
                            />
                        </template>
                    </a-form-item>
                    <!-- 参数类型 -->
                    <a-form-item
                        size="small"
                        class="varType-select"
                    >
                        <a-select
                            size="small"
                            v-model="dataForm.varType"
                            @change="inputTypeChange($event, dataForm, index)"
                        >
                            <a-select-option
                                v-for="item in varTypeOptionsForItemHandle(dataForm.varTypeOptionsForItem)"
                                :key="item.value"
                            >
                                <a-tooltip
                                    :title="item.label"
                                >
                                    {{ item.label }}
                                </a-tooltip>
                            </a-select-option>
                        </a-select>
                    </a-form-item>
                    <!-- 参数值 (引用类型) -->
                    <a-form-item
                        size="small"
                        class="varValue"
                        v-if="dataForm.varType === 'reference'"
                    >
                        <a-tree-select
                            size="small"
                            :tree-data="treeData"
                            @focus="loadTreeData(dataForm)"
                            placeholder="请选择引用值"
                            tree-default-expand-all
                            :replace-fields="replaceFields"
                            v-show="dataForm.varType === 'reference'"
                            v-decorator="[`extractTreeData_${dataForm.id}`, {
                                rules: [
                                    { validator: validateTree }
                                ]
                            }]"
                            dropdown-class-name="tree-select-dropdown"
                            label-in-value
                            @change="selectTreeChange(index, $event, dataForm)"
                            :get-popup-container="(trigger) => trigger.parentNode"
                            show-checked-strategy="SHOW_PARENT"
                            :dropdown-style="{
                                maxHeight: '300px',
                                overflow: 'auto',
                                fontSize: '12px',
                                width: '260px',
                            }"
                        >
                        </a-tree-select>
                    </a-form-item>
                    <!-- 参数值 (非引用类型) -->
                    <a-form-item
                        size="small"
                        class="varValue"
                        v-if="!isOutputDesc && dataForm.varType !== 'reference'"
                    >
                        <!-- boolean值下拉框 -->
                        <a-select
                            size="small"
                            placeholder="请输入参数值"
                            v-decorator="[`extractVarValue_${dataForm.id || index}`, {
                                rules: [
                                    { required: true, message: '请输入参数值' }
                                ]
                            }]"
                            @change="extractChange(index, $event, 'varValue')"
                            v-if="dataForm.varType === 'Boolean'"
                        >
                            <a-select-option value="true">
                                True
                            </a-select-option>
                            <a-select-option value="false">
                                False
                            </a-select-option>
                        </a-select>
                        <!-- 非boolean值输入框 -->
                        <a-input
                            placeholder="请输入参数值"
                            @change="extractChange(index, $event, 'varValue')"
                            size="small"
                            v-decorator="[`extractVarValue_${dataForm.id || index}`, {
                                rules: [
                                    { required: true, message: '请输入参数值' }
                                ]
                            }]"
                            v-else
                        />
                    </a-form-item>
                    <!-- 输出可以配置字段描述 -->
                    <a-form-item
                        size="small"
                        class="varValue"
                        v-if="isOutputDesc && dataForm.varType !== 'reference'"
                    >
                        <a-input
                            placeholder="请输入参数描述"
                            @input="extractChange(index, $event, 'varDesc')"
                            size="small"
                            v-decorator="[`extractVarDesc_${dataForm.id || index}`]"
                        />
                    </a-form-item>
                    <a-icon
                        type="minus-circle"
                        class="del-icon"
                        @click.stop="delExtractVars(index)"
                        v-if="isDynamics && currInputData.length > 1"
                    />
                </div>
            </a-form>
        </div>
    </div>
</template>

<script>
import {
    getAllCanSelectArgs,
    deleteTempOutputs
} from '@/views/flow/getArgs';
import LogicFlow from '@logicflow/core';

import { processTreeData, uniqueValue } from '@/views/flow/common/common';
import {
    updateReferenceVarNameById,
    deleteReferenceVarNameById
} from '@/views/flow/basics/lfEvent.js';
import { varTypeApiBodyOption } from '@/views/flow/common/commonData';

export default {
    props: {
        varTypeOptions: {
            type: Array,
            default: () => ([])
        }, // 输入类型
        inputValueOptions: {
            type: Array,
            default: () => ([])
        }, // 引用参数列表
        inputData: {
            type: Array,
            default: () => ([])
        }, // 输入参数原始数据
        allFlatArgs: {
            type: Array,
            default: () => ([])
        },
        isDynamics: {
            type: Boolean,
            default: false
        },
        isOutput: {
            type: Boolean,
            default: false
        },
        inputTooltip: {
            type: String,
            default: '输入描述'
        },
        // 自定义参数名选择方式
        customInput: {
            type: Boolean,
            default: false
        },
        customInputConfig: {
            type: Array,
            default: () => ([])
        },
        // 输出参数是否需要描述
        isOutputDesc: {
            type: Boolean,
            default: false
        },
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        },
        bodyType: {
            type: String,
            default: ''
        },
        needFilter: {
            type: Boolean,
            default: false
        }, // 是否需要过滤数据（api专用）
        titleText: {
            type: String,
            default: ''
        },
        headerKey: {
            type: String,
            default: ''
        },
        headerValue: {
            type: String,
            default: ''
        },
    },
    data() {
        return {
            varTypeApiBodyOption,
            inputType: 'String',
            replaceFields: { // 树形控件字段映射
                title: 'title',
                value: 'key',
                label: 'label',
                children: 'children'
            },
            currInputData: [], // 当前输入参数
            inputForm: this.$form.createForm(this),
            maxIndex: 0, // 最大索引
            treeData: [], // 可选择树结构
        };
    },
    watch: {
        // 数据变化时处理数据，当数据中存在originalVarType时，设置varTypeOptionsForItem字段，代替varTypeOptions
        currInputData: {
            handler(val) {
                val.map((element) => {
                    const { originalVarType = '' } = element || {};
                    if (originalVarType.length > 0) {
                        let varTypeItem = varTypeApiBodyOption.find(item => item.value === originalVarType);
                        let varTypeOptionsForItem = [
                            { label: '引用', value: 'reference' },
                            varTypeItem
                        ];
                        this.$set(element, 'varTypeOptionsForItem', varTypeOptionsForItem);
                    }
                });
            }
        }
    },
    mounted() {
        if (this.inputData?.length > 0) {
            if (this.needFilter) {
                if (this.bodyType === 'x_www_form_urlencoded') {
                    this.currInputData = this.inputData.filter(item =>
                    ('urlencoded' === item?.requestType ||
                        'header' === item?.requestType ||
                        'params' === item?.requestType
                    )
                    );
                } else if (this.bodyType === 'form_data') {
                    this.currInputData = this.inputData.filter(item =>
                    ('formData' === item?.requestType ||
                        'header' === item?.requestType ||
                        'params' === item?.requestType
                    )
                    );
                } else if (this.bodyType === 'json') {
                    this.currInputData = this.inputData.filter(item =>
                    ('json' === item?.requestType ||
                        'header' === item?.requestType ||
                        'params' === item?.requestType
                    )
                    );
                } else {
                    this.currInputData = this.inputData.filter(item =>
                    ('header' === item.requestType ||
                        'params' === item.requestType
                    )
                    );
                }
            } else {
                this.currInputData = this.inputData;
            }
        }
        this.currInputData.map((element) => {
            if (!element.id) {
                this.$set(element, 'id', uniqueValue());
            }
            const { varType = '', referenceNodeId, referenceVarName } = element || {};
            // 引用类型
            if (varType === 'reference') {
                element.referenceTreeData = {
                    label: this.getTreeLabel(element),
                    value: referenceNodeId ? referenceNodeId + '___' + referenceVarName : '',
                };
                this.$nextTick(() => {
                    this.inputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.varName,
                        [`extractTreeData_${element.id}`]: element.referenceTreeData,
                    });
                });
            } else {
                // 非引用类型
                this.$nextTick(() => {
                    this.inputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.varName,
                        [`extractVarValue_${element.id}`]: element.varValue,
                        [`extractVarDesc_${element.id}`]: element.varDesc,
                    });
                });
                if (this.isOutputDesc) {
                    this.$nextTick(() => {
                        this.inputForm.setFieldsValue({
                            [`extractVarDesc_${element.id}`]: element.varDesc,
                        });
                    });
                }
            }
            return element;
        });
        console.log('输入参数初始化', this.currInputData);
        this.initValidate();
    },
    methods: {
        /**
        * 自定义树选中的内容
        *
        * @returns 返回树节点的标签字符串
        */
        getTreeLabel(data) {
            const { referenceNodeId, varValue, referenceTreeData } = data || {};
            if (referenceNodeId && varValue) {
                // 开始节点需要用系统参数来描述
                if (+referenceNodeId === 1) {
                    return `系统参数/${varValue}`;
                }
                const { nodeName = '' } = this.lf.getNodeModelById(referenceNodeId)?.getProperties() || {};
                return nodeName ? `${nodeName}/${varValue}` : varValue;
            }
            if (referenceTreeData?.label) {
                return referenceTreeData?.label;
            }
            if (varValue) {
                return varValue;
            }
            return '请选择引用值';
        },
        /**
         * 初始化验证函数
         *
         * 该函数在延迟100毫秒后执行，用于初始化表单验证。
         * 首先打印当前表单的值，然后验证表单字段，最后打印输入变量表单的验证结果。
         */
        initValidate() {
            setTimeout(() => {
                // this.intentForm?.validateFields();
                this.inputForm?.validateFields();
                // this.$refs.outputVarsForm?.inputForm?.validateFields();
            }, 300);
        },
        onSearch() { },
        // 输入类型改变
        inputTypeChange(e, dataForm, index) {
            if (e.includes('Array')) {
                dataForm.varValue = '[]';
                dataForm.referenceNodeId = '';
                dataForm.referenceVarName = '';
                dataForm.referenceVarType = '';
                dataForm.referenceVarId = '';
                this.inputForm.setFieldsValue({
                    [`extractVarValue_${dataForm.id || index}`]: '[]'
                });
            } else if (e === 'reference') {
                dataForm.varValue = null;
                this.inputForm.setFieldsValue({
                    [`extractVarValue_${dataForm.id || index}`]: null
                });
            } else if (e === 'Boolean') {
                dataForm.varValue = null;
                this.inputForm.setFieldsValue({
                    [`extractVarValue_${dataForm.id || index}`]: null
                });
            } else {
                dataForm.varValue = '';
                dataForm.referenceNodeId = '';
                dataForm.referenceVarName = '';
                dataForm.referenceVarType = '';
                dataForm.referenceVarId = '';
                this.inputForm.setFieldsValue({
                    [`extractVarValue_${dataForm.id || index}`]: ''
                });
            }
            // 类型变化
            if (this.isOutput) {
                const { id } = this.currInputData[index] || {};
                deleteReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id
                });
                deleteTempOutputs();
            }
            this.change();
        },
        inputValueChange() {
            this.change();
        },
        /**
        * 处理选择树组件变化的事件
        *
        * @param index 选择树的索引
        * @param e 选择树变化的事件对象
        * @param dataForm 数据表单对象
        */
        selectTreeChange(index, e, dataForm) {
            const data = this.allFlatArgs.find(item =>
                item.referenceNodeId + '___' + (item.realNamePath || item.varName) === e?.value
            );
            const { referenceNodeId, realNamePath, varName, originalVarType, varType, id } = data || {};
            this.$set(dataForm, 'referenceNodeId', referenceNodeId);
            this.$set(dataForm, 'varValue', varName);
            this.$set(dataForm, 'referenceVarName', realNamePath || varName);
            this.$set(dataForm, 'referenceVarType', originalVarType || varType);
            this.$set(dataForm, 'referenceVarId', id);
            this.$set(dataForm, 'referenceTreeData', {
                label: this.getTreeLabel(dataForm),
                value: referenceNodeId + '___' + (realNamePath || varName),
            });
            this.$nextTick(() => {
                this.inputForm.setFieldsValue({
                    [`extractTreeData_${dataForm.id}`]: dataForm.referenceTreeData,
                });
            });
            this.change();
        },
        // 自定义参数名改变
        selectCustomValue(index, e, dataForm) {
            this.$set(dataForm, 'varName', e);
            const targetConfig = this.customInputConfig[0]?.children.find(item => item.varName === e);
            const data = this.customInputConfig[0]?.children.find(item =>
                item.referenceNodeId + '___' + (item.realNamePath || item.varName) === e
            );
            if (data) {
                const { varName, id } = data || {};
                this.$set(dataForm, 'varName', varName);
                this.$set(dataForm, 'varNameId', id);
            }
            // 设置入参的原始类型
            if (targetConfig || data) {
                this.$set(dataForm, 'originalVarType', targetConfig?.varType || data.varType);
            } else {
                this.$set(dataForm, 'originalVarType', '');
            }
            dataForm.varValue = undefined;
            dataForm.referenceNodeId = '';
            dataForm.referenceVarName = '';
            dataForm.referenceVarType = '';
            dataForm.referenceVarId = '';
            const { varType, id } = dataForm || {};
            let updataKey = '';
            if (varType === 'reference') {
                updataKey = `extractTreeData_${id}`;
            } else {
                updataKey = `extractVarValue_${id}`;
            }
            this.$nextTick(() => {
                this.inputForm.setFieldsValue({
                    [updataKey]: ''
                });
                this.inputForm.setFieldsValue({
                    [`extractVarValue_${dataForm.id || index}`]: ''
                });
            });
            this.change();
        },
        // 文件数据改变更新
        change() {
            const value = this.currInputData;

            this.$emit('changeInputData', value);
        },
        /**
         * @description: 参数抽取输入框变化
         * @param {number} index 入参索引
         * @param {string} key 参数键
         * @param {string} value 参数值
         * @return {*}
         */
        extractChange(index, value, key) {
            const newVal = value.target ? value.target.value : value;
            // this.$set(this.currInputData[index], key, newVal);
            this.currInputData[index][key] = newVal;
            this.change();
            // 更新输出参数的引用变量名
            if (key === 'varName' && this.isOutput) {
                const { id, varName } = this.currInputData[index] || {};
                updateReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id,
                    updateVarName: varName
                });
                deleteTempOutputs();
            }
        },
        addInputParams() {
            let currMax = Math.max(...this.currInputData.map(item => item.id));
            console.log('currMax', currMax);

            this.maxIndex = currMax + 1;
            let data = {
                varName: undefined,
                varType: this.isOutputDesc ? 'String' : 'reference',
                varDesc: '',
                id: uniqueValue(),
                varValue: undefined,
                referenceNodeId: '',
                referenceVarName: '',
                referenceVarType: ''
            };
            this.currInputData.push(data);
        },
        /**
         * @description: 参数抽取校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @return {*}
         */
        validateUniqueVarName(rule, value, callback) {
            const allNames = this.currInputData?.map(item => item.varName) || [];
            const duplicates = allNames.filter(name => name === value && name);
            if (duplicates.length > 1) {
                callback(new Error('参数名不能重复'));
            } else {
                callback();
            }
        },
        /**
        * 校验树形数据
        *
        * @param rule 校验规则对象
        * @param value 树形数据
        * @param callback 回调函数，用于处理校验结果
        */
        validateTree(rule, value, callback) {
            // 没有配置
            if (!value) {
                callback(new Error('请选择引用值'));
            }
            if (value?.label.includes('/') && !value?.value) {
                callback(new Error('引用字段不存在'));
            }
            if (!value?.value) {
                callback(new Error('请选择引用值'));
            }
            callback();
        },
        delExtractVars(index) {
            // 删除输出参数时，删除引用变量名
            if (this.isOutput) {
                deleteReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: this.currInputData[index]?.id
                });
            }
            this.currInputData.splice(index, 1);
            this.$nextTick(() => {
                this.currInputData.forEach((element) => {
                    this.inputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.varName,
                        [`extractVarValue_${element.id}`]: element.varValue,
                    });
                });
            });

            this.change();
        },

        apiInputChange(inputParams) {
            console.log('inputParams', inputParams);

            this.currInputData = inputParams;
            this.$nextTick(() => {
                this.currInputData.forEach((element) => {
                    this.inputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.varName,
                        [`extractVarValue_${element.id}`]: element.varValue,
                    });
                });
            });
        },
        /**
         * 右侧下拉框是否禁用
         * @param {value} value 值
         * @return {boolean} true/false
         */
        getIsDisabled(value) {

            return ['EMPTY', 'NOT_EMPTY'].includes(value);
        },
        /**
        * 加载树结构数据
        *
        */
        loadTreeData(value) {
            this.treeData = this.getRightTreeData(value);
        },
        /**
         * 获取右侧下拉框的选项
         * @param {value} value 值
         * @return {*}
        */
        getRightTreeData(value) {
            const { id } = this.clickNode;
            const referenceVarType = value.originalVarType || '';
            const op = 'EQUAL';
            const data = {
                varType: referenceVarType,
                opOption: op,
                addArgs: [],
                nodeId: id,
                lf: this.lf
            };
            const { argsMap } = getAllCanSelectArgs(data);
            const arr = processTreeData(argsMap, this.$createElement);
            return arr;
        },
        /**
         * 输入/输出标题文字
         * @return {*} 标题文字
        */
        getText() {
            if (this.titleText) {
                return this.titleText;
            }
            return this.isOutput ? '输出' : '输入';
        },
        /**
         * 获取该行的类型选项
         * @param {Array} data 该行的类型选项
         * @return {Array} 该行的类型选项或默认类型选项
        */
        varTypeOptionsForItemHandle(data) {
            if (data && data.length > 0) {
                return data;
            } else {
                return this.varTypeOptions;
            }
        }
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
        font-size: 12px;

        .config-item-minus {
            margin-left: 10px;
        }
    }
    /deep/ .ant-form-item {
        font-size: 12px;
        margin-bottom: 0 !important;
        .ant-form-item-control {
            line-height: 24px;
            .ant-form-explain {
                font-size: 12px;
            }
        }
    }

    .config-item-header {
        display: flex;
        align-items: center;
        color: #84868c;
        gap: 4px;
        font-size: 12px;
        height: 24px;

        .header-key {
            width: 120px;
        }
        .header-type {
            flex: 0 0 80px;
        }
    }

    .config-item-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 6px;
        font-weight: bold;

        span {
            margin-right: 10px;
            font-weight: bold;
            color: #151b26;
            font-size: 14px;
        }
    }

    .config-item-content-item {
        display: flex;
        align-items: flex-start;
        gap: 4px;
        margin-bottom: 5px;
        color: #151b26;

        .intention-input {
            width: 120px;
            font-size: 12px;

            .config-item-content-key {
                max-width: 60px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
            /deep/ .ant-select {
                width: 120px !important;
            }
        }
        .varType-select {
            width: 80px;
            font-size: 12px;
        }
        /deep/ .varValue {
            width: 142px;
            font-size: 12px;
            .arg-type {
                // display: none;
            }
        }
        .del-icon {
            color: #151b26;
            font-size: 14px;
            margin-top: 5px;
        }


    }

    /deep/ .ant-select-selection-selected-value {
        .arg-type {
            display: none;
        }
    }
}
/deep/ .tree-select-dropdown {
    left: -115px !important;
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
.config-item-content-key::before {
    content: '*';
    color: #f33d3d;
}
.originalVarType {
    display: inline-block;
    height: 20px;
    line-height: 20px;
    margin-left: 4px;
    padding: 0 5px;
    white-space: nowrap;
    border-radius: 4px;
    background-color: #e8e9eb;
}
.config-item-content-key-warp {
    display: flex;
    align-items: center;
}
</style>