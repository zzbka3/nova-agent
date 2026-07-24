<!--
 * @Author: hewenquan
 * @Date: 2025-06-20 17:14:06
 * @LastEditTime: 2025-11-03 16:42:25
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/index.vue
 * @Description: flowContainer
-->
<template>
    <div
        class="flow-wrapper"
    >
        <div class="flow-head">
            <div class="head-left">
                <a-button
                    icon="left"
                    @click="back"
                >
                </a-button>
                <div class="flow-info">
                    <a-tooltip
                        :title="agentData.name"
                    >
                        <div
                            class="flow-name"
                        >
                            {{ agentData.name }}
                        </div>
                    </a-tooltip>
                    <a-tooltip
                        :title="agentData.remark"
                    >
                        <div class="flow-desc">
                            {{ agentData.remark || '暂无描述' }}
                        </div>
                    </a-tooltip>
                </div>
            </div>
            <div
                v-if="showFlow"
            >
                <!-- <el-button
                    @click="validateFlow"
                >
                    校验数据
                </el-button> -->
                <el-button
                    type="primary"
                    @click="publishFlow"
                >
                    发布
                </el-button>
            </div>
        </div>
        <flowContainer
            class="flow-container"
            ref="flowContainer"
            @syncAgentData="syncAgentData"
            :key="appId"
            v-if="showFlow"
        />
    </div>
</template>

