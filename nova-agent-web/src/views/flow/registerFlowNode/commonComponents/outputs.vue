<!--
 * @Author: hewenquan
 * @Date: 2025-06-19 16:09:06
 * @LastEditTime: 2025-09-03 19:07:34
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/outputs.vue
 * @Description: 节点调试输出面板
-->
<template>
    <div class="outputs-wrapper">
        <div
            class="outputs-container"
        >
            <div class="outputs-wrapper">
                <div class="status-wrapper">
                    <img
                        class="status-icon"
                        :src="statusIcon"
                    />
                    <span class="status-text">
                        {{ statusText }}
                    </span>
                    <span
                        v-if="costTime"
                        class="success-tag"
                    >
                        {{ costTime }}
                    </span>
                    <span
                        class="success-tag"
                        v-if="usedTokens"
                    >
                        {{ usedTokens }}
                    </span>
                </div>
                <div
                    class="trigger"
                    @click="showOutputsDetail = !showOutputsDetail"
                >
                    {{ showOutputsDetail ? '收起' : '展开' }}
                    <a-icon
                        :type="showOutputsDetail ? 'up' : 'down'"
                        class="trigger-icon"
                    />
                </div>
            </div>
            <div
                class="outputs-detail"
                v-show="showOutputsDetail"
            >
                <div
                    class="inputs-container"
                >
                    <div
                        class="inputs-title"
                        v-if="inputVars"
                    >
                        <span
                            class="inputs-trigger"
                            @click="showInputsDetail = !showInputsDetail"
                        >
                            <a-icon
                                :type="showInputsDetail ? 'caret-down' : 'caret-right'"
                            />
                            输入
                        </span>
                        <a-icon
                            type="copy"
                            class="copy-icon"
                            @click="copy(inputVars)"
                        />
                    </div>
                    <JsonEditorVue
                        v-show="showInputsDetail"
                        v-model="inputVars"
                        class="jse-theme-dark"
                        mode="tree"
                        :main-menu-bar="false"
                        :navigation-bar="false"
                        :read-only="true"
                        lang="zh"
                        theme="Dark"
                    />
                </div>
                <div
                    class="inputs-container"
                    v-if="outputVars"
                >
                    <div
                        class="inputs-title"
                    >
                        <span
                            @click="showOutPutsDetail = !showOutPutsDetail"
                            class="inputs-trigger"
                        >
                            <a-icon
                                :type="showOutPutsDetail ? 'caret-down' : 'caret-right'"
                            />
                            输出
                        </span>
                        <a-icon
                            type="copy"
                            class="copy-icon"
                            @click="copy(outputVars)"
                        />
                    </div>
                    <JsonEditorVue
                        v-show="showOutPutsDetail"
                        v-model="outputVars"
                        class="jse-theme-dark"
                        mode="tree"
                        :main-menu-bar="false"
                        :navigation-bar="false"
                        :read-only="true"
                        lang="zh"
                        theme="Dark"
                    />
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import VCA from '@vue/composition-api';
import JsonEditorVue from 'json-editor-vue';
import Vue from 'vue';
import { copy } from '@baidu/metis-js-util';
Vue.use(VCA);
export default {
    props: {
        outputsData: {
            type: Object,
            default: () => ({})
        },
    },
    data() {
        return {
            showOutputsDetail: false, // 是否展开输出详情
            showInputsDetail: false, // 是否展开输入详情
            showOutPutsDetail: false // 是否展开输出详情
        };
    },
    components: {
        JsonEditorVue
    },
    computed: {
        // 节点允许状态
        status() {
            return this.outputsData?.status;
        },
        statusIcon() {
            const iconPath = {
                init: 'runFinish',
                finish: 'runFinish',
                exception: 'runError',
                running: 'running',
            };
            return require(`../../image/${iconPath[this.status]}.png`);
        },
        // 执行时长
        costTime() {
            const { costTime } = this.outputsData || {};
            if (costTime) {
                return `${costTime / 1000}s`;
            }
            return null;
        },
        // 执行时长
        usedTokens() {
            const { usedTokens } = this.outputsData || {};
            if (usedTokens) {
                return `${usedTokens} Tokens`;
            }
            return null;
        },
        /**
         * 根据状态返回对应的中文状态文本
         *
         * @returns {string} 返回对应的中文状态文本
         */
        statusText() {
            switch (this.status) {
                case 'init':
                    return '初始化';
                case 'finish':
                    return '运行成功';
                case 'exception':
                    return '运行失败';
                case 'running':
                    return '运行中';
                default:
                    return '未知状态';
            }
        },
        // 输入变量
        inputVars() {
            const { inputVars } = this.outputsData || {};
            if (inputVars) {
                const parsedInputVars = JSON.parse(inputVars);
                if (Array.isArray(parsedInputVars) &&  parsedInputVars.length) {
                    let result = {};
                    parsedInputVars.forEach(item => {
                        const { referenceVarName, varName, varValue} = item;
                        let key = varName || referenceVarName;
                        result[key] = varValue;
                    });
                    return result;
                } else {
                     return null;
                }
            }
            return null;
        },
        // 输出变量
        outputVars() {
            const { outputVars } = this.outputsData || {};
            if (outputVars) {
                const parsedOutputVars = JSON.parse(outputVars);
                if (Array.isArray(parsedOutputVars) &&  parsedOutputVars.length) {
                    let result = {};
                    parsedOutputVars.forEach(item => {
                        // result[item.varName] = item.varValue;
                        const { referenceVarName, varName, varValue} = item;
                        let key = varName || referenceVarName;
                        result[key] = varValue;
                    });
                    return result;
                } else {
                     return null;
                }
            }
            return null;
        }
    },
    methods: {
        copy(data) {
            const res = copy(JSON.stringify(data));
            if (res) {
                this.$message.success('复制成功');
            }
        }
    }
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
@import 'vanilla-jsoneditor/themes/jse-theme-dark.css';
// safari position 有问题
.outputs-wrapper {
    position: relative;
}
.outputs-container {
    position: fixed;
    top: calc(100% + 10px);
    left: 0;
    width: 100%;
    background: #303540;
    color: #fff;
    border-radius: 12px;
    padding: 12px;
    .outputs-detail {
        .inputs-container {
            margin-top: 10px;
            .inputs-trigger {
                cursor: pointer;
            }
            .copy-icon {
                cursor: pointer;
            }
        }
        /deep/ .jse-main {
            min-height: unset;
            .jse-tree-mode {
                border: none;
                background: none;
                .jse-contents {
                    border: none;
                }
            }
        }
    }
    .outputs-wrapper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 36px;
        .status-wrapper {
            display: flex;
            align-items: center;
        }
        .status-icon {
            width: 16px;
            height: 16px;
        }
        .status-text {
            padding: 0 8px;
            color: #fff;
            font-size: 14px;
            font-weight: 500;
            line-height: 24px;
        }
        .success-tag {
            color: #30bf13;
            background-color: #30bf1333;
            padding: 0 8px;
            border-radius: 6px;
            line-height: 20px;
            margin-right: 10px;
        }
        .trigger {
            cursor: pointer;
            .trigger-icon {
                width: 16px;
                height: 16px;
            }
        }
    }
}
</style>