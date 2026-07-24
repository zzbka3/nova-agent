<!-- eslint-disable max-len -->
<!--
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-06-27 13:19:45
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-24 16:48:57
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/components/conditional.vue
 * @Description: conditional
-->
<template>
    <div
        :class="[
            'node-container',
            {
                'node-selected': isSelected,
                'node-error': !validateStatus
            }
        ]"
        :ref="`conditional_${getNodeId}`"
    >
        <nodeOperate
            v-if="isSelected"
            :model="model"
            :lf="lf"
            @editNode="handleEditNode"
        />
        <nodeTitle
            ref="nodeTitleRef"
            class="custom-node-title"
            :model="model"
            :node-name="getNodeName"
            :node-icon="require('../../image/branch.png')"
            :validate-status="validateStatus"
            :show-fold="true"
            @toggleFoldAll="toggleFoldAll"
            :all-show="allShow"
            :err-result="errResult"
            :lf="lf"
        />
        <!-- <div class="node-title">
            <a-icon
                :type="allShow ? 'caret-down' : 'caret-right'"
                @click.stop="toggleFoldAll"
            />
            <img
                src="../../image/branch.png"
                class="node-icon"
            />
            分支器
        </div> -->
        <div
            class="conditional-item"
            v-for="(item, index) in conditionalList"
            :key="index"
        >
            <div class="item-title">
                <div class="title-icon-text">
                    <a-icon
                        :type="innerConditionsShows[index] ? 'caret-down' : 'caret-right'"
                        @click.stop="toggleFold(index)"
                    />
                    <h4>{{ index === 0 ? '如果' : '否则如果' }}</h4>
                </div>
                <div class="priority">
                    优先级 {{ index + 1 }}
                </div>
            </div>
            <div
                :class="{
                    'item-content': true,
                    'item-content-hide': !innerConditionsShows[index],
                }"
            >
                <div
                    class="condition-op"
                    :ref="`condition-op_${getNodeId}`"
                    v-show="item.innerConditions.length > 1"
                >
                    <div class="op-text">
                        {{ item.innerLogic === 'AND' ? '且' : '或' }}
                    </div>
                </div>
                <div
                    :class="{
                        'inner-conditions-box': true,
                        'conditions-box': item.innerConditions.length > 1,
                    }"
                    :ref="`inner-conditions-box_${getNodeId}`"
                >
                    <div
                        v-for="(ele, eleIndex) in item.innerConditions"
                        :key="eleIndex"
                        class="inner-conditions-item"
                    >
                        <div
                            class="inner-conditions"
                        >
                            <div
                                class="item-left"
                            >
                                <a-tooltip
                                    placement="topLeft"
                                >
                                    <template
                                        slot="title"
                                        v-if="ele.left.referenceVarName"
                                    >
                                        {{ `${nodeNameMap[ele.left.referenceNodeId] || ''}` }}{{ ele.left.referenceVarName }}
                                    </template>
                                    {{ `${nodeNameMap[ele.left.referenceNodeId] || ''}` }}{{ ele.left.referenceVarName || '暂未配置' }}
                                </a-tooltip>
                            </div>
                            <div class="item-op">
                                {{ getOpLabel(ele.op) }}
                            </div>
                            <div
                                :class="[
                                    'item-right',
                                    {
                                        'item-left': ele.right.varType === 'reference',
                                    }
                                ]"
                            >
                                <a-tooltip>
                                    <template slot="title">
                                        <span v-if="ele.right.varType === 'reference'">
                                            {{ `${nodeNameMap[ele.right.referenceNodeId] || ''}` }}{{ ele.right.referenceVarName || '暂未配置' }}
                                        </span>
                                        <span v-else>
                                            {{ ele.right.varValue || '暂未配置' }}
                                        </span>
                                    </template>
                                    <span v-if="ele.right.varType === 'reference'">
                                        {{ `${nodeNameMap[ele.right.referenceNodeId] || ''}` }}{{ ele.right.referenceVarName || '暂未配置' }}
                                    </span>
                                    <span v-else>
                                        {{ isEmpty(ele.right.varValue) ? '暂未配置': ele.right.varValue }}
                                    </span>
                                </a-tooltip>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="conditional-item">
            <div class="item-title">
                <div class="title-icon-text">
                    <h4>否则</h4>
                </div>
            </div>
        </div>
        <outputs
            :outputs-data="outputsData"
            v-if="showOutputs"
        />
    </div>
</template>
<script>
import LogicFlow from '@logicflow/core';
import nodeOperate from '../commonComponents/nodeOperate.vue';
import validateConditionalNode from '../../validateUtils/validateConditionalNode';
import outputs from '../commonComponents/outputs.vue';
import {
    getAllArgs,
    opOptions,
} from '@/views/flow/getArgs';
import nodeTitle from '../commonComponents/nodeTitle.vue';
import { isEmpty } from '@/views/flow/common/common';
export default {
    props: {
        name: {
            type: String,
            default: ''
        },
        model: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    components: {
        nodeOperate,
        nodeTitle,
        outputs
    },
    data() {
        return {
            // 条件列表
            conditionalList: [],
            // 条件选项
            opOptions,
            innerConditionsShows: [], // 条件列表的展开状态
            allShow: true, // 是否全部展开
            isSelected: false, // 是否选中当前节点
            arrArgs: [], // 系统参数树
            nodeNameMap: {}, // 节点名称映射
            isError: false,
            validateStatus: true, // 校验状态
            errResult: [], // 校验错误提示
            // 是否展示输出
            showOutputs: false,
            outputsData: {}, // 输出面板数据
        };
    },
    watch: {
        // 监听条件列表变化
        'conditionalList': {
            handler(newVal) {
                // eslint-disable-next-line max-len
                const innerConditionsShows = JSON.parse(sessionStorage.getItem(`${this.getNodeId}innerConditionsShows`));
                if (innerConditionsShows && innerConditionsShows.length > 0) {
                    if (newVal.length === innerConditionsShows.length) {
                        this.innerConditionsShows = innerConditionsShows;
                    } else {
                        // 补全缺失项为 true
                        for (let i = innerConditionsShows.length; i < newVal.length; i++) {
                            innerConditionsShows.push(true);
                        }
                        this.innerConditionsShows = innerConditionsShows;
                        // 更新 sessionStorage
                        // eslint-disable-next-line max-len
                        sessionStorage.setItem(`${this.getNodeId}innerConditionsShows`, JSON.stringify(innerConditionsShows));
                    }
                } else {
                    newVal.forEach((_, index) => {
                        this.$set(this.innerConditionsShows, index, true);
                    });
                    // eslint-disable-next-line max-len
                    sessionStorage.setItem(`${this.getNodeId}innerConditionsShows`, JSON.stringify(this.innerConditionsShows));
                }
            },
            immediate: true
        }
    },
    computed: {
        getNodeId() {
            const graph = this.model;
            return graph.id;
        },
        getNodeName() {
            return this.model?.properties?.nodeName;
        },
        // 获取节点属性数据
        propertiesData() {
            return this.model.properties || {};
        },
    },
    mounted() {
        this.init();
        this.initBus();
        this.$nextTick(() => {
            this.initResizeObserver();
        });
    },
    methods: {
        isEmpty,
        initResizeObserver() {
            const boxEls = this.$refs[`inner-conditions-box_${this.getNodeId}`] || [];
            const opEls = this.$refs[`condition-op_${this.getNodeId}`] || [];
            const boxes = Array.isArray(boxEls) ? boxEls : [boxEls];
            const ops = Array.isArray(opEls) ? opEls : [opEls];
            this.resizeObservers = [];
            boxes.forEach((boxEl, index) => {
                const opEl = ops[index];
                if (!boxEl || !opEl) return;
                const opTextEl = opEl.querySelector('.op-text');
                if (!opTextEl) return;
                const observer = new ResizeObserver(entries => {
                    for (let entry of entries) {
                        const height = entry.contentRect.height;
                        if (height > 40) {
                            opEl.style.height = height + 'px';
                        } else {
                            opEl.style.height = '0px';
                        }
                        const pseudoHeight = Math.max(0, height / 2 - 25); // 避免负值
                        opTextEl.style.setProperty('--pseudo-height', `${pseudoHeight}px`);
                    }
                });
                observer.observe(boxEl);
                this.resizeObservers.push(observer);
            });
        },
        initBus() {
            this.bus.$on('validateFlowChild', () => this.validateFlowChild());
            this.bus.$on('node:click', (args) => {
                this.isSelected = this.getNodeId === args?.data?.id;
            });
            this.bus.$on('validateConfigById', ({ nodeId }) => {
                if (nodeId === this.getNodeId) {
                    const data = validateConditionalNode({
                        model: this.model,
                        lf: this.lf
                    });
                    this.validateStatus = data.validateStatus;
                    this.errResult = data.errResult;
                }
            });
            this.bus.$on('removeSessionStorage', () => {
                sessionStorage.removeItem(`${this.getNodeId}allShow`);
                sessionStorage.removeItem(`${this.getNodeId}innerConditionsShows`);
            });
            // 处理输出面板
            this.bus.$on('openOutputs', (data) => {
                if (data && data?.nodes) {
                    const filtered = data.nodes.filter(item => item.nodeId === this.getNodeId);
                    this.showOutputs = filtered.length > 0;
                    this.outputsData = this.showOutputs ? filtered[0] : {};
                } else {
                    this.showOutputs = false;
                }
            });
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
        init() {
            const { conditionList } = this.propertiesData;
            this.conditionalList = conditionList;
            this.arrArgs = getAllArgs({ nodeId: this.getNodeId, lf: this.lf });
            this.getNodeNameMap();
            const showVal = sessionStorage.getItem(`${this.getNodeId}allShow`);
            this.allShow = showVal === null ? true : showVal === 'true';
            this.$nextTick(() => {
                this.setCustomAnchors();
            });
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
        /**
         * @description: 设置自定义锚点
         * @return {*}
         */
        setCustomAnchors() {
            const ref = this.$refs[`conditional_${this.getNodeId}`];
            const items = ref.getElementsByClassName('conditional-item');
            const customAnchors = [];
            Array.from(items).forEach((item, index) => {
                const top = item.offsetTop;
                const height = item.clientHeight;
                const { conditionIndex = -1 } = this.conditionalList[index] || {};
                customAnchors.push({
                    indexId: conditionIndex,
                    height: top + height / 2
                });
            });
            this.model.setProperties({
                customAnchors: customAnchors
            });
            setTimeout(() => {
                this.model.updatePath();
            }, 50);
        },
        /**
		 * 获取对比条件的label
         * @param {string} op 值
         * @return {string} label值
         */
        getOpLabel(op) {
            const opItem = this.opOptions.find(item => item.value === op);
            return opItem ? opItem.label : '';
        },
        /**
         * 切换折叠状态
         * @param {index} index 下标
         * @return {*}
         */
        toggleFold(index) {
            this.$set(this.innerConditionsShows, index, !this.innerConditionsShows[index]);
            sessionStorage.setItem(`${this.getNodeId}innerConditionsShows`, JSON.stringify(this.innerConditionsShows));
            this.$nextTick(() => {
                this.setCustomAnchors();
            });
        },
        /**
         * 切换全部折叠状态
         * @return {*}
         */
        toggleFoldAll(allShow) {
            this.allShow = allShow;
            this.innerConditionsShows.forEach((_, index) => {
                this.$set(this.innerConditionsShows, index, allShow);
            });
            sessionStorage.setItem(`${this.getNodeId}allShow`, allShow);
            sessionStorage.setItem(`${this.getNodeId}innerConditionsShows`, JSON.stringify(this.innerConditionsShows));
            this.$nextTick(() => {
                this.setCustomAnchors();
            });
        },
        getTitle(val, arr) {
            const item = arr.find(item => item.nodeId === val);
            return item ? item.title : '';
        },
        validateFlowChild() {
            const { validateStatus, errResult } = validateConditionalNode({
                model: this.model,
                lf: this.lf
            });
            this.validateStatus = validateStatus;
            this.errResult = errResult;
            // 校验不通过时，执行相关操作
            if (!validateStatus) {
                console.log(errResult);
                // 清空已经选择的节点
            }
            this.bus.$emit('childValidateDone', {
                nodeId: this.getNodeId,
                validateStatus, // 校验是否通过
            });
        },
        /**
         * @description: 编辑节点标题
         * @return {*}
         */
        handleEditNode() {
            this.$refs.nodeTitleRef.editNodeName();
        }
    },
    beforeDestroy() {
        if (this.resizeObservers && this.resizeObservers.length) {
            this.resizeObservers.forEach(observer => observer.disconnect());
        }
    }
};
</script>
<style lang="less" scoped>
@import url('../../customCss/index.less');
.custom-node-title {
    margin-bottom: 10px;
}
.conditional-item {
    width: 100%;
    background: #f9f9fb;
    border-radius: 5px;
    padding: 5px;
    margin-bottom: 10px;
    padding: 10px 5px;

    .item-title {
        width: 100%;
        text-align: left;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .title-icon-text {
            display: flex;
            justify-content: space-between;
            align-items: center;
            h4 {
                margin: 0;
                font-size: 14px;
                margin-left: 5px;
            }
        }
    }

    .item-content {
        width: 100%;
        display: flex;
        align-items: center;
        flex: 1;
        height: auto;
        .condition-op {
            float: left;
            width: 32px;
            height: 100%;

            .op-text {
                margin-left: 25%;
                background: #f9f9fb;
                color: #1890ff;
                height: 100%;
                display: flex;
                flex-direction: column;
                justify-content: center;
            }
            .op-text::before {
                content: '';
                display: block;
                border-radius: 5px 0 0 0;
                width: 13px;
                height: var(--pseudo-height);
                margin-left: 5px;
                border-top: #d4d6d9 1.5px solid;
                border-left: #d4d6d9 1.5px solid;
            }
            .op-text::after {
                content: '';
                display: block;
                border-radius: 0 0 0 5px;
                width: 13px;
                height: var(--pseudo-height);
                margin-left: 5px;
                border-bottom: #d4d6d9 1.5px solid;
                border-left: #d4d6d9 1.5px solid;
            }
        }

        .inner-conditions-box {
            width: 352px;
            .inner-conditions-item {
                min-height: 20px;
            }
            .inner-conditions {
                display: flex;
                align-items: center;
                width: 100%;
                margin: 4px 0;
                font-size: 12px;

                .item-left {
                    border-radius: 5px;
                    background-color: #fff;
                    padding: 2px 5px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    max-width: 50%;
                    border: 1px solid #e8e9eb;
                }

                .item-op {
                    margin: 0 5px;
                    flex: 0 0 auto;
                    font-size: 14px;
                }

                .item-right {
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    max-width: 50%;
                }
            }

        }
        .conditions-box {
            width: 320px;
        }
    }
    .item-content-hide {
        height: 0px;
        overflow: hidden;
    }
}
</style>