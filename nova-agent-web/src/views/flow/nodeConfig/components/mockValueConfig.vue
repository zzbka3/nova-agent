<!--
 * @Author: hewenquan
 * @Date: 2025-09-03 13:56:36
 * @LastEditTime: 2025-11-24 19:25:04
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/components/mockValueConfig.vue
 * @Description: 业务字段mock值配置
-->
<template>
    <div class="common-area mock-value-config">
        <div class="common-title">
            <a-icon
                :type="expandedVars ? 'caret-down' : 'caret-right'"
                @click.stop="expandVars"
            />
            {{ title }}
        </div>
        <div
            v-show="expandedVars"
            class="mock-config-content"
        >
            <div
                class="mock-config-item mock-title-item"
            >
                <div class="args-item-name mock-config-title">
                    参数名
                </div>
                <div class="args-item-type mock-config-title">
                    类型
                </div>
                <div
                    class="args-item-value mock-config-title"
                >
                    值
                </div>
            </div>
            <div
                v-for="(item, index) in mockConfig"
                :key="item.id"
                class="mock-config-item"
            >
                <div class="args-item-name">
                    {{ item.varNameAbbr }}
                </div>
                <div class="args-item-type">
                    {{ item.varType }}
                </div>
                <div
                    class="args-item-value"
                >
                    <a-select
                        v-if="item.selectOptions"
                        size="small"
                        v-model="item.varValue"
                        class="config-select-item"
                        @change="updateConfigData($event, index)"
                    >
                        <a-select-option
                            v-for="option in item.selectOptions"
                            :key="option.value"
                        >
                            <a-tooltip
                                :title="option.label"
                            >
                                {{ option.label }}
                            </a-tooltip>
                        </a-select-option>
                    </a-select>
                    <a-input
                        v-else
                        placeholder="请输入mock值"
                        size="small"
                        :value="item.varValue"
                        @change="updateVarValue($event, index)"
                        :max-length="50"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        title: {
            type: String,
            default: ''
        },
        // 配置数组
        configData: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            // 默认显示输出内容
            expandedVars: true,
            mockConfig: [], // mock 配置数据
        };
    },
    mounted () {
        this.init();
    },
    watch: {
        configData: {
            handler() {
                this.init();
            },
            deep: true
        }
    },
    methods: {
        init() {
            if (this.configData && this.configData.length) {
                this.mockConfig = this.configData;
            }
        },
        /**
        * 更新输入框变量值
        *
        * @param event 事件对象
        * @param item 当前项数据
        * @param index 当前项的索引
        */
        updateVarValue(event, index) {
            const target = event.target.value || '';
            this.updateConfigData(target, index);
        },
        /**
        * 更新配置数据
        *
        * @param value 需要更新的值
        * @param item 当前项
        * @param index 当前项的索引
        */
        updateConfigData(value, index) {
            this.mockConfig[index].varValue = value;
            this.$emit('update:configData', this.mockConfig);
            this.$emit('updateConfigData', this.mockConfig);
        },
        expandVars() {
            this.expandedVars = !this.expandedVars;
            this.$emit('updateNodeAttributes', this.expandedVars);
        }
    }
};
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.mock-value-config {
    margin-top: 20px;
    .common-title {
        margin-bottom: 10px;
    }
}
.mock-config-item {
    display: flex;
    margin-bottom: 15px;
    align-items: center;
    .args-item-name {
        color: #876300;
        overflow: hidden;
        width: 90px;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .args-item-type {
        margin-left: 4px;
        height: 20px;
        line-height: 20px;
        padding: 0 5px;
        white-space: nowrap;
        border-radius: 4px;
        background-color: #e8e9eb;
        max-width: 65px;
        margin-right: 20px;
    }
    .args-item-value {
        color: #000;
        display: inline-block;
        .config-select-item {
            width: 166px;
        }
    }
    .mock-config-title {
        color: #8c8c8c;
        background: unset;
    }
}
.mock-title-item {
    margin-bottom: 5px;
}
</style>