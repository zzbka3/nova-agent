<!--
 * @Author: hewenquan
 * @Date: 2025-07-31 14:33:49
 * @LastEditTime: 2025-09-12 14:15:27
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/varsTree.vue
 * @Description: 输出显示
-->
<template>
    <div
        :class="[
            'vars-tree',
            {
                'common-area': showCommonArea
            }
        ]"
    >
        <div class="common-title">
            <a-icon
                :type="expandedVars ? 'caret-down' : 'caret-right'"
                @click.stop="expandVars"
            />
            {{ title }}
        </div>
        <a-tree
            v-if="treeData && treeData.length"
            v-show="expandedVars"
            :auto-expand-parent="true"
            :tree-data="treeData"
            :replace-fields="replaceFields"
            @expand="onExpand"
            @click.stop
        >
            <template
                slot="title"
                slot-scope="scopeTree"
            >
                <div class="args-item">
                    <span class="args-item-name">
                        {{ scopeTree.varNameAbbr || scopeTree.varName }}
                    </span>
                    <span class="args-item-type">
                        {{ scopeTree.originalVarType || scopeTree.varType }}
                    </span>
                    <a-tooltip>
                        <template slot="title">
                            {{ scopeTree.varValue || '暂未配置' }}
                        </template>
                        <span
                            class="args-item-value"
                            v-if="showVarValue"
                        >
                            ：{{ scopeTree.varValue || '暂未配置' }}
                        </span>
                    </a-tooltip>
                </div>
            </template>
        </a-tree>
        <div
            v-else
            v-show="expandedVars"
            class="not-tree-data"
        >
            暂无配置
        </div>
    </div>
</template>

<script>
export default {
    props: {
        // 显示区块背景
        showCommonArea: {
            type: Boolean,
            default: true
        },
        // 是否显示展开状态
        showExpanded: {
            type: Boolean,
            default: true
        },
        // 标题
        title: {
            type: String,
            default: '输出'
        },
        // 树数据
        treeData: {
            type: Array,
            default: () => []
        },
        // 树渲染字段
        replaceFields: {
            type: Object,
            default: () => {
                return {
                    children: 'children',
                    title: 'varName',
                    key: 'varName'
                };
            }
        },
        // 显示参数值
        showVarValue: {
            type: Boolean,
            default: false
        }
    },
    watch: {
        showExpanded(val) {
            this.expandedVars = val;
        }
    },
    data() {
        return {
            // 默认显示输出内容
            expandedVars: this.showExpanded
        };
    },
    methods: {
        /**
         * @description: 树展开和收起
         * @param {*} expandedKeys
         * @param {*} expanded
         * @return {*}
         */
        onExpand(expandedKeys, { expanded }) {
            setTimeout(() => {
                this.$emit('updateNodeAttributes', expanded);
            }, 200);
        },
        /**
         * @description: 全部内容区展开和收起
         * @return {*}
         */
        expandVars() {
            this.expandedVars = !this.expandedVars;
            this.$emit('updateNodeAttributes', this.expandedVars);
        }
    }
};
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.common-area {
    padding: 12px 0 !important;
    .common-title {
        padding-left: 12px;
    }
}
.vars-tree {
    margin-top: 10px;
    /deep/ .ant-tree-node-content-wrapper {
        width: calc(100% - 24px);
    }
    .args-item {
        display: flex;
        align-content: center;
        width: 100%;
    }
    .args-item-name {
        color: #876300;
        overflow: hidden;
        max-width: 160px;
        white-space: nowrap;
        text-overflow: ellipsis;
        flex-shrink: 0;
    }
    .args-item-type {
        margin-left: 4px;
        flex-shrink: 0;
        padding: 0 5px;
        white-space: nowrap;
        border-radius: 4px;
        background-color: #e8e9eb;
    }
    .args-item-value {
        color: #000;
        overflow: hidden;
        display: inline-block;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 200px;
    }
    .not-tree-data {
        padding-left: 12px;
    }
}
</style>