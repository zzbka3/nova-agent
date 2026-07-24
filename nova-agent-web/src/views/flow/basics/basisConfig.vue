<!--
 * @Author: hewenquan
 * @Date: 2025-07-04 14:41:16
 * @LastEditTime: 2025-09-11 10:01:11
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @FilePath: /metis-front/src/views/flow/basics/basisConfig.vue
 * @Description: 画布基础配置
-->
<template>
    <div class="basis-config">
        <div
            class="expand-basis"
            @click="openBasisConfig"
        >
            <img
                class="expand-img"
                src="../image/basisConfig.png"
            >
        </div>
        <a-drawer
            class="basis-drawer"
            placement="left"
            :visible="basicVisible"
            width="400"
            :mask="true"
            @close="closeBasisConfig"
            :destroy-on-close="true"
            :closable="false"
        >
            <div class="config-header">
                <span class="config-title">
                    应用配置
                </span>
                <div class="config-right">
                    <span
                        class="conversation-config"
                        @click.stop="openConversationConfig"
                    >
                        对话设置
                    </span>
                    <span
                        class="close-icon"
                        @click="closeBasisConfig"
                    >
                        <a-icon type="left" />
                    </span>
                </div>
            </div>
            <div class="config-cont">
                <div class="base-info">
                    <div
                        class="card-title"
                        @click.stop="baseInfoShow = !baseInfoShow"
                    >
                        <a-icon
                            class="title-icon"
                            :type="baseInfoShow ? 'caret-down' : 'caret-right'"
                        />
                        <span class="title-text">基本信息</span>
                    </div>
                    <div
                        :class="[
                            'base-info-cont',
                            { 'base-info-cont-hide': !baseInfoShow }
                        ]"
                    >
                        <!-- <div class="info-left">
                            <div class="info-img"></div>
                            <div class="info-btn"></div>
                        </div> -->
                        <div class="info-right">
                            <a-form :form="form">
                                <a-form-item>
                                    <a-input
                                        class="name-input"
                                        placeholder="请输入应用名称"
                                        :max-length="50"
                                        v-decorator="[
                                            'name',
                                            { rules: [{ required: true, message: '应用名称不能为空' }] }
                                        ]"
                                    />
                                    <div class="input-counter">
                                        {{ form.getFieldValue('name') ? form.getFieldValue('name').length : 0 }}/50
                                    </div>
                                </a-form-item>
                                <a-form-item>
                                    <a-textarea
                                        class="remark-input"
                                        v-model="formData.remark"
                                        :max-length="100"
                                        placeholder="请输入应用描述"
                                        :auto-size="{ minRows: 2, maxRows: 6 }"
                                    />
                                    <div class="input-counter remark-counter">
                                        {{ formData.remark ? formData.remark.length : 0 }}/100
                                    </div>
                                </a-form-item>
                            </a-form>
                        </div>
                    </div>
                </div>
                <div class="memory-variable opening-lines">
                    <div class="config-item-title">
                        记忆
                    </div>
                    <div
                        class="card-title memory-title"
                        @click.stop="memoryVariableShow = !memoryVariableShow"
                    >
                        <div>
                            <a-icon
                                class="title-icon"
                                :type="memoryVariableShow ? 'caret-down' : 'caret-right'"
                            />
                            <span class="title-text">记忆变量</span>
                            <a-tooltip class="radio-tooltip">
                                <template slot="title">
                                    开发者可根据应用场景设定记忆变量，每个变量支持存储一维、单个数据。
                                    记忆变量的值需要通过工作流中记忆变量节点进行写入和读取。读取记忆变量的值之后，
                                    可以在后续连接的节点中引用该记忆变量的值。
                                </template>
                                <a-icon type="question-circle" />
                            </a-tooltip>
                        </div>
                        <div>
                            <a-icon
                                type="plus"
                                class="memory-plus"
                                @click.stop="addMemoryVariable"
                            />
                        </div>
                    </div>
                    <div
                        :class="[
                            'opening-lines-cont',
                            { 'opening-lines-cont-hide': !memoryVariableShow }
                        ]"
                    >
                        <p class="memory-desc">
                            请先配置该应用的记忆变量，然后在工作流中配置记忆变量节点来写入和读取记忆变量的值。
                        </p>
                        <div class="memory-content">
                            <div
                                v-for="item in memorySchema"
                                :key="item.varName"
                            >
                                <a-dropdown-button
                                    @click="handleButtonClick"
                                    class="memory-item"
                                >
                                    <span class="memory-name">{{ item.varName }}</span>
                                    <a-menu
                                        slot="overlay"
                                        @click="e => handleMenuClick(e, item)"
                                    >
                                        <a-menu-item
                                            key="edit"
                                            class="memory-menu-item"
                                        >
                                            <a-icon type="edit" />
                                            编辑
                                        </a-menu-item>
                                        <a-menu-item
                                            key="copy"
                                            class="memory-menu-item"
                                        >
                                            <a-icon type="copy" />
                                            复制名称
                                        </a-menu-item>
                                        <a-menu-item
                                            key="delete"
                                            class="memory-menu-item"
                                        >
                                            <a-icon type="minus-circle" />
                                            删除
                                        </a-menu-item>
                                    </a-menu>
                                </a-dropdown-button>
                                <!-- <span>{{ item.varName }}</span> -->
                            </div>
                        </div>
                    </div>
                </div>
                <div class="opening-lines">
                    <div class="config-item-title">
                        对话
                    </div>
                    <div
                        class="card-title"
                        @click.stop="openingLinesShow = !openingLinesShow"
                    >
                        <a-icon
                            class="title-icon"
                            :type="openingLinesShow ? 'caret-down' : 'caret-right'"
                        />
                        <span class="title-text">开场白</span>
                    </div>
                    <div
                        :class="[
                            'opening-lines-cont',
                            { 'opening-lines-cont-hide': !openingLinesShow }
                        ]"
                    >
                        <a-textarea
                            class="opening-lines-input"
                            v-model="formData.openingLines"
                            placeholder="请输入开场白"
                            :rows="4"
                        />
                    </div>
                    <div
                        class="card-title"
                        @click.stop="suggestedQuestionShow = !suggestedQuestionShow"
                    >
                        <a-icon
                            class="title-icon"
                            :type="suggestedQuestionShow ? 'caret-down' : 'caret-right'"
                        />
                        <span class="title-text">推荐问</span>
                    </div>
                    <div
                        :class="[
                            'question-cont',
                            { 'question-cont-hide': !suggestedQuestionShow }
                        ]"
                    >
                        <div
                            class="question-item"
                            v-for="(item, index) in formData.suggestedQuestion"
                            :key="index"
                        >
                            <a-input
                                class="question-input"
                                v-model="item.question"
                                placeholder="请输入推荐问"
                                @input="questionInput(index)"
                                @blur="questionBlur($event, index)"
                            />
                            <a-icon
                                type="close"
                                :class="[
                                    'question-close',
                                    { 'disabled-close': formData.suggestedQuestion.length === index + 1 }
                                ]"
                                @click="deleteQuestion(index)"
                            />
                        </div>
                    </div>
                </div>
            </div>
        </a-drawer>
        <a-modal
            title="对话设置"
            :visible="conversationVisible"
            @ok="handleOk"
            @cancel="handleCancel"
            width="800px"
        >
            <div class="conversation-box">
                <div class="conversation-left">
                    <div
                        :class="[
                            'conversation-item',
                            { 'conversation-item-active': conversationActive === item.key }
                        ]"
                        v-for="(item) in conversationMenu"
                        :key="item.key"
                    >
                        {{ item.title }}
                    </div>
                </div>
                <div class="conversation-right">
                    <h2 class="right-item-title">
                        参考对话轮数
                    </h2>
                    <p class="right-item-desc">
                        设置应用中使用对话历史时的对话轮数，比如意图识别节点、信息收集节点、全局跳转节点等。对整个工作流Agent生效。
                    </p>
                    <div class="right-item-slider">
                        <div class="slider-label">
                            <span class="label-text">参考对话轮数：</span>
                            <a-tooltip>
                                <template slot="title">
                                    传入大模型上下文的最大对话轮数。推荐值为2，数值越大，多轮对话中上下文相关性越强，但Tokens消耗越多。
                                </template>
                                <a-icon
                                    type="question-circle"
                                    class="label-icon"
                                />
                            </a-tooltip>
                        </div>
                        <div class="slider-cont">
                            <a-row>
                                <a-col :span="16">
                                    <a-slider
                                        v-model="referenceTurns"
                                        :min="0"
                                        :max="100"
                                        :step="1"
                                    />
                                </a-col>
                                <a-col :span="4">
                                    <a-input-number
                                        v-model="referenceTurns"
                                        :min="0"
                                        :max="100"
                                        style="marginLeft: 16px"
                                    />
                                </a-col>
                            </a-row>
                        </div>
                    </div>
                </div>
            </div>
        </a-modal>
        <memorySchema
            ref="memorySchema"
            :visible.sync="memoryVariableVisible"
            @memoryHandleOk="memoryHandleOk"
            @memoryHandleCancel="memoryHandleCancel"
            :is-edit="isEdit"
            :memory-schema="memorySchema"
            @updataMemory="updataMemory"
            @deleteMemory="deleteMemory"
        />
    </div>
