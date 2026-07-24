<template>
    <a-modal
        v-model="showAgentModal"
        width="700px"
        :footer="null"
        :after-close="afterClose"
        :destroy-on-close="true"
    >
        <template slot="title">
            <div class="agent-modal-title">
                <span>选择工作流</span>
                <a-tag
                    class="agent-modal-title-tag"
                >
                    {{ selectAgentNum() ? `已添加${selectAgentNum()}个` : '暂未添加工作流' }}
                </a-tag>
            </div>
        </template>
        <div class="agent-list-warp">
            <div class="agent-list-warp-header">
                <div>
                    <a-input-search
                        placeholder="搜索关键词"
                        style="width: 240px"
                        @search="onSearch"
                        v-model="searchQuery"
                    />
                </div>
            </div>
            <a-spin
                tip="Loading..."
                :spinning="loading"
            >
                <div class="agent-list-warp-content">
                    <div
                        class="agent-list-warp-content-item"
                        v-for="i in flowList"
                        :key="i.appId"
                    >
                        <div class="item-content">
                            <div
                                class="item-content-title"
                            >
                                {{ i.name }}
                            </div>
                            <div class="item-content-desc">
                                {{ i.remark || '暂无描述' }}
                            </div>
                            <div class="content-id">
                                <a-tag color="green">
                                    Agent 工作流
                                </a-tag>
                                <a-divider type="vertical" />
                                <a-tag>
                                    ID: {{ i.appId }}
                                </a-tag>
                            </div>
                        </div>
                        <a-button
                            class="item-add-btn"
                            type="primary"
                            @click="addAgent(i)"
                        >
                            添加
                        </a-button>
                    </div>
                    <div v-if="!flowList.length">
                        <a-empty />
                    </div>
                    <a-pagination
                        class="agent-pagination"
                        :current="pagination.current"
                        :page-size="pagination.pageSize"
                        :total="pagination.total"
                        v-if="pagination.total > 5"
                        @change="onChangeList"
                    />
                </div>
            </a-spin>
        </div>
    </a-modal>
</template>

<script>
import { getAgentList, verifyWorkFlow } from '@/views/flow/apiList';
import { flowRequest } from '@/views/flow/common/request';
import LogicFlow from '@logicflow/core';
import { workFlowOutPut } from '../codeDealt';
import getNodeInitNames from '@/views/flow/common/getNodeInitNames';
export default {
    props: {
        // 开启流程选择
        openAgentSelect: {
            type: Boolean,
            default: false
        },
        lf: {
            type: LogicFlow,
            default: () => ({}),
            required: true
        },
    },
    watch: {
        openAgentSelect() {
            this.showAgentModal = this.openAgentSelect;
            if (this.openAgentSelect) {
                this.pagination.current = 1;
                this.searchQuery = '';
                this.getAgentList();
            }
        }
    },
    data() {
        return {
            // 是否显示工作流选择
            showAgentModal: false,
            pagination: {
                total: 0,
                current: 1,
                pageSize: 5,
                showTotal: total => `共${total}条`,
            },
            loading: false, // 加载状态
            searchQuery: '', // 关键字查询
            flowList: [], // 工组流数组
        };
    },
    computed: {
        productLine() {
            return this.$route.params.productLine;
        }
    },
    created() {
        // this.getAgentList();
    },
    methods: {
        /**
         * @description: modal关闭
         * @return {*}
         */
        afterClose() {
            this.$emit('update:openAgentSelect', false);
        },
        /**
         * @description: 已经选择的agent 去重数
         * @return {*}
         */
        selectAgentNum() {
            const { nodes = [] } = this.lf.getGraphData();
            const filterWorkFlow = nodes.filter(node => node.type === 'WORKFLOW_AGENT');
            if (!filterWorkFlow || !filterWorkFlow.length) {
                return 0;
            }
            const allAgentAppid = filterWorkFlow.map(item => item.properties?.workflowAgentId);
            const uniqueAgentAppid = [...new Set(allAgentAppid)];
            if (uniqueAgentAppid) {
                return uniqueAgentAppid.length;
            }
            return 0;
        },
        onSearch() {
            this.pagination.current = 1;
            this.getAgentList();
        },
        // 翻页
        onChangeList(page) {
            this.pagination.current = page;
            this.getAgentList();
        },
        /**
         * @description: 获取知识库列表
         * @return {*}
         */
        async getAgentList() {
            this.loading = true;
            // 列表请求
            const { current, pageSize } = this.pagination || {};
            const data = {
                query: this.searchQuery,
                page: current,
                pageSize: pageSize,
                isPublished: 1
            };
            flowRequest({
                url: getAgentList,
                method: 'get',
                params: data
            }).then(res => {
                const { total = 0, list = [] } = res || {};
                this.pagination.total = total;
                this.flowList = list;
                this.loading = false;
            }).catch(err => {
                console.log(err);
                this.loading = false;
            });
        },
        /**
         * @description: 添加agent
         * @param {*} item
         * @return {*}
         */
        addAgent(item) {
            const { appId } = this.$route.params || {};
            this.loading = true;
            flowRequest({
                url: verifyWorkFlow,
                method: 'get',
                params: {
                    parentId: appId,
                    childId: item.appId
                }
            }).then(res => {
                if (res) {
                    this.loading = false;
                    this.afterClose();
                    // 获取不重复的节点名称
                    let nodeName = getNodeInitNames('', 'WORKFLOW_AGENT', this.lf, item.name);
                    this.lf.dnd.startDrag({
                        type: 'WORKFLOW_AGENT',
                        properties: {
                            outputVars: workFlowOutPut(),
                            nodeName: nodeName,
                            workflowAgentId: item.appId
                        }
                    });
                } else {
                    this.$message.error('此 Agent 不支持添加至当前工作流，存在循环引用问题');
                    this.loading = false;
                }
            }).catch(err => {
                console.log(err);
                this.$message.error('校验循环引用失败，请联系RD反馈问题');
                this.loading = false;
            });
        }
    }
};
</script>

<style lang="less" scoped>
.agent-list-warp {
    padding: 0 16px;
    min-height: 400px;
    .agent-pagination {
        display: flex;
        justify-content: flex-end;
        margin-top: 20px;
    }
}
.agent-modal-title {
    display: flex;
    align-items: center;
    .agent-modal-title-tag {
        margin-left: 10px;
    }
}
.agent-list-warp-content {
    margin-top: 12px;

    .agent-list-warp-content-item {
        display: flex;
        overflow: hidden;
        padding: 12px 0;
        border-bottom: 1px solid #e8e9eb;
        justify-content: flex-end;
        align-items: center;
        .item-content {
            display: flex;
            overflow: hidden;
            flex: 1;
            flex-direction: column;
            padding: 0 16px;
            .item-content-title {
                color: #192338;
                font-weight: 600;
                display: inline-block;
                max-width: 100%;
                vertical-align: bottom;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .item-content-desc {
                color: #ccc;
                font-size: 12px;
                font-weight: 400;
                margin: 5px 0;
            }
            .content-id {
                /deep/ .ant-tag {
                    margin-right: 0;
                }
            }
        }
    }
}
</style>