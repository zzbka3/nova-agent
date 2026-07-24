import Vue from 'vue';
import code from './components/code.vue';
import { setAnchorStyle, connectedTargetRules, connectedSourceRules } from './commonUtils';
import getNodeInitNames from '../common/getNodeInitNames';
export default function registerConnect(lf) {
  lf.register('CODE', ({ HtmlNode, HtmlNodeModel }) => {
    class codeNode extends HtmlNode {
      setHtml(rootEl) {
        const model = this.props.model;
        const { properties } = this.props.model;
        const el = document.createElement('div');
        // bca-disable-line
        rootEl.innerHTML = '';
        rootEl.appendChild(el);
        const Profile = Vue.extend({
          render: function (h) {
            return h(code, {
              props: {
                name: properties.name,
                model,
                lf
              },
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
    class codeNodeModel extends HtmlNodeModel {
      initNodeData(data) {
        if (data.text) {
          data.text.editable = false;
        }
        const { nodeName = '', width = 400 } = this.properties || {};
        let name = getNodeInitNames(nodeName, 'CODE', lf, '代码');
        super.initNodeData(data);
        this.width = width;
        this.setIsShowAnchor();
        this.properties.nodeName = name;
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
        console.log('updatePath');
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
      view: codeNode,
      model: codeNodeModel
    };
  });
}