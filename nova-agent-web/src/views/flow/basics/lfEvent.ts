import { Modal } from 'ant-design-vue'

// Handle click on edge delete marker
export function customAnchorClickEvent({ edge, lf, bus }: { edge: any; lf: any; bus: any }) {
  Modal.confirm({
    title: '确定要删除该连线吗？',
    content: '删除后将无法恢复',
    onOk: () => {
      lf.deleteEdge(edge.id)
    }
  })
}

// Handle backspace/delete key
export function customBackEvent({ lf, isEditName, bus }: { lf: any; isEditName: boolean; bus: any }) {
  if (isEditName) return
  const selected = lf.getSelectElements(true)
  if (selected?.edges?.length) {
    Modal.confirm({
      title: '确定要删除选中的连线吗？',
      content: '删除后将无法恢复',
      onOk: () => {
        selected.edges.forEach((edge: any) => lf.deleteEdge(edge.id))
      }
    })
  }
}

// Update reference var name by id in memory node
export function updateReferenceVarNameById({ nodeId, lf, varNameId, updateVarName }: any) {
  const model = lf.getNodeModelById(nodeId)
  if (!model) return
  const { outputVars = [] } = model.getProperties()
  const updated = outputVars.map((item: any) => {
    if (item.id === varNameId) {
      return { ...item, varName: updateVarName }
    }
    return item
  })
  model.setProperties({ outputVars: updated })
}

// Delete reference var name by id in memory node
export function deleteReferenceVarNameById({ nodeId, lf, varNameId }: any) {
  const model = lf.getNodeModelById(nodeId)
  if (!model) return
  const { outputVars = [] } = model.getProperties()
  model.setProperties({
    outputVars: outputVars.filter((item: any) => item.id !== varNameId)
  })
}
