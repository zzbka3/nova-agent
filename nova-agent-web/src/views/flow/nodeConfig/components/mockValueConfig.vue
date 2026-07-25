<template>
  <div class="common-area mock-value-config">
    <div class="common-title">
      <CaretDownOutlined v-if="expandedVars" @click.stop="expandVars" />
      <CaretRightOutlined v-else @click.stop="expandVars" />
      {{ title }}
    </div>
    <div v-show="expandedVars" class="mock-config-content">
      <div class="mock-config-item mock-title-item">
        <div class="args-item-name mock-config-title">参数名</div>
        <div class="args-item-type mock-config-title">类型</div>
        <div class="args-item-value mock-config-title">值</div>
      </div>
      <div v-for="(item, index) in mockConfig" :key="item.id || index" class="mock-config-item">
        <div class="args-item-name">{{ item.varNameAbbr || item.varName }}</div>
        <div class="args-item-type">{{ item.varType }}</div>
        <div class="args-item-value">
          <a-select
            v-if="item.selectOptions"
            size="small"
            v-model:value="item.varValue"
            class="config-select-item"
            @change="updateConfigData($event, index)"
          >
            <a-select-option v-for="opt in item.selectOptions" :key="opt.value" :value="opt.value">
              <a-tooltip :title="opt.label">{{ opt.label }}</a-tooltip>
            </a-select-option>
          </a-select>
          <a-input
            v-else
            placeholder="请输入mock值"
            size="small"
            :value="item.varValue"
            @change="updateVarValue($event, index)"
            :maxlength="50"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { CaretDownOutlined, CaretRightOutlined } from '@ant-design/icons-vue'

const props = defineProps<{
  title: string
  configData: any[]
}>()

const emit = defineEmits(['update:configData', 'updateConfigData'])

const expandedVars = ref(true)
const mockConfig = ref<any[]>([])

onMounted(() => init())
watch(() => props.configData, () => init(), { deep: true })

function init() {
  if (props.configData?.length) {
    mockConfig.value = [...props.configData]
  }
}

function updateVarValue(event: any, index: number) {
  updateConfigData(event.target.value || '', index)
}

function updateConfigData(value: any, index: number) {
  mockConfig.value[index].varValue = value
  emit('update:configData', [...mockConfig.value])
  emit('updateConfigData', [...mockConfig.value])
}

function expandVars() {
  expandedVars.value = !expandedVars.value
}
</script>

<style lang="less" scoped>
@import url('../../customCss/index.less');
.mock-value-config { margin-top: 20px; .common-title { margin-bottom: 10px; } }
.mock-config-item { display: flex; margin-bottom: 15px; align-items: center;
  .args-item-name { color: #876300; overflow: hidden; width: 90px; white-space: nowrap; text-overflow: ellipsis; }
  .args-item-type { margin-left: 4px; height: 20px; line-height: 20px; padding: 0 5px; white-space: nowrap; border-radius: 4px; background-color: #e8e9eb; max-width: 65px; margin-right: 20px; }
  .args-item-value { color: #000; display: inline-block; flex: 1; .config-select-item { width: 166px; } }
  .mock-config-title { color: #8c8c8c; background: unset; }
}
.mock-title-item { margin-bottom: 5px; }
</style>
