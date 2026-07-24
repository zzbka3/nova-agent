<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-09-04 16:17:06
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-11-03 16:23:28
 * @FilePath: /metis-front/src/views/flow/nodeConfig/memoryConfig.vue
 * @Description: 记忆变量
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
                v-model="mod"
                @change="modeRadioChange"
            >
                <a-radio
                    :value="item.value"
                    v-for="item in memoryModOption"
                    :key="item.value"
                >
                    <span>{{ item.label }}</span>
                </a-radio>
            </a-radio-group>
        </div>
        <!-- 写入配置 -->
        <div
            class="input-vars"
            v-if="inputVars && inputVars.length > 0 && mod === 'write'"
        >
            <div class="connect-config-item">
                <InputComponents
                    :var-type-options="varTypeOptions"
                    :input-value-options="arrArgs"
                    :all-flat-args="allFlatArgs"
                    :input-data="inputVars"
                    :is-dynamics="true"
                    :is-output="true"
                    :custom-input="true"
                    :custom-input-config="workFlowVarsConfig"
                    :click-node="clickNode"
                    :lf="lf"
                    @changeInputData="changeInputData"
                    title-text="写入配置"
                    header-key="写入变量"
                    header-value="写入值"
                ></InputComponents>
            </div>
        </div>
        <div
            class="input-vars"
            v-if="mod === 'read'"
        >
            <div class="connect-config-item">
                <div class="config-item-title">
                    <div class="flex-center">
                        <span>
                            读取配置
                        </span>
                    </div>
                    <div @click="addInputParams">
                        <a-icon
                            class="config-item-plus"
                            type="plus"
                        />
                    </div>
                </div>
                <div class="config-item-header">
                    <span class="header-key">输出参数</span>
                    <span class="header-value">读取变量</span>
                </div>
                <a-form :form="inputForm">
                    <div
                        class="config-item-content-item flex-center"
                        v-for="(dataForm, index) in readVars"
                        :key="dataForm.id"
                    >
                        <a-form-item
                            size="small"
                            class="intention-input"
                        >
                            <a-input
                                placeholder="请输入参数名"
                                size="small"
                                @change="extractChange(index, $event, 'varName')"
                                v-decorator="[`extractVarName_${dataForm.id}`, {
                                    rules: [
                                        { required: true, message: '请输入参数名' },
                                        {
                                            pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
                                            message: '只能输入字母、数字、_、-，并以字母开头'
                                        },
                                        { validator: validateUniqueVarName }
                                    ]
                                }]"
                            />
                        </a-form-item>
                        <a-form-item
                            size="small"
                            class="intention-input"
                        >
                            <a-tree-select
                                size="small"
                                :tree-data="workFlowVarsConfig"
                                placeholder="请选择引用值"
                                tree-default-expand-all
                                :replace-fields="replaceFields"
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
                        <a-icon
                            type="minus-circle"
                            class="del-icon"
                            @click.stop="delExtractVars(index)"
                        />
                    </div>
                </a-form>
            </div>
        </div>
        <!-- 输出 -->
        <div class="input-vars">
            <varsTree
                :tree-data="outputVars"
                title="输出"
            />
        </div>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import { memoryModOption } from '@/views/flow/common/commonData';
import { memorySchemaList } from '@/views/flow/common/modelList';
import { getAllFlatArgs, getAllArgs, deleteTempOutputs } from '@/views/flow/getArgs';
import { checkReferenceVarName, processTreeData, uniqueValue } from '@/views/flow/common/common';
import InputComponents from './components/InputComponents';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import { updateReferenceVarNameById, deleteReferenceVarNameById } from '@/views/flow/basics/lfEvent.js';
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
            mod: 'write', // 模式
            memoryModOption, // 模式单选框
            memorySchemaList, // 记忆变量列表
            inputVars: [], // 写入配置
            outputVars: [], // 输出配置
            readVars: [], // 读取配置
            varTypeOptions: [ // 变量类型
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
            ],
            replaceFields: { // 树形控件字段映射
                title: 'title',
                value: 'key',
                label: 'label',
                children: 'children'
            },
            arrArgs: [], // 系统参数树
            allFlatArgs: [], // 平铺后的系统参数树
            workFlowVarsConfig: [], // 记忆变量树
            inputForm: this.$form.createForm(this),
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
        },
    },
    components: {
        InputComponents,
        varsTree
    },
    methods: {
        /**
         * @description: 初始化方法
         * @return {*}
         */
        init() {
            const { id } = this.clickNode;
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            this.workFlowVarsConfig = processTreeData(this.memorySchemaList, this.$createElement);
            const { mod = 'write', inputVars = [], outputVars = [], readVars = [] } = this.propertiesData;
            const inputVarsData = [
                {
                    varName: null,
                    varType: 'reference',
                    varValue: null,
                    referenceNodeId: '',
                    referenceVarName: null,
                    referenceVarType: '',
                    originalVarType: 'String'
                }
            ];
            this.mod = mod;
            if (inputVars && inputVars.length > 0) {
                this.inputVars = checkReferenceVarName({ inputVars, allFlatArgs: this.allFlatArgs });
            } else {
                this.inputVars = inputVarsData;
            }
            this.readVars = readVars;
            this.outputVars = outputVars;
            this.handleDisableWorkFlowVars();
        },
        /**
         * @description: 更新记忆变量可选
         * @return {*}
         */
        handleDisableWorkFlowVars() {
            this.workFlowVarsConfig[0].children.forEach(item => {
                this.$set(item, 'disabled', false);
            });
            if (this.mod === 'read') {
                // read 模式 -> 处理 readVars，使用 referenceVarName，并设置表单
                this.$nextTick(() => {
                    this.readVars.forEach(item => {
                        const workFlowIndex = this.workFlowVarsConfig[0].children.findIndex(
                            v => v.varName === item.referenceVarName
                        );
                        if (workFlowIndex > -1) {
                            this.$set(this.workFlowVarsConfig[0].children[workFlowIndex], 'disabled', true);
                        }
                        this.inputForm.setFieldsValue({
                            [`extractVarName_${item.id}`]: item.varName,
                            [`extractTreeData_${item.id}`]: item.referenceTreeData,
                        });
                    });
                });
            } else {
                // write 模式 -> 处理 inputVars，使用 varName，不需要设置表单
                this.inputVars.forEach(item => {
                    const workFlowIndex = this.workFlowVarsConfig[0].children.findIndex(
                        v => v.varName === item.varName
                    );
                    if (workFlowIndex > -1) {
                        this.$set(this.workFlowVarsConfig[0].children[workFlowIndex], 'disabled', true);
                    }
                });
            }
        },
        /**
         * @description: 模式单选框变化
         * @return {*}
         */
        async modeRadioChange() {
            this.handleDisableWorkFlowVars();
            await this.setOutputVars();
            this.updateLf();
        },
        /**
         * @description: 更新逻辑流图
         * @return {*}
         */
        updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            edgeModel.setProperties({
                mod: this.mod,
                inputVars: this.inputVars,
                outputVars: this.outputVars,
                readVars: this.readVars
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
        /**
         * @description: 修改读取数据
         * @return {*}
         */
        async changeInputData(value) {
            this.inputVars = value;
            this.handleDisableWorkFlowVars();
            await this.setOutputVars();
            this.updateLf();
        },
        /**
         * @description: 添加读取数据
         * @return {*}
         */
        addInputParams() {
            this.readVars.push({
                varName: null,
                varType: 'String',
                varValue: null,
                id: uniqueValue(),
                referenceNodeId: '',
                referenceVarName: null,
                referenceVarType: '',
            });
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
            if (!value || !value?.value) {
                callback(new Error('请选择引用值'));
            }
            callback();
        },
        /**
         * @description: 修改读取值
         * @param {number} index 数据下标
         * @param {object} e 数据
         * @param {object} dataForm 表单数据
         * @return {*}
         */
        selectTreeChange(index, e, dataForm) {
            const data = this.workFlowVarsConfig[0].children.find(item =>
                item.referenceNodeId + '___' + item.varName === e?.value
            );
            const { referenceNodeId, realNamePath, varName, originalVarType, varType, id } = data || {};
            this.$set(dataForm, 'referenceNodeId', referenceNodeId);
            this.$set(dataForm, 'varValue', varName);
            this.$set(dataForm, 'referenceVarName', realNamePath || varName);
            this.$set(dataForm, 'referenceVarType', originalVarType || varType);
            this.$set(dataForm, 'referenceVarId', id);
            this.$set(dataForm, 'referenceTreeData', {
                label: '',
                value: referenceNodeId + '___' + (realNamePath || varName),
            });
            this.updateLf();
        },
        /**
         * @description: 修改读取参数名
         * @param {number} index 数据下标
         * @param {object} value 属性值
         * @param {string} key 属性名
         * @return {*}
         */
        async extractChange(index, value, key) {
            const newVal = value.target ? value.target.value : value;
            this.readVars[index][key] = newVal;
            const { id, varName } = this.readVars[index] || {};
            this.handleDisableWorkFlowVars();
            await this.setOutputVars();
            updateReferenceVarNameById({
                nodeId: this.clickNode?.id,
                lf: this.lf,
                varNameId: id,
                updateVarName: varName
            });
            deleteTempOutputs();
            this.updateLf();
        },
        /**
         * @description: 删除读取数据
         * @param {number} index 数据下标
         * @return {*}
         */
        async delExtractVars(index) {
            this.readVars.splice(index, 1);
            this.handleDisableWorkFlowVars();
            await this.setOutputVars();
            deleteReferenceVarNameById({
                nodeId: this.clickNode?.id,
                lf: this.lf,
                varNameId: this.readVars[index]?.id
            });
            deleteTempOutputs();
            this.updateLf();
        },
        /**
         * @description: 修改输出数据
         * @return {*}
         */
        setOutputVars() {
            let value = [];
            this.outputVars = [];
            this.$nextTick(() => {
                if (this.mod === 'write') {
                    this.inputVars.forEach(item => {
                        value.push({
                            ...item,
                            varType: 'String',
                        });
                    });
                    value.unshift({
                        varName: 'isSuccess',
                        varType: 'Boolean',
                        varValue: ''
                    });
                } else {
                    this.readVars.forEach(item => {
                        value.push({
                            ...item,
                            varType: 'String',
                        });
                    });
                }
                value.forEach(item => {
                    const existIndex = this.outputVars.findIndex(v => v.id === item.id);
                    if (existIndex > -1) {
                        Object.assign(this.outputVars[existIndex], item);
                    } else {
                        this.outputVars.push(item);
                    }
                });
                // 再删除多余的
                this.outputVars = this.outputVars.filter(v =>
                    value.some(item => item.id === v.id || item.varName === 'isSuccess')
                );
            });
        },
    },
    mounted() {
        this.init();
    },
};
</script>
<style lang="less" scoped>
.flex-center {
    display: flex;
    align-items: center;
}

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
                margin-right: 65px;
                font-size: 12px;
                box-sizing: border-box;

                .radio-tooltip {
                    margin-left: 5px;
                }
            }
        }
    }

    .input-vars {
        margin-top: 20px;

        .connect-config-item {
            display: block;
            padding: 0;
            font-size: 12px;
        }

        .config-item-header {
            display: flex;
            align-items: center;
            color: #84868c;
            gap: 4px;
            font-size: 12px;
            height: 24px;

            .header-key {
                width: 160px;
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
                width: 160px;
                font-size: 12px;

                .config-item-content-key {
                    max-width: 60px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }

                /deep/ .ant-select {
                    width: 160px !important;
                }
            }

            .del-icon {
                color: #151b26;
                font-size: 14px;
                margin-top: 5px;
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
    }

    .config-item-plus {
        font-size: 16px;
        cursor: pointer;
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

    /deep/ .ant-select-selection-selected-value {
        .arg-type {
            display: none;
        }
    }
}
</style>