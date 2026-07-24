<!--
 * @Author: hewenquan
 * @Date: 2025-07-29 11:22:49
 * @LastEditTime: 2025-07-29 11:26:05
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/nodeTemplate.vue
 * @Description: 回复模版
-->
<template>
    <div
        class="node-area-bg"
        v-if="mode === 'template'"
    >
        <div class="common-title">
            {{ templateTitle }}
        </div>
        <div class="node-area-content">
            {{ templateText }}
        </div>
    </div>
</template>

<script>

export default {
    props: {
        templateTitle: {
            type: String,
            default: '回复模板'
        },
        templateText: {
            type: String,
            default: ''
        },
        mode: {
            type: String,
            default: 'template'
        }
    }
};
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
</style>