<script>
import flowContainer from './flowContainer.vue';
import { validateFlow, validateEdges } from './validateUtils/lf';
import { publish, initInfo } from './apiList';
import { flowRequest } from './common/request';
import { setInitInfo, deleteTempOutputs } from './getArgs';
import { sleep } from '@/utils/common';
export default {
    components: {
        flowContainer
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
        productLine() {
            return this.$route.params.productLine;
        },
    },
    data() {
        return {
            agentData: {}, // 画布详情
            showFlow: false, // 渲染画布
        };
    },
    created() {
        this.getInitInfo();
    },
    mounted() {
        window.addEventListener('beforeunload', this.removeSessionStorage);
    },
    methods: {
        /**
         * @description: 获取画布初始化入参数
         * @return {*}
         */
        async getInitInfo() {
            try {
                const res = await flowRequest({
                    url: initInfo,
                });
                const { systemVars, workFlowVars } = res || {};
                if (!systemVars) {
                    this.$message.error('系统参数获取失败，请稍后再试');
                    return;
                }
                const parseInfo = JSON.parse(systemVars);
                // 设置初始化数据的 nodeId
                parseInfo.nodeId = '1';
                parseInfo.key = '1-' + parseInfo.key;
                const cycleChildren = (children, nodeId) => {
                    return children.map(arg => {
                        arg.referenceNodeId = nodeId;
                        arg.id = '1_' + arg.varName;
                        if (arg.children) {
                            arg.children = cycleChildren(arg.children, nodeId);
                        }
                        return arg;
                    });
                };
                parseInfo.children = cycleChildren(parseInfo.children, parseInfo.nodeId);
                let workFlowVarsConfig = [];
                // 系统参数补充
                if (workFlowVars && JSON.parse(workFlowVars)) {
                    JSON.parse(workFlowVars).forEach(element => {
                        const { varName, varType } = element;
                        const workFlowItem = {
                            nodeId: '1',
                            varName,
                            id: '1_' + varName,
                            originalVarType: varType,
                            varType: varType
                        };
                        workFlowVarsConfig.push(workFlowItem);
                    });
                }
                setInitInfo({
                    parseInfo, workFlowVarsConfig: {
                        nodeId: '1',
                        children: workFlowVarsConfig,
                        title: '流程参数',
                        key: 'workFlowVars',
                    }
                });
                this.showFlow = true;
            } catch (error) {
                console.log(error, 'error');
            }
        },
        /**
         * @description: 同步画布详情数据
         * @param {*} data
         * @return {*}
         */
        syncAgentData(data) {
            this.agentData = data;
        },
        /**
         * 返回上一个页面
         */
        back() {
            // 清除定时保存定时器
            this.$refs.flowContainer.clearSaveInterval();
            this.$router.push({
                path: `/${this.productLine}/home/flowList`
            });
        },
        async validateFlow() {
            const flowConfig = await validateFlow({ bus: this.bus, lf: this.$refs.flowContainer.lf }).catch(error => {
                console.log(error, 'validateFlow');
                this.bus.$emit('setError', [error]);
            });
            if (flowConfig) {
                this.$message.success('校验通过');
            }
            console.log(flowConfig, 'flowConfig');
        },
        validateEdges() {
            const result = validateEdges({ lf: this.$refs.flowContainer.lf, bus: this.bus });
            console.log(result, '_validateEdges');
        },
        openOutputs() {
            const testData = {
                nodes: [
                    {
                        nodeId: '1',
                        inputVars: '[]',
                        outputVars: '[]',
                        status: 'finish', // 'INIT、REACH、RUNNING、FINISH、EXCEPTION'
                        exception: null,
                        costTime: 17,
                        usedTokens: 222
                    },
                    {
                        nodeId: '2',
                        // eslint-disable-next-line max-len, no-useless-escape
                        inputVars: '[{\"referenceNodeId\":\"1\",\"referenceVarName\":\"rawQuery\",\"referenceVarType\":\"String\",\"varName\":\"bookName\",\"varType\":\"reference\",\"varValue\":\"23\"},{\"referenceNodeId\":\"1\",\"referenceVarName\":\"fileUrls\",\"referenceVarType\":\"ArrayString\",\"varName\":\"fileUrls\",\"varType\":\"reference\",\"varValue\":[]}]',
                        outputVars: '[]',
                        status: 'RUNNING'
                    }
                ],
                edges: [
                    {
                        'id': null,
                        'creator': null,
                        'createTime': null,
                        'updater': null,
                        'updateTime': null,
                        'conversationId': null,
                        'appId': null,
                        'executeId': null,
                        'edgeId': '1',
                        'fromNodeId': '1',
                        'targetNodeId': '2',
                        'conditionMatch': 1
                    }
                ]
            };
            this.bus.$emit('openOutputs', testData);
            this.changeLineColor({ edges: testData.edges, lineType: 'EDGE_BEZIER_A' });
            setTimeout(() => {
                testData.nodes[1].status = 'finish';
                this.bus.$emit('openOutputs', testData);
            }, 3000);
            setTimeout(() => {
                this.changeLineColor({ edges: testData.edges, lineType: 'EDGE_BEZIER' });
                // this.bus.$emit('openOutputs');
            }, 6000);
        },
        /**
         * 发布画布
         */
        async publishFlow() {
            this.$loading.show();
            // 发布时清楚上下文参数，强制检查一次
            deleteTempOutputs();
            await sleep(100);
            // 画布配置校验
            const flowConfig = await validateFlow({
                bus: this.bus,
                lf: this.$refs.flowContainer.lf
            }).catch(error => {
                this.$loading.hide();
                console.log(error, 'validateFlow');
            });
            if (!flowConfig) {
                this.$loading.hide();
                return;
            }
            // 连线校验
            const edgesResult = validateEdges({
                bus: this.bus,
                lf: this.$refs.flowContainer.lf
            });
            if (!edgesResult) {
                this.$loading.hide();
                return;
            }
            const saveRes = await this.$refs.flowContainer.saveFlow('published').catch(() => {
                this.$loading.hide();
                this.$message.error('请检查 agent 配置是否正确');
            });
            if (saveRes) {
                // 发布
                const publishRes = await flowRequest({
                    url: publish,
                    method: 'get',
                    params: {
                        appId: this.appId
                    }
                }).catch(() => {
                    this.$loading.hide();
                    this.$message.error('发布失败');
                });
                if (publishRes) {
                    this.$loading.hide();
                    this.$message.success('agent 发布成功');
                }
            }
        },
        removeSessionStorage() {
            this.bus.$emit('removeSessionStorage');
        }
    },
    beforeDestroy() {
        window.removeEventListener('beforeunload', this.removeSessionStorage);
        this.removeSessionStorage();
    }
};
</script>

<style lang='less' scoped>
.flow-wrapper {
    .flow-head {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: space-between;
        box-sizing: border-box;
        padding: 0 20px;
        width: 100%;
        height: 56px;
        background: #f2f5f9;
        box-shadow: inset 0 -1px #e8e9eb;
    }
    .head-left {
        display: flex;
        align-items: center;
        .flow-info {
            margin-left: 20px;
            text-align: left;
            .flow-name {
                color: #000;
                font-size: 16px;
                font-weight: 500;
                line-height: 24px;
                max-width: 300px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .flow-desc {
                color: #84868c;
                font-size: 12px;
                line-height: 20px;
                max-width: 300px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
        }
    }

    .flow-container {
        height: calc(100vh - 56px);
    }
}
</style>