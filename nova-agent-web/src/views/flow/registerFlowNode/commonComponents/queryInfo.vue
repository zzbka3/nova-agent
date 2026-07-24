<!-- eslint-disable max-len -->
<!--
 * @Author: hewenquan
 * @Date: 2025-07-28 19:19:12
 * @LastEditTime: 2025-08-20 16:00:20
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/queryInfo.vue
 * @Description: 输出显示
-->
<template>
    <div class="query-info node-area-bg">
        <div class="input-vars-item">
            <div class="var-name">
                <a-icon
                    :type="inputVarsShows ? 'caret-down' : 'caret-right'"
                    @click.stop="changeInputShow"
                />
                {{ infoTitle }}
            </div>
            <div
                class="var-value"
                v-if="inputVarsShows"
            >
                值
            </div>
        </div>
        <template
            v-if="queryInfoData && queryInfoData.length"
        >
            <div
                :class="[
                    'input-vars-item',
                    { 'input-vars-item-hide': !inputVarsShows }
                ]"
                v-for="(item, index) in queryInfoData"
                :key="index"
            >
                <a-tooltip
                    :title="item.varName || '未命名'"
                    placement="topLeft"
                >
                    <div class="var-name">
                        <span class="text">{{ item.varName || '未命名' }}</span>
                        <span class="type"> {{ item.varType || '' }}</span>
                    </div>
                </a-tooltip>
                <div class="var-value">
                    <div
                        class="var-value-box"
                        v-if="item.varValue || item.referenceVarName"
                    >
                        <a-tooltip
                            :title="`${nodeNameMap[item.referenceNodeId] || ''}${item.referenceVarName || item.varValue}`"
                        >
                            <span class="var-value-info">
                                {{ `${nodeNameMap[item.referenceNodeId] || ''}` }}{{ item.referenceVarName || item.varValue }}
                            </span>
                        </a-tooltip>
                    </div>
                    <span v-else>未选择</span>
                </div>
            </div>
        </template>
        <div
            v-else
            v-show="inputVarsShows"
        >
            暂未配置
        </div>
    </div>
</template>

<script>
export default {
    props: {
        infoTitle: {
            type: String,
            default: '输出'
        },
        // query数据
        infoData: {
            type: Array,
            default: () => []
        },
        // 引用参数列表
        arrArgs: {
            type: Array,
            default: () => []
        },
        // 初始展开状态, 默认展开
        inputExpanded: {
            type: Boolean,
            default: true
        }
    },
    watch: {
        infoData: {
            handler(val) {
                this.queryInfoData = val;
            },
            deep: true
        },
        inputExpanded(val) {
            this.inputVarsShows = val;
        },
        arrArgs() {
            this.getNodeNameMap();
        }
    },
    data() {
        return {
            inputVarsShows: this.inputExpanded, // 输入变量展示
            queryInfoData: this.infoData,
            nodeNameMap: {}, // 节点名称映射
        };
    },
    mounted() {
        this.getNodeNameMap();
        this.bus.$on('editNodeName', ({ isEditName = false, nodeName, nodeId }) => {
            // 节点名称编辑成功后的回调
            if (!isEditName) {
                if (this.nodeNameMap[nodeId]) {
                    this.$set(this.nodeNameMap, nodeId, `${nodeName}/`);
                    this.$forceUpdate();
                }
            }
        });
    },
    methods: {
        /**
        * 切换输入框变量的显示状态
        *
        * 切换 this.inputVarsShows 的布尔值，用于控制输入框变量的显示和隐藏状态。
        */
        changeInputShow() {
            this.inputVarsShows = !this.inputVarsShows;
            this.$emit('updateNodeAttributes', this.inputVarsShows);
        },
        /**
        * 获取节点名称映射
        *
        * 遍历 arrArgs 数组，将每个节点的 nodeId 作为键，title 作为值存入 nodeNameMap 对象中
        */
        getNodeNameMap() {
            this.arrArgs.map(item => {
                this.nodeNameMap[item.nodeId] = `${item.title}/`;
            });
        },
    }
};
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.query-info {
    .input-vars-item {
        display: flex;
        align-items: center;
        gap: 10px;
        height: auto;
        margin-bottom: 4px;
        .var-name {
            width: 50%;
            display: flex;
            align-items: center;
            color: #151b26;
            font-weight: 500;
            .anticon {
                margin-right: 5px;
            }
            .text {
                overflow: hidden;
                max-width: 160px;
                white-space: nowrap;
                text-overflow: ellipsis;
                color: #5c5f66;
            }
            .type {
                padding: 0px 5px;
                text-align: center;
                border-radius: 4px;
                background: #e8e9eb;
                font-weight: 400;
                margin-left: 3px;
                color: #5c5f66;
            }
        }
        .var-value {
            width: 50%;
            .var-value-box {
                display: flex;
                box-sizing: border-box;
                width: fit-content;
                max-width: calc(100% - 8px);
                padding: 0 4px;
                border: 1px solid #e8e9eb;
                border-radius: 4px;
                background-color: #fff;
                .var-value-info {
                    display: inline-block;
                    width: 100%;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                }
            }
        }
    }
    .input-vars-item-hide {
        height: 0;
        overflow: hidden;
    }
}
</style>