<!--
 * @Author: hewenquan
 * @Date: 2025-07-07 15:00:37
 * @LastEditTime: 2026-07-16 17:11:20
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @FilePath: /metis-front/src/views/flow/basics/checkConversation.vue
 * @Description: 调试弹窗
-->
<template>
    <a-spin
        :spinning="spinning"
        class="spin-box"
    >
        <div class="check-chat">
            <div class="check-title">
                预览与调试
            </div>
            <div
                class="chat-wrapper"
                ref="chatWrapper"
            >
                <div
                    v-for="(item, index) in allMsgList"
                    :key="index"
                    class="chat-item"
                >
                    <div
                        v-if="item.role === 'tip'"
                        class="msg-item-tip"
                    >
                        <a-divider>
                            {{ item.msg }}
                        </a-divider>
                    </div>
                    <div
                        v-else
                        class="chat-item-content"
                    >
                        <img
                            v-if="item.role === 'robot'"
                            class="msg-icon"
                            src="../image/robotIcon.png"
                        />
                        <img
                            v-else
                            class="msg-icon"
                            src="../image/userIcon.png"
                        />
                        <div
                            :class="[
                                'msg-item',
                                {
                                    'msg-item-robot': item.role === 'robot',
                                }
                            ]"
                        >
                            <MsgCard
                                :data="item.msg"
                                :close-hide="true"
                                :origin="2"
                                @emitEvent="emitEvent"
                            >
                            </MsgCard>
                            <!-- 消息耗时提示 -->
                            <div
                                v-if="item.role === 'robot'"
                                class="answer-info"
                            >
                                <span
                                    class="answer-info-item"
                                    v-if="item.totalCostTime"
                                >
                                    {{ Number(item.totalCostTime / 1000).toFixed(2) }}s
                                </span>
                                <span
                                    class="answer-info-item"
                                    v-if="item.totalUsedTokens > -1"
                                >
                                    | {{ item.totalUsedTokens }} Tokens
                                </span>
                                <span
                                    class="answer-info-item"
                                    v-if="item.requestId"
                                >
                                    <a-tooltip
                                        :title="item.requestId"
                                    >
                                        | {{ item.requestId }}
                                    </a-tooltip>
                                </span>
                                <a-tooltip
                                    title="复制requestId"
                                >
                                    <a-icon
                                        type="copy"
                                        class="copy-request-icon"
                                        v-if="item.requestId"
                                        @click="copyRequest(item.requestId)"
                                    />
                                </a-tooltip>
                            </div>
                        </div>
                    </div>
                    <div
                        class="chat-item-fileName"
                        v-if="item.fileName"
                    >
                        文件名：{{ item.fileName }}
                    </div>
                </div>
            </div>
            <!-- 功能区 -->
            <div class="chat-footer">
                <div
                    class="file-content"
                    v-if="fileId"
                >
                    <a-icon type="file" />
                    <a-tooltip
                        :title="fileName"
                    >
                        <span class="file-name">
                            {{ fileName }}
                        </span>
                    </a-tooltip>
                    <a-icon
                        type="close"
                        class="file-close"
                        @click="deleteFile"
                    />
                </div>
                <div class="chat-input-content">
                    <div
                        :class="[
                            'clear-btn',
                            {
                                'clear-btn-disabled': !allMsgList.length
                            }
                        ]"
                        @click="clearMsg"
                    >
                        <a-tooltip
                            title="清空历史会话"
                        >
                            <img
                                class="clear-img"
                                src="../image/clear.png"
                            />
                        </a-tooltip>
                    </div>
                    <div class="chat-input">
                        <a-textarea
                            class="chat-input-textarea"
                            placeholder="请输入您的问题"
                            :auto-size="{ minRows: 1, maxRows: 4 }"
                            v-model="inputMsg"
                            @keydown.native="handleKeyCode($event)"
                            :max-length="99999"
                        />
                        <div class="expand-wrapper">
                            <div class="upload">
                                <a-tooltip
                                    title="仅支持单个文件上传，支持png、 jpg、jpeg(限制200M)"
                                >
                                    <img
                                        src="../image/upload.png"
                                        class="upload-img"
                                        @click="uploadFile"
                                    />
                                </a-tooltip>
                            </div>
                            <div
                                :class="[
                                    'send',
                                    {
                                        'forbidden-send': forbiddenSend
                                    }
                                ]"
                                @click="sendMsg"
                            >
                                <img
                                    src="../image/send.png"
                                    class="send-img"
                                />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="chat-tip">
                    以上内容均由AI生成，仅供参考
                </div>
            </div>
        </div>
    </a-spin>
