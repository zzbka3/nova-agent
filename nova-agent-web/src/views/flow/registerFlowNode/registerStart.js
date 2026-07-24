/*
 * @Author: hewenquan
 * @Date: 2025-06-24 22:47:46
 * @LastEditTime: 2025-07-31 15:10:07
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerStart.js
 * @Description: startNode 注册
 */
import startNode from './components/startNode.vue';
import Vue from 'vue';
import { setAnchorStyle } from './commonUtils';
export default function registerStart(lf) {
  lf.register('START', ({ HtmlNode, HtmlNodeModel }) => {
    class StartNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(startNode, {
              props: {
                model,
                isSelected: this.isSelected,
                lf
              },
              on: {
                'select-button': (type) => {
                  console.log('select-button', type);
                }
              }
            });
          }
        });
        new Profile().$mount(el);
        setTimeout(() => {
          if (rootEl?.querySelector('div')?.clientHeight) {
            const clientHeight = rootEl.querySelector('div').clientHeight;
            this.props.model.height = clientHeight + 2;
          }
        }, 0);
      }
    }
    class StartModel extends HtmlNodeModel {
      // 自定义节点形状属性
      initNodeData(data) {
        data.text = {
          value: (data.text && data.text.value) || '',
          x: data.x,
          y: data.y + 35,
          dragable: false,
          editable: true,
        };
        data.draggable = false;
        data.isShowAnchor = true;
        // console.log(data, 'StartModel');
        super.initNodeData(data);
        this.setIsShowAnchor();
        this.r = 20;
        this.width = 360;
        this.height = 300;
        this.properties.nodeName = '开始';
      }
      /**
       * 设置节点的高度
       */
      setCustomAttributes(attrs) {
        const {currentHeight = 280, expand = false} = attrs || {};
        const { height, y } = this || {};
        let targetY = y;
        // 收起
        if (!expand) {
          targetY = y - (height - currentHeight) / 2;
        } else {
          // 展开
          targetY = y + (currentHeight - height) / 2;
        }
        this.y = targetY;
        this.height = currentHeight + 2;
        this.updatePath();
      }
      /**
       * @description: 更新节点连线
       * @return {*}
       */
      updatePath() {
        // 更新节点连接边的path
        this.incoming.edges.forEach((egde) => {
          // 调用自定义的更新方案
          egde.updatePathByAnchor();
        });
        this.outgoing.edges.forEach((edge) => {
          // 调用自定义的更新方案
          edge.updatePathByAnchor();
        });
      }
      // 自定义节点样式属性
      getNodeStyle() {
        const style = super.getNodeStyle();
        return style;
      }
      // 自定义锚点样式
      getAnchorStyle() {
        const style = super.getAnchorStyle();
        return setAnchorStyle(style);
      }
      // 自定义节点outline
      getOutlineStyle() {
        const style = super.getOutlineStyle();
        style.stroke = '#88f';
        return style;
      }
      /**
       * 设置锚点显示状态为显示
       *
       */
      setIsShowAnchor() {
        this.isShowAnchor = true;
      }
      getConnectedTargetRules() {
        const rules = super.getConnectedTargetRules();
        const notAsTarget = {
          message: '起始节点不能作为连线的终点',
          validate: () => false
        };
        rules.push(notAsTarget);
        return rules;
      }
      getDefaultAnchor() {
        const { width, x, y, id } = this;
        return [
          {
            x: x + width / 2,
            y,
            name: 'right',
            id: `${id}__end`,
            isShowAnchor: true
          },
        ];
      }
    }
    return {
      view: StartNode,
      model: StartModel
    };
  });
}
