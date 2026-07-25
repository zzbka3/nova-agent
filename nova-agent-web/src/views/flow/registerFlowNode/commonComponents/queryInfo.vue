<template>
  <div class="query-info node-area-bg">
    <div class="input-vars-item">
      <div class="var-name">
        <CaretDownOutlined v-if="inputVarsShows" @click.stop="changeInputShow" />
        <CaretRightOutlined v-else @click.stop="changeInputShow" />
        {{ infoTitle }}
      </div>
      <div class="var-value" v-if="inputVarsShows">值</div>
    </div>
    <template v-if="queryInfoData?.length">
      <div
        :class="['input-vars-item', { 'input-vars-item-hide': !inputVarsShows }]"
        v-for="(item, index) in queryInfoData"
        :key="index"
      >
        <a-tooltip :title="item.varName || '未命名'" placement="topLeft">
          <div class="var-name">
            <span class="text">{{ item.varName || '未命名' }}</span>
            <span class="type">{{ item.varType || '' }}</span>
          </div>
        </a-tooltip>
        <div class="var-value">
          <div class="var-value-box" v-if="item.varValue || item.referenceVarName">
            <a-tooltip :title="`${nodeNameMap[item.referenceNodeId] || ''}${item.referenceVarName || item.varValue}`">
              <span class="var-value-info">
                {{ nodeNameMap[item.referenceNodeId] || '' }}{{ item.referenceVarName || item.varValue }}
              </span>
            </a-tooltip>
          </div>
          <span v-else>未选择</span>
        </div>
      </div>
    </template>
    <div v-else v-show="inputVarsShows">暂未配置</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, reactive, inject } from 'vue'
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'

const props = withDefaults(defineProps<{
  infoTitle?: string
  infoData?: any[]
  arrArgs?: any[]
  inputExpanded?: boolean
}>(), {
  infoTitle: '输出',
  infoData: () => [],
  arrArgs: () => [],
  inputExpanded: true,
})

const emit = defineEmits(['updateNodeAttributes'])
const bus: any = inject('$bus')

const inputVarsShows = ref(props.inputExpanded)
const queryInfoData = ref(props.infoData)
const nodeNameMap = reactive<Record<string, string>>({})

watch(() => props.infoData, (val) => { queryInfoData.value = val }, { deep: true })
watch(() => props.inputExpanded, (val) => { inputVarsShows.value = val })
watch(() => props.arrArgs, () => getNodeNameMap())

onMounted(() => {
  getNodeNameMap()
  bus?.on('editNodeName', ({ isEditName, nodeName, nodeId }: any) => {
    if (!isEditName && nodeNameMap[nodeId]) {
      nodeNameMap[nodeId] = `${nodeName}/`
    }
  })
})

function changeInputShow() {
  inputVarsShows.value = !inputVarsShows.value
  emit('updateNodeAttributes', inputVarsShows.value)
}

function getNodeNameMap() {
  props.arrArgs.forEach((item: any) => {
    nodeNameMap[item.nodeId] = `${item.title}/`
  })
}
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.query-info {
  .input-vars-item {
    display: flex; align-items: center; gap: 10px; height: auto; margin-bottom: 4px;
    .var-name { width: 50%; display: flex; align-items: center; color: #151b26; font-weight: 500;
      .text { overflow: hidden; max-width: 160px; white-space: nowrap; text-overflow: ellipsis; color: #5c5f66; }
      .type { padding: 0 5px; text-align: center; border-radius: 4px; background: #e8e9eb; font-weight: 400; margin-left: 3px; color: #5c5f66; }
    }
    .var-value { width: 50%;
      .var-value-box { display: flex; box-sizing: border-box; width: fit-content; max-width: calc(100% - 8px); padding: 0 4px; border: 1px solid #e8e9eb; border-radius: 4px; background: #fff;
        .var-value-info { display: inline-block; width: 100%; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
      }
    }
  }
  .input-vars-item-hide { height: 0; overflow: hidden; }
}
</style>
