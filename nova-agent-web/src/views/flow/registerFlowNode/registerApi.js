/*
 * @Author: hewenquan
 * @Date: 2025-06-23 15:24:02
 * @LastEditTime: 2025-10-23 14:40:30
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerApi.js
 * @Description: 结束节点
 */

import API from './components/apiNode.vue';
import Vue from 'vue';
import { setAnchorStyle, connectedTargetRules, connectedSourceRules } from './commonUtils';
import getNodeInitNames from '../common/getNodeInitNames';
export default function registerEnd(lf) {
  lf.register('API', ({ HtmlNode, HtmlNodeModel }) => {
    class apiNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const { properties } = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(API, {
              props: {
                name: properties.name,
                model,
                lf
              }
            });
          }
        });
        new Profile().$mount(el);

        // 动态更新高度
        setTimeout(() => {
          if (rootEl?.querySelector('div')?.clientHeight) {
            const clientHeight = rootEl.querySelector('div').clientHeight;
            this.props.model.height = clientHeight + 2;
          }
        }, 0);
      }
    }
    class apiModel extends HtmlNodeModel {
      initNodeData(data) {
        super.initNodeData(data);
        const { nodeName = '', width = 400 } = this.properties || {};
        let name = getNodeInitNames(nodeName, 'API', lf, 'API');
        this.r = 20;
        this.width = width;
        this.height = 110;
        this.properties.nodeName = name;
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
      // 自定义节点outline
      getOutlineStyle() {
        const style = super.getOutlineStyle();
        style.stroke = '#88f';
        return style;
      }
      /**
       * @description: 起始节点规则
       * @return {*}
       */
      getConnectedSourceRules() {
        const rules = super.getConnectedSourceRules();
        rules.push(connectedSourceRules);
        return rules;
      }
      /**
       * @description: 获取当前节点作为边的目标节点规则。
       * @return {*}
       */
      getConnectedTargetRules() {
        const rules = super.getConnectedTargetRules();
        rules.push(connectedTargetRules);
        return rules;
      }
      // 自定义锚点
      getDefaultAnchor() {
        const { width, x, y, id } = this;
        return [
          {
            x: x - width / 2,
            y,
            name: 'left',
            id: `${id}__start`
          },
          {
            x: x + width / 2,
            y,
            name: 'right',
            id: `${id}__end`
          },
        ];
      }
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

    }
    return {
      view: apiNode,
      model: apiModel
    };
  });
}
