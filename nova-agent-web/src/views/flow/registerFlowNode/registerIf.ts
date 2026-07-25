import { createApp } from 'vue'
import nodeComp from './components/ifNode.vue'
import getNodeInitNames from '../common/getNodeInitNames'
import { setAnchorStyle, connectedSourceRules, connectedTargetRules } from './commonUtils'

export default function registerIf(lf: any) {
  lf.register('IF', ({ HtmlNode, HtmlNodeModel }: any) => {
    class Node extends HtmlNode {
      setHtml(rootEl: HTMLElement) {
        const model = this.props.model
        const el = document.createElement('div')
        rootEl.innerHTML = ''
        rootEl.appendChild(el)
        createApp(nodeComp, { model, lf }).mount(el)
        setTimeout(() => {
          const d = rootEl.querySelector('div')
          if (d?.clientHeight) { (this.props.model as any).height = d.clientHeight + 2 }
        }, 0)
      }
    }
    class NodeModel extends HtmlNodeModel {
      initNodeData(data: any) {
        if (data.text) data.text.editable = false
        const { nodeName = '' } = this.properties || {}
        let name = getNodeInitNames(nodeName, 'IF', lf, '分支器')
        super.initNodeData(data)
        this.width = 400
        this.setIsShowAnchor()
        this.properties.nodeName = name
      }
      setIsShowAnchor() { this.isShowAnchor = true }
      getAnchorStyle() { return setAnchorStyle(super.getAnchorStyle()) }
      setCustomAttributes(attrs: any) {
        const { currentHeight = 280, expand = false } = attrs || {}
        const { height, y } = this
        this.y = expand ? y + (currentHeight - height) / 2 : y - (height - currentHeight) / 2
        this.height = currentHeight + 2
        this.updatePath()
      }
      updatePath() {
        this.incoming.edges.forEach((e: any) => e.updatePathByAnchor())
        this.outgoing.edges.forEach((e: any) => e.updatePathByAnchor())
      }
      getConnectedSourceRules() { const r = super.getConnectedSourceRules(); r.push(connectedSourceRules); return r }
      getConnectedTargetRules() { const r = super.getConnectedTargetRules(); r.push(connectedTargetRules); return r }
      getDefaultAnchor() {
        const { width, x, y, id } = this
        return [
          { x: x - width / 2, y, name: 'left', id: id + '__start' },
          { x: x + width / 2, y, name: 'right', id: id + '__end' },
        ]
      }
    }
    return { view: Node, model: NodeModel }
  })
}
