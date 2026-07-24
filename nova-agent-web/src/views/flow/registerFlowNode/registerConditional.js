/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-06-27 13:19:30
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-23 14:39:03
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerConditional.js
 * @Description: conditional
 */
import Vue from 'vue';
import conditional from './components/conditional.vue';
import { setAnchorStyle, connectedTargetRules, connectedSourceRules } from './commonUtils';
import getNodeInitNames from '../common/getNodeInitNames';
export default function registerConnect(lf) {
  lf.register('IF', ({ HtmlNode, HtmlNodeModel }) => {
    class conditionalNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        // const { properties } = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        this.props.model.zIndex = 0;
        // this.props.model.autoToFront = false;
        const Profile = Vue.extend({
          render: function (h) {
            return h(conditional, {
              props: {
                // conditionList: properties?.conditionList?.length ? properties.conditionList : conditionList,
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
        Vue.nextTick(() => {
          if (rootEl?.querySelector('div')?.clientHeight) {
            const clientHeight = rootEl.querySelector('div').clientHeight;
            // const clientWidth = rootEl.querySelector('div').clientWidth;
            this.props.model.height = clientHeight + 2;
            // this.props.model.width = clientWidth + 2;
          }
        });
      }
    }
    class conditionalNodeModel extends HtmlNodeModel {
      initNodeData(data) {
        if (data.text) {
          data.text.editable = false;
        }
        super.initNodeData(data);
        const { conditionList, nodeName = '', width = 400 } = data.properties || {};
        if (!conditionList || !conditionList.length) {
          this.properties.conditionList = [{
            innerLogic: 'AND',
            innerConditions: [],
            conditionIndex: 1,
            id: 1
          }];
        }
        this.width = width;
        this.setIsShowAnchor();
        let name = getNodeInitNames(nodeName, 'IF', lf, '分支器');
        this.properties.nodeName = name;
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
       * 设置锚点显示状态为显示
       *
       */
      setIsShowAnchor() {
        this.isShowAnchor = true;
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
          height,
          x,
          y,
          id,
          properties: { customAnchors },
        } = this;
        const leftAnchor = {
          x: x - width / 2,
          y,
          name: 'left',
          id: `${id}_0`
        };
        let Anchor = [leftAnchor];
        let rightAnchor = [];
        if (customAnchors && customAnchors.length) {
          customAnchors.forEach((field) => {
            rightAnchor.push({
              x: x + width / 2 - 14,
              y: y - height / 2 + field.height,
              id: `${id}__${field.indexId}__end`,
              name: 'right',
              meta: {
                anchorIndex: field.indexId,
                type: 'IF'
              }
            });
          });
        }
        return [...Anchor, ...rightAnchor];
      }
    }
    return {
      view: conditionalNode,
      model: conditionalNodeModel
    };
  });
}