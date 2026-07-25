import { getNodeMap } from '../basics/flowConfig'

export default function getNodeInitNames(
  nodeName: string,
  type: string,
  lf: any,
  typeName?: string
): string {
  if (nodeName) {
    return nodeName
  }
  const graphData = lf?.getGraphData() || { nodes: [] }
  const nodes = graphData.nodes || []
  const nodeMap = getNodeMap() as Record<string, string>
  const name = typeName || nodeMap[type] || '未知节点'

  let index = 0
  let hasSame = nodes.some((n: any) => {
    const p = n.properties || {}
    return p.nodeName === name
  })

  while (hasSame) {
    index++
    const testName = `${name}${index}`
    hasSame = nodes.some((n: any) => {
      const p = n.properties || {}
      return p.nodeName === testName
    })
  }

  return index > 0 ? `${name}${index}` : name
}