</template>

<script>
import { flowRequest } from '../common/request';
import sessionData from '@/utils/sessionData';
import LogicFlow from '@logicflow/core';
import { getFile } from '@/utils/uploader';
import axios from 'axios';
import { runningInfo, talk, fileUpload } from '../apiList';
import { copy } from '@baidu/metis-js-util';
import { workFlowMap } from './flowConfig';
export default {
    props: {
        // 调试会话ID
        conversationId: {
            type: String,
            required: true
        },
        lf: {
            type: LogicFlow,
            default: () => ({}),
            required: true
        },
    },
    data() {
        return {
            inputMsg: '', // 输入框内容
            // 当前消息列表
            msgList: [],
            historyMsgList: [], // 历史消息列表
            robotMessageLoading: false, // 机器消息加载中
            loadingTimer: null, // 机器消息加载超时定时器
            statusTimer: null, // 机器消息加载状态定时器
            fileId: '', // 文件ID
            fileName: '', // 文件名
            spinning: false,
            sendTimesTamp: Date.now(), // 发送会话时间戳
        };
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
        allMsgList() {
            if (this.historyMsgList.length > 0) {
                return [
                    ...this.historyMsgList,
                    {
                        role: 'tip',
                        msg: '配置已更新'
                    },
                    ...this.msgList
                ];
            } else {
                return [...this.msgList];
            }
        },
        /**
         * 禁止发送消息
         *
         */
        forbiddenSend() {
            if (this.robotMessageLoading) {
                return true;
            }
            if (!this.inputMsg || !this.inputMsg.trim()) {
                return true;
            }
            return false;
        },
        // 产品线ID
        productLine() {
            return this.$route.params.productLine;
        },
    },
    created() {
        const msgList = sessionData.get(`${this.appId}__msgList`);
        this.historyMsgList = msgList || [];
        this.scrollBottom();
    },
    methods: {
        /**
         * 获取运行状态信息
         *
         * @returns {Promise<void>} 无返回值
         */
        async getRunningInfo() {
            const data = {
                app_id: this.appId,
                conversation_id: this.conversationId,
                timestamp: this.sendTimesTamp
            };
            const res = await flowRequest({
                url: runningInfo,
                method: 'post',
                data
            }).catch(err => {
                console.log(err);
            });
            if (!res) {
                this.$message.error('获取运行状态信息失败');
                return;
            }
            // 等待下一次轮训
            if (res === 'NO_RUNNING_LOG') {
                this.statusTimer = setTimeout(() => {
                    this.statusTimer = null;
                    this.getRunningInfo();
                }, 1 * 1000);
                return;
            }
            const { nodes = [], edges = [], finish = 0 } = res || {};
            this.bus.$emit('openOutputs', {
                nodes
            });
            this.changeLineColor({ edges, lineType: 'EDGE_BEZIER_A' });
            // 未完成时，轮训继续获取运行信息
            if (finish < 1) {
                this.statusTimer = setTimeout(() => {
                    this.statusTimer = null;
                    this.getRunningInfo();
                }, 1 * 1000);
            } else {
                this.clearTimeoutRunningInfo();
            }
        },
        /**
         * 清除定时器运行信息
         *
         * 清除当前状态定时器，并将状态定时器置为null
         */
        clearTimeoutRunningInfo() {
            if (this.statusTimer) {
                clearTimeout(this.statusTimer);
                this.statusTimer = null;
            }
        },
        /**
         * 修改边线的颜色
         *
         * @param {Object} options - 包含边线和边线类型的对象
         * @param {Array} options.edges - 边线数组，默认为空数组
         * @param {string} options.lineType - 边线类型，默认为 'EDGE_BEZIER'
         */
        changeLineColor({ edges = [], lineType = 'EDGE_BEZIER' }) {
            const lf = this.lf;
            let outEdges = [];
            edges.forEach(item => {
                const { id, edgeId, type } = item || {};
                // 条件匹配成功时，边线颜色为绿色
                if (item.conditionMatch > 0) {
                    outEdges.push(edgeId);
                }
                // 恢复默认边线颜色
                if (lineType === 'EDGE_BEZIER' && type === 'EDGE_BEZIER_A') {
                    outEdges.push(id);
                }
            });
            outEdges.forEach(edge => {
                lf.changeEdgeType(edge, lineType);
            });
        },
        // 键盘回车事件
        handleKeyCode(event) {
            if (event.keyCode === 13) {
                if (!event.metaKey) {
                    event.preventDefault();
                    this.sendMsg();
                } else {
                    this.inputMsg = this.inputMsg + '\n';
                }
            }
        },
        /**
         * 滚动到底部
         */
        scrollBottom() {
            this.$nextTick(() => {
                const chatMain = this.$refs.chatWrapper;
                chatMain.scrollTo({
                    top: chatMain.scrollHeight + 200,
                    left: 0,
                    behavior: 'smooth'
                });
            });
        },
        async sendMsg() {
            if (!this.inputMsg) {
                this.$message.warning('请输入内容');
                return;
            }
            if (this.robotMessageLoading) {
                this.$message.warning('请等待回复结束再发送新消息');
                return;
            }
            this.clearTimeoutRunningInfo();
            // 添加用户消息
            this.msgList.push({
                role: 'user',
                msg: [
                    {
                        type: 'text',
                        value: this.inputMsg
                    }
                ],
                fileName: this.fileName,
                fileId: this.fileId,
                id: Date.now(),
            });
            const { edges = [] } = this.lf.getGraphData();
            this.changeLineColor({ edges, lineType: 'EDGE_BEZIER' });
            this.bus.$emit('openOutputs', { nodes: [] });
            this.tmpSaveMsgList();
            this.scrollBottom();
            this.changRobotMessageLoading(true);
            const data = {
                app_id: this.appId,
                conversation_id: this.conversationId,
                rawQuery: this.inputMsg,
                debug: 1
            };
            if (this.fileId) {
                data.file_ids = [this.fileId];
            }
            this.inputMsg = '';
            flowRequest({
                url: talk,
                method: 'post',
                data
            }).catch(err => {
                this.$message.error(err.message);
                this.changRobotMessageLoading(false);
            }).then(res => {
                const {
                    answer = '',
                    totalCostTime = '',
                    requestId = '',
                    totalUsedTokens = '',
                    workFlows = []
                } = res || {};
                if (!answer) {
                    this.$message.error('获取回复失败');
                    this.changRobotMessageLoading(false);
                    return;
                }
                let answerMsg = [
                    {
                        type: 'imMarkdown',
                        value: answer
                    }
                ];
                answerMsg = this.addWorkFlows(answerMsg, workFlows);
                this.msgList.push({
                    role: 'robot',
                    msg: answerMsg,
                    id: Date.now(),
                    totalCostTime,
                    requestId,
                    totalUsedTokens
                });
                this.tmpSaveMsgList();
            });
            this.sendTimesTamp = Date.now();
            this.getRunningInfo();
        },
        /**
         * @description: 追加流程组建的操作信息
         * @return {*}
         */
        addWorkFlows(answerMsg, workFlows) {
            if (!workFlows || !Array.isArray(workFlows) || !workFlows.length) {
                return answerMsg;
            }
            // 所有所有的fields
            let allFlatFlow = workFlows.reduce(((pre, cur) => {
                const { fields = [] } = cur || {};
                if (fields && Array.isArray(fields) && fields.length) {
                    pre.push(...cur.fields);
                }
                return pre;
            }), []);
            if (allFlatFlow && allFlatFlow.length) {
                let answerAction =
                    `<p>
                    <span
                        style="color: rgb(231, 95, 51);"
                    ><strong>执行动作: </strong></span>
                `;
                allFlatFlow.forEach(item => {
                    const { fieldName = '', fieldValue } = item || {};
                    if (workFlowMap && workFlowMap[fieldName]) {
                        answerAction += `<p>${workFlowMap[fieldName]}：${fieldValue}</p>`;
                    }
                });
                answerAction += '</p>';
                answerMsg.push(
                    {
                        type: 'html',
                        value: answerAction
                    },
                );
            }
            return answerMsg;
        },
        /**
         * 改变机器人消息加载状态
         *
         * @param boolean 是否正在加载消息
         */
        changRobotMessageLoading(boolean) {
            if (this.robotMessageLoading !== boolean) {
                this.robotMessageLoading = boolean;
                if (boolean) {
                    this.pushLoadingTalk();
                    this.loadingTimer = setTimeout(() => {
                        this.changRobotMessageLoading(false);
                        this.loadingTimer = null;
                    }, 30 * 1000);
                } else {
                    this.deleteLoadingTalk();
                }
            }
        },
        /**
         * 向消息列表中推送正在加载的聊天信息
         */
        pushLoadingTalk() {
            const talk = {
                id: Date.now(),
                role: 'robot',
                isLoading: true,
                msg: [
                    {
                        type: 'textLoading',
                        value: '工作流Agent执行中'
                    }
                ],
            };
            this.msgList.push(talk);
        },
        /**
         * @description: 删除C端追加的加载中提示语
         * @return {*}
         */
        deleteLoadingTalk() {
            const loadingIndex = this.msgList.findIndex(item => item.isLoading);
            if (loadingIndex > -1) {
                this.msgList.splice(loadingIndex, 1);
            }
        },
        /**
         * 临时保存消息列表
         *
         */
        tmpSaveMsgList() {
            this.changRobotMessageLoading(false);
            sessionData.set(`${this.appId}__msgList`, [...this.historyMsgList, ...this.msgList], 60);
        },
        /**
         * 清除消息
         *
         * @returns 无返回值
         */
        clearMsg() {
            if (!this.allMsgList.length) {
                return;
            }
            this.$emit('clearMsg');
            this.deleteFile();
            const { edges = [] } = this.lf.getGraphData();
            this.changeLineColor({ edges, lineType: 'EDGE_BEZIER' });
            // 清空输出内容
            this.clearTimeoutRunningInfo();
            this.bus.$emit('openOutputs', { nodes: [] });
            sessionData.clear(`${this.appId}__msgList`);
            this.msgList = [];
            this.historyMsgList = [];
        },
        /**
         * 删除文件
         *
         * 重置文件ID和文件名
         */
        deleteFile() {
            this.fileId = '';
            this.fileName = '';
        },
        /**
         * @description: 上传文件
         * @return {*}
         */
        async uploadFile() {
            const file = await getFile({
                accept: '.png, .jpg, .jpeg'
            });
            const fd = new FormData();
            fd.append('file', file);
            fd.append('app_id', this.appId);
            fd.append('conversation_id', this.conversationId);
            this.spinning = true;
            axios.defaults.headers.common['Authorization'] = 'Bearer aa37261e7fe213b6d1b7747b50c94d96';
            axios.defaults.headers.common['PRODUCT-LINE-ID'] = this.productLine;
            const res = await axios({
                url: fileUpload,
                data: fd,
                method: 'post'
            }).finally(() => {
                this.spinning = false;
            });
            const { status, data } = res.data || {};
            if (status === 0 && data) {
                this.fileId = data;
                this.fileName = file.name;
            } else {
                this.$message.error('上传失败');
            }
        },
        emitEvent(event, data) {
            if (!event) {
                return;
            }
            switch (event) {
                // 视频预览
                case 'clickImg': {
                    this.$viewerApi({
                        images: [data],
                        options: {
                            toolbar: false,
                            navbar: false,
                        }
                    });
                    break;
                }
                default:
                    break;
            }
        },
        /**
         * @description: 复制requestId
         * @param {*} requestId
         * @return {*}
         */
        copyRequest(requestId) {
            const res = copy(requestId);
            if (res) {
                this.$message.success('requestId 复制成功');
            }
        }
    },
};
</script>

