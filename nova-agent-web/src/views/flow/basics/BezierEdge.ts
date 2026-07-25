import { BezierEdgeModel, BezierEdge, h } from '@logicflow/core'

// Custom edge view with delete button
class CustomEdge extends BezierEdge {
  getStartArrow() {
    return h('path', {
      stroke: '#ccc',
      fill: '#3f58fd',
      d: 'M0 0 m-4,0 a4,4 0 1,0 8,0 a4,4 0 1,0 -8,0',
      transform: 'translate(-2,0)',
    })
  }

  getShape() {
    const path = super.getShape()
    const { startPoint, endPoint } = this.props.model
    const x = (startPoint.x + endPoint.x) / 2
    const y = (startPoint.y + endPoint.y) / 2
    const pathD = `M ${startPoint.x} ${startPoint.y} L ${endPoint.x} ${endPoint.y}`

    return h('g', {}, [
      h('path', {
        d: pathD,
        stroke: 'transparent',
        strokeWidth: 50,
        fill: 'none',
      }),
      path,
      h('image', {
        href: '',
        x: x - 10,
        y: y - 10,
        className: 'edgeMarkClass',
        display: (this.props.model as any).properties?.showAddMark ? 'block' : 'none',
        width: 18,
        height: 18,
        pointerEvents: 'all',
        style: 'cursor: pointer;transition:0.3s',
        onclick: () => {
          this.props.graphModel.eventCenter.emit('custom:anchorClick', {
            edge: this.props.model,
            tag: 'delete'
          })
        },
        onmouseover: (e: any) => {
          const image = e.target
          image.setAttribute('width', 22)
          image.setAttribute('height', 22)
          image.setAttribute('x', x - 12)
          image.setAttribute('y', y - 12)
        },
        onmouseout: (e: any) => {
          const image = e.target
          image.setAttribute('width', 18)
          image.setAttribute('height', 18)
          image.setAttribute('x', x - 10)
          image.setAttribute('y', y - 10)
        }
      })
    ])
  }
}

// Default bezier edge model
class DefaultBezierEdgeModel extends BezierEdgeModel {
  getEdgeStyle() {
    const style = super.getEdgeStyle()
    const isHover = (this as any).properties?.showAddMark
    style.stroke = isHover ? '#3f58fd' : '#d0d5dc'
    style.cursor = 'pointer'
    style.strokeDasharray = '10 0'
    return style
  }

  updatePathByAnchor() {
    const sourceNodeModel = this.graphModel.getNodeModelById(this.sourceNodeId)
    const sourceAnchor = sourceNodeModel?.getDefaultAnchor().find(
      (anchor: any) => anchor.id === this.sourceAnchorId
    )
    const targetNodeModel = this.graphModel.getNodeModelById(this.targetNodeId)
    const targetAnchor = targetNodeModel?.getDefaultAnchor().find(
      (anchor: any) => anchor.id === this.targetAnchorId
    )

    if (sourceAnchor) {
      this.updateStartPoint({ x: sourceAnchor.x, y: sourceAnchor.y })
    }
    if (targetAnchor) {
      this.updateEndPoint({ x: targetAnchor.x, y: targetAnchor.y })
    }
    (this as any).pointsList = []
    this.initPoints()
  }
}

// Animation edge model
class AnimationModel extends BezierEdgeModel {
  constructor(data: any, graphModel: any) {
    super(data, graphModel)
    ;(this as any).isAnimation = true
  }

  getEdgeAnimationStyle() {
    const style = super.getEdgeAnimationStyle()
    style.stroke = '#5dc822'
    style.animationDuration = '30s'
    style.animationDirection = 'normal'
    style.strokeDasharray = '10 5'
    return style
  }

  updatePathByAnchor() {
    const sourceNodeModel = this.graphModel.getNodeModelById(this.sourceNodeId)
    const sourceAnchor = sourceNodeModel?.getDefaultAnchor().find(
      (anchor: any) => anchor.id === this.sourceAnchorId
    )
    const targetNodeModel = this.graphModel.getNodeModelById(this.targetNodeId)
    const targetAnchor = targetNodeModel?.getDefaultAnchor().find(
      (anchor: any) => anchor.id === this.targetAnchorId
    )

    if (sourceAnchor) {
      this.updateStartPoint({ x: sourceAnchor.x, y: sourceAnchor.y })
    }
    if (targetAnchor) {
      this.updateEndPoint({ x: targetAnchor.x, y: targetAnchor.y })
    }
    (this as any).pointsList = []
    this.initPoints()
  }
}

export const defaultEdge = {
  type: 'EDGE_BEZIER',
  view: CustomEdge,
  model: DefaultBezierEdgeModel,
}

export const animationEdge = {
  type: 'EDGE_BEZIER_A',
  view: CustomEdge,
  model: AnimationModel,
}
