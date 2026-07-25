<template>
  <div class="node-panel flex-center">
    <div class="node-select flex-center">
      <a-popover v-model:open="popoverVisible" trigger="click">
        <template #content>
          <div class="select-container">
            <div class="container-item" v-for="item in nodeList" :key="item.desc">
              <div class="container-item-desc">{{ item.desc }}</div>
              <div v-for="child in item.children" :key="child.type" @click="selectNodeType(child)" class="container-item-child">
                <span class="container-item-child-text">{{ child.text }}</span>
              </div>
            </div>
          </div>
        </template>
        + 节点
      </a-popover>
    </div>
    <div class="flex-center node-operator">
      <a-tooltip placement="topLeft" :title="shrink ? '展开节点' : '折叠节点'">
        <div class="operator-icon" @click="triggerNode">
          <CompressOutlined v-if="!shrink" />
          <ExpandOutlined v-else />
        </div>
      </a-tooltip>
      <a-tooltip placement="topLeft" title="居中视图">
        <div class="operator-icon" @click="viewCenter">
          <AimOutlined />
        </div>
      </a-tooltip>
      <a-tooltip placement="topLeft" title="自动布局">
        <div class="operator-icon" @click="autoLayout">
          <ApartmentOutlined />
        </div>
      </a-tooltip>
      <div class="scale-operate">
        <a-popover v-model:open="scaleVisible" trigger="click">
          <template #content>
            <div class="scale-container">
              <div class="scale-item" v-for="item in scaleList" :key="item" @click="setScale(item)">{{ item * 100 }}%</div>
            </div>
          </template>
          {{ (scale * 100).toFixed(0) }}%
          <DownOutlined class="scale-icon" />
        </a-popover>
      </div>
    </div>
    <div :class="['node-check flex-center', { 'node-check-active': openCheck }]" @click="checkLf">
      <CheckCircleOutlined v-if="!openCheck" class="check-icon" />
      <CloseCircleOutlined v-else class="check-icon" />
      调试
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  CompressOutlined, ExpandOutlined, AimOutlined, ApartmentOutlined,
  DownOutlined, CheckCircleOutlined, CloseCircleOutlined
} from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import { nodeList } from './flowConfig'

const props = defineProps<{
  lf: any
  nodeList?: any[]
  saveFlow: Function
  flowData: any
}>()

const emit = defineEmits(['openCheck'])

const propsNodeList = props.nodeList || nodeList

const popoverVisible = ref(false)
const scale = ref(1)
const scaleVisible = ref(false)
const scaleList = [2, 1.5, 1, 0.7, 0.5, 0.3]
const openCheck = ref(false)
const shrink = ref(false)

onMounted(() => {
  initLfEvent()
})

function initLfEvent() {
  const { SCALE_X = 1 } = props.lf.getTransform()
  scale.value = Number(SCALE_X.toFixed(2))
  if (scale.value > 1) {
    props.lf.zoom(1)
    props.lf.translateCenter()
    scale.value = 1
  }
  if (props.flowData?.nodes?.length > 20) {
    props.lf.zoom(0.3)
    props.lf.translateCenter()
    scale.value = 0.3
  }
  props.lf.on('graph:transform', (data: any) => {
    if (data.type === 'zoom') {
      scale.value = Number(data.transform.SCALE_X.toFixed(2))
    }
  })
}

function viewCenter() { props.lf.translateCenter() }

function autoLayout() {
  if (shrink.value) {
    shrink.value = false
    // emit triggerNode through bus
  }
  setTimeout(() => {
    props.lf.extension?.dagre?.layout?.({ align: '' })
  }, 0)
}

function setScale(s: number) {
  props.lf.zoom(s)
  props.lf.translateCenter()
}

function selectNodeType(nodeConfig: any) {
  popoverVisible.value = false
  if (nodeConfig.type === 'WORKFLOW_AGENT') {
    // Open agent selection modal
    message.info('请选择工作流 Agent')
  } else {
    props.lf.dnd.startDrag({ type: nodeConfig.type })
  }
}

function triggerNode() {
  shrink.value = !shrink.value
  // bus emit triggerNode handled by individual nodes
}

function checkLf() {
  if (openCheck.value) return
  openCheck.value = true
  emit('openCheck', true)
}

function closeCheckDialog() {
  Modal.confirm({
    title: '退出调试提示',
    content: '工作流Agent正在调试中，离开后应用配置将更新，确定要退出调试吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      openCheck.value = false
      emit('openCheck', false)
    },
  })
}

defineExpose({ closeCheckDialog })
</script>

<style lang="less" scoped>
.flex-center { display: flex; align-items: center; justify-content: center; }
.node-panel {
  position: absolute; bottom: 20px; left: 50%; height: 40px; transform: translateX(-50%);
  background: white; padding: 8px; box-shadow: 0 0 10px 1px rgb(228, 224, 219);
  border: 1px solid #d4d6d9; border-radius: 6px; z-index: 101;
  .node-select { margin-right: 8px; padding: 2px 8px; color: #fff; border-radius: 6px; background: #2468f2; font-size: 12px; font-weight: 500; line-height: 20px; cursor: pointer; }
  .node-operator { flex: 1; justify-content: space-between; margin-right: 8px; padding: 0 8px; border-right: 1px solid #e8e9eb; border-left: 1px solid #e8e9eb;
    .operator-icon { width: 24px; height: 24px; margin-right: 8px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
  }
  .node-check { cursor: pointer; width: 56px; height: 24px; color: #fff; border-radius: 6px; background: #34c759; font-size: 12px; font-weight: 500; line-height: 20px;
    &:hover { background: #2eb250; }
    .check-icon { margin-right: 4px; font-size: 14px; }
  }
  .node-check-active { cursor: not-allowed; color: #b8babf; background: inherit !important; }
}
.select-container {
  display: flex; overflow-y: auto; flex-direction: column; box-sizing: border-box; width: 240px; min-height: 300px; max-height: calc(100vh - 130px); padding: 12px 8px 8px; border-radius: 12px; background: #fff;
  .container-item { margin-bottom: 8px;
    .container-item-desc { padding-left: 6px; color: #84868c; font-size: 12px; font-weight: 500; line-height: 20px; }
    .container-item-child { box-sizing: border-box; margin-top: 2px; padding: 6px; border-radius: 8px; cursor: pointer; display: flex; align-items: center;
      .container-item-child-text { margin-left: 8px; font-size: 13px; color: #151b26; }
      &:hover { background: #f7f7f9; }
    }
  }
}
.scale-operate { cursor: pointer; display: flex; align-items: center; justify-content: center; width: 64px; height: 24px; text-align: center; color: #151b26; font-size: 12px; font-weight: 400; line-height: 20px;
  .scale-icon { font-size: 10px; margin-left: 2px; }
  &:hover { border-radius: 6px; background: #f7f7f9; }
}
.scale-container { width: 122px; padding: 8px;
  .scale-item { margin-bottom: 4px; padding: 5px 0 5px 8px; cursor: pointer; border-radius: 6px; &:hover { background: #f7f7f9; } }
}
</style>