<style lang="less" scoped>
.check-chat {
    width: 100%;
    height: 100vh;
    background: #f2f5f9;
    overflow-y: auto;
    position: relative;
    .check-title {
        padding: 18px 20px 14px;
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        color: #151B26;
    }
    .chat-wrapper {
        padding: 0 16px 20px;
        height: calc(100vh - 160px);
        box-sizing: border-box;
        overflow-y: auto;
        .chat-item {
            margin-bottom: 16px;
            .chat-item-content {
                display: flex;
            }
            .chat-item-fileName {
                margin-left: 50px;
            }
            .msg-item-tip {
                width: 100%;
                text-align: center;
            }
            .msg-icon {
                width: 32px;
                height: 32px;
                margin-right: 12px;
                margin-top: 6px;
            }
            .msg-item {
                box-sizing: border-box;
                width: fit-content;
                padding: 5px 10px;
                border-radius: 12px;
                background: #4b87ff;
                line-height: 24px;
                overflow-wrap: anywhere;
                /deep/ .normal-text {
                    color: #fff;
                }
                /deep/ .sed-editor-html {
                    border-top: 1px solid  #e8e8e8;
                    padding-top: 8px;
                }
            }
            .msg-item-robot {
                background: #fff;
                color: #192338;
                border: 1px solid #e8e9eb;
                position: relative;
                min-width: 240px;
                margin-bottom: 15px;
            }
            .answer-info {
                position: absolute;
                bottom: -25px;
                right: 0;
                color: #84868c;
                display: flex;
                align-items: center;
                justify-content: flex-end;
                .answer-info-item {
                    flex-wrap: nowrap;
                    margin-left: 5px;
                    max-width: 100px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
                .copy-request-icon {
                    cursor: pointer;
                }
            }
        }
    }
    .chat-footer {
        position: absolute;
        bottom: 0;
        width: 100%;
        padding: 16px;
        .file-content {
            display: inline-flex;
            align-items: center;
            box-sizing: border-box;
            padding: 5px 8px;
            border: 1px solid #e8e9eb;
            border-radius: 6px;
            background: #fff;
            margin-bottom: 5px;
            margin-left: 43px;
            .file-name {
                display: inline-block;
                max-width: 160px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                color: #151B26;
                padding: 0 10px;
            }
            .file-close {
                cursor: pointer;
            }
        }
        .chat-input-content {
            display: flex;
            align-items: center;
            .clear-btn {
                display: flex;
                align-items: center;
                flex-shrink: 0;
                justify-content: center;
                width: 36px;
                height: 36px;
                margin-right: 8px;
                cursor: pointer;
                color: #5c5f66;
                border: 1px solid #e8e9eb;
                border-radius: 18px;
                background: #ffffff80;
                &:hover {
                    background: #e0e4eb;
                }
                .clear-img {
                    width: 16px;
                    height: 16px;
                }
            }
            .clear-btn-disabled {
                cursor: not-allowed;
                &:hover {
                    background: #ffffff80;
                }
            }
            .chat-input {
                position: relative;
                display: flex;
                align-items: flex-end;
                box-sizing: border-box;
                width: 100%;
                padding: 9px 11px;
                border: 1px solid #e8e9eb;
                border-radius: 12px;
                background: #fff;
                box-shadow: 0 1px #09122105, 0 1px 1px #09122105, 0 3px 3px #09122103, 0 9px 9px #09122103;
                .chat-input-textarea {
                    border: none;
                }
                .expand-wrapper {
                    display: flex;
                    .upload{
                        border-right: 1px solid #e8e9eb;
                        padding: 0 12px;
                        .upload-img {
                            width: 24px;
                            height: 24px;
                            cursor: pointer;
                        }
                    }
                    .send {
                        padding-left: 12px;
                        cursor: pointer;
                        .send-img {
                            width: 24px;
                            height: 24px;
                        }
                    }
                    .forbidden-send {
                        cursor: not-allowed;
                    }
                }
            }
        }
        .chat-tip {
            margin-top: 9px;
            margin-left: 44px;
            color: #84868c;
            font-size: 12px;
            line-height: 14px;
        }
    }
}
</style>