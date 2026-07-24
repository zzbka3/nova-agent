<!--
 * @Author: v_yangxing06 v_yangxing06@baidu.com
 * @Date: 2025-12-04 18:26:54
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @LastEditTime: 2026-07-16 17:31:50
 * @FilePath: /metis-front/src/views/flow/autonomyAgent.vue
 * @Description: 自主规划Agent详情
-->
<template>
    <div
        class="autonomy-agent"
        @click="hideEditPrompt"
    >
        <div class="autonomy-agent-head">
            <div class="head-left">
                <a-button
                    icon="left"
                    @click="saveAutonomyAgent('back')"
                >
                </a-button>
                <div class="flow-info">
                    <a-tooltip
                        :title="autonomyAgentName"
                    >
                        <div
                            class="flow-name"
                        >
                            {{ autonomyAgentName }}
                        </div>
                    </a-tooltip>
                    <a-tooltip
                        :title="autonomyAgentRemark"
                    >
                        <div class="flow-desc">
                            {{ autonomyAgentRemark || '暂无描述' }}
                        </div>
                    </a-tooltip>
                </div>
            </div>
            <div>
                <a-button
                    type="primary"
                    @click="publishAutonomyAgent"
                >
                    发布
                </a-button>
            </div>
        </div>
        <div class="agent-config">
            <!-- Agent配置  -->
            <div class="agent-config-left">
                <div class="agent-config-title">
                    <div>Agent配置</div>
                    <div class="model-config">
                        <div>
                            <span class="model-title">
                                选择模型：
                            </span>
                            <a-select
                                v-model="autonomyAgentData.modelNames"
                                placeholder="请选择模型"
                                class="model-names-select"
                                style="width: 230px"
                                dropdown-class-name="model-select-dropdown"
                                @change="saveAutonomyAgent"
                            >
                                <a-select-option
                                    v-for="item in autonomyAgentModels"
                                    :key="item.modelCode"
                                    :value="item.modelCode"
                                >
                                    <div class="model-option">
                                        <img
                                            :src="item.icon"
                                            class="model-icon"
                                        />
                                        <span>{{ item.modelName }}</span>
                                    </div>
                                </a-select-option>
                            </a-select>
                        </div>
                        <div>
                            <span class="model-title">
                                备选模型：
                            </span>
                            <a-select
                                v-model="autonomyAgentData.autonomousFallbackModel"
                                placeholder="请选择备选模型"
                                class="model-names-select"
                                style="width: 230px"
                                dropdown-class-name="model-select-dropdown"
                                :allow-clear="true"
                                @change="saveAutonomyAgent"
                            >
                                <a-select-option
                                    v-for="item in autonomyAgentModels"
                                    :key="item.modelCode"
                                    :value="item.modelCode"
                                >
                                    <div class="model-option">
                                        <img
                                            :src="item.icon"
                                            class="model-icon"
                                        />
                                        <span>{{ item.modelName }}</span>
                                    </div>
                                </a-select-option>
                            </a-select>
                        </div>
                    </div>
                </div>
                <div class="agent-config-content">
                    <div
                        class="agent-info"
                    >
                        <div class="config-item">
                            <div class="config-label">
                                基本信息
                            </div>
                            <div class="config-content">
                                <a-input
                                    placeholder="请输入Agent名称"
                                    v-model="autonomyAgentData.name"
                                    :max-length="50"
                                    @blur="saveAutonomyAgent"
                                />
                                <a-textarea
                                    placeholder="请输入Agent描述"
                                    :rows="4"
                                    v-model="autonomyAgentData.remark"
                                    :max-length="100"
                                    @blur="saveAutonomyAgent"
                                />
                            </div>
                        </div>
                        <div class="prompt-config">
                            <div class="prompt-config-title">
                                <div class="prompt-config-name">
                                    业务流程配置
                                </div>
                                <a-radio-group
                                    v-model="autonomyAgentData.config.promptType"
                                    button-style="solid"
                                    size="small"
                                >
                                    <a-radio-button :value="0">
                                        自由配置
                                    </a-radio-button>
                                </a-radio-group>
                            </div>
                            <div>
                                <div class="prompt-config-label">
                                    prompt设置
                                </div>
                                <div
                                    class="prompt-config-content"
                                    @click.stop
                                >
                                    <a-textarea
                                        placeholder="请输入prompt"
                                        v-model="autonomyAgentData.config.promptText"
                                        @blur="handlePromptBlur"
                                        v-show="!showPromptTextPreview"
                                        auto-size
                                    />
                                    <div
                                        v-html="promptTextPreview"
                                        v-show="showPromptTextPreview"
                                        @click="showPromptTextareaHandle"
                                        class="prompt-preview"
                                    >
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="extend-config">
                        <div class="extend-config-title">
                            能力扩展
                        </div>
                        <div class="config-label">
                            平台变量
                        </div>
                        <autoAgentVar
                            :var-list="autonomyAgentData.config.varList"
                            @updateVar="updateVar"
                        />
                    </div>
                </div>
            </div>
            <!-- 预览与调试 -->
            <div class="agent-config-right">
                <autoAgentConversation
                    :is-save-agent.sync="isSaveAgent"
                    :autonomy-agent-data="autonomyAgentData"
                />
            </div>
        </div>
    </div>
