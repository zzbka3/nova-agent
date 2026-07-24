<!--
 * @Author: hewenquan
 * @Date: 2025-07-07 19:33:13
 * @LastEditTime: 2025-10-30 10:31:04
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/dialogTitle.vue
 * @Description: 配置头部
-->
<template>
    <div class="dialog-title">
        <div class="node-title">
            <img
                :src="nodeIcon"
                class="node-icon"
            />
            <span>
                {{ nodeTitle }}
            </span>
        </div>
        <div
            v-if="getNodeType === 'WORKFLOW_AGENT' && referAgentDetail.appId"
            class="agent-detail"
        >
            <!-- 工作流描述 -->
            <div class="agent-desc-container">
                <span class="agent-desc">
                    {{ referAgentDetail.remark || '暂无智能体描述' }}
                </span>
                <a-button
                    type="primary"
                    size="small"
                    @click="toAgentDetail"
                >
                    跳转
                </a-button>
            </div>
            <div class="agent-info">
                <div class="agent-info-title">
                    版本信息
                </div>
                <div class="agent-info-detail">
                    <div class="agent-info-item">
                        更新人: {{ referAgentDetail.updatorName }}
                    </div>
                    <div class="agent-info-item">
                        更新时间: {{ referAgentDetail.updateTime }}
                    </div>
                    <div class="agent-info-item">
                        发布时间: {{ referAgentDetail.publishedTime }}
                    </div>
                </div>
            </div>
        </div>
        <a-tooltip
            :title="descMap[getNodeType]"
            v-if="descMap[getNodeType]"
        >
            <div class="dialog-desc">
                {{ descMap[getNodeType] }}
            </div>
        </a-tooltip>
    </div>
</template>

<script>
import { getNodeMap, nodeList, originalNode } from '@/views/flow/basics/flowConfig.js';
import { getAgentDetail } from '@/views/flow/apiList';
import { flowRequest } from '@/views/flow/common/request';
export default {
    props: {
        clickNode: {
            type: Object,
            default: () => ({})
        }
    },
    computed: {
        // 获取节点类型
        getNodeType() {
            return this.clickNode.type;
        },
        // 获取节点标题
        nodeTitle() {
            // 返回节点名称
            if (this.propertiesData?.nodeName) {
                return this.propertiesData?.nodeName;
            }
            return getNodeMap(this.getNodeType);
        },
        // 获取节点图标
        nodeIcon() {
            let iconMap = {};
            [...originalNode, ...nodeList].forEach(item => {
                item.children.forEach(child => {
                    iconMap[child.type] = child.icon;
                });
            });
            return iconMap[this.getNodeType];
        },
        propertiesData() {
            return this.clickNode.properties || {};
        }
    },
    created() {
        this.getWorkFlowAgentDetail();
    },
    data() {
        return {
            // 节点描述枚举
            descMap: {
                START: '工作流运行的起点，开始节点支持定义此工作流所需的输入参数，包括用户输入的原始内容、用户与应用的对话历史和用户在应用对话中上传的文件变量，将会自动从用户输入中获取。',
                END: '工作流的最终节点，输出工作流运行后的最终结果。',
                IF: '连接多个下游分支节点，若设定条件成立则运行对应的条件分支，若均不成立则运行“否则”分支。',
                INTENT: '识别用户的输入意图，并分配到不同分支执行。',
                KNOWLEDGE: '根据输入的参数，在选定的知识库中检索相关片段并召回，返回切片列表。',
                API: '配置外部 API 服务，并调用该服务。',
                LLM: '调用大语言模型，根据输入参数和提示词生成回复',
                MESSAGE: '支持工作流运行过程中的消息输出。',
                CODE: '编写代码，处理输入输出变量来生成返回值。',
                REWRITE: '根据历史对话改写用户query, 对语义指代及省略补全。将改写后的query输入知识库节点可改善检索效果。',
                TEXT_PROCESSOR: '对多个字符串变量的格式进行处理。',
                MEMORY: '用于写入或读取 Agent 中的记忆变量，节点与 Agent 中的记忆变量名称需要相同才能匹配。',
            },
            // 引用的agent详情
            referAgentDetail: {}
        };
    },
    methods: {
        /**
         * @description: 获取引用的agent详情
         * @return {*}
         */
        async getWorkFlowAgentDetail() {
            if (this.getNodeType !== 'WORKFLOW_AGENT') {
                return;
            }
            if (!this.propertiesData?.workflowAgentId) {
                return;
            }
            const data = await flowRequest({
                url: getAgentDetail,
                method: 'get',
                params: {
                    appId: this.propertiesData.workflowAgentId
                }
            }).catch((error) => {
                console.log(error);
            });
            const {remark = '', updateTime = '', updatorName = '', publishedTime = ''} = data || {};
            this.referAgentDetail = {
                remark,
                updateTime,
                updatorName,
                publishedTime,
                appId: this.propertiesData.workflowAgentId
            };
        },
        /**
         * @description: 打开新的 agent 详情页面
         * @return {*}
         */
        toAgentDetail() {
            const { productLine } =  this.$route.params || {};
            window.open(`/${productLine}/flow/${this.referAgentDetail.appId}`, '_blank');
        }
    }
};
</script>

<style lang="less" scoped>
@import url('../customCss/index.less');
.dialog-title {
    border-bottom: 1px solid #e8e9eb;
    padding-bottom: 16px;
    margin-bottom: 16px;
    .dialog-desc {
        margin-top: 8px;
        font-size: 12px;
        font-weight: 400;
        line-height: 20px;
        color: #84868c;
        word-break: break-all;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
    }
    .agent-desc-container {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .agent-desc {
            display: inline-block;
            width: 300px;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
        }
    }
    .agent-info {
        border-top: 1px solid #e8e9eb;
        padding-top: 16px;
        margin-top: 16px;
        .agent-info-title {
            font-weight: bold;
            color: #151b26;
            font-size: 14px;
        }
        .agent-info-detail {
            display: flex;
            flex-wrap: wrap;
            .agent-info-item {
                width: 100%;
                flex-shrink: 0;
                margin-top: 6px;
            }
        }
    }
}
</style>