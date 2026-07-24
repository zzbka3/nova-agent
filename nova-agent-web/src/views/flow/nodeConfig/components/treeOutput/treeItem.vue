<!--
 * @Author: hewenquan
 * @Date: 2025-08-12 10:45:40
 * @LastEditTime: 2025-11-10 11:11:08
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/components/treeOutput/treeItem.vue
 * @Description: tree 子项
-->
<template>
    <div class="tree-item-wrapper">
        <div
            class="tree-item-content"
            v-for="(item, index) in treeFromData"
            :key="item.id"
        >
            <!-- 单一树处理数据 -->
            <div class="tree-form-wrapper">
                <a-form-item
                    size="small"
                    class="tree-form-item tree-form-query"
                >
                    <a-input
                        placeholder="请输入参数名"
                        size="small"
                        @change="treeValueChange(index, $event, 'varName')"
                        v-decorator="[`extractVarName_${item.id}`, {
                            rules: [
                                { required: true, message: '请输入参数名'},
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
                    class="tree-form-item tree-form-vartype"
                >
                    <a-select
                        size="small"
                        v-model="item.varType"
                        @change="treeValueChange(index, $event, 'varType')"
                    >
                        <a-select-option
                            v-for="optionItem in getVarTypeOptions(item)"
                            :key="optionItem.value"
                        >
                            <a-tooltip
                                :title="optionItem.label"
                            >
                                {{ optionItem.label }}
                            </a-tooltip>
                        </a-select-option>
                    </a-select>
                </a-form-item>
                <div class="operate-wrapper">
                    <a-button
                        shape="circle"
                        icon="minus"
                        size="small"
                        class="operate-icon"
                        @click="deleteItem(item, index)"
                        :disabled="treeFromData.length === 1"
                    >
                    </a-button>
                    <a-button
                        shape="circle"
                        icon="plus"
                        size="small"
                        class="operate-icon"
                        :disabled="item.varType !== 'Object'"
                        @click="addObjectItem(item, index)"
                    >
                    </a-button>
                </div>
            </div>
            <div
                v-if="item.children && item.children.length"
                class="item-children"
            >
                <TreeItem
                    :tree-data.sync="item.children"
                    @valueChange="valueChange"
                    :click-node="clickNode"
                    :lf="lf"
                />
            </div>
        </div>
    </div>
</template>

<script>
import { uniqueValue } from '@/views/flow/common/common';
import { deepClone } from '@baidu/metis-js-util';
import { varTypeApiBodyOption } from '@/views/flow/common/commonData';
import LogicFlow from '@logicflow/core';
import { updateReferenceVarNameById, deleteReferenceVarNameById } from '@/views/flow/basics/lfEvent.js';
import { deleteTempOutputs } from '@/views/flow/getArgs';
export default {
    name: 'TreeItem',
    props: {
        // 当前树的数据
        treeData: {
            type: Array,
            default: () => []
        },
        // 当前树的最大 z-index
        maxZIndx: {
            type: Number,
            default: 5
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
            treeFromData: [], // 树结构数据
            varTypeOptions: varTypeApiBodyOption, // 变量类型选项
        };
    },
    mounted() {
        this.init();
    },
    watch: {
        treeData() {
            this.init();
        }
    },
    methods: {
        /**
        * 获取变量类型选项
        *
        * @returns {Array} 返回变量类型选项数组
        */
        getVarTypeOptions(item) {
            //  限制树的层级到自定义层级, 操作最大层级后将不允许再添加子级
            if (item.zIndex >= this.maxZIndx) {
                return this.varTypeOptions.filter(item => item.label !== 'Object');
            }
            return this.varTypeOptions;
        },
        init() {
            this.treeFromData = deepClone(this.treeData) || [];
        },
        /**
         * @description: 参数抽取输入框变化
         * @param {number} index 入参索引
         * @param {string} key 参数键
         * @param {string} value 参数值
         * @return {*}
         */
        treeValueChange(index, value, key) {
            const targetValue = value.target ? value.target.value : value;
            this.treeFromData[index][key] = targetValue;
            const { zIndex = 1, id } = this.treeFromData[index] || {};
            // 参数类型改变后，是否清空子children
            if (key === 'varType') {
                if (targetValue === 'Object') {
                    this.treeFromData[index].children = [
                        {
                            varName: undefined,
                            varType: 'String',
                            id: uniqueValue(),
                            zIndex: zIndex + 1
                        }
                    ];
                } else {
                    this.treeFromData[index].children = [];
                }
                // 参数类型改变，删除旧参数名引用
                deleteReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id
                });
                deleteTempOutputs();
            } else if (key === 'varName') {
                // 参数名改变，通知其它节点同步更新
                const { id, varName } = this.treeFromData[index] || {};
                updateReferenceVarNameById({
                    nodeId: this.clickNode?.id,
                    lf: this.lf,
                    varNameId: id,
                    updateVarName: varName
                });
                deleteTempOutputs();
            }
            this.valueChange();
        },
        // 数据改变更新
        valueChange() {
            this.$emit('update:treeData', this.treeFromData);
            this.$emit('valueChange', this.treeFromData);
        },
        /**
         * @description: 参数抽取校验方法
         * @param {object} rule 规则
         * @param {string} value 值
         * @param {Function} callback 回调函数
         * @return {*}
         */
        validateUniqueVarName(rule, value, callback) {
            const allNames = this.treeFromData?.map(item => item.varName) || [];
            const duplicates = allNames.filter(name => name === value && name);
            if (duplicates.length > 1) {
                callback(new Error('参数名不能重复'));
            } else {
                callback();
            }
        },
        /**
        * 删除参数
        *
        * @param data 要删除的项目数据
        * @param index 要删除的项目索引
        */
        deleteItem(data) {
            deleteReferenceVarNameById({
                nodeId: this.clickNode?.id,
                lf: this.lf,
                varNameId: data?.id
            });
            this.treeFromData = this.treeFromData.filter(item => item.id !== data.id);
            this.valueChange();
        },
        /**
        * Object 添加子级选项
        *
        * @param data 要添加的对象数据
        * @param index 要插入的索引位置
        */
        addObjectItem(data, index) {
            const { children = [], zIndex = 1 } = data || {};
            const addItem = {
                varName: undefined,
                varType: 'String',
                id: uniqueValue(),
                zIndex: zIndex + 1
            };
            if (children && Array.isArray(children)) {
                children.push(addItem);
            }
            this.treeFromData[index].children = children;
        }
    }
};
</script>

<style lang="less" scoped>
.tree-item-wrapper {
    .item-children {
        padding-left: 15px;
    }
    .tree-form-wrapper {
        display: flex;
        .tree-form-item {
            margin: 0px 10px 5px 0;
            /deep/ .ant-form-item-control {
                line-height: 1;
            }
        }
        .tree-form-query {
            width: calc(100% - 150px);
            box-sizing: border-box;
        }
        .tree-form-vartype {
            width: 120px;
            flex-shrink: 0;
            box-sizing: border-box;
        }
        .operate-wrapper {
            display: flex;
            padding-top: 2px;
            .operate-icon {
                cursor: pointer;
                width: 18px;
                height: 18px;
                min-width: 18px;
                font-size: 12px;
                line-height: 18px;
                &:last-child {
                    margin-left: 5px;
                }
            }
        }
    }
}
</style>