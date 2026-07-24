/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-02 13:16:01
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @LastEditTime: 2025-11-13 17:06:12
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/registerIntention.js
 * @Description: registerIntention
 */
import Vue from 'vue';
import intention from './components/intention.vue';
import { setAnchorStyle, intentionOutputList, connectedTargetRules, connectedSourceRules } from './commonUtils';
import getNodeInitNames from '../common/getNodeInitNames';
import { uniqueValue } from '@/views/flow/common/common';
export default function registerConnect(lf) {
  lf.register('INTENT', ({ HtmlNode, HtmlNodeModel }) => {
    class conditionalNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        this.props.model.zIndex = 0;
        const Profile = Vue.extend({
          render: function (h) {
            return h(intention, {
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
    class conditionalNodeModel extends HtmlNodeModel {
      initNodeData(data) {
        if (data.text) {
          data.text.editable = false;
        }
        super.initNodeData(data);
        // eslint-disable-next-line max-len
        const { intentItems, inputVars, width = 400, mode, temperature, maxOutputTokens, talkHistory, nodeName = '' } = data.properties || {};
        this.width = width;
        let extractVars = [];
        (intentItems || []).forEach(item => {
          item.extractVars.forEach(ele => {
            if (!ele.id) {
              ele.id = uniqueValue();
            }
          });
          extractVars = [...extractVars, ...item.extractVars];
        });
        this.properties.outputVars = [...intentionOutputList, ...extractVars];
        if (!intentItems || !intentItems.length) {
          this.properties.intentItems = [
            {
              intentItemsIndex: 1,
              id: 1,
              targetNodes: [],
              intentName: '意图1',
              intentDesc: '',
              demos: [],
              extractVars: [],
            }
          ];
        }
        if (!inputVars || !inputVars.length) {
          this.properties.inputVars = [
            {
              varName: 'query',
              varType: 'reference',
              varValue: '',
              referenceNodeId: '',
              referenceVarName: '',
              referenceVarType: '',
            }
          ];
        }
        this.properties.mode = mode ? mode : 'speed';
        this.properties.temperature = temperature ? temperature : 0.0001;
        this.properties.maxOutputTokens = maxOutputTokens ? maxOutputTokens : 1024;
        this.properties.talkHistory = talkHistory ? talkHistory : 1;
        this.setIsShowAnchor();
        let name = getNodeInitNames(nodeName, 'INTENT', lf, '意图识别');
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
            const y1 = field.height === 0 ? y : y - height / 2 + field.height;
            const x1 = field.height === 0 ? x + width / 2 : x + width / 2 - 26;
            rightAnchor.push({
              x: x1,
              y: y1,
              id: `${id}__${field.indexId}__end`,
              name: 'right',
              meta: {
                anchorIndex: field.indexId,
                type: 'INTENT'
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