</template>

<script>
import { debounce } from '../common/common.js';
import memorySchema from './components/memorySchema.vue';
export default {
    props: {
        // 基础配置数据
        basisConfigData: {
            type: Object,
            default: () => ({})
        },
        // 基础配置详情数据
        agentData: {
            type: Object,
            default: () => ({})
        },
        // 是否是调试模式
        openCheck: {
            type: Boolean,
            default: true
        },
    },
    data() {
        return {
            basicVisible: false, // 基础配置开启
            basicData: {
                width: 100,
                height: 100,
            },
            baseInfoShow: true, // 基本信息是否展开
            openingLinesShow: true, // 开场白是否展开
            memoryVariableShow: true, // 记忆变量是否展开
            suggestedQuestionShow: true, // 推荐问是否展开
            form: this.$form.createForm(this),
            formData: { // 表单数据
                name: '', // 应用名称
                remark: '', // 应用描述
                openingLines: '', // 开场白
                suggestedQuestion: [ // 推荐问
                    {
                        question: '',
                        indexId: 1
                    }
                ],
            },
            indexId: 1, // 推荐问索引id
            conversationVisible: false, // 对话设置是否开启
            memoryVariableVisible: false, // 添加记忆变量弹窗
            conversationMenu: [ // 对话设置菜单
                {
                    title: '参考对话轮数',
                    key: 'referenceTurns'
                }
            ],
            conversationActive: 'referenceTurns',
            referenceTurns: 0, // 参考对话轮数
            memorySchema: [], // 记忆变量列表
            isEdit: false, // 是否为编辑状态
        };
    },
    components: {
        memorySchema
    },
    watch: {
        // 监听表单数据变化，更新提交数据
        formData: {
            handler(val) {
                let submitData = JSON.parse(JSON.stringify(val));
                let suggestedQuestion = (val.suggestedQuestion || [])
                    .map(item => item.question?.trim())
                    .filter(q => q);
                submitData.suggestedQuestion = JSON.stringify(suggestedQuestion);
                submitData.memorySchema = JSON.stringify(val.memorySchema);
                this.$emit('change', submitData);
            },
            deep: true
        },
    },
    methods: {
        /**
         * 打开基础配置弹窗
         */
        openBasisConfig() {
            if (this.openCheck) {
                this.$parent.$refs?.nodePanel?.closeCheckDialog();
                return;
            }
            this.basicVisible = true;
            this.basicData = this.basisConfigData;
            const data = JSON.parse(JSON.stringify(this.agentData));
            this.memorySchema = data.memorySchema ? JSON.parse(data.memorySchema) : [];
            let suggestedQuestion = data.suggestedQuestion ? JSON.parse(data.suggestedQuestion) : [];
            if (Array.isArray(suggestedQuestion) && suggestedQuestion.length > 0) {
                suggestedQuestion = suggestedQuestion.map((item, index) => ({
                    question: item,
                    indexId: index + 1
                }));
                if (suggestedQuestion.length < 3) {
                    suggestedQuestion.push({
                        question: '',
                        indexId: suggestedQuestion.length + 1
                    });
                }
            } else {
                suggestedQuestion = [{
                    question: '',
                    indexId: 1
                }];
            }
            this.indexId = suggestedQuestion.length;
            // 初始化表单数据
            this.formData = {
                ...data,
                suggestedQuestion: suggestedQuestion,
                memorySchema: this.memorySchema,
            };
            this.$nextTick(() => {
                this.form.setFieldsValue({
                    name: this.formData.name,
                });
            });
        },
        /**
         * 关闭基础配置面板
         */
        closeBasisConfig() {
            this.form.validateFields(['name']).then(values => {
                this.$set(this.formData, 'name', values?.name);
                this.basicVisible = false;
                this.$nextTick(() => {
                    this.$emit('closeBasisConfig');
                });
            }).catch(() => {
                this.$message.error('请输入应用名称');
            });
        },
        /**
         * 推荐问输入事件
         * @param {Number} index 推荐问索引
         */
        questionInput: debounce(function (index) {
            this.indexId++;
            if (index + 1 === this.formData.suggestedQuestion.length && this.formData.suggestedQuestion.length < 3) {
                this.formData.suggestedQuestion.push({
                    question: '',
                    indexId: this.indexId
                });
            }
        }, 500),
        /**
         * 推荐问失去焦点事件
         * @param {Event} event 事件对象
         * @param {Number} index 推荐问索引
         */
        questionBlur(event, index) {
            const lastIndex = this.formData.suggestedQuestion.length - 1;
            if (!event.target.value && this.formData.suggestedQuestion[lastIndex].question) {
                const item = this.formData.suggestedQuestion.splice(index, 1);
                this.formData.suggestedQuestion.push(item);
            }
        },
        /**
         * 删除推荐问
         * @param {Number} index 推荐问索引
         */
        deleteQuestion(index) {
            if (this.formData.suggestedQuestion.length !== index + 1) {
                this.formData.suggestedQuestion.splice(index, 1);
            }
        },
        /**
         * 确定按钮事件
         */
        handleOk() {
            this.conversationVisible = false;
            this.formData.referenceTurns = this.referenceTurns;
        },
        /**
         * 取消按钮事件
         */
        handleCancel() {
            this.conversationVisible = false;
        },
        /**
         * 打开对话设置面板
         */
        openConversationConfig() {
            this.conversationVisible = true;
            this.referenceTurns = this.formData.referenceTurns || 0;
        },
        /**
         * 添加记忆变量
         */
        addMemoryVariable() {
            this.isEdit = false;
            this.memoryVariableVisible = true;
            this.$refs.memorySchema?.init();
        },
        /**
         * 添加记忆变量确定按钮事件
         */
        memoryHandleOk(data) {
            this.memorySchema = data;
            this.formData.memorySchema = this.memorySchema;
            this.memoryVariableVisible = false;
        },
        /**
         * 添加记忆变量确定按钮事件
         */
        memoryHandleCancel() {
            this.memoryVariableVisible = false;
        },
        /**
         * @description: 修改记忆变量
         * @return {*}
         */
        handleButtonClick() {
            this.isEdit = true;
            this.memoryVariableVisible = true;
            this.$refs.memorySchema?.init();
        },
        /**
         * @description: 菜单点击事件
         * @param {Event} e 事件对象
         * @param {Object} item 记忆变量
         * @return {*}
         */
        handleMenuClick(e, item) {
            const { key = '' } = e || {};
            const { varName = '' } = item || {};
            switch (key) {
                case 'edit':
                    this.handleButtonClick();
                    break;
                case 'copy':
                    this.copyText(varName).then(() => {
                        this.$message.success('复制名称成功');
                    });
                    break;
                case 'delete':
                    this.deleteMemoryList(item);
                    break;
                default:
                    break;
            }
        },
        /**
         * @description: 复制文本方法
         * @param {String} text 文本
         * @return {Promise}
         */
        copyText(text) {
            if (navigator.clipboard && window.isSecureContext) {
                // 现代 API
                return navigator.clipboard.writeText(text);
            } else {
                // 兼容处理
                let textArea = document.createElement('textarea');
                textArea.value = text;
                textArea.style.position = 'fixed'; // 避免页面滚动
                textArea.style.opacity = 0;
                document.body.appendChild(textArea);
                textArea.focus();
                textArea.select();
                document.execCommand('copy');
                document.body.removeChild(textArea);
                return Promise.resolve();
            }
        },
        /**
         * @description: 删除记忆变量列表
         * @param {*} item 记忆变量
         * @return {*}
         */
        deleteMemoryList(item) {
            this.$confirm({
                title: '删除提示',
                content: '删除后不可撤销。如应用已发布，更新发布后该应用的用户将无法使用该记忆记忆变量，是否继续？',
                okText: '确认',
                cancelText: '取消',
                onOk: () => {
                    const index = this.memorySchema.indexOf(item);
                    if (index !== -1) {
                        const data = this.memorySchema.splice(index, 1);
                        this.$emit('deleteMemory', data);
                    }
                },
            });
        },
        /**
         * @description: 修改记忆变量数据
         * @param {object} data
         * @return {*}
         */
        updataMemory(data) {
            this.$emit('updataMemory', data);
        },
        /**
         * @description: 删除记忆变量数据
         * @param {object} data
         * @return {*}
         */
        deleteMemory(data) {
            this.$emit('deleteMemory', data);
        }
    },
};
</script>

