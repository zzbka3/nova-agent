import { getNodeMap } from './basics/flowConfig'

// System arguments
let systemArgs: any[] = []

// Operation options
export const opOptions = [
  { label: '等于', value: 'EQUAL' },
  { label: '不等于', value: 'NOT_EQUAL' },
  { label: '长度大于等于', value: 'LENGTH_GT_EQUAL' },
  { label: '长度大于', value: 'LENGTH_GT' },
  { label: '长度小于等于', value: 'LENGTH_LT_EQUAL' },
  { label: '长度小于', value: 'LENGTH_LT' },
  { label: '为空', value: 'EMPTY' },
  { label: '不为空', value: 'NOT_EMPTY' },
  { label: '包含', value: 'CONTAINS' },
  { label: '不包含', value: 'NOT_CONTAINS' },
  { label: '大于', value: 'GT' },
  { label: '大于等于', value: 'GT_EQUAL' },
  { label: '小于', value: 'LT' },
  { label: '小于等于', value: 'LT_EQUAL' },
]

// Temporary output cache
let tempOutputs: Record<string, any[]> = {}

export function deleteTempOutputs() {
  tempOutputs = {}
}

export function setInitInfo({ parseInfo, workFlowVarsConfig }: { parseInfo: any; workFlowVarsConfig: any }) {
  systemArgs = [parseInfo]
  // extraArgs stored separately
}

// Recursively get all parent node outputs
function getAllParentNodeOutputs({ nodeId = '', lf }: { nodeId?: string; lf: any }): any[] {
  if (!nodeId || !lf) return []

  const incomingNodes = lf.getNodeIncomingNode(nodeId) || []
  if (!incomingNodes.length) return []

  let outputs: any[] = []
  incomingNodes.forEach((mode: any) => {
    if (tempOutputs[mode.id]?.length) {
      outputs.push(...tempOutputs[mode.id])
    } else {
      let currentNodeOutputs: any[] = []
      const { outputVars = [], nodeName, readOnlyOutputs } = mode.getProperties() || {}

      const cycleChildren = ({ children, parentArgType = '', parentRealNamePath = '' }: any): any[] => {
        if (!children?.length) return []
        return children.map((arg: any) => {
          const realType = arg.varNameType || arg.varType
          if (realType !== 'ArrayObject') {
            const config: any = {
              referenceNodeId: mode.id,
              ...arg,
              canSelect: true,
            }
            if (['Object', 'ArrayObject'].includes(parentArgType)) {
              config.realNamePath = `${parentRealNamePath}.${arg.varName}`
            } else {
              config.realNamePath = arg.realNamePath || arg.varName
            }
            if (arg.children?.length) {
              config.children = cycleChildren({
                children: arg.children,
                parentArgType: realType,
                parentRealNamePath: config.realNamePath || parentRealNamePath || arg.varType,
              })
              return config
            } else {
              config.childrenCanSelect = false
              return config
            }
          }
          return {
            ...arg,
            referenceNodeId: mode.id,
            canSelect: true,
            childrenCanSelect: false,
          }
        })
      }

      let realOutputs = mode.type === 'API' ? readOnlyOutputs : outputVars
      if (Array.isArray(realOutputs) && realOutputs.length) {
        currentNodeOutputs.push({
          title: nodeName || (getNodeMap(mode.type) as string),
          key: mode.id + '-' + mode.type,
          nodeId: mode.id,
          children: realOutputs.map((item: any) => ({
            ...item,
            children: cycleChildren({
              children: item.children,
              parentArgType: item.varNameType || item.varType,
              parentRealNamePath: item.varName,
            }),
            referenceNodeId: mode.id,
          })),
        })
      }
      const parentOut = getParentNodeOutputs({ nodeId: mode.id, lf })
      if (parentOut?.length) currentNodeOutputs.push(...parentOut)
      outputs.push(...currentNodeOutputs)
      tempOutputs[mode.id] = currentNodeOutputs
    }
  })
  return [...outputs]
}

function getParentNodeOutputs({ nodeId = '', lf }: { nodeId?: string; lf: any }): any[] {
  const allOutputs = getAllParentNodeOutputs({ nodeId, lf })
  const seen: Record<string, boolean> = {}
  return allOutputs.reduce((pre: any[], cur: any) => {
    if (!seen[cur.nodeId]) {
      pre.push(cur)
      seen[cur.nodeId] = true
    }
    return pre
  }, [])
}

export function getAllArgs({ addArgs = [], nodeId, lf }: { addArgs?: any[]; nodeId: string; lf: any }): any[] {
  const cycleChildren = ({ children, parentArgType = '', parentRealNamePath = '' }: any): any[] =>
    children.map((arg: any) => {
      let config: any = { ...arg, canSelect: true }
      if (['Object', 'ArrayObject'].includes(parentArgType)) {
        config.realNamePath = `${parentRealNamePath}.${arg.varName}`
      } else {
        config.realNamePath = arg.realNamePath || arg.varName
      }
      if (arg.varType !== 'ArrayObject') {
        if (arg.children?.length) {
          config.children = cycleChildren({
            children: arg.children,
            parentArgType: arg.varType,
            parentRealNamePath: config.realNamePath || parentRealNamePath || arg.varName,
          })
          return config
        }
        return config
      }
      config.childrenCanSelect = false
      return config
    })

  let allArgs = [...systemArgs, ...addArgs]
  if (nodeId) {
    const parentOutputs = getParentNodeOutputs({ nodeId, lf })
    allArgs = [...systemArgs, ...addArgs, ...parentOutputs]
  }
  return allArgs.map(arg => ({ ...arg, children: cycleChildren({ children: arg.children }) }))
}

export function getAllFlatArgs({ nodeId, lf, filterNodeId = '' }: any): any[] {
  let allArgs = getAllArgs({ nodeId, lf })
  let result: any[] = []
  const cycle = (children: any[]) => children.forEach((c: any) => { result.push(c); if (c.children?.length) cycle(c.children) })
  if (filterNodeId) allArgs = allArgs.filter((i: any) => i.nodeId === filterNodeId)
  allArgs.forEach((i: any) => cycle(i.children))
  return result
}
