import { h, createApp, nextTick } from 'vue'
import startNode from './components/startNode.vue'
import { setAnchorStyle } from './commonUtils'

export default function registerStart(lf: any) {
  lf.register('START', ({ HtmlNode, HtmlNodeModel }: any) => {
    class StartNode extends HtmlNode {
      setHtml(rootEl: HTMLElement) {
        const model = this.props.model
        const el = document.createElement('div')
        rootEl.innerHTML = ''
        rootEl.appendChild(el)
        const app = createApp(startNode, { model, lf })
        const instance = app.mount(el)
        setTimeout(() => {
          const div = rootEl.querySelector('div')
          if (div?.clientHeight) {
            ;(this.props.model as any).height = div.clientHeight + 2
          }
        }, 0)
      }
    }
    class StartModel extends HtmlNodeModel {
      initNodeData(data: any) {
        if (data.text) data.text.editable = false
        super.initNodeData(data)
        this.setIsShowAnchor()
        this.r = 20
        this.width = 360
        this.height = 300
        this.properties.nodeName = '开始'
      }
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
      getNodeStyle() { return super.getNodeStyle() }
      getAnchorStyle() { return setAnchorStyle(super.getAnchorStyle()) }
      getOutlineStyle() { const s = super.getOutlineStyle(); s.stroke = '#88f'; return s }
      setIsShowAnchor() { this.isShowAnchor = true }
      getConnectedTargetRules() {
        const rules = super.getConnectedTargetRules()
        rules.push({ message: '起始节点不能作为连线的终点', validate: () => false })
        return rules
      }
      getDefaultAnchor() {
        const { width, x, y, id } = this
        return [{ x: x + width / 2, y, name: 'right', id: `${id}__end` }]
      }
    }
    return { view: StartNode, model: StartModel }
  })
}
