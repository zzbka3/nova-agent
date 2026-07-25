import { createApp } from 'vue'
import apiNode from './components/apiNode.vue'
import getNodeInitNames from '../common/getNodeInitNames'
import { setAnchorStyle, connectedSourceRules, connectedTargetRules } from './commonUtils'

export default function registerApi(lf: any) {
  lf.register('API', ({ HtmlNode, HtmlNodeModel }: any) => {
    class ApiNode extends HtmlNode {
      setHtml(rootEl: HTMLElement) {
        const model = this.props.model
        const el = document.createElement('div')
        rootEl.innerHTML = ''
        rootEl.appendChild(el)
        const app = createApp(apiNode, { model, lf })
        app.mount(el)
        setTimeout(() => {
          const div = rootEl.querySelector('div')
          if (div?.clientHeight) {
            ;(this.props.model as any).height = div.clientHeight + 2
          }
        }, 0)
      }
    }
    class ApiNodeModel extends HtmlNodeModel {
      initNodeData(data: any) {
        if (data.text) data.text.editable = false
        const { nodeName = '' } = this.properties || {}
        let name = getNodeInitNames(nodeName, 'API', lf, 'API')
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
        let targetY = y
        if (!expand) targetY = y - (height - currentHeight) / 2
        else targetY = y + (currentHeight - height) / 2
        this.y = targetY
        this.height = currentHeight + 2
        this.updatePath()
      }
      updatePath() {
        this.incoming.edges.forEach((e: any) => e.updatePathByAnchor())
        this.outgoing.edges.forEach((e: any) => e.updatePathByAnchor())
      }
      getConnectedSourceRules() {
        const rules = super.getConnectedSourceRules()
        rules.push(connectedSourceRules)
        return rules
      }
      getConnectedTargetRules() {
        const rules = super.getConnectedTargetRules()
        rules.push(connectedTargetRules)
        return rules
      }
      getDefaultAnchor() {
        const { width, x, y, id } = this
        return [
          { x: x - width / 2, y, name: 'left', id: `${id}__start` },
          { x: x + width / 2, y, name: 'right', id: `${id}__end` },
        ]
      }
    }
    return { view: ApiNode, model: ApiNodeModel }
  })
}
