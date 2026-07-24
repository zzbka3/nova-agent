/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-09-04 16:13:46
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-23 14:34:18
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerMemory.js
 * @Description: 记忆变量 MEMORY
 */
import Vue from 'vue';
import memory from './components/memory.vue';
import { setAnchorStyle, connectedTargetRules, connectedSourceRules } from './commonUtils';
import getNodeInitNames from '../common/getNodeInitNames';
export default function registerConnect(lf) {
  lf.register('MEMORY', ({ HtmlNode, HtmlNodeModel }) => {
    class memoryNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const { properties } = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(memory, {
              props: {
                name: properties.name,
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
    class memoryNodeModel extends HtmlNodeModel {
      initNodeData(data) {
        if (data.text) {
          data.text.editable = false;
        }
        const { nodeName = '', width = 400 } = this.properties || {};
        let name = getNodeInitNames(nodeName, 'MEMORY', lf, '记忆变量');
        super.initNodeData(data);
        this.width = width;
        this.setIsShowAnchor();
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
      getDefaultAnchor() {
        const {
          width,
          x,
          y,
          id,
        } = this;
        const leftAnchor = {
          x: x - width / 2,
          y,
          name: 'left',
          id: `${id}__start`
        };
        let Anchor = [leftAnchor];
        const rightAnchor = {
          x: x + width / 2,
          y,
          name: 'right',
          id: `${id}__end`
        };
        Anchor.push(rightAnchor);
        return [...Anchor];
      }
    }
    return {
      view: memoryNode,
      model: memoryNodeModel
    };
  });
}