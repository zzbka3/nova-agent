<!--
 * @Author: hewenquan
 * @Date: 2025-07-21 10:34:25
 * @LastEditTime: 2025-11-05 10:57:27
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/basics/planDetail.vue
 * @Description: 方案详情
-->
<template>
    <a-drawer
        title="发布渠道详情"
        class="plan-drawer"
        placement="right"
        :visible="planDetailVisible"
        width="600"
        :mask="true"
        @close="closePlanDetail"
        :destroy-on-close="true"
    >
        <div class="plan-detail">
            <a-tree
                :default-expand-all="true"
                :show-line="true"
            >
                <a-icon
                    slot="switcherIcon"
                    type="down"
                />
                <a-tree-node
                    :title="`${item.appName} | AppId: ${item.appId} | Secret Key: ${item.secretKey}`"
                    v-for="item in planDetailData"
                    :key="item.appId"
                >
                    <a-tree-node
                        v-for="sub in item.channelList"
                        :key="sub.channelId"
                        :title="` 渠道ID: ${sub.channelId} | ${sub.channelName} | entranceId: ${getEntranceId(sub.url)}`"
                    >
                        <a-tree-node
                            v-for="plan in sub.planList"
                            :key="plan.planId"
                            :title="`接入方案名称: ${plan.planName} | 方案ID: ${plan.planId}`"
                        />
                    </a-tree-node>
                </a-tree-node>
            </a-tree>
        </div>
    </a-drawer>
</template>

<script>
export default {
    props: {
        planDetailData: {
            type: Array,
            default: () => ([]),
        },
        // 控制抽屉的显示与隐藏
        planDetailVisible: {
            type: Boolean,
            default: false,
        }
    },
    data() {
        return {
        };
    },
    mounted() {
        console.log(this.planDetailData, 'planDetailData');
    },
    methods: {
        closePlanDetail() {
            this.$emit('update:planDetailVisible', false);
        },
        /**
         * @description: 获取entranceId
         * @param {*} url
         * @return {*}
         */
        getEntranceId(url) {
            if (!url) {
                return '--';
            }
            const parts = url.split('/');
            return parts[parts.length - 1];
        },
    }
};
</script>

<style lang="less" scoped>
.plan-drawer {
    .plan-title {
        border-bottom: 1px solid #e8e9eb;
        padding-bottom: 16px;
        margin-bottom: 16px;
    }
}
</style>