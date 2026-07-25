<template>
  <div class="flow-list-page">
    <div class="list-header">
      <h2>工作流 Agent 列表</h2>
      <a-button type="primary" @click="createNew">新建工作流</a-button>
    </div>
    <a-table
      :columns="columns"
      :data-source="agentList"
      :loading="loading"
      row-key="appId"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a @click="editFlow(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="deleteFlow(record)">删除</a>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'published' ? 'green' : 'blue'">
            {{ record.status === 'published' ? '已发布' : '草稿' }}
          </a-tag>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'

const router = useRouter()
const loading = ref(false)

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'remark', key: 'remark' },
  { title: '状态', key: 'status', dataIndex: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' },
]

const agentList = ref([
  { appId: 'demo-1', name: '示例工作流', remark: '这是一个示例工作流Agent', status: 'draft', createTime: '2025-07-01' },
])

function createNew() {
  // Navigate to a new flow - use a temporary appId
  const newId = `new-${Date.now()}`
  router.push({ path: `/flow/${newId}` })
}

function editFlow(record: any) {
  router.push({ path: `/flow/${record.appId}` })
}

function deleteFlow(record: any) {
  Modal.confirm({
    title: '确定要删除该工作流吗？',
    content: '删除后将无法恢复',
    onOk: () => {
      message.success('删除成功')
      agentList.value = agentList.value.filter(item => item.appId !== record.appId)
    }
  })
}
</script>

<style lang="less" scoped>
.flow-list-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }
}
</style>
