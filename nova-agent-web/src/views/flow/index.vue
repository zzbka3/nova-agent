<template>
  <div class="flow-wrapper">
    <div class="flow-head">
      <div class="head-left">
        <a-button @click="back">
          <template #icon><ArrowLeftOutlined /></template>
        </a-button>
        <div class="flow-info">
          <a-tooltip :title="agentData.name">
            <div class="flow-name">{{ agentData.name || '未命名工作流' }}</div>
          </a-tooltip>
          <a-tooltip :title="agentData.remark">
            <div class="flow-desc">{{ agentData.remark || '暂无描述' }}</div>
          </a-tooltip>
        </div>
      </div>
      <div v-if="showFlow">
        <a-button type="primary" @click="publishFlow">发布</a-button>
      </div>
    </div>
    <FlowContainer
      class="flow-container"
      ref="flowContainerRef"
      :app-id="appId"
      @syncAgentData="syncAgentData"
      v-if="showFlow"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import FlowContainer from './flowContainer.vue'

const router = useRouter()
const route = useRoute()
const bus: any = inject('$bus')

const flowContainerRef = ref()
const agentData = ref<any>({})
const showFlow = ref(false)

const appId = computed(() => route.params.appId as string)

onMounted(() => {
  // For now, show the flow directly without API init
  showFlow.value = true
})

function syncAgentData(data: any) {
  agentData.value = data
}

function back() {
  flowContainerRef.value?.clearSaveInterval()
  router.push({ path: '/flow/list' })
}

async function publishFlow() {
  message.info('发布功能待完善')
}
</script>

<style lang="less" scoped>
.flow-wrapper {
  .flow-head {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 0 20px;
    width: 100%;
    height: 56px;
    background: #f2f5f9;
    box-shadow: inset 0 -1px #e8e9eb;
  }
  .head-left {
    display: flex;
    align-items: center;
    .flow-info {
      margin-left: 20px;
      text-align: left;
      .flow-name {
        color: #000;
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        max-width: 300px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
      .flow-desc {
        color: #84868c;
        font-size: 12px;
        line-height: 20px;
        max-width: 300px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }
    }
  }
  .flow-container {
    height: calc(100vh - 56px);
  }
}
</style>
