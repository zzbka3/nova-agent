import { reactive } from 'vue'

// Shared reactive state for the flow canvas
export const flowState = reactive({
  systemArgs: [] as any[],
  extraArgs: {} as any,
  tempOutputs: {} as Record<string, any[]>,
  memorySchemaList: [] as any[],
})

export function setMemorySchemaList(list: any[]) {
  flowState.memorySchemaList = list
}

// Model list (would be fetched from API)
export const modelList = [
  { label: 'ernie-4.5-turbo-128k', value: 'ernie-4.5-turbo-128k' },
  { label: 'ernie-4.0-turbo-8k', value: 'ernie-4.0-turbo-8k' },
  { label: 'ernie-3.5-8k', value: 'ernie-3.5-8k' },
]