</template>
<script>
import { getAgentDetail, saveAgent, publish, getAutonomyAgentModels } from './apiList';
import { flowRequest } from './common/request';
import autoAgentVar from '@/views/flow/basics/components/autoAgentVar.vue';
import autoAgentConversation from '@/views/flow/basics/components/autoAgentConversation.vue';
export default {
    data() {
        return {
            autonomyAgentData: {
                config: {}
            }, // Agent配置数据
            promptTextPreview: '', // prompt预览
            showPromptTextPreview: true, // 是否展示Prompt预览
            autonomyAgentName: '', // Agent名称
            autonomyAgentRemark: '',  // Agent描述
            isSaveAgent: false, // 配置更新状态
            autonomyAgentModels: [], // 模型列表
        };
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
        productLine() {
            return this.$route.params.productLine;
        },
    },
    components: {
        autoAgentVar,
        autoAgentConversation
    },
    created() {
        this.getAutonomyAgentModels();
        this.getAgentDetail();
    },
    methods: {
        /**
         * @description: prompt展示输入框
         * @return {*}
         */
        showPromptTextareaHandle() {
            this.showPromptTextPreview = false;
        },
        /**
         * @description: prompt输入框失焦
         * @return {*}
         */
        handlePromptBlur() {
            this.hideEditPrompt();
            this.saveAutonomyAgent();
        },
        /**
         * @description: 隐藏输入框，展示预览
         * @return {*}
         */
        hideEditPrompt() {
            this.promptTextPreviewHandel();
            this.showPromptTextPreview = true;
        },
        /**
         * @description: prompt预览，变量高亮处理
         * @return {*}
         */
        promptTextPreviewHandel() {
            const rawText = this.autonomyAgentData?.config?.promptText;
            // 判断是否存在变量（以 $ 开头）
            const hasVars = /\$[a-zA-Z0-9_]+/.test(rawText);
            // 如果没有变量，直接赋值并替换换行即可
            if (!hasVars) {
                this.promptTextPreview = rawText?.replace(/\n/g, '<br/>');
                return;
            }
            // 有变量 → 变量替换 + 换行替换
            let str = rawText?.replace(/(\$[a-zA-Z0-9_]+)/g, (match, p1, offset, fullStr) => {
                const before = fullStr.slice(0, offset);
                const after = fullStr.slice(offset + match.length);
                // 如果已经包裹就不处理
                if (
                    before.endsWith('<span class="var-text">') &&
                    after.startsWith('</span>')
                    ) {
                    return match;
                }
                return `<span class="var-text">${match}</span>`;
            });
            str = str?.replace(/\n/g, '<br/>');
            this.promptTextPreview = str;
        },
        // 获取 agent 详情
        async getAgentDetail() {
            if (!this.appId) {
                this.$router.back();
                return;
            }
            const data = await flowRequest({
                url: getAgentDetail,
                method: 'get',
                params: {
                    appId: this.appId
                }
            }).catch(() => {
                this.$message.error('获取 agent 详情失败, 请重试');
            });
            const copyData = JSON.parse(JSON.stringify(data || {}));
            const config = JSON.parse(copyData?.config);
            const hasAutonomousFallbackModel = Object.prototype.hasOwnProperty.call(
                copyData,
                'autonomousFallbackModel'
            );
            const autonomousFallbackModel = hasAutonomousFallbackModel
                ? copyData.autonomousFallbackModel
                : config?.autonomousFallbackModel;
            this.autonomyAgentData = {
                ...copyData,
                autonomousFallbackModel: autonomousFallbackModel || undefined,
                config,
            };
            this.autonomyAgentRemark = copyData?.remark || '';
            this.autonomyAgentName = copyData?.name || '';
            this.promptTextPreviewHandel();
        },
        /**
         * @description: 保存Agent配置
         * @return {*}
         */
        async saveAutonomyAgent(type) {
            const postData = JSON.parse(JSON.stringify(this.autonomyAgentData));
            postData.config.modelNames = postData.modelNames;
            postData.autonomousFallbackModel = postData.autonomousFallbackModel || '';
            postData.config.autonomousFallbackModel = postData.autonomousFallbackModel || '';
            postData.config =JSON.stringify(postData.config);
            await flowRequest({
                url: saveAgent,
                method: 'post',
                data: postData
            }).catch(() => {
                this.$message.error('保存失败, 请重试');
            });
            this.isSaveAgent = true;
            this.getAgentDetail();
            // 返回前保存配置
            if (type === 'back') {
                this.$router.push({
                    path: `/${this.productLine}/home/flowList`
                });
            }
        },
        async getAutonomyAgentModels() {
            const res = await flowRequest({
                url: getAutonomyAgentModels,
                method: 'get',
                params: {
                    appId: this.appId
                }
            }).catch((err) => {
                console.log(err);
            });
            this.autonomyAgentModels = res || [];
        },
        /**
         * @description: 发布Agent
         * @return {*}
         */
        async publishAutonomyAgent() {
            this.$loading.show();
            // 发布前先保存
            await this.saveAutonomyAgent();
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
        },
        /**
         * @description: 更新变量
         * @return {*}
         */
        updateVar(list) {
            this.autonomyAgentData.config.varList = list;
            this.saveAutonomyAgent();
        },
    }
};
</script>
<style lang="less" scoped>
.autonomy-agent {
    width: 100%;
    height: 100%;
    .autonomy-agent-head {
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
        box-sizing: border-box;
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
    .agent-config {
        height: calc(100% - 56px);
        background-color: #fff;
        display: flex;
        .agent-config-left {
            width: 70%;
            height: 100%;
            border-right: 1px solid #e8e9eb;
            .agent-config-title {
                display: flex;
                justify-content: space-between;
                align-items: center;
                width: 100%;
                height: 53px;
                padding: 0 20px;
                color: #000;
                font-size: 16px;
                font-weight: 500;
                border-bottom: 1px solid #e8e9eb;
                box-sizing: border-box;
                .model-config {
                    display: flex;
                    align-items: center;
                    gap: 16px;
                    .model-title {
                        color: #000;
                        font-size: 14px;
                        font-weight: 500;
                    }
                }
                .model-option {
                    display: flex;
                    align-items: center;
                }
                .model-icon {
                    width: 20px;
                    margin-right: 6px;
                }
            }
            .agent-config-content {
                width: 100%;
                display: flex;
                .agent-info {
                    width: 50%;
                    border-right: 1px solid #e8e9eb;
                    padding: 0 10px 10px;
                    overflow-y: auto;
                    height: calc(100vh - 110px);
                }
                .config-item {
                    .config-label {
                        color: #000;
                        font-size: 15px;
                        font-weight: 500;
                        text-align: left;
                        padding: 10px 0;
                    }
                    /deep/ .ant-input {
                        margin: 5px 0;
                    }
                }
                .prompt-config {
                    .prompt-config-title {
                        display: flex;
                        justify-content: space-between;
                        padding: 10px 0;
                        .prompt-config-name {
                            color: #000;
                            font-size: 15px;
                            font-weight: 500;
                        }
                    }
                    .prompt-config-label {
                        color: #000;
                        font-size: 14px;
                        font-weight: 500;
                        text-align: left;
                        padding-bottom: 10px;
                    }
                    .prompt-config-content {
                        /deep/ .ant-input {
                            min-height: calc(100vh - 385px);
                        }
                    }
                }
            }
        }
        .agent-config-right {
            width: 30%;
            height: 100%;
        }
    }
    .extend-config {
        width: 50%;
        height: calc(100vh - 110px);
        background-color: #fff;
        padding: 0 10px 10px;
        box-sizing: border-box;
        overflow-y: auto;
        .extend-config-title {
            color: #000;
            font-size: 15px;
            font-weight: 500;
            text-align: left;
            padding: 10px 0;
        }
        .config-label {
            color: #000;
            font-size: 14px;
            text-align: left;
            padding: 5px 0 10px;
            font-weight: 500;
        }
    }
}
.prompt-preview {
    text-align: left;
    min-height: calc(100vh - 385px);
    width: 100%;
    line-height: 1.5;
    vertical-align: bottom;
    box-sizing: border-box;
    padding: 4px 11px;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    background-color: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    white-space: pre-wrap;
    /deep/ .var-text {
        background-color: #dfe8fe;
        color: #465af2;
        padding: 3px;
        border-radius: 4px;
        box-sizing: border-box
    }
}
</style>
