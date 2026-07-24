<!--
 * @Author: hewenquan
 * @Date: 2025-07-08 10:38:28
 * @LastEditTime: 2026-01-07 14:23:28
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/list.vue
 * @Description: 流程列表
-->
<template>
    <a-spin
        :spinning="spinning"
        class="spin-box"
    >
        <div class="flow-list-container">
            <div class="flow-title">
                <h1>智能体管理</h1>
            </div>
            <div class="flow-list">
                <div class="flow-list-tab">
                    <a-radio-group
                        v-model="agentType"
                        size="large"
                        @change="agentTypeChange"
                    >
                        <a-radio-button :value="0">
                            工作流Agent
                        </a-radio-button>
                        <a-radio-button :value="1">
                            自主规划Agent
                        </a-radio-button>
                    </a-radio-group>
                </div>
                <div class="flow-search">
                    <a-input-search
                        placeholder="请输入智能体名称或ID"
                        class="flow-search-input"
                        v-model="searchQuery"
                        @search="searchFlow"
                        @blur="searchFlow"
                    />
                    <div class="flow-btn-wrapper">
                        <a-tooltip
                            title="上传json文件，且大小不超过5M"
                        >
                            <a-button
                                type="primary"
                                ghost
                                class="flow-btn"
                                @click="importFlow"
                                v-if="agentType === 0"
                            >
                                导入应用
                            </a-button>
                        </a-tooltip>
                        <a-button
                            type="primary"
                            class="flow-btn"
                            @click="createFlow"
                        >
                            创建应用
                        </a-button>
                    </div>
                </div>
                <div>
                    <a-table
                        :columns="columns"
                        :data-source="flowList"
                        row-key="appId"
                        @change="onChangeList"
                        :pagination="pagination"
                        :scroll="{ x: 1200 }"
                    >
                        <template
                            slot="appId"
                            slot-scope="text"
                        >
                            <a-tooltip
                                title="点击复制"
                                @click="copyAppid(text)"
                            >
                                <span class="plan-bind-info">
                                    {{ text }}
                                </span>
                            </a-tooltip>
                        </template>
                        <template
                            slot="planBindInfoList"
                            slot-scope="text, record"
                        >
                            <a-tooltip
                                :title="text && text.length ? '点击查看详情' : ''"
                                @click="viewPlanBindInfo(text, record)"
                            >
                                <span class="plan-bind-info">
                                    {{ getPlanBindInfo(text) }}
                                </span>
                            </a-tooltip>
                        </template>
                        <template
                            slot="isPublished"
                            slot-scope="text"
                        >
                            <a-tag
                                color="green"
                                v-if="text > 0"
                            >
                                已发布
                            </a-tag>
                            <a-tag
                                v-else
                            >
                                未发布
                            </a-tag>
                        </template>
                        <template
                            slot="operation"
                            slot-scope="text, record"
                        >
                            <a-button
                                type="link"
                                @click="editFlow(record)"
                            >
                                编辑
                            </a-button>
                            <a-button
                                type="link"
                                @click="copyFlow(record)"
                            >
                                复制
                            </a-button>
                            <a-button
                                type="link"
                                v-if="record.isPublished > 0 && agentType === 0"
                                @click="exportFlow(record)"
                            >
                                导出
                            </a-button>
                            <a-button
                                type="link"
                                @click="deleteFlow(record)"
                            >
                                删除
                            </a-button>
                        </template>
                    </a-table>
                </div>
            </div>
        </div>
        <planDetail
            :plan-detail-data="planDetailData"
            :plan-detail-visible.sync="planDetailVisible"
        />
    </a-spin>
</template>

