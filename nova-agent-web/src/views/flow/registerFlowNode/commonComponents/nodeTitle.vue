<!--
 * @Author: hewenquan
 * @Date: 2025-07-03 14:04:45
 * @LastEditTime: 2025-11-13 10:20:33
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/nodeTitle.vue
 * @Description: nodeTitle.vue
-->
<template>
    <div class="node-title">
        <div class="flex-center">
            <a-icon
                v-if="showFold"
                :type="allShow ? 'caret-down' : 'caret-right'"
                @click.stop="toggleFoldAll"
            />
            <img
                :src="nodeIcon"
                class="node-icon"
            />
            <a-input
                v-if="isEditName"
                ref="nodeTitleRef"
                v-model="nodeNameText"
                class="node-name"
                @blur="handleBlur"
                @click.stop
                @pressEnter="handleBlur($event, true)"
                @keydown.stop
            />
            <a-tooltip
                :title="nodeName"
                v-else
            >
                <span
                    class="node-name-text"
                >{{ nodeName }}</span>
            </a-tooltip>
            <span
                v-if="modeText"
                class="mode-text"
            >
                {{ modeText }}
            </span>
        </div>
        <div
            class="error-icon"
            v-if="!validateStatus"
        >
            <a-tooltip>
                <template
                    #title
                    v-if="errResult && errResult.length"
                >
                    <div
                        v-for="(item, index) in errResult"
                        :key="index"
                    >
                        {{ index + 1 }}: {{ item }};
                    </div>
                </template>
                <a-icon
                    type="info-circle"
                    style="color: red;"
                />
            </a-tooltip>
        </div>
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import { deleteTempOutputs } from '@/views/flow/getArgs';
export default {
    props: {
        // 节点名称
        nodeName: {
            type: String,
            default: '未知节点'
        },
        // 节点图标
        nodeIcon: {
            type: String,
            default: ''
        },
        // 节点验证状态
        validateStatus: {
            type: Boolean,
            default: true
        },
        // 节点校验错误信息
        errResult: {
            type: Array,
            default: () => []
        },
        // 是否展示折叠图标
        showFold: {
            type: Boolean,
            default: false
        },
        // 模式文本
        modeText: {
            type: String,
            default: ''
        },
        // 节点模型
        model: {
            type: Object,
            default: () => { }
        },
        allShow: {
            type: Boolean,
            default: false
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    data() {
        return {
            nodeNameText: '', // 节点名称
            isEditName: false, // 是否编辑节点名称
            originNameText: '' // 原节点名称
        };
    },
    mounted() {
        this.initBus();
    },
    computed: {
        // 当前节点ID
        getNodeId() {
            return this.model?.id;
        }
    },
    methods: {
        initBus() {
            // 折叠和展开节点
            this.bus.$on('triggerNode', (args) => {
                if (args === this.allShow) {
                    this.toggleFoldAll();
                }
            });
        },
        /**
         * @description: 切换全部折叠
         * @return {*}
         */
        toggleFoldAll() {
            // this.allShow = !this.allShow;
            this.$emit('toggleFoldAll', !this.allShow);
        },
        /**
         * @description: 编辑节点名称
         * @return {*}
         */
        editNodeName() {
            if (this.isEditName) {
                return;
            }
            this.isEditName = true;
            this.bus.$emit('editNodeName', { isEditName: true });
            this.nodeNameText = this.nodeName;
            this.originNameText = this.nodeName;
            this.$nextTick(() => {
                this.$refs.nodeTitleRef.focus();
            });
        },
        /**
         * @description: 失去焦点
         * @return {*}
         */
        handleBlur(event, isPressEnter) {
            this.isEditName = false;
            const { nodes = [] } = this.lf.getGraphData();
            const filterNodes = nodes.filter(item => item.id !== this.getNodeId);
            const hasEqualNodeName = filterNodes.some(item => item?.properties?.nodeName === this.nodeNameText);
            this.bus.$emit('editNodeName', {
                isEditName: false,
                nodeName: this.nodeNameText,
                nodeId: this.getNodeId
            });
            // 存在重复的命名
            if (hasEqualNodeName) {
                if (!isPressEnter) {
                    this.$message.warning('节点名称重复，当前修改失效');
                }
            } else {
                if (this.originNameText !== this.nodeNameText) {
                    this.model.setProperties({
                        nodeName: this.nodeNameText
                    });
                    deleteTempOutputs();
                }
            }
        }
    }
};
</script>

<style scoped lang="less">
@import url('../../customCss/index.less');
.node-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 14px;
    font-weight: 500;
    height: 24px;
    .node-icon {
        width: 24px;
        height: 24px;
        // margin-right: 8px;
        margin: 0 8px;
    }

    .mode-text {
        height: 20px;
        padding: 0 8px;
        color: #ff9326;
        border-radius: 4px;
        background: #fff4e6;
        font-size: 12px;
        line-height: 20px;
        margin-left: 5px;
    }
    .node-name {
        height: 24px;
        font-size: 12px;
        position: unset !important;
    }
    .node-name-text {
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
    }
}
</style>
