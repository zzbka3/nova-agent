// Node list for the side panel - categories and node types
export const originalNode = [
  {
    desc: '开始/结束',
    children: [
      { text: '开始', type: 'START' },
      { type: 'END', text: '结束' }
    ]
  }
]

export const nodeList = [
  {
    desc: '工具引入',
    children: [
      { type: 'API', text: 'API' },
      { type: 'WORKFLOW_AGENT', text: '工作流' }
    ]
  },
  {
    desc: '业务逻辑',
    children: [
      { text: '分支器', type: 'IF' },
      { type: 'INTENT', text: '意图识别' },
      { type: 'CODE', text: '代码' },
    ]
  },
  {
    desc: '信息&知识',
    children: [
      { type: 'KNOWLEDGE', text: '知识库' },
      { type: 'LLM', text: '大模型' },
      { type: 'TEXT_PROCESSOR', text: '文本处理' },
      { type: 'WORKFLOW', text: '流程组件' },
      { type: 'MEMORY', text: '记忆变量' },
    ]
  },
  {
    desc: '输入&输出',
    children: [
      { type: 'MESSAGE', text: '消息节点' }
    ]
  }
]

// Default graph data for new workflow agent
export const defaultNodeData = {
  nodes: [
    { id: '1', type: 'START', x: 471, y: 400, properties: { width: 360, height: 300, nodeName: '开始' } },
    { id: '2', type: 'END', x: 1435, y: 400, properties: { width: 400, height: 150, nodeName: '结束' } }
  ],
  edges: []
}

// Default autonomy agent config
export const defaultAutonomyAgent = {
  promptText: '',
  promptType: 0,
  temperature: 0.01,
  modelNames: 'ernie-4.5-turbo-128k',
  varList: []
}

// Get node type name mapping
export function getNodeMap(nodeType?: string): string | Record<string, string> {
  const nodeMap: Record<string, string> = {}
  ;[...originalNode, ...nodeList].forEach(item => {
    item.children.forEach(child => {
      nodeMap[child.type] = child.text
    })
  })
  if (nodeType) {
    return nodeMap[nodeType] || '未知节点类型'
  }
  return nodeMap
}

// Workflow component mapping
export const workFlowMap: Record<string, string> = {
  materialId: '物料卡',
  imageUrlList: '图片链接',
  videoList: '视频链接',
  standardId: '标准问id',
  standardAnswerId: '标准问答案id',
  turnToManualFlow: '人工客服',
  openOrderList: '调起订单',
  openWordList: '调起词条',
  showEvaluate: '邀评',
  showHalf: '弹起浮层',
  sugStandardId: '标准问id',
  sugList: '气泡json配置',
  questionCategoryIds: '打标-产品问题类型',
  agentContainerAutoSwitchBuzName: 'bot'
}
