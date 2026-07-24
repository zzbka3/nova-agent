<!--
 * @Author: hewenquan
 * @Date: 2025-07-01 18:56:16
 * @LastEditTime: 2025-11-03 14:13:50
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonComponents/nodeOperate.vue
 * @Description: 节点操作
-->
<template>
    <div class="operate-container">
        <a-tooltip
            title="重命名"
        >
            <a-icon
                class="operate-icon"
                type="edit"
                @click="$emit('editNode')"
            />
        </a-tooltip>
        <a-tooltip
            title="复制"
        >
            <a-icon
                class="operate-icon"
                type="copy"
                @click.stop="copyNode"
            />
        </a-tooltip>
        <a-tooltip
            title="删除"
        >
            <a-icon
                class="operate-icon"
                type="delete"
                @click.stop="deleteNode"
            />
        </a-tooltip>
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import getNodeInitNames from '@/views/flow/common/getNodeInitNames';
export default {
    props: {
        properties: {
            type: Object,
            default: (() => {})
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
    data() {
        return {
        };
    },
    computed: {
        getNodeId() {
            const graph = this.model;
            return graph.id;
        }
    },
    methods: {
        /**
         * 删除节点
         *
         * 弹出一个确认框，询问用户是否确定要删除当前节点。
         * 如果用户确认，则调用 lf 对象的 deleteNode 方法删除当前节点。
         */
        deleteNode() {
            this.$confirm({
                title: '确定要删除该节点吗？',
                content: '删除后将无法恢复',
                onOk: () => {
                    this.lf.deleteNode(this.getNodeId);
                }
            });
        },
        /**
         * 复制节点
         *
         * @description 复制当前模型中的节点，并在现有节点的基础上添加一个新的节点。
         *              新的节点位置会在原节点基础上向右移动100像素，向下移动20像素。
         */
        copyNode() {
            // 从模型中获取节点的x坐标、y坐标、属性和类型
            const { x, y, properties = {}, type } = this.model || {};
            const { nodeName } = properties;
            // 节点名称不可重复
            let name = getNodeInitNames('', type, this.lf, nodeName);
            // 添加基础节点
            // 添加节点到逻辑流程图（lf）中
            this.lf.addNode({
                // 设置节点类型
                type,
                // 设置节点的x坐标，向右偏移100
                x: x + 100,
                // 设置节点的y坐标，向下偏移20
                y: y + 20,
                // 设置节点的属性
                properties: {
                    ...properties,
                    nodeName: name
                }
            });
        }
    }
};
</script>

<style lang="less" scoped>
.operate-container {
    position: fixed;
    z-index: 2;
    top: -40px;
    right: 0px;
    height: 32px;
    display: flex;
    align-items: center;
    padding: 4px;
    cursor: pointer;
    border-radius: 10px;
    background: #fff;
    gap: 4px;
    .operate-icon {
        margin: 0 5px;
        color: #000;
    }
}
</style>