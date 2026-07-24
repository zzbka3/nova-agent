<!--
 * @Author: hewenquan
 * @Date: 2025-08-11 18:46:19
 * @LastEditTime: 2025-11-10 11:09:52
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/components/treeOutput/index.vue
 * @Description: tree 参数选择， 服务于代码节点输出参数编辑
-->
<template>
    <div class="tree-output">
        <div class="tree-var-title">
            <div class="flex-center">
                {{ treeOutputTitle }}
            </div>
            <!-- 增加按钮 -->
            <a-icon
                class="tree-var-plus"
                @click="addTreeItem"
                type="plus"
            />
        </div>
        <div class="tree-var-header">
            <span class="header-query">
                参数名
            </span>
            <span class="header-type">
                类型
            </span>
        </div>
        <!-- 循环trees树 -->
        <a-form
            :form="treeOutputForm"
        >
            <tree-item
                :tree-data.sync="treeData"
                @valueChange="valueChange"
                :click-node="clickNode"
                :lf="lf"
            />
        </a-form>
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import TreeItem from './treeItem';
import { uniqueValue } from '@/views/flow/common/common';
export default {
    data() {
        return {
            treeOutputForm: this.$form.createForm(this),
            treeData: [],
        };
    },
    props: {
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        },
        inputData: {
            type: Array,
            default: () => ([])
        }, // 输入参数原始数据
        // 节点输出参数标题
        treeOutputTitle: {
            type: String,
            default: '输出'
        }
    },
    components: {
        TreeItem
    },
    mounted() {
        this.init();
    },
    methods: {
        init() {
            this.treeData = JSON.parse(JSON.stringify(this.inputData)) || [];
            // 铺平后设置表单值
            let result = [];
            const cycleChildren = (children) => {
                return children.map(argConfig => {
                    if (argConfig.children && argConfig.children.length) {
                        result.push(argConfig);
                        cycleChildren(argConfig.children);
                    } else {
                        result.push(argConfig);
                    }
                });
            };
            this.treeData.forEach(item => {
                result.push(item);
                if (item.children && item.children.length) {
                    cycleChildren(item.children);
                }
            });
            // 设置表单初始化值
            result.forEach((element) => {
                this.$nextTick(() => {
                    this.treeOutputForm.setFieldsValue({
                        [`extractVarName_${element.id}`]: element.varName,
                        [`extractVarValue_${element.id}`]: element.varValue,
                    });
                });
            });
        },
        /**
        * 添加树形结构项
        *
        * 向当前输入数据数组中添加一个新的树形结构项。
        *
        * @returns 无返回值
        */
        addTreeItem() {
            let data = {
                varName: undefined,
                varType: 'String',
                id: uniqueValue(),
                zIndex: 1
            };
            this.treeData.push(data);
        },
        /**
        * 当值发生变化时触发的函数
        *
        * @param value 传入的新值
        */
        valueChange(value) {
            this.treeData = value;
            this.$emit('valueChange', value);
        }
    }
};
</script>

<style lang="less" scoped>
.tree-output {
    .tree-var-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 6px;
        font-weight: bold;
        margin-right: 10px;
        font-weight: bold;
        color: #151b26;
        font-size: 14px;
        .tree-var-plus {
            cursor: pointer;
        }
    }
    .tree-var-header {
        display: flex;
        align-items: center;
        color: #84868c;
        gap: 4px;
        font-size: 12px;
        height: 24px;
        .header-query {
            width: 180px;
        }
        .header-type {
            width: 100px;
        }
    }
}
</style>