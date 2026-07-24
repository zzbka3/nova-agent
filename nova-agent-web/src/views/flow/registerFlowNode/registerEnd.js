/*
 * @Author: hewenquan
 * @Date: 2025-06-23 15:24:02
 * @LastEditTime: 2025-07-17 19:44:39
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerEnd.js
 * @Description: 结束节点
 */

import end from './components/end.vue';
import Vue from 'vue';
import { setAnchorStyle } from './commonUtils';
export default function registerEnd(lf) {
  lf.register('END', ({ HtmlNode, HtmlNodeModel }) => {
    class EndNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        // console.log(properties);
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(end, {
              props: {
                model,
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
        // 动态更新高度
        if (rootEl?.querySelector('div')?.clientHeight) {
          setTimeout(() => {
            const clientHeight = rootEl.querySelector('div').clientHeight;
            this.props.model.height = clientHeight + 2;
          }, 0);
        }
      }
    }
    class EndModel extends HtmlNodeModel {
      initNodeData(data) {
        super.initNodeData(data);
        this.setIsShowAnchor();
        this.r = 20;
        this.width = 400;
        this.height = 150;
        this.properties.nodeName = '结束';
      }
      // 自定义锚点样式
      getAnchorStyle() {
        const style = super.getAnchorStyle();
        return setAnchorStyle(style);
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
      /**
       * 设置锚点显示状态为显示
       *
       */
      setIsShowAnchor() {
        this.isShowAnchor = true;
      }
      // 自定义节点outline
      getOutlineStyle() {
        const style = super.getOutlineStyle();
        style.stroke = '#88f';
        return style;
      }
      getConnectedSourceRules() {
        const rules = super.getConnectedSourceRules();
        const notAsTarget = {
          message: '终止节点不能作为连线的起点',
          validate: () => false
        };
        rules.push(notAsTarget);
        return rules;
      }
      getDefaultAnchor() {
        const { width, x, y, id } = this;
        return [
          {
            x: x - width / 2,
            y: y + 3,
            name: 'left',
            id: `${id}__start`
          },
        ];
      }
    }
    return {
      view: EndNode,
      model: EndModel
    };
  });
}