<style scoped lang="less">
.expand-basis {
    position: absolute;
    z-index: 10;
    top: 6px;
    left: -1px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    cursor: pointer;
    border: 1px solid #d4d6d9;
    border-radius: 0 12px 12px 0;
    background: #fff;
    box-shadow: 0 4px 16px #0000000f;

    &:hover {
        background: #f2f5f9;
    }

    .expand-img {
        width: 14px;
        height: 14px;
    }
}

.basis-drawer {
    /deep/ .ant-drawer-body {
        padding: 0;
    }

    .config-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0 16px;
        height: 50px;
        border-bottom: 1px solid #e8e9eb;
    }

    .config-title {
        color: #151b26;
        font-size: 16px;
        font-weight: 500;
        line-height: 20px;
    }

    .config-right {
        display: flex;
        align-items: center;

        .conversation-config {
            display: flex;
            align-items: center;
            cursor: pointer;
            color: #2468f2;
            font-size: 12px;
            font-weight: 500;
            line-height: 20px;
        }

        .close-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 24px;
            height: 24px;
            cursor: pointer;
            border: 1px solid #e8e9eb;
            border-radius: 6px;
            background: #fff;
            margin-left: 16px;
            font-size: 10px;

            &:hover {
                background: #f2f5f9;
            }
        }
    }

    .config-cont {
        .base-info {
            padding: 0px 20px;
            border-bottom: 1px solid #e8e9eb;

            .base-info-cont {
                display: flex;
                margin-bottom: 16px;
                transition: 0.5s;
                max-height: 190px;
                overflow: hidden;

                .info-left {
                    width: 72px;
                    margin-right: 10px;

                    .info-img {
                        width: 72px;
                        height: 72px;
                        background-color: #d4d6d9;
                        margin-bottom: 10px;
                        border-radius: 10px;
                    }

                    .info-btn {
                        width: 72px;
                        height: 28px;
                        background-color: #d4d6d9;
                        border-radius: 5px;
                    }
                }

                .info-right {
                    flex: 1;

                    /deep/ .ant-form-item {
                        padding-bottom: 12px;
                        margin-bottom: 0px;

                        &:nth-child(2) {
                            padding-bottom: 0px;
                        }

                        .ant-form-item-control {
                            line-height: 1;
                        }

                        .ant-form-item-children {
                            position: relative;
                        }

                        .ant-form-explain {
                            font-size: 12px;
                        }
                    }

                    /deep/ .name-input {
                        font-size: 12px;
                        padding-right: 40px;
                        border-radius: 6px;
                        height: 30px;
                    }

                    /deep/ .remark-input {
                        font-size: 12px;
                        padding-bottom: 25px;
                        border-radius: 6px;
                        height: 64px;
                        min-height: 64px;
                        resize: none;
                    }

                    .input-counter {
                        position: absolute;
                        bottom: -4px;
                        right: 8px;
                        font-size: 12px;
                        color: #999;
                        line-height: 24px;
                        pointer-events: none;
                    }

                    .remark-counter {
                        bottom: 6px;
                    }
                }
            }

            .base-info-cont-hide {
                max-height: 0px;
                margin-bottom: 0px;
            }
        }

        .opening-lines {
            padding: 0px 20px;

            .config-item-title {
                height: 36px;
                margin: 0;
                color: #84868c;
                font-size: 14px;
                font-weight: 400;
                line-height: 36px;
            }

            .opening-lines-cont {
                border-bottom: 1px solid #e8e9eb;
                padding-bottom: 16px;
                transition: 0.5s;
                max-height: 1000px;
                overflow: hidden;

                .opening-lines-input {
                    resize: none;
                    font-size: 12px;
                    border-radius: 6px;
                }

                .memory-desc {
                    color: #84868c;
                    margin: 0 0 16px;
                    font-size: 12px;
                    font-weight: 400;
                    line-height: 20px;
                }

                .memory-content {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 10px;

                    .memory-item {
                        &:hover /deep/ .ant-btn {
                            background-color: #e3e8f0;
                        }

                        /deep/ .ant-btn {
                            height: 24px;
                            font-size: 12px;
                            border: none;
                            color: #151b26;
                            background-color: #f2f5f9;
                            line-height: 20px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            padding: 2px 5px;

                            .memory-name {
                                font-size: 12px;
                                display: inline-block;
                                white-space: nowrap;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                max-width: 140px;
                            }
                        }
                    }
                }
            }

            .opening-lines-cont-hide {
                max-height: 0px;
                padding-bottom: 0px;
            }
        }

        .memory-variable {
            padding: 0px 20px;
            border-bottom: 1px solid #e8e9eb;

            .opening-lines-cont {
                border-bottom: none;
            }
        }

        .question-cont {
            border-bottom: 1px solid #e8e9eb;
            padding-bottom: 16px;
            max-height: 140px;
            transition: 0.5s;
            overflow: hidden;

            .question-item {
                position: relative;
                margin-bottom: 10px;

                .question-input {
                    font-size: 12px;
                    padding-right: 25px;
                    border-radius: 6px;
                }

                .question-close {
                    font-size: 10px;
                    position: absolute;
                    right: 10px;
                    top: 50%;
                    transform: translateY(-50%);
                    color: #5c5f66;
                    cursor: pointer;
                }

                .disabled-close {
                    color: #b8babf;
                    cursor: not-allowed;
                }

                &:last-child {
                    margin-bottom: 0px;
                }
            }
        }

        .question-cont-hide {
            max-height: 0px;
            padding-bottom: 0px;
        }

        .card-title {
            padding: 13px 0;
            display: flex;
            align-items: center;
            cursor: pointer;

            .title-icon {
                font-size: 14px;
            }

            .title-text {
                padding-left: 8px;
                color: #303540;
                font-size: 14px;
                font-weight: 500;
            }

            .radio-tooltip {
                margin-left: 5px;
            }

            .memory-plus {
                cursor: pointer;
                color: #2468f2;
                font-size: 16px;
            }
        }

        .memory-title {
            display: flex;
            justify-content: space-between;
        }
    }
}

