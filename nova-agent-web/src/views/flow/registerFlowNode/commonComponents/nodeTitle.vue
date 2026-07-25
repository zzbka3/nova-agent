<template>
  <div class="node-title">
    <div class="flex-center">
      <CaretDownOutlined v-if="showFold && allShow" @click.stop="$emit('toggleFoldAll', !allShow)" />
      <CaretRightOutlined v-if="showFold && !allShow" @click.stop="$emit('toggleFoldAll', !allShow)" />
      <img :src="nodeIcon" class="node-icon" />
      <a-input
        v-if="isEditName"
        ref="nodeTitleRef"
        v-model:value="nodeNameText"
        class="node-name"
        @blur="handleBlur"
        @click.stop
        @keydown.enter="handleBlur($event, true)"
        @keydown.stop
      />
      <a-tooltip :title="nodeName" v-else>
        <span class="node-name-text">{{ nodeName }}</span>
      </a-tooltip>
      <span v-if="modeText" class="mode-text">{{ modeText }}</span>
    </div>
    <div class="error-icon" v-if="!validateStatus">
      <a-tooltip>
        <template #title>
          <div v-for="(item, index) in errResult" :key="index">
            {{ index + 1 }}: {{ item }};
          </div>
        </template>
        <InfoCircleFilled style="color: red;" />
      </a-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { CaretDownOutlined, CaretRightOutlined, InfoCircleFilled } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { deleteTempOutputs } from '../../getArgs'

const props = defineProps<{
  nodeName: string
  nodeIcon: string
  validateStatus?: boolean
  errResult?: string[]
  showFold?: boolean
  modeText?: string
  model: any
  allShow?: boolean
  lf: any
}>()

const emit = defineEmits(['toggleFoldAll'])

const bus: any = inject('$bus')

const nodeNameText = ref('')
const isEditName = ref(false)
const originNameText = ref('')
const nodeTitleRef = ref()

const getNodeId = () => props.model?.id

onMounted(() => {
  bus?.on('triggerNode', (args: boolean) => {
    if (args === props.allShow) {
      emit('toggleFoldAll', !props.allShow)
    }
  })
})

function editNodeName() {
  if (isEditName.value) return
  isEditName.value = true
  bus?.emit('editNodeName', { isEditName: true })
  nodeNameText.value = props.nodeName
  originNameText.value = props.nodeName
  setTimeout(() => nodeTitleRef.value?.focus(), 0)
}

function handleBlur(event?: Event, isPressEnter?: boolean) {
  isEditName.value = false
  const { nodes = [] } = props.lf.getGraphData()
  const hasEqual = nodes.some(
    (n: any) => n.id !== getNodeId() && n?.properties?.nodeName === nodeNameText.value
  )
  bus?.emit('editNodeName', {
    isEditName: false,
    nodeName: nodeNameText.value,
    nodeId: getNodeId(),
  })

  if (hasEqual) {
    if (!isPressEnter) message.warning('节点名称重复，当前修改失效')
  } else {
    if (originNameText.value !== nodeNameText.value) {
      props.model.setProperties({ nodeName: nodeNameText.value })
      deleteTempOutputs()
    }
  }
}

defineExpose({ editNodeName })
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.node-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  height: 24px;
  .node-icon { width: 24px; height: 24px; margin: 0 8px; }
  .mode-text { height: 20px; padding: 0 8px; color: #ff9326; border-radius: 4px; background: #fff4e6; font-size: 12px; line-height: 20px; margin-left: 5px; }
  .node-name { height: 24px; font-size: 12px; }
  .node-name-text { max-width: 200px; overflow: hidden; text-overflow: ellipsis; }
}
</style>
