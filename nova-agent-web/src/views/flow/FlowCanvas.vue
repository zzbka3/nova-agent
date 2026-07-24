<template>
  <div class="flow-wrapper">
    <div class="flow-toolbar">
      <span class="app-name">Nova Agent 工作流编排</span>
      <div>
        <button @click="zoomIn">+</button>
        <span style="margin:0 10px">{{ scale }}%</span>
        <button @click="zoomOut">-</button>
        <button @click="viewCenter">居中</button>
        <button @click="getData" style="margin-left:10px;background:#2468f2;color:#fff;border:none;padding:4px 12px;border-radius:4px;cursor:pointer">导出数据</button>
      </div>
    </div>
    <div class="flow-body">
      <div class="node-panel">
        <div style="font-weight:600;margin-bottom:12px">节点列表</div>
        <div v-for="g in groups" :key="g.desc" style="margin-bottom:12px">
          <div style="font-size:10px;color:#aaa;margin-bottom:4px">{{ g.desc }}</div>
          <div v-for="n in g.children" :key="n.type"
               @mousedown="drag($event, n)"
               style="display:flex;align-items:center;padding:6px 8px;border-radius:6px;cursor:grab;margin-bottom:2px"
               @mouseover="e => e.currentTarget.style.background='#f0f3fa'"
               @mouseleave="e => e.currentTarget.style.background=''">
            <span :style="{display:'flex',alignItems:'center',justifyContent:'center',width:'24px',height:'24px',borderRadius:'4px',background:n.color,color:'#fff',fontSize:'12px',marginRight:'8px'}">{{ n.icon }}</span>
            {{ n.text }}
          </div>
        </div>
      </div>
      <div class="canvas-wrap">
        <div ref="canvasRef" style="width:100%;height:100%"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/style/index.css'

const groups = [
  { desc: '基础', children: [
    { type: 'start', text: '开始', icon: '▶', color: '#576b95' },
    { type: 'end', text: '结束', icon: '■', color: '#07c160' }
  ]},
  { desc: '工具', children: [
    { type: 'api', text: 'API', icon: 'A', color: '#f0a040' },
    { type: 'llm', text: '大模型', icon: '模', color: '#8b5cf6' }
  ]},
  { desc: '逻辑', children: [
    { type: 'if', text: '分支器', icon: '分', color: '#f56c6c' },
    { type: 'code', text: '代码', icon: '<>', color: '#3b82f6' }
  ]},
  { desc: '知识', children: [
    { type: 'knowledge', text: '知识库', icon: '知', color: '#f5a623' },
    { type: 'message', text: '消息', icon: '消', color: '#55b7e6' }
  ]}
]

const canvasRef = ref(null)
const lf = shallowRef(null)
const scale = ref(100)

const nodeColors = {
  start: { fill: '#eef2ff', stroke: '#576b95' },
  end: { fill: '#eefaf3', stroke: '#07c160' },
  api: { fill: '#fff8f0', stroke: '#f0a040' },
  llm: { fill: '#f5f0ff', stroke: '#8b5cf6' },
  if: { fill: '#fff0f0', stroke: '#f56c6c' },
  code: { fill: '#f0f4ff', stroke: '#3b82f6' },
  knowledge: { fill: '#fffaf0', stroke: '#f5a623' },
  message: { fill: '#f0f9ff', stroke: '#55b7e6' },
}

onMounted(() => {
  const logicFlow = new LogicFlow({
    container: canvasRef.value,
    grid: { size: 20, visible: true, type: 'dot', config: { color: '#dce0e8', thickness: 1 } },
    keyboard: { enabled: true },
    guards: {
      beforeDelete: ({ data }) => {
        if (data?.type === 'start' || data?.type === 'end') {
          alert('开始和结束节点不能删除')
          return false
        }
        return true
      }
    }
  })

  // 注册自定义节点
  Object.entries(nodeColors).forEach(([type, colors]) => {
    logicFlow.register(type, ({ RectNode, RectNodeModel }) => {
      class Model extends RectNodeModel {
        initNodeData(data) {
          super.initNodeData(data)
          this.width = 150
          this.height = 55
          this.properties.text = data.text || type
        }
        getNodeStyle() {
          return { ...super.getNodeStyle(), ...colors, strokeWidth: 2, rx: 8, ry: 8 }
        }
      }
      return { view: RectNode, model: Model }
    })
  })

  // 分支器用菱形
  logicFlow.register('if', ({ DiamondNode, DiamondNodeModel }) => {
    class Model extends DiamondNodeModel {
      initNodeData(data) {
        super.initNodeData(data)
        this.rx = 60
        this.ry = 38
        this.properties.text = data.text || '分支器'
      }
      getNodeStyle() {
        return { ...super.getNodeStyle(), fill: '#fff0f0', stroke: '#f56c6c', strokeWidth: 2 }
      }
    }
    return { view: DiamondNode, model: Model }
  })

  // 设置初始数据
  logicFlow.render({
    nodes: [
      { id: 'n1', type: 'start', x: 200, y: 300, text: '开始' },
      { id: 'n2', type: 'end', x: 600, y: 300, text: '结束' }
    ],
    edges: []
  })

  logicFlow.translateCenter()
  lf.value = logicFlow

  // 监听缩放
  logicFlow.on('graph:transform', ({ transform }) => {
    scale.value = Math.round(transform.SCALE_X * 100)
  })

  // 监听事件
  logicFlow.on('node:click', ({ data }) => console.log('点击节点:', data))
  logicFlow.on('node:dbclick', ({ data }) => console.log('双击节点:', data))
})

function drag(event, node) {
  if (!lf.value) return
  event.preventDefault()
  lf.value.dnd.startDrag({ type: node.type, text: node.text })
}
function zoomIn() { lf.value?.zoom(true) }
function zoomOut() { lf.value?.zoom(false) }
function viewCenter() { lf.value?.translateCenter() }
function getData() {
  const data = lf.value?.getGraphData()
  console.log('画布数据:', JSON.stringify(data, null, 2))
  alert('JSON 已输出到控制台 (F12)')
}
</script>

<style scoped>
.flow-wrapper { display:flex; flex-direction:column; width:100%; height:100vh; overflow:hidden; }
.flow-toolbar { display:flex; align-items:center; justify-content:space-between; padding:0 20px; height:46px; background:#f8f9fc; border-bottom:1px solid #e8e9eb; flex-shrink:0; }
.flow-body { display:flex; flex:1; overflow:hidden; }
.node-panel { width:160px; background:#fff; border-right:1px solid #e8e9eb; padding:14px 10px; overflow-y:auto; flex-shrink:0; user-select:none; }
.canvas-wrap { flex:1; position:relative; overflow:hidden; }

:deep(.lf-graph) { background: #f6f7fa !important; }
:deep(.lf-node) { filter: drop-shadow(0 1px 3px rgba(0,0,0,.1)); }
</style>