.conversation-box {
    width: 100%;
    display: flex;

    .conversation-left {
        width: 164px;

        .conversation-item {
            height: 32px;
            border-radius: 6px;
            background-color: #00000000;
            padding-inline: 12px 0;
            color: #5c5f66;
            font-size: 14px;
            font-weight: 500;
            line-height: 32px;

            &:hover {
                background-color: #f2f5f9;
            }
        }

        .conversation-item-active {
            color: #151b26;
            background-color: #f2f5f9;
        }
    }

    .conversation-right {
        flex: 1;
        margin: 0 4px 0 16px;
        padding: 0 20px 0 16px;
        border-left: 1px solid #e8e9eb;

        .right-item-title {
            margin: 0 0 12px;
            letter-spacing: 0;
            color: #151b26;
            font-size: 16px;
            font-weight: 500;
            line-height: 22px;
        }

        .right-item-desc {
            margin: 0 0 16px;
            letter-spacing: 0;
            color: #84868c;
            font-size: 14px;
            font-weight: 400;
            line-height: 20px;
        }

        .right-item-slider {
            display: flex;

            .slider-label {
                line-height: 40px;

                .label-text {
                    letter-spacing: 0;
                    color: #151b26;
                    font-size: 14px;
                    font-weight: 400;
                }

                .label-icon {
                    color: #84868c;
                    cursor: pointer;
                }
            }

            .slider-cont {
                flex: 1;
                margin-left: 16px;
            }
        }
    }
}
</style>
<style lang="less">
.memory-menu-item {
    font-size: 12px;
}
</style>