import { createApp } from 'vue'
import end from './components/end.vue'
import { setAnchorStyle } from './commonUtils'

export default function registerEnd(lf: any) {
  lf.register('END', ({ HtmlNode, HtmlNodeModel }: any) => {
    class EndNode extends HtmlNode {
      setHtml(rootEl: HTMLElement) {
        const model = this.props.model
        const el = document.createElement('div')
        rootEl.innerHTML = ''
        rootEl.appendChild(el)
        const app = createApp(end, { model, lf })
        app.mount(el)
        setTimeout(() => {
          const div = rootEl.querySelector('div')
          if (div?.clientHeight) {
            ;(this.props.model as any).height = div.clientHeight + 2
          }
        }, 0)
      }
    }
    class EndModel extends HtmlNodeModel {
      initNodeData(data: any) {
        super.initNodeData(data)
        this.setIsShowAnchor()
        this.r = 20
        this.width = 400
        this.height = 150
        this.properties.nodeName = '结束'
      }
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
      getOutlineStyle() { const s = super.getOutlineStyle(); s.stroke = '#88f'; return s }
      setIsShowAnchor() { this.isShowAnchor = true }
      getConnectedSourceRules() {
        const rules = super.getConnectedSourceRules()
        rules.push({ message: '终止节点不能作为连线的起点', validate: () => false })
        return rules
      }
      getDefaultAnchor() {
        const { width, x, y, id } = this
        return [{ x: x - width / 2, y: y + 3, name: 'left', id: `${id}__start` }]
      }
    }
    return { view: EndNode, model: EndModel }
  })
}
