<template>
  <div class="operate-container">
    <a-tooltip title="重命名">
      <EditOutlined class="operate-icon" @click="$emit('editNode')" />
    </a-tooltip>
    <a-tooltip title="复制">
      <CopyOutlined class="operate-icon" @click.stop="copyNode" />
    </a-tooltip>
    <a-tooltip title="删除">
      <DeleteOutlined class="operate-icon" @click.stop="deleteNode" />
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { EditOutlined, CopyOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { Modal } from 'ant-design-vue'
import getNodeInitNames from '../../common/getNodeInitNames'

const props = defineProps<{
  model: any
  lf: any
}>()

const emit = defineEmits(['editNode'])

const getNodeId = () => props.model?.id

function deleteNode() {
  Modal.confirm({
    title: '确定要删除该节点吗？',
    content: '删除后将无法恢复',
    onOk: () => {
      props.lf.deleteNode(getNodeId())
    }
  })
}

function copyNode() {
  const { x, y, properties = {}, type } = props.model || {}
  const { nodeName } = properties
  const name = getNodeInitNames('', type, props.lf, nodeName)
  props.lf.addNode({
    type,
    x: x + 100,
    y: y + 20,
    properties: { ...properties, nodeName: name },
  })
}
</script>

<style lang="less" scoped>
.operate-container {
  position: fixed;
  z-index: 2;
  top: -40px;
  right: 0;
  height: 32px;
  display: flex;
  align-items: center;
  padding: 4px;
  cursor: pointer;
  border-radius: 10px;
  background: #fff;
  gap: 4px;
  .operate-icon { margin: 0 5px; color: #000; }
}
</style>
