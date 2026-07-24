<!--
 * @Author: v_yangxing06 v_yangxing06@baidu.com
 * @Date: 2025-12-07 10:28:19
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @LastEditTime: 2025-12-07 18:54:03
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/autoAgentVar.vue
 * @Description: 变量配置
-->

<template>
    <div class="var-container">
        <div
            v-for="(item,index) in varList"
            :key="index"
            class="var-config-item"
        >
            <div class="var-item-box">
                <div class="var-item-name">
                    变量名
                </div>
                <a-input
                    v-model="item.name"
                    placeholder="请输入变量名"
                    @blur="updateVar"
                />
            </div>
            <div class="var-item-box">
                <div class="var-item-name">
                    类型
                </div>
                <a-select
                    placeholder="请选择类型"
                    v-model="item.type"
                    @change="updateVar"
                >
                    <a-select-option value="string">
                        mock
                    </a-select-option>
                </a-select>
            </div>
            <div class="var-item-box">
                <div class="var-item-name">
                    值
                </div>
                <a-input
                    v-model="item.value"
                    placeholder="请输入值"
                    @blur="updateVar"
                />
            </div>
            <a-icon
                @click="removeVar(index)"
                type="delete"
            />
        </div>
        <div
            class="add-var"
            @click="addVar"
        >
            + 增加变量
        </div>
    </div>
</template>

<script>
export default {
    props: {
        // 变量列表
        varList: {
            type: Array,
            default: () => []
        },
    },
    data() {
        return {};
    },
    methods: {
        /**
         * @description: 更新变量
         * @return {*}
         */
        updateVar() {
            this.$emit('updateVar', [...this.varList]);
        },
        /**
         * @description: 新增变量
         * @return {*}
         */
        addVar() {
            const list = [...this.varList];
            list.push({ name: '', type: 'string', value: '' });
            this.$emit('updateVar', list);
        },
        /**
         * @description: 删除变量
         * @return {*}
         */
        removeVar(index) {
            const list = [...this.varList];
            list.splice(index, 1);
            this.$emit('updateVar', list);
        }
    }
};
</script>

<style lang="less" scoped>
.var-container {
    width: 100%;
    .var-config-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        .var-item-box {
            .var-item-name {
                text-align: left;
                margin-bottom: 5px;
                color: #000000A6;
                font-size: 14px;
            }
            /deep/ .ant-input {
                width: 190px;
            }
            /deep/ .ant-select {
                width: 90px;
            }
        }
        .anticon-delete {
            margin-top: 30px;
            cursor: pointer;
            &:hover {
                color: #1890ff;
            }
        }
    }
    .add-var {
        width: 100%;
        height: 32px;
        font-size: 14px;
        line-height: 32px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        color: #000000A6;
        cursor: pointer;
        margin-top: 5px;
        &:hover {
            color: #1890ff;
            border: 1px solid #1890ff;
        }
    }
}
</style>