<script>
const columns = [
    {
        title: '智能体ID',
        dataIndex: 'appId',
        ellipsis: true,
        align: 'center',
        width: 100,
        scopedSlots: { customRender: 'appId' },
    },
    {
        title: '智能体名称',
        dataIndex: 'name',
        ellipsis: true,
        align: 'center',
        width: 150,
    },
    {
        title: '应用描述',
        dataIndex: 'remark',
        ellipsis: true,
        align: 'center',
        width: 100,
    },
    {
        title: '模型名称',
        dataIndex: 'modelNames',
        ellipsis: true,
        align: 'center',
        width: 100,
    },
    {
        title: '发布渠道',
        dataIndex: 'planBindInfoList',
        ellipsis: true,
        width: 150,
        align: 'center',
        scopedSlots: { customRender: 'planBindInfoList' },
    },
    {
        title: '发布状态',
        dataIndex: 'isPublished',
        width: 100,
        align: 'center',
        scopedSlots: { customRender: 'isPublished' },
    },
    {
        title: '更新人',
        dataIndex: 'updatorName',
        ellipsis: true,
        align: 'center',
        width: 100,
    },
    {
        title: '更新时间',
        dataIndex: 'updateTime',
        width: 180,
        align: 'center',
        ellipsis: true,
    },
    {
        title: '发布时间',
        dataIndex: 'publishedTime',
        width: 180,
        align: 'center',
        ellipsis: true,
    },
    {
        title: '操作',
        dataIndex: 'operation',
        width: 280,
        fixed: 'right',
        scopedSlots: { customRender: 'operation' },
    },
];
import { getFile } from '@/utils/uploader';
import { flowRequest } from './common/request';
import axios from 'axios';
import { getAgentList, saveAgent, copyAgent, deleteAgent, importAgent, exportAgent } from './apiList';
import { defaultNodeData, defaultAutonomyAgent } from '@/views/flow/basics/flowConfig.js';
import planDetail from './basics/planDetail.vue';
import { copy } from '@baidu/metis-js-util';
import sessionData from '@/utils/sessionData';

