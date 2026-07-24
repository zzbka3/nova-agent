<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-09-03 14:12:48
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @LastEditTime: 2025-09-16 17:11:05
 * @FilePath: /metis-front/src/views/flow/basics/components/memorySchema.vue
 * @Description: 修改/添加记忆变量
-->
<template>
    <a-modal
        title="添加记忆变量"
        :visible="visible"
        @ok="memoryHandleOk"
        @cancel="memoryHandleCancel"
        width="850px"
    >
        <a-alert
            message="删除变量或修改变量名称/描述，应用更新发布后会导致应用用户对应的变量数据被删除或重置为默认值，请谨慎操作"
            class="alert-text"
            banner
        />
        <a-form :form="form">
            <a-table
                :columns="columns"
                :data-source="data"
                :pagination="false"
                :scroll="{ x: '100%', y: 300 }"
                :row-key="record => record.id"
            >
                <template
                    v-for="col in cols"
                    :slot="col.title"
                    slot-scope="text, record, index"
                >
                    <a-form-item :key="col.title + index">
                        <a-input
                            v-if="col.title === 'varValue'"
                            :placeholder="col.placeholder"
                            v-decorator="[
                                col.title + index,
                                {
                                    rules: rules[col.title]
                                }
                            ]"
                            @change="e => handleChange(e.target.value, record, col.title)"
                        />
                        <div
                            v-else
                            class="input-box"
                        >
                            <div v-if="isShow(index, col.title)">
                                <a-textarea
                                    :ref="col.title + index"
                                    :placeholder="col.placeholder"
                                    :auto-size="{ minRows: 2, maxRows: 4 }"
                                    @blur="e => handleBlur(col.title, index)"
                                    v-decorator="[
                                        col.title + index,
                                        {
                                            rules: rules[col.title]
                                        }
                                    ]"
                                    :max-length="col.maxLength"
                                    @change="e => handleChange(e.target.value, record, col.title)"
                                />
                            </div>
                            <a-input
                                v-else
                                :placeholder="col.placeholder"
                                @focus="() => handleFocus(col.title, index)"
                                :max-length="col.maxLength"
                                v-decorator="[
                                    col.title + index,
                                    {
                                        rules: rules[col.title]
                                    }
                                ]"
                            />
                            <div class="input-counter">
                                {{ record[col.title].length }}/{{ col.maxLength }}
                            </div>
                        </div>
                    </a-form-item>
                </template>
                <template
                    slot="operation"
                    slot-scope="text, record, index"
                >
                    <div class="editable-row-operations">
                        <a-icon
                            type="delete"
                            @click="() => del(index)"
                        />
                    </div>
                </template>
            </a-table>
            <a-form-item>
                <a-tooltip class="radio-tooltip">
                    <template slot="title">
                        变量最多支持添加20个
                    </template>
                    <a-button
                        type="link"
                        block
                        icon="plus"
                        class="add-btn"
                        @click="add"
                        :disabled="data.length >= 20"
                    >
                        新增变量
                    </a-button>
                </a-tooltip>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
<script>
import { uniqueValue } from '@/views/flow/common/common';
export default {
    props: {
        // 记忆变量弹窗是否显示
        visible: {
            type: Boolean,
            default: false,
        },
        // 记忆变量列表
        memorySchema: {
            type: Array,
            default() {
                return [];
            },
        },
        // 是否修改
        isEdit: {
            type: Boolean,
            default: false,
        },
    },
    data() {
        return {
            data: [], // 记忆变量列表
            columns: [ // 表格属性
                { title: '名称', dataIndex: 'varName', scopedSlots: { customRender: 'varName' }, width: '30%' },
                { title: '描述', dataIndex: 'varDesc', scopedSlots: { customRender: 'varDesc' }, width: '30%' },
                { title: '默认值', dataIndex: 'varValue', scopedSlots: { customRender: 'varValue' }, width: '30%' },
                { title: '操作', dataIndex: 'operation', scopedSlots: { customRender: 'operation' }, width: '10%' },
            ],
            cols: [ // 输入框属性
                { title: 'varName', placeholder: '请输入参数名', maxLength: 50 },
                { title: 'varDesc', placeholder: '请输入描述', maxLength: 400 },
                { title: 'varValue', placeholder: '请输入默认值' }
            ],
            form: this.$form.createForm(this), // 表单
            rules: { // 校验规则
                varName: [
                    { required: true, message: '请输入参数名' },
                    {
                        pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
                        message: '只能输入字母、数字、下划线，并以字母开头'
                    },
                    { validator: this.validateUniqueVarName }
                ],
                varDesc: [{ required: false, }],
                varValue: [{ required: false }]
            },
            isFocused: [], // 输入框焦点状态
        };
    },
    methods: {
        /**
         * @description: 初始化方法
         * @return {*}
         */
        init() {
            this.data = JSON.parse(JSON.stringify(this.memorySchema));
            this.isFocused = [];
            this.$nextTick(() => {
                this.data.forEach((item, index) => {
                    this.isFocused.push({
                        varName: false,
                        varDesc: false,
                    });
                    this.form.setFieldsValue({
                        [`varName${index}`]: item.varName,
                        [`varDesc${index}`]: item.varDesc,
                        [`varValue${index}`]: item.varValue,
                    });
                });
                if (!this.isEdit) {
                    this.add();
                }
            });
        },
        /**
         * @description: 记忆变量确定按钮点击事件
         * @return {*}
         */
        memoryHandleOk() {
            this.form.validateFields((err) => {
                if (!err) {
                    this.$emit('memoryHandleOk', this.data);
                }
            });
        },
        /**
         * @description: 记忆变量取消按钮点击事件
         * @return {*}
         */
        memoryHandleCancel() {
            this.$emit('memoryHandleCancel');
        },
        /**
         * @description: 输入框值改变事件
         * @param {object} value 值
         * @param {object} item 数据对象
         * @param {string} key 修改字段
         * @return {*}
         */
        handleChange(value, item, key) {
            this.$set(item, key, value);
            if (key === 'varName') {
                this.$emit('updataMemory', item);
            }
        },
        /**
         * @description: 参数名校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @return {*}
         */
        validateUniqueVarName(rule, value, callback) {
            const allNames = this.data?.map(v => v.varName) || [];
            const duplicates = allNames.filter(name => name === value);
            if (duplicates.length > 1) {
                callback(new Error('参数名不能重复'));
            } else {
                callback();
            }
        },
        /**
         * @description: 描述校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @return {*}
         */
        validateUniqueVarDesc(rule, value, callback) {
            const allNames = this.data?.map(v => v.varDesc) || [];
            const duplicates = allNames.filter(name => name === value);
            if (duplicates.length > 1) {
                callback(new Error('描述不能重复'));
            } else {
                callback();
            }
        },
        /**
         * @description: input输入框聚焦事件（隐藏input，显示文本框）
         * @param {string} key 输入框字段名
         * @param {number} index 输入框下标
         * @return {*}
         */
        handleFocus(key, index) {
            const item = this.isFocused[index];
            this.$set(item, key, true);
            this.$nextTick(() => {
                const input = this.$refs[`${key}${index}`][0];
                input.focus();
            });
        },
        /**
         * @description: 文本框失焦事件（隐藏文本框，显示input）
         * @param {string} key 文本框字段名
         * @param {number} index 文本框下标
         * @return {*}
         */
        handleBlur(key, index) {
            const item = this.isFocused[index];
            this.$set(item, key, false);
        },
        /**
         * @description: 删除按钮点击事件
         * @param {number} index 下标
         * @return {*}
         */
        del(index) {
            this.$confirm({
                title: '删除提示',
                content: '删除后不可撤销。如应用已发布，更新发布后该应用的用户将无法使用该记忆记忆变量，是否继续？',
                okText: '确认',
                cancelText: '取消',
                onOk: () => {
                    const data = this.data.splice(index, 1);
                    this.isFocused = [];
                    this.$nextTick(() => {
                        this.data.forEach((item, index) => {
                            this.isFocused.push({
                                varName: false,
                                varDesc: false,
                            });
                            this.form.setFieldsValue({
                                [`varName${index}`]: item.varName,
                                [`varDesc${index}`]: item.varDesc,
                                [`varValue${index}`]: item.varValue,
                            });
                        });
                        this.$emit('deleteMemory', data);
                    });
                },
            });
        },
        /**
         * @description: 是否显示输入框
         * @param {number} index 输入框下标
         * @param {string} key 输入框字段名
         * @return {boolean} true/false
         */
        isShow(index, key) {
            const item = this.isFocused?.[index];
            return item?.[key] || false;
        },
        /**
         * @description: 新增按钮点击事件
         * @param {*} index 输入框下标
         * @param {*} key 输入框字段名
         * @return {*}
         */
        add() {
            if (this.data.length >= 20) return;
            this.data.push({
                varName: '',
                varDesc: '',
                varType: 'String',
                varValue: '',
                id: uniqueValue()
            });
            this.isFocused.push({
                varName: true,
                varDesc: false,
            });
            this.$nextTick(() => {
                const input = this.$refs[`varName${this.data.length - 1}`][0];
                input.focus();
            });
        }
    },
};
</script>
<style lang="less" scoped>
.alert-text {
    margin-bottom: 10px;
}

/deep/ .ant-table-tbody {
    .ant-table-row-cell-break-word {
        padding: 0px 10px;
    }
}

/deep/ .ant-table-row-cell-break-word {
    vertical-align: top;
}

/deep/ .ant-form-item {
    margin-bottom: 0;
    margin: 4px 0;
}

.input-box {
    position: relative;
    display: inline-block;
    width: 100%;

    .input-name {
        padding-right: 40px;
    }

    .input-counter {
        position: absolute;
        bottom: 8px;
        right: 8px;
        font-size: 14px;
        color: #84868C;
        line-height: 24px;
        pointer-events: none;
        background-color: #fff;
        padding-left: 5px;
        font-weight: 400;
        opacity: 0.9;
    }
}

.add-btn {
    width: 120px;
}
.editable-row-operations {
    margin-top: 12px;
}
</style>