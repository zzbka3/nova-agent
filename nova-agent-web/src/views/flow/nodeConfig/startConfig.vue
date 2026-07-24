<!--
 * @Author: hewenquan
 * @Date: 2025-06-17 11:24:08
 * @LastEditTime: 2025-09-03 14:31:49
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/startConfig.vue
 * @Description: 开始节点
-->
<template>
    <div class="start-config">
        <varsTree
            :tree-data="getArgs"
            title="输入"
        />
        <mockValueConfig
            title="业务字段"
            :config-data.sync="startMockVars"
            @updateConfigData="updateConfigData"
        />
    </div>
</template>
<script>
import { systemArgs } from '../getArgs';
import varsTree from '@/views/flow/registerFlowNode/commonComponents/varsTree.vue';
import mockValueConfig from './components/mockValueConfig.vue';
import { startMockVars } from '@/views/flow/basics/codeDealt';
import LogicFlow from '@logicflow/core';
export default {
    props: {
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    computed: {
        getArgs() {
            return systemArgs[0]?.children || [];
        },
        // 监听点击节点数据
        propertiesData() {
            return this.clickNode.properties || {};
        }
    },
    data() {
        return {
            startMockVars: []
        };
    },
    mounted() {
        this.init();
    },
    components: {
        varsTree,
        mockValueConfig
    },
    watch: {
        // 监听点击节点数据
        propertiesData() {
            this.init();
        }
    },
    methods: {
        init() {
            const { inputVars } = this.propertiesData;
            if (inputVars && inputVars.length) {
                this.startMockVars = inputVars;
            } else {
                this.startMockVars = startMockVars();
            }
        },
        updateConfigData(mockConfig) {
            console.log(mockConfig, 'mockConfig');
            this.updateLf();
        },
        /**
         * @description: 更新逻辑流图
         * @return {*}
         */
         updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            edgeModel.setProperties({
                inputVars: this.startMockVars,
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
    }
};
</script>
<style lang="less" scoped>
@import url('../customCss/index.less');
.start-title {
    margin-bottom: 10px;
}
.start-args {
    .start-args-item {
        margin-bottom: 4px;
        .args-item-name {
            color: #876300;
            overflow: hidden;
            max-width: 160px;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        .args-item-type {
            margin-left: 4px;
            padding: 0 5px;
            white-space: nowrap;
            border-radius: 4px;
            background-color: #e8e9eb;
        }
    }
}
</style>