export default {
    data() {
        return {
            spinning: false,
            flowList: [],
            columns,
            pagination: {
                total: 0,
                current: 1,
                pageSize: 10,
                showTotal: total => `共${total}条`,
            },
            // eslint-disable-next-line max-len
            planDetailData: [], // 发布渠道详情
            planDetailVisible: false, // 发布渠道详情弹窗
            searchQuery: '', // 搜索框内容
            agentType: 0, // 智能体类型 0 工作流Agent， 1 自主规划Agent
        };
    },
    mounted() {
        this.getList();
    },
    components: {
        planDetail
    },
    computed: {
        // 产品线ID
        productLine() {
            return this.$route.params.productLine;
        },
    },
    methods: {
        // 切换智能体类型
        agentTypeChange() {
            this.flowList = [];
            this.searchQuery = '';
            this.pagination = {
                total: 0,
                current: 1,
                pageSize: 10,
                showTotal: total => `共${total}条`,
            };
            this.getList();
        },
        viewPlanBindInfo(text, record) {
            if (!text || !text.length) {
                return '';
            }
            this.planDetailVisible = true;
            this.planDetailData = record.planBindInfoList;
        },
        /**
         * 获取流程列表
         *
         * @param searchKey 搜索关键词
         */
        getList() {
            // 编辑智能体后返回列表页，获取进入智能体时的页码，保证页面停留在之前的页码&&智能体类型
            let currPage = sessionData.get('flowListPageNum');
            let agentType = sessionData.get('agentType');
            if (currPage && [0, 1].includes(agentType)) {
                this.pagination.current = currPage;
                this.agentType = agentType;
            }
            // 列表请求
            const { current, pageSize } = this.pagination || {};
            const data = {
                query: this.searchQuery,
                page: current,
                pageSize: pageSize,
                agentType: this.agentType
            };
            flowRequest({
                url: getAgentList,
                method: 'get',
                params: data,
                requestOptions: {
                    shouldToast: false
                }
            }).then(res => {
                const { total = 0, list = [] } = res || {};
                this.pagination.total = total;
                this.flowList = list.map(item => {
                    let result = {};
                    const listKeys = Object.keys(item);
                    listKeys.forEach(key => {
                        if (item[key]) {
                            result[key] = item[key];
                        } else {
                            result[key] = '--';
                        }
                    });
                    return result;
                });
            }).catch(err => {
                console.log(err);
                this.$message.warning('暂无权限，请联系管理员');
            });
            // 清除在编辑时记录的智能体列表页缓存&智能体类型
            sessionData.clear('flowListPageNum');
            sessionData.clear('agentType');
        },
        // 翻页
        onChangeList(pagination) {
            this.pagination.current = pagination.current;
            this.getList();
        },
        /**
         * 搜索流程
         *
         * @param value 要搜索的值
         */
        searchFlow() {
            this.pagination.current = 1;
            this.pagination.pageSize = 10;
            this.getList();
        },
        /**
         * 创建一个流程
         *
         * 将路由跳转到流程页面
         */
        async createFlow() {
            // 0 工作流Agent， 1 自主规划Agent
            const defaultConfig = this.agentType === 0 ? defaultNodeData : defaultAutonomyAgent;
            const modelNames = this.agentType === 0 ? '' : 'ernie-4.5-turbo-128k';
            // 工作流Agent,先保存一个空的流程，生成appId
            const resAppId = await flowRequest({
                url: saveAgent,
                method: 'post',
                data: {
                    name: '我的智能体应用',
                    remark: '',
                    modelNames,
                    reference_turns: 2,
                    status: 'draft',
                    agentType: this.agentType,
                    config: JSON.stringify(defaultConfig)
                }
            }).catch(() => {
                const message = this.agentType === 0 ? '新建流程失败， 请重试' : '新建智能体失败， 请重试';
                this.$message.error(message);
            });
            if (resAppId) {
                sessionData.set('flowListPageNum', 1, 60);
                sessionData.set('agentType', this.agentType, 60);
                // 0 工作流Agent， 1 自主规划Agent
                const path = this.agentType === 0
                    ? `/${this.productLine}/flow/${resAppId}`
                    : `/${this.productLine}/autonomyAgent/${resAppId}`;
                this.$router.push({ path });
            }
        },
        /**
         * 复制流程
         *
         * @param {Object} record - 包含appId的对象
         */
        async copyFlow(record) {
            const res = await flowRequest({
                url: copyAgent,
                method: 'get',
                params: {
                    appId: record.appId
                }
            }).catch(() => {
                this.$message.error('复制失败，请重试');
            });
            if (res) {
                this.$message.success('复制成功');
                sessionData.set('flowListPageNum', 1, 60);
                sessionData.set('agentType', this.agentType, 60);
                // 0 工作流Agent， 1 自主规划Agent
                const path = this.agentType === 0
                    ? `/${this.productLine}/flow/${res}`
                    : `/${this.productLine}/autonomyAgent/${res}`;
                this.$router.push({ path });
            }
        },
        /**
         * 编辑流程
         *
         * @param record 记录信息
         */
        editFlow(record) {
            sessionData.set('flowListPageNum', this.pagination.current, 60);
            sessionData.set('agentType', this.agentType, 60);
            // 0 工作流Agent， 1 自主规划Agent
            const path = this.agentType === 0
                ? `/${this.productLine}/flow/${record.appId}`
                : `/${this.productLine}/autonomyAgent/${record.appId}`;
            this.$router.push({ path });
        },
        /**
         * 删除流程
         *
         * @param record 要删除的流程记录对象
         */
        deleteFlow(record) {
            this.$confirm({
                title: '确定删除该流程吗？',
                content: '删除后不可恢复',
                onOk: () => {
                    console.log(record.appId);
                    flowRequest({
                        url: deleteAgent,
                        method: 'get',
                        params: {
                            appId: record.appId
                        }
                    }).then(() => {
                        this.$message.success('删除成功');
                        this.getList();
                    }).catch(() => {
                        this.$message.warning('删除失败，请重试');
                    });
                }
            });
        },
        /**
         * 导入流程
         */
        async importFlow() {
            const file = await getFile({
                accept: '.json'
            });
            const fd = new FormData();
            fd.append('file', file);
            console.log(file, 'file');
            if (file.size > 5 * 1024 * 1024) {
                this.$message.error('文件大小不能超过5M');
                return;
            }
            this.spinning = true;
            axios.defaults.headers.common['PRODUCT-LINE-ID'] = this.productLine;
            const res = await axios({
                url: importAgent,
                data: fd,
                method: 'post'
            }).finally(() => {
                this.spinning = false;
            });
            const { status, data, message = '' } = res.data || {};
            if (status === 0 && data) {
                this.$router.push({
                    path: `/${this.productLine}/flow/${data}`
                });
            } else {
                this.$message.error(message || '导入失败，请重试');
            }
        },
        /**
         * 导出流程
         *
         * @param record 包含 appId 的记录对象
         */
        async exportFlow(record) {
            axios.defaults.headers.common['PRODUCT-LINE-ID'] = this.productLine;
            const res = await axios({
                url: exportAgent,
                method: 'get',
                params: {
                    appId: record.appId
                },
                responseType: 'blob',
            });
            console.log(res);
            const fileName = `_agent_${record.appId}`;
            this.downloadJson(res, fileName, true);
        },
        /**
         * @description: 下载JSON
         * @param {*} result
         * @param {*} title
         * @param {*} setName
         * @return {*}
         */
        downloadJson(result, title, setName = false) {
            if (!result) {
                return this.$message.error('获取数据失败');
            }
            if (result.data.type === 'application/json') {
                const reader = new FileReader();
                reader.readAsText(result.data, 'utf-8');
                reader.onload = () => {
                    // const msg = JSON.parse(reader.result);
                    // // reader.result里面含报错信息
                    // if (msg.status !== 0) {
                    //     this.$message.error(msg.message);
                    //     return;
                    // }
                    const fileNames = result.headers['content-disposition'];
                    if (fileNames) {
                        // 解码
                        let fileName = decodeURIComponent(fileNames.match(/=(.*)$/)[1]);
                        if (setName) {
                            fileName = title + '.json';
                        }
                        // 处理返回的文件流
                        const content = result.data;
                        const blob = new Blob([content], {
                            type: 'application/octet-stream; charset=utf-8',
                        });
                        if ('download' in document.createElement('a')) {
                            // 非IE下载
                            const a = document.createElement('a'); // 创建一个a标签
                            a.download = fileName; // 指定文件名称
                            a.style.display = 'none'; // 页面隐藏
                            a.href = URL.createObjectURL(blob); // href用于下载地址
                            document.body.appendChild(a); // 插到页面上
                            a.click(); // 通过点击触发
                            URL.revokeObjectURL(a.href); // 释放URL 对象
                            document.body.removeChild(a); // 删掉a标签
                        } else {
                            // IE10 + 下载
                            navigator.msSaveBlob(blob, fileName);
                        }
                    }
                };
            }
        },
        /**
         * @description: 获取发布渠道预览信息
         * @param {*} info
         * @return {*}
         */
        getPlanBindInfo(info) {
            if (!info || !info?.length) {
                return '--';
            }
            const { appName = '', channelList = [] } = info?.[0] || {};
            const { channelName = '', planList = [] } = channelList?.[0] || {};
            const planName = planList[0]?.planName || '';
            return `${appName}/${channelName}/${planName}`;
        },
        copyAppid(text) {
            if (!text) {
                return;
            }
            const res = copy(text);
            if (res) {
                this.$message.success('复制成功');
            }
        }
    }
};
</script>

<style lang="less" scoped>
.flow-list-container {
    padding: 0 40px;
    .flow-list {
        margin: 0 100px 40px 0;
        padding: 20px;
        min-height: calc(100vh - 64px - 22px - 54px);
        width: 100%;
        box-sizing: border-box;
        text-align: left;
        background: rgba(255, 255, 255, .88);
        box-shadow: 0 1px 2px -2px rgba(212, 212, 255, .55), 0 3px 6px 0 rgba(215, 215, 255, .41);
        border-radius: 8px;
    }
    .flow-list-tab {
        margin-bottom: 20px;
    }
    .flow-search {
        display: flex;
        align-content: center;
        justify-content: space-between;
        margin-bottom: 20px;
        .flow-search-input {
            width: 256px;
        }
        .flow-btn-wrapper {
            display: flex;
            margin-left: 16px;
            .flow-btn {
                margin-right: 8px;
            }
        }
    }
    .plan-bind-info {
        cursor: pointer;
        display: inline-block;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}
</style>