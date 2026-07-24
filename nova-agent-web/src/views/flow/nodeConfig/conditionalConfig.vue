<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-06-25 18:17:45
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @LastEditTime: 2025-11-14 15:56:14
 * @FilePath: /metis-front/src/views/flow/nodeConfig/conditionalConfig.vue
 * @Description: 条件分支组件
-->
<template>
    <div class="container">
        <div class="container-title">
            <div class="container-title-text">
                条件分支
            </div>
        </div>
        <draggable
            v-model="conditionList"
            handle=".dragSort-icon"
            animation="200"
            :group="{ name: 'condition', pull: false, put: false }"
            class="condition-wrapper"
            @end="onDragEnd"
        >
            <transition-group>
                <div
                    class="condition-item"
                    v-for="(condition, index) in conditionList"
                    :key="condition.id"
                >
                    <!-- 每一项标题 -->
                    <div class="condition-title">
                        <div class="title-icon-text">
                            <a-icon
                                type="unordered-list"
                                class="dragSort-icon"
                            />
                            <h4>{{ index === 0 ? '如果' : '否则如果' }}</h4>
                        </div>
                        <a-icon
                            class="title-icon"
                            type="minus-circle"
                            @click="removeConditionList(index)"
                        />
                    </div>
                    <!-- 条件内容 -->
                    <div class="condition-cont">
                        <div
                            class="condition-op"
                            v-show="condition.innerConditions.length > 1"
                        >
                            <div class="op-text">
                                {{ condition.innerLogic === 'AND' ? '且' : '或' }}
                            </div>
                        </div>
                        <div
                            :class="{
                                'inner-conditions-box': true,
                                'conditions-box': condition.innerConditions.length > 1
                            }"
                        >
                            <div
                                class="inner-conditions"
                                v-for="(innerCondition, innerIndex) in condition.innerConditions"
                                :key="`${condition.id}_${innerCondition.innerConditionIndex}`"
                            >
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        点击修改
                                    </template>
                                    <a-popover
                                        :class="{
                                            'popover-box': true,
                                            'popover-error': innerCondition.error,
                                            'popover-active': activeInnerIndex === innerIndex && activeIndex === index
                                        }"
                                        overlay-class-name="popover-card"
                                        ref="popoverRef"
                                        placement="bottom"
                                        trigger="click"
                                        :visible="popoverVisible[index][innerIndex]"
                                        @visibleChange="handlePopoverClickChange($event, index, innerIndex, true)"
                                    >
                                        <template slot="content">
                                            <a-form
                                                class="inner-condition-form"
                                                :form="form"
                                                :label-col="{ span: 6 }"
                                                :wrapper-col="{ span: 18 }"
                                            >
                                                <!-- 左侧选择框 -->
                                                <a-form-item
                                                    label="引用变量："
                                                    :required="true"
                                                >
                                                    <a-tree-select
                                                        class="left-tree-select"
                                                        :tree-data="arrArgs"
                                                        size="small"
                                                        placeholder="请选择引用变量"
                                                        :get-popup-container="(trigger) => trigger.parentNode"
                                                        v-decorator="['left.referenceTreeData', { rules: [
                                                            { validator: validateTree }
                                                        ]}]"
                                                        label-in-value
                                                        :replace-fields="replaceFields"
                                                        tree-default-expand-all
                                                        dropdown-class-name="left-tree-select-dropdown"
                                                        :dropdown-style="{
                                                            maxHeight: '300px',
                                                            overflow: 'auto',
                                                            fontSize: '12px',
                                                            width: '260px',
                                                        }"
                                                        @change="onChange(
                                                            $event,
                                                            innerCondition,
                                                            'left',
                                                            'referenceTreeData'
                                                        )"
                                                    >
                                                    </a-tree-select>
                                                </a-form-item>
                                                <!-- 条件关系选择框 -->
                                                <a-form-item
                                                    label="条件关系："
                                                    :required="true"
                                                >
                                                    <a-select
                                                        show-search
                                                        size="small"
                                                        placeholder="请选择条件关系"
                                                        @change="onOpChange($event, innerCondition)"
                                                        v-decorator="['op', { rules: [
                                                            { required: true, message: '请选择条件关系' }
                                                        ]}]"
                                                    >
                                                        <a-select-option
                                                            v-for="item in argOptions"
                                                            :key="item.value"
                                                        >
                                                            {{ item.label }}
                                                        </a-select-option>
                                                    </a-select>
                                                </a-form-item>
                                                <!-- 右侧选择框 -->
                                                <a-form-item
                                                    label="比较变量："
                                                    :required="true"
                                                >
                                                    <div class="right-input-box">
                                                        <a-form-item>
                                                            <!-- 右侧引用类型选择 -->
                                                            <a-select
                                                                show-search
                                                                class="varType-select"
                                                                size="small"
                                                                :disabled="getIsDisabled(innerCondition.op)"
                                                                :value="innerCondition.right.varType"
                                                                @change="
                                                                    onChange($event, innerCondition, 'right', 'varType')
                                                                "
                                                            >
                                                                <a-select-option
                                                                    v-for="item in getRightOptions(innerCondition)"
                                                                    :key="item.value"
                                                                >
                                                                    {{ item.label }}
                                                                </a-select-option>
                                                            </a-select>
                                                        </a-form-item>
                                                        <!-- 类型为引用时，选择框 -->
                                                        <a-form-item
                                                            class="right-form-item"
                                                            v-show="innerCondition.right.varType === 'reference'"
                                                        >
                                                            <a-tree-select
                                                                class="varValue-select"
                                                                size="small"
                                                                placeholder="请选择引用变量"
                                                                :tree-data="getRightTreeData(innerCondition)"
                                                                :replace-fields="replaceFields"
                                                                :get-popup-container="(trigger) => trigger.parentNode"
                                                                :disabled="getIsDisabled(innerCondition.op)"
                                                                :dropdown-match-select-width="false"
                                                                tree-default-expand-all
                                                                dropdown-class-name="right-tree-select-dropdown"
                                                                :dropdown-style="{
                                                                    maxHeight: '300px',
                                                                    overflow: 'auto',
                                                                    fontSize: '12px',
                                                                    width: '260px',
                                                                }"
                                                                label-in-value
                                                                v-decorator="
                                                                    getIsDisabled(innerCondition.op)
                                                                        && innerCondition.right.varType === 'reference'
                                                                        ? []
                                                                        : ['right.referenceTreeData', { rules: [
                                                                            { validator: validateTree }
                                                                        ]}]"
                                                                @change="
                                                                    onChange(
                                                                        $event,
                                                                        innerCondition,
                                                                        'right',
                                                                        'referenceTreeData'
                                                                    )
                                                                "
                                                            >
                                                            </a-tree-select>
                                                        </a-form-item>
                                                        <!-- 非引用类型 -->
                                                        <a-form-item
                                                            class="right-form-item"
                                                            v-show="innerCondition.right.varType === 'String'"
                                                        >
                                                            <!-- string类型输入框 -->
                                                            <a-input
                                                                :disabled="getIsDisabled(innerCondition.op)"
                                                                size="small"
                                                                placeholder="请输入常量"
                                                                @input="
                                                                    onInputChange(
                                                                        $event,
                                                                        innerCondition.right,
                                                                        'varValue'
                                                                    )
                                                                "
                                                                class="varValue-select"
                                                                v-decorator="
                                                                    getIsDisabled(innerCondition.op)
                                                                        && innerCondition.right.varType === 'String'
                                                                        ? []
                                                                        : ['right.varValue', { rules: [
                                                                            { required: true, message: '请输入常量' }
                                                                        ]}]"
                                                            />
                                                        </a-form-item>
                                                        <a-form-item
                                                            class="right-form-item"
                                                            v-show="innerCondition.right.varType === 'Integer'
                                                                || innerCondition.right.varType === 'Number'"
                                                        >
                                                            <!-- Integer类型输入框 -->
                                                            <a-input-number
                                                                :disabled="getIsDisabled(innerCondition.op)"
                                                                size="small"
                                                                placeholder="请输入常量"
                                                                class="varValue-select"
                                                                @change="
                                                                    onNumberChange($event, innerCondition.right)
                                                                "
                                                                v-decorator="
                                                                    getIsDisabled(innerCondition.op) && (
                                                                        innerCondition.right.varType === 'Integer'
                                                                        || innerCondition.right.varType === 'Number'
                                                                    )
                                                                        ? []
                                                                        : ['right.varValue', { rules: [
                                                                            { required: true, message: '请输入常量' }
                                                                        ]}]"
                                                            />
                                                        </a-form-item>
                                                        <a-form-item
                                                            class="right-form-item"
                                                            v-show="innerCondition.right.varType === 'Boolean'"
                                                        >
                                                            <a-select
                                                                show-search
                                                                class="varValue-select"
                                                                size="small"
                                                                :disabled="getIsDisabled(innerCondition.op)"
                                                                @change="
                                                                    onNumberChange($event, innerCondition.right)
                                                                "
                                                                v-decorator="
                                                                    getIsDisabled(innerCondition.op) && (
                                                                        innerCondition.right.varType === 'Boolean'
                                                                    )
                                                                        ? []
                                                                        : ['right.varValue', { rules: [
                                                                            { required: true, message: '请选择' }
                                                                        ]}]"
                                                            >
                                                                <a-select-option
                                                                    v-for="item in BooleanOption"
                                                                    :key="item.value"
                                                                >
                                                                    {{ item.label }}
                                                                </a-select-option>
                                                            </a-select>
                                                        </a-form-item>
                                                    </div>
                                                </a-form-item>
                                            </a-form>
                                        </template>
                                        <!-- 左侧值显示 -->
                                        <div
                                            v-if="innerCondition.left.referenceVarName"
                                            class="item-left"
                                        >
                                            <a-tooltip>
                                                <template slot="title">
                                                    {{ `${getTitle(innerCondition.left.referenceNodeId, arrArgs)}/` }}
                                                    {{ innerCondition.left.referenceVarName }}
                                                </template>
                                                <span>
                                                    {{ getTitle(innerCondition.left.referenceNodeId, arrArgs) }}/
                                                </span>
                                                {{ innerCondition.left.referenceVarName }}
                                            </a-tooltip>
                                        </div>
                                        <div
                                            v-else-if="innerCondition.left.referenceTreeData"
                                            class="item-left"
                                        >
                                            {{ innerCondition.left.referenceTreeData.label }}
                                        </div>
                                        <!-- 操作符 -->
                                        <div class="item-op">
                                            {{ getOpLabel(innerCondition.op) }}
                                        </div>
                                        <!-- 右侧值显示 -->
                                        <div
                                            :class="[
                                                'item-right',
                                                {
                                                    'item-left': innerCondition.right.varType === 'reference',
                                                }
                                            ]"
                                            v-if="!isEmpty(innerCondition.right.varValue)
                                                || innerCondition.right.referenceVarName"
                                        >
                                            <a-tooltip>
                                                <template slot="title">
                                                    <span v-if="innerCondition.right.varType === 'reference'">
                                                        {{ `${getTitle(innerCondition.right.referenceNodeId,
                                                                       arrArgs)}/` }}
                                                    </span>
                                                    <!-- eslint-disable-next-line max-len  -->
                                                    {{ innerCondition.right.referenceVarName || innerCondition.right.varValue }}
                                                </template>
                                                <span v-if="innerCondition.right.varType === 'reference'">
                                                    {{ `${getTitle(innerCondition.right.referenceNodeId, arrArgs)}/` }}
                                                </span>
                                                <!-- eslint-disable-next-line max-len  -->
                                                {{ innerCondition.right.referenceVarName || innerCondition.right.varValue }}
                                            </a-tooltip>
                                        </div>
                                        <div
                                            v-else-if="innerCondition.right.referenceTreeData"
                                            class="item-left"
                                        >
                                            {{ innerCondition.right.referenceTreeData.label }}
                                        </div>
                                    </a-popover>
                                </a-tooltip>
                                <a-icon
                                    class="title-icon"
                                    type="minus-circle"
                                    @click="removeInnerConditions(index, innerIndex)"
                                />
                            </div>
                            <a-button
                                icon="plus"
                                type="primary"
                                class="add-inner-btn inner-btn"
                                @click="addInnerConditions(index)"
                            >
                                条件添加
                            </a-button>
                            <a-button
                                type="primary"
                                class="inner-btn"
                                v-if="condition.innerConditions.length > 1"
                                @click="changeInnerLogic(index)"
                            >
                                且<a-icon type="swap" />或
                            </a-button>
                        </div>
                    </div>
                </div>
            </transition-group>
        </draggable>
        <a-button
            class="add-btn"
            icon="plus"
            @click="addConditionList"
        >
            添加分支
        </a-button>
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import draggable from 'vuedraggable';
import {
    getAllArgs,
    getAllFlatArgs,
    getArgOptions,
    opOptions,
    argsRules,
    getAllCanSelectArgs,
} from '@/views/flow/getArgs';
import { isEmpty, processTreeData } from '@/views/flow/common/common';
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
            conditionList: [], // 条件列表
            arrArgs: [], // 系统参数树
            allFlatArgs: [], // 所有参数平铺结构
            rightTreeData: [], // 系统参数树
            argOptions: [], // 条件选项
            innerConditionIndex: 1, // innerCondition索引
            form: this.$form.createForm(this), // 用于在子组件中调整表单值
            activeIndex: -1, // 当前修改conditionList索引
            activeInnerIndex: -1, // 当前修改innerCondition索引
            replaceFields: { // 树形控件字段映射
                title: 'title',
                value: 'key',
                label: 'label',
                children: 'children'
            },
            opOptions, // 操作符选项
            argsRules, // 右侧类型可选
            argType: '', // 参数类型
            popoverVisible: [], // 条件列表popover显示状态
            isValidate: false,
            BooleanOption: [
                { label: 'true', value: true },
                { label: 'false', value: false }
            ]
        };
    },
    components: {
        draggable
    },
    computed: {
        // 监听点击节点数据
        dataList() {
            const { conditionList = [] } = this.clickNode.properties || {};
            return conditionList;
        }
    },
    watch: {
        // 监听点击节点数据
        dataList() {
            this.init();
        }
    },
    mounted() {
        this.init();
    },
    methods: {
        isEmpty,
        /**
         * 初始化
         * @return {*}
         */
        init() {
            const { id } = this.clickNode;
            // 获取系统参数树（全部可选参数）
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            // 获取所有参数平铺结构
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.conditionList = [];
            if (this.dataList.length > 0) {
                this.conditionList = this.dataList.map((item) => {
                    return {
                        ...item,
                    };
                });
            }
            this.conditionList.forEach((item, index) => {
                this.$set(this.popoverVisible, index, []);
                item.innerConditions.forEach((ele, innerIndex) => {
                    this.innerConditionIndex++;
                    const rightKey = ele.right.varType === 'reference' ? 'referenceVarName' : 'varValue';
                    const { left, op, right } = ele || {};
                    this.$set(this.popoverVisible[index], innerIndex, false);
                    if (op === 'EMPTY' || op === 'NOT_EMPTY') {
                        if (!left.referenceVarName || !op) {
                            ele.error = true;
                        }
                    } else {
                        if (!left.referenceVarName || isEmpty(right[rightKey]) || !op) {
                            ele.error = true;
                        }
                    }
                    left.referenceTreeData = this.getReferenceTreeData(left);
                    right.referenceTreeData = this.getReferenceTreeData(right);
                    this.$set(ele, 'innerConditionIndex', this.innerConditionIndex);
                });
            });
        },
        /**
        * 重构选中引用树的数据
        *
        * @param data 包含引用节点ID和引用变量名的对象
        * @returns 引用树的数据对象，或者null
        */
        getReferenceTreeData(data) {
            const { referenceNodeId, referenceVarName, referenceTreeData } = data || {};
            if (referenceNodeId) {
                let label = '';
                // 系统参数特殊处理
                if (+referenceNodeId === 1) {
                    label = `系统参数/${referenceVarName}`;
                } else {
                    const { nodeName = '' } = this.lf.getNodeModelById(referenceNodeId)?.getProperties() || {};
                    label = nodeName ? `${nodeName}/${referenceVarName}` : referenceVarName;
                }
                return {
                    label: label,
                    value: referenceNodeId + '___' + referenceVarName,
                };
            }
            if (referenceTreeData?.label) {
                return {
                    label: referenceTreeData?.label,
                    value: '',
                };
            }
            return null;
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
        /**
         * 添加条件列表
         * @return {*}
         */
        addConditionList() {
            if (this.conditionList.length >= 10) {
                return;
            }
            const maxId = this.conditionList.length
                ? Math.max(...this.conditionList.map(item => item.id))
                : 0;
            const newId = maxId + 1;
            this.conditionList.push({
                innerLogic: 'AND',
                innerConditions: [],
                conditionIndex: newId,
                id: newId
            });
            this.$set(this.popoverVisible, this.conditionList.length - 1, []);
            this.updateLf();
        },
        /**
         * 删除条件列表
         * @param {index} index 下标
         * @return {*}
         */
        removeConditionList(index) {
            if (this.conditionList.length <= 1) return;
            // 如果锚点已经有链接线
            const { targetNodes = [] } = this.conditionList[index];
            if (targetNodes && targetNodes.length) {
                targetNodes.forEach(item => {
                    if (item.edgeId) {
                        this.lf.deleteEdge(item.edgeId);
                    }
                });
            }
            this.conditionList.splice(index, 1);
            this.updateLf();
        },
        /**
         * 切换条件逻辑
         * @param {index} index 下标
         * @return {*}
         */
        changeInnerLogic(index) {
            this.conditionList[index].innerLogic =
                this.conditionList[index].innerLogic === 'AND' ? 'OR' : 'AND';
            this.updateLf();
        },
        /**
         * 添加内部条件
         * @param {index} index 下标
         * @return {*}
         */
        addInnerConditions(index) {
            if (this.conditionList[index].innerConditions.length >= 10) {
                return;
            }
            this.innerConditionIndex++;
            this.conditionList[index].innerConditions.push({
                left: {
                    varType: 'reference',
                    referenceVarName: '',
                    referenceVarType: '',
                    referenceNodeId: ''
                },
                right: {
                    varType: 'reference',
                    varValue: '',
                    referenceVarName: '',
                    referenceVarType: '',
                    referenceNodeId: ''
                },
                op: '',
                innerConditionIndex: this.innerConditionIndex,
                error: true
            });
            this.$nextTick(() => {
                setTimeout(() => {
                    // eslint-disable-next-line max-len
                    this.handlePopoverClickChange(true, index, this.conditionList[index].innerConditions.length - 1, false);
                }, 0);
            });
            this.updateLf();
        },
        /**
         * 删除内部条件
         * @param {index} index 下标
         * @return {*}
         */
        removeInnerConditions(conditionIndex, innerIndex) {
            this.conditionList[conditionIndex].innerConditions.splice(innerIndex, 1);
            this.updateLf();
        },
        /**
         * 树组件、数字狂、下拉框等组件的回调函数
         * @param {value} value 值
         * @param {item} item 对象
         * @param {node} node 节点名称
         * @param {field} field 字段名称
         * @return {*}
         */
        onChange(value, item, node, field) {
            // 更新右侧下拉框的选项
            if (node === 'right') {
                // eslint-disable-next-line max-len
                const { referenceNodeId = '', varType = '', realNamePath, id, originalVarType = '' } = this.getSelectedData(value);
                this.$set(item[node], 'referenceNodeId', referenceNodeId || '');
                this.$set(item[node], 'referenceVarType', originalVarType || varType || '');
                this.$set(item[node], 'referenceVarName', realNamePath || value);
                this.$set(item[node], 'referenceVarId', id);
                if (field === 'varType') {
                    // eslint-disable-next-line max-len
                    const rightKey = value === 'reference' ? 'right.referenceTreeData' : 'right.varValue';
                    this.$set(item[node], 'varValue', '');
                    this.$set(item[node], 'referenceVarName', '');
                    this.$set(item[node], 'referenceVarId', '');
                    this.$set(item[node], field, value);
                    this.$nextTick(() => {
                        this.form.setFieldsValue({
                            [rightKey]: null,
                        });
                    });
                } else {
                    this.$set(item[node], 'referenceTreeData', this.getReferenceTreeData(item[node]));
                    this.$nextTick(() => {
                        this.form.setFieldsValue({
                            'right.referenceTreeData': item.right.referenceTreeData,
                        });
                    });
                }
            }
            // 更新左侧下拉框的选项
            if (node === 'left') {
                // eslint-disable-next-line max-len
                const { referenceNodeId = '', varType = '', realNamePath, id, originalVarType = '' } = this.getSelectedData(value);
                this.$set(item[node], 'referenceNodeId', referenceNodeId);
                this.$set(item[node], 'referenceVarType', originalVarType || varType);
                this.$set(item[node], 'referenceVarName', realNamePath || value);
                this.$set(item[node], 'referenceVarId', id);
                this.$set(item[node], 'varValue', '');
                this.$set(item[node], 'referenceTreeData', this.getReferenceTreeData(item[node]));
                this.$nextTick(() => {
                    this.form.setFieldsValue({
                        'left.referenceTreeData': item.left.referenceTreeData,
                    });
                });
                // eslint-disable-next-line max-len
                const rightKey = item.right.varType === 'reference' ? 'right.referenceTreeData' : 'right.varValue';
                this.$set(item, 'op', '');
                this.$set(item.right, 'varValue', '');
                this.$set(item.right, 'referenceVarName', '');
                this.$set(item.right, 'referenceVarId', '');
                this.$set(item.right, 'referenceNodeId', '');
                this.$set(item.right, 'referenceTreeData', null);
                this.argOptions = getArgOptions({ varType });
                this.$nextTick(() => {
                    this.form.setFieldsValue({
                        [rightKey]: null,
                        op: '',
                    });
                });
            }
            this.updataError();
            this.updateLf();
        },
        onNumberChange(value, item) {
            this.$set(item, 'varValue', value);
            this.$set(item, 'referenceNodeId', '');
            this.$set(item, 'referenceVarType', '');
            this.$set(item, 'referenceVarName', '');
            this.updataError();
            this.updateLf();
        },
        /**
         * 条件关系下拉框的回调函数
         * @param {value} value 值
         * @param {item} item 对象
         * @return {*}
         */
        onOpChange(value, item) {
            // eslint-disable-next-line max-len
            const rightKey = item.right.varType === 'reference' ? 'right.referenceTreeData' : 'right.varValue';
            this.$set(item, 'op', value);
            this.$set(item.right, 'varType', 'reference');
            this.$set(item.right, 'varValue', '');
            this.$set(item.right, 'referenceVarName', '');
            this.$set(item.right, 'referenceNodeId', '');
            this.$set(item.right, 'referenceVarId', '');
            this.$set(item.right, 'referenceTreeData', null);
            this.form.setFieldsValue({
                [rightKey]: null,
            });
            this.updataError();
            this.updateLf();
        },
        /**
         * input输入框的回调函数
         * @param {value} value 值
         * @param {item} item 对象
         * @return {*}
         */
        onInputChange(e, item, node) {
            this.$set(item, node, e.target.value);
            this.updataError();
            this.updateLf();
        },
        /**
         * 获取对比条件的label
         * @param {op} op 值
         * @return {label} label值
         */
        getOpLabel(op) {
            const opItem = this.opOptions.find(item => item.value === op);
            return opItem ? opItem.label : '';
        },
        /**
         * 获取右侧下拉框的选项
         * @param {value} value 值
         * @return {array} varType选项
         */
        getRightOptions(value) {
            const referenceVarType = value.left.referenceVarType || '';
            const op = value.op || '';
            if (!(referenceVarType && op)) return [{ label: '引用', value: 'reference' }];
            const arr = this.argsRules[referenceVarType][op].argsMap;
            console.log('argsRules', argsRules);
            console.log('arr', arr);
            this.argType = this.argsRules[referenceVarType][op].argType;
            return [{ label: '引用', value: 'reference' }, ...arr.map(item => ({ label: item, value: item }))];
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
         * 更新lf数据
         * @return {*}
         */
        updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            // const conditionList = this.extractValidInnerConditions(this.conditionList);
            if (this.isValidate) {
                this.form.validateFieldsAndScroll();
            }
            edgeModel.setProperties({
                conditionList: this.conditionList
            });
            setTimeout(() => {
                edgeModel.updatePath();
            }, 0);
        },
        updataError() {
            if (this.activeIndex < 0) {
                return;
            }
            // eslint-disable-next-line max-len
            const rightKey = this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex].right.varType === 'reference' ? 'referenceVarName' : 'varValue';
            // eslint-disable-next-line max-len
            const { left, op, right } = this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex] || {};
            if (op === 'EMPTY' || op === 'NOT_EMPTY') {
                if (left.referenceVarName && op) {
                    this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex].error = false;
                } else {
                    this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex].error = true;
                }
            } else {
                if (left.referenceVarName && op && !isEmpty(right[rightKey])) {
                    this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex].error = false;
                } else {
                    this.conditionList[this.activeIndex].innerConditions[this.activeInnerIndex].error = true;
                }
            }
        },
        /**
         * 处理popover点击事件
         * @param {value} value 值
         * @param {index} index 下标
         * @param {innerIndex} innerIndex 下标
         * @return {*}
         */
        handlePopoverClickChange(visible, index, innerIndex, isValidate) {
            this.$set(this.popoverVisible[index], innerIndex, visible);
            if (!visible) {
                this.activeIndex = -1;
                this.activeInnerIndex = -1;
                this.form.resetFields([
                    'left.referenceTreeData',
                    'right.referenceTreeData',
                    'op',
                    'right.referenceVarName',
                    'right.varValue'
                ]);
                this.isValidate = false;
                return;
            }
            this.activeIndex = index;
            this.activeInnerIndex = innerIndex;
            const data = this.conditionList[index].innerConditions[innerIndex] || {};
            const leftVarValue = data?.left?.referenceTreeData || {};
            const op = data?.op || '';
            // eslint-disable-next-line max-len
            const rightVarValue = data?.right?.varType === 'reference' ? data?.right?.referenceTreeData : data?.right?.varValue;
            // eslint-disable-next-line max-len
            const rightKey = data?.right?.varType === 'reference' ? 'right.referenceTreeData' : 'right.varValue';
            this.argOptions = getArgOptions({ varType: data?.left?.referenceVarType });
            this.form.getFieldValue('left.referenceTreeData');
            this.form.getFieldValue('op');
            this.form.getFieldValue('right.referenceVarName');
            this.form.getFieldValue('right.referenceTreeData');
            this.form.getFieldValue('right.varValue');
            this.form.setFieldsValue({
                'left.referenceTreeData': leftVarValue,
                op,
                [rightKey]: rightVarValue,
            });
            if (isValidate) {
                setTimeout(() => {
                    this.form.validateFieldsAndScroll();
                }, 100);
                this.isValidate = true;
            }
        },
        /**
         * 提取有效的innerConditions
         * @param {conditionList} conditionList 对象
         * @return {array} conditionList数组
        */
        extractValidInnerConditions(conditionList) {
            return conditionList.map(item => {
                const validConditions = (item.innerConditions || []).filter(inner => {
                    return inner.left?.referenceVarName && inner.op;
                });
                return {
                    ...item,
                    innerConditions: validConditions
                };
            });
        },
        /**
         * 拖拽结束回调
         * @return {*}
        */
        onDragEnd() {
            this.updateLf();
        },
        /**
         * 获取右侧下拉框的选项
         * @param {value} value 值
         * @return {*}
        */
        getRightTreeData(value) {
            const { id } = this.clickNode;
            const referenceVarType = value.left.referenceVarType || '';
            const op = value.op || '';
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
         * 获取引用字段的标题
         * @param {val} val 值
         * @param {arr} arr 值
         * @return {string} 标题
        */
        getTitle(val, arr) {
            const item = arr.find(item => item.nodeId === val);
            return item ? item.title : '';
        },
        /**
         * 获取右侧下拉框的选项
         * @param {val} val 值
         * @return {object} 对象
        */
        getSelectedData(e) {
            const node = this.allFlatArgs.find(item =>
                item.referenceNodeId + '___' + (item.realNamePath || item.varName) === e?.value
            );
            return node || {};
        },
    },
};
</script>
<style lang="less" scoped>
.container {
    .container-title {
        margin: 0 0 12px;
        height: 24px;
        .container-title-text {
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            color: #151b26;
        }
    }
    .condition-item {
        width: 100%;
        background: #f9f9fb;
        border-radius: 10px;
        padding: 10px 5px;
        margin-bottom: 10px;

        .condition-title {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            .title-icon-text {
                display: flex;
                align-items: center;
                .dragSort-icon {
                    cursor: grabbing;
                }
                h4 {
                    margin: 0;
                    margin-left: 5px;
                }
            }
        }

        .condition-cont {
            display: flex;
            align-items: center;
            flex: 1;

            .inner-conditions-box {
                position: relative;
                width: 100%;
                z-index: 0;
                width: 100%;

                .inner-conditions {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 8px;
                    flex: 1;

                    .popover-box {
                        display: flex;
                        align-items: center;
                        width: calc(100% - 28px);
                        border-radius: 5px;
                        border: #e8e9eb 1px solid;
                        height: 30px;
                        margin-right: 10px;
                        padding: 0 5px;
                        cursor: pointer;
                        background: #f2f2f4;

                        .item-left {
                            border-radius: 5px;
                            background-color: #fff;
                            padding: 0 3px;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            max-width: 50%;
                            border: 1px solid #e8e9eb;
                            font-size: 12px;
                            font-weight: 400;
                        }

                        .item-op {
                            margin: 0 5px;
                            flex: 0 0 auto;
                            font-size: 12px;
                            font-weight: 400;
                        }

                        .item-right {
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            max-width: 50%;
                            font-size: 12px;
                            font-weight: 400;
                        }
                        &:hover {
                            background: #e6f0ff;
                            border: 1px solid #2468f2;
                        }
                        &.popover-error {
                            border: 1px solid #f33e3e;
                            background: #f2f2f4;
                        }
                        &.popover-active {
                            background: #e6f0ff;
                            border: 1px solid #2468f2;
                        }
                    }
                }
            }
            .conditions-box {
                width: calc(100% - 32px);
            }

            .conditions-box::before {
                content: '';
                width: 100%;
                height: calc(100% - 30px);
                border: #d4d6d9 1.5px solid;
                border-radius: 6px;
                position: absolute;
                top: 50%;
                left: -18px;
                transform: translateY(-50%);
                z-index: -2;
            }

            .conditions-box::after {
                content: '';
                position: absolute;
                top: 0;
                left: -5px;
                width: 100%;
                height: 100%;
                background: #f9f9fb;
                z-index: -1;
            }

            .condition-op {
                position: relative;
                width: 32px;
                z-index: 20;

                .op-text {
                    position: absolute;
                    left: 40%;
                    top: 45%;
                    transform: translate(-50%, -50%);
                    background: #f9f9fb;
                    color: #1890ff;
                }
            }
        }

    }

    .add-btn {
        width: 100%;
        height: 28px;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 400;
        &:hover {
            color: #528EFF;
            border-color: #528EFF;
        }
    }

    .title-icon {
        cursor: pointer;
        font-size: 16px;
        color: #333;
        font-weight: bold;
    }
    .add-inner-btn {
        margin-right: 10px;
    }
    .inner-btn {
        background: #fff;
        color: #2468f2;
        height: 24px;
        border-color: #e8e9eb;
        text-shadow: none;
        box-shadow: none;
        font-size: 12px;
        font-weight: 400;
        &:hover {
            border-color: #2468f2;
        }
    }
}

/deep/ .right-tree-select-dropdown {
    left: -110px !important;
    li {
        margin: 2px 0;
    }
    .ant-select-tree-title {
        font-size: 12px;
    }
}
/deep/ .left-tree-select-dropdown {
    left: -10px !important;
    li {
        margin: 2px 0;
    }
    .ant-select-tree-title {
        font-size: 12px;
    }
}
</style>
<style lang="less" scoped>
.popover-card {
    .ant-popover-inner {
        border-radius: 10px;
    }
}
.inner-condition-form {
    width: 350px;
    padding: 10px 10px;
    .ant-form-item {
        margin-bottom: 5px;
        .ant-form-item-required {
            font-size: 12px;
        }
        .right-input-box {
            display: flex;
        }
        .left-tree-select {
            .arg-type {
                display: none;
            }
        }
    }

    .varType-select {
        width: 90px;
        margin-right: 6px;
    }

    .varValue-select {
        width: 150px;
        .arg-type {
            display: none;
        }
    }
}
</style>