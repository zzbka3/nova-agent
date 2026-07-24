/*
 * @Author: hewenquan
 * @Date: 2025-07-08 14:49:03
 * @LastEditTime: 2025-10-23 14:37:50
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerKnowledge.js
 * @Description: 注册知识节点
 */
import Vue from 'vue';
import knowledge from './components/knowledge.vue';
import { setAnchorStyle, connectedTargetRules, connectedSourceRules, largeModelViews } from './commonUtils';
import { deepClone } from '@baidu/metis-js-util';
import getNodeInitNames from '../common/getNodeInitNames';
export default function registerConnect(lf) {
  lf.register('KNOWLEDGE', ({ HtmlNode, HtmlNodeModel }) => {
    class knowledgeNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const { properties } = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(knowledge, {
              props: {
                properties,
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
        setTimeout(() => {
          if (rootEl?.querySelector('div')?.clientHeight) {
            const clientHeight = rootEl.querySelector('div').clientHeight;
            // const clientWidth = rootEl.querySelector('div').clientWidth;
            this.props.model.height = clientHeight + 2;
            // this.props.model.width = clientWidth + 2;
          }
        }, 0);
      }
    }
    class knowledgeNodeModel extends HtmlNodeModel {
      initNodeData(data) {
        let outputVars = deepClone(largeModelViews) || [];
        if (data.text) {
          data.text.editable = false;
        }
        super.initNodeData(data);
        const { nodeName = '', width = 400 } = this.properties || {};
        let name = getNodeInitNames(nodeName, 'KNOWLEDGE', lf, '知识库');
        this.width = width;
        this.setIsShowAnchor();
        this.properties.nodeName = name;
        this.properties.outputVars = outputVars;

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
       * 设置锚点显示状态为显示
       *
       */
      setIsShowAnchor() {
        this.isShowAnchor = true;
      }
      getAnchorStyle() {
        const style = super.getAnchorStyle();
        return setAnchorStyle(style);
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
      view: knowledgeNode,
      model: knowledgeNodeModel
    };
  });
}