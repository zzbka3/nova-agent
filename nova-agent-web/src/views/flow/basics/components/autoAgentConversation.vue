<!--
 * @Author: v_yangxing06 v_yangxing06@baidu.com
 * @Date: 2025-12-07 17:15:45
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @LastEditTime: 2026-07-16 17:11:25
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/autoAgentConversation.vue
 * @Description: 预览与调试
-->
<template>
    <div class="check-chat">
        <div class="check-title">
            预览与调试
        </div>
        <div
            class="chat-wrapper"
            ref="chatWrapper"
        >
            <div
                v-for="item in msgList"
                :key="item.id"
                class="chat-item"
            >
                <div class="chat-item-content">
                    <img
                        v-if="item.form === 0"
                        class="msg-icon"
                        src="@/views/flow/image/robotIcon.png"
                    />
                    <img
                        v-else
                        class="msg-icon"
                        src="@/views/flow/image/userIcon.png"
                    />
                    <div
                        :class="[
                            'msg-item',
                            {
                                'msg-item-robot': item.form === 0,
                            }
                        ]"
                    >
                        <MsgCard
                            :data="item.card_content"
                            :ref-key="item.id"
                            :key="item.id"
                            :current-msg="item"
                            @bodyScroll="bodyScroll"
                            :origin="2"
                            :close-hide="true"
                            :streaming="item.streaming"
                            @emitEvent="emitEvent"
                        >
                        </MsgCard>
                    </div>
                </div>
                <div
                    v-if="item.showTip"
                    class="msg-item-tip"
                >
                    <a-divider>
                        配置已更新
                    </a-divider>
                </div>
            </div>
        </div>
        <!-- 功能区 -->
        <div class="chat-footer">
            <div class="chat-input-content">
                <div
                    :class="[
                        'clear-btn',
                        {
                            'clear-btn-disabled': !msgList.length
                        }
                    ]"
                    @click="clearMsg"
                >
                    <a-tooltip
                        title="清空历史会话"
                    >
                        <img
                            class="clear-img"
                            src="@/views/flow/image/clear.png"
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
                                src="@/views/flow/image/send.png"
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
</template>

<script>
import sessionData from '@/utils/sessionData';
import { conversation } from '@/views/flow/apiList.js';
import { flowRequest } from '@/views/flow/common/request.js';
import { uniqueValue } from '@/views/flow/common/common';
import { sleep } from '@/utils/common.js';

export default {
    data() {
        return {
            inputMsg: '', // 输入框内容
            msgList: [], // 当前消息列表
            robotMessageLoading: false, // 机器消息加载中
            loadingTimer: null, // 机器消息加载超时定时器
            abortController: null,
            conversationId: '', // 会话 id
            isStreaming: false, // 是否在流失输出
        };
    },
    props: {
        // 配置是否更新
        isSaveAgent: {
            type: Boolean,
            default: false,
        },
        // Agent配置
        autonomyAgentData: {
            type: Object,
            default: () => {},
        },
    },
    watch: {
        // 监听更新状态
        isSaveAgent: {
            handler(val) {
                if (val) {
                    this.setTip();
                }
            }
        }
    },
    computed: {
        appId() {
            return this.$route.params.appId;
        },
        /**
         * 禁止发送消息
         *
         */
        forbiddenSend() {
            if (this.robotMessageLoading || this.isStreaming) {
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
        // prompt设置
        promptTex() {
            return this.autonomyAgentData.config.promptText;
        }
    },
    created() {
        this.init();
    },
    methods: {
        async init() {
            // 没有会话ID，创建会话
            if (!this.conversationId) {
                await this.getConversationId();
            }
            const msgList = sessionData.get(`${this.appId}__msgList`) || [];
            // 历史消息为非流式展示
            this.msgList = msgList?.map(item => {
                this.$set(item, 'streaming', false);
                return item;
            }) || [];
            this.setTip();
            this.bodyScroll();
        },
        /**
         * 创建一个新的会话
         */
        async getConversationId() {
            const res = await this.createConversation().catch(() => {
                this.$message.error('调试失败，请稍后重试');
            });
            this.conversationId = res;
        },
        emitEvent(event) {
            if (!event) {
                return;
            }
            if (event === 'updateStreaming') {
                this.isStreaming = true;
                this.bodyScroll();
            } else if (event === 'streamFished') {
                this.isStreaming = false;
            }
        },
        /**
         * 创建一个新的会话
         *
         * @returns {Promise<any>} 返回一个Promise对象，成功时resolve会话数据，失败时reject
         */
        createConversation() {
            return new Promise((resolve, reject) => {
                flowRequest({
                    url: conversation,
                    method: 'get',
                    params: {
                        appId: this.appId
                    }
                }).then(res => {
                    resolve(res);
                }).catch(() => {
                    reject();
                });
            });
        },
        /**
         * @description: 发送消息
         * @return {*}
         */
        async sendMsg() {
            if (!this.promptTex.trim()) {
                this.$message.warning('请完善Agent配置');
                return;
            }
            if (!this.inputMsg.trim()) {
                this.$message.warning('请输入内容');
                return;
            }
            if (this.robotMessageLoading || this.isStreaming) {
                this.$message.warning('请等待回复结束再发送新消息');
                return;
            }
            // 添加用户消息
            this.msgList.push({
                time: Date.now(),
                id: uniqueValue(),
                not_border: false,
                allWidth: false,
                card_type: 'card',
                streaming: true,
                form: 1,
                showTip: false,
                card_content: [
                    {
                        'type': 'text',
                        'value': this.inputMsg
                    },
                ],
            });
            this.tmpSaveMsgList();
            this.changRobotMessageLoading(true);
            this.$emit('update:isSaveAgent', false);
            this.bodyScroll();
            const content = {
                appId: this.appId,
                conversationId: this.conversationId,
                query: this.inputMsg,
                debug: 1
            };
            this.inputMsg = '';
            this.mockHttp(content);
        },
        async mockHttp(content) {
            this.abortController = new AbortController();
            const url = '/api/agent-manager/proxy/stream/v1/agent/stream-talk';
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    'PRODUCT-LINE-ID': this.productLine,
                },
                method: 'POST',
                body: JSON.stringify(content),
                signal: this.abortController.signal
            });
            if (!response.ok) {
                console.error(`API请求失败，状态码：${response.status}`);
                this.changRobotMessageLoading(false);
            }
            const reader = response.body?.getReader();
            const decoder = new TextDecoder();
            let finish = false;
            let count = 0;
            let beforeCount = -1;
            let beforeDecode = '';
            let streamMsg = '';
            while (!finish) {
                const { done, value } = await reader.read();
                console.log(done, count, beforeCount, 'chat_original_data', value);
                if (done) {
                    finish = true;
                    console.log(count, 'done_count_number');
                    if (count > 0) {
                        this.updateStreaming(this.msgList.length - 1, 'finish', streamMsg);
                    } else {
                        this.changRobotMessageLoading(false);
                    }
                    break;
                }
                const decodeValue = decoder.decode(value);
                let jsonArray = [];
                if (beforeCount === count) {
                    const allDecodeCode = beforeDecode + decodeValue;
                    jsonArray = this.parsePack(allDecodeCode);
                } else {
                    beforeDecode = '';
                    jsonArray = this.parsePack(decodeValue);
                }
                // 为代码
                if (decodeValue.length > 100 && !jsonArray.length) {
                    beforeCount = count;
                    beforeDecode += decodeValue;
                }
                console.log(JSON.stringify(jsonArray), count, 'jsonArray');
                if (jsonArray.length) {
                    count++;
                    if (count === 1) {
                        this.changRobotMessageLoading(false);
                    }
                    jsonArray.forEach((item) => {
                        const { content, finish_reason } = item;
                        const text = content?.[0]?.text || '';
                        console.log(finish_reason, content, 'finish_reason');
                        if (finish_reason === 'error') {
                            if (count > 1) {
                                this.updateStreaming(this.msgList.length - 1, 'finish', streamMsg);
                                this.changRobotMessageLoading(false);
                            } else {
                                this.changRobotMessageLoading(false);
                            }
                        }
                        if (count === 1) {
                            count++;
                            this.addRobotMsg(content);
                        } else {
                            this.updateMsgList(content, this.msgList.length - 1);
                        }
                        streamMsg = streamMsg + text;
                        if (finish_reason === 'stop') {
                            this.updateStreaming(this.msgList.length - 1, 'finish', streamMsg);
                        }
                        this.bodyScroll();
                    });
                }
            }
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
            const baseMsg = {
                card_type: 'card',
                form: 0,
                id: uniqueValue(),
                seqId: Date.now(),
                time: Date.now(),
                msgStatus: 2,
                isRt: false,
                isLoading: true
            };
            const talk = {
                ...baseMsg,
                card_content: [
                    {
                        type: 'textLoading',
                        value: 'Agent执行中'
                    }
                ]
            };
            this.msgList.push(talk);
            this.bodyScroll();
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
        parsePack(str) {
            const lines = str.split('\n');
            let result = [];
            lines.forEach(line => {
                if (line.startsWith('data:')) {
                    const text = line.replace(/^data:\s*/, '').trim();
                    if (text === '[DONE]') {
                        result.push({
                            content: [{ type: 'markdown', text: '' }],
                            finish_reason: 'stop'
                        });
                    } else if (text) {
                        result.push({
                            content: [{ type: 'markdown', text }],
                            finish_reason: null
                        });
                    }
                }
            });
            return result;
        },
        async updateMsgList(content, num) {
            if (!content) {
                return;
            }
            let result = [];
            result = this.msgModelHandel(content);
            if (!result.length) {
                return;
            }
            if (num < -1) {
                return;
            }
            await sleep(300);
            this.$nextTick(() => {
                this.msgList[num].card_content[0].value = result;
            });
        },
        /**
         * @description: 添加模型回复消息
         * @return {*}
         */
        addRobotMsg(content) {
            let result = [];
            if (!content) {
                return;
            }
            result = this.msgModelHandel(content);
            const defaultMsg = {
                time: Date.now(),
                form: 0,
                not_border: false,
                allWidth: false,
                card_type: 'card',
                streaming: true,
                showTip: false,
            };
            const robotAnswer = {
                ...defaultMsg,
                id: uniqueValue(),
                card_content: [
                    {
                        'type': 'imStreaming',
                        'value': result
                    },
                ],
            };
            this.msgList.push(robotAnswer);
        },
        /**
         * @description: 统一处理msg模型
         * @return {*}
         */
        msgModelHandel(content) {
            let result = [];
            content.forEach(item => {
                const { type, text = '' } = item;
                if (type === 'markdown' && text) {
                    result.push({
                        streamingType: 'markdown',
                        streamingValue: text
                    });
                }
            });
            return result;
        },
        async updateStreaming(num, type, streamMsg) {
            if (num < -1) {
                return;
            }
            await sleep(300);
            this.$nextTick(() => {
                this.msgList[num].card_content[0].value = [
                    {
                        streamingType: type,
                        streamingValue: type
                    }
                ];
                if (streamMsg) {
                    // eslint-disable-next-line max-len
                    this.tmpSaveMsgList([{ type: 'markdown', text: streamMsg }], num);
                }
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
         * 清除消息
         */
        clearMsg() {
            if (!this.msgList.length) {
                return;
            }
            if (this.robotMessageLoading || this.isStreaming) {
                this.$message.warning('请等待回复结束再操作');
                return;
            }
            this.conversationId = '';
            this.$emit('update:isSaveAgent', false);
            sessionData.clear(`${this.appId}__msgList`);
            this.msgList = [];
            this.robotMessageLoading = false;
            this.isStreaming = false;
            this.getConversationId();
        },
        /**
         * 临时保存消息列表
         *
         */
        tmpSaveMsgList(val, index) {
            this.deleteLoadingTalk();
            const sessionMsgList = sessionData.get(`${this.appId}__msgList`) || [];
            let msgList = JSON.parse(JSON.stringify(this.msgList));
            msgList = msgList.map(msg => {
                const match = sessionMsgList.find(item => item.id === msg.id);
                if (match) {
                    return {
                        ...msg,
                        card_content: JSON.parse(JSON.stringify(match.card_content))
                    };
                }
                return msg;
            });
            if (val) {
                const result = this.msgModelHandel(val) || [];
                msgList[index].card_content[0].value = result;
            }
            sessionData.set(`${this.appId}__msgList`, msgList, 60);
        },
        /**
         * 滚动到底部
         */
        bodyScroll() {
            this.$nextTick(() => {
                const chatMain = this.$refs.chatWrapper;
                chatMain.scrollTo({
                    top: chatMain.scrollHeight + 200,
                    left: 0,
                    behavior: 'smooth'
                });
            });
        },
        /**
         * 添加配置更新提示
         */
        setTip() {
            const lastIndex = this.msgList?.length - 1;
            this.msgList?.forEach((item, index) => {
                this.$set(item, 'showTip', index === lastIndex);
            });
        },
    }
};
</script>

<style lang="less" scoped>
.check-chat {
    width: 100%;
    height: 100%;
    background: #f2f5f9;
    overflow-y: auto;
    position: relative;
    .check-title {
        padding: 18px 20px 14px;
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        color: #151B26;
        text-align: left;
    }
    .chat-wrapper {
        padding: 0 16px 20px;
        height: calc(100vh - 160px - 56px);
        box-sizing: border-box;
        overflow-y: auto;
        .chat-item {
            margin-bottom: 16px;
            .chat-item-content {
                display: flex;
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
                color: #fff;
                text-align: left;
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
                text-align: left;
                line-height: 24px;
                /deep/ .normal-text {
                    color: #192338;
                }
                /deep/ .sed-editor-html {
                    border-top: 1px solid  #e8e8e8;
                    padding-top: 8px;
                }
            }
        }
    }
    .chat-footer {
        position: absolute;
        bottom: 0;
        width: 100%;
        padding: 16px;
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