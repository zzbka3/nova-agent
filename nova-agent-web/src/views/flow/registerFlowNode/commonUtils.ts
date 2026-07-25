export function setAnchorStyle(style: any) {
  style.stroke = 'rgb(24, 125, 255)'
  style.fill = 'rgb(24, 125, 255)'
  style.r = 3
  style.hover = style.hover || {}
  style.hover.r = 8
  style.hover.fill = 'rgb(24, 125, 255)'
  style.hover.stroke = 'rgb(24, 125, 255)'
  return style
}

export const connectedSourceRules = {
  message: '左侧不能作为连线的起点',
  validate: (source: any, target: any, sourceAnchor: any) => {
    // Allow connection only from right-side anchors
    return sourceAnchor?.name !== 'left'
  }
}

export const connectedTargetRules = {
  message: '节点右侧不能作为连线的终点',
  validate: (source: any, target: any, sourceAnchor: any, targetAnchor: any) => {
    // Disallow connection to right-side anchors
    return sourceAnchor?.name !== 'left' && targetAnchor?.name !== 'right'
  }
}

export const intentionOutputList = [
  { varName: 'thought', varType: 'String', varDesc: '意图识别的模型思考过程' },
  { varName: 'classification', varType: 'String', varDesc: '识别的对应意图' },
  { varName: 'classificationID', varType: 'String', varDesc: '识别的对应意图的序号' },
]

export const largeModelOutputList = [
  { varName: 'OutputList', varType: 'ArrayObject', varDesc: '' }
]

export const largeModelViews = [
  {
    varName: 'OutputList',
    varType: 'ArrayObject',
    varDesc: '',
    expanded: false,
    children: [
      { varName: 'segment_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.segment_id' },
      { varName: 'document_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.document_id' },
      { varName: 'dataset_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.dataset_id' },
      { varName: 'score', varType: 'Number', varDesc: '', realNamePath: 'OutputList.score' },
      { varName: 'content', varType: 'String', varDesc: '', realNamePath: 'OutputList.content' },
      { varName: 'document_name', varType: 'String', varDesc: '', realNamePath: 'OutputList.document_name' },
      { varName: 'dataset_name', varType: 'String', varDesc: '', realNamePath: 'OutputList.dataset_name' },
      { varName: 'word_count', varType: 'Integer', varDesc: '', realNamePath: 'OutputList.word_count' },
      { varName: 'original_chunk_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.original_chunk_id' },
      { varName: 'original_chunk_offset', varType: 'Number', varDesc: '', realNamePath: 'OutputList.original_chunk_offset' },
      { varName: 'url', varType: 'String', varDesc: '', realNamePath: 'OutputList.url' },
    ]
  },
]
