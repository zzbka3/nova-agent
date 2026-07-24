/*
 * @Author: hewenquan
 * @Date: 2025-07-07 14:30:55
 * @LastEditTime: 2025-11-03 17:25:10
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/lf.js
 * @Description: 校验画布
 */
import { message } from 'ant-design-vue';
import validateEdgesRules from './validateEdges';
/**
 * 验证流程配置
 *
 * @param {Object} param 参数对象
 * @param {Object} param.bus 事件总线对象
 * @returns {Promise} 返回一个Promise对象，Promise resolve时返回所有子组件验证通过后的数据，Promise reject时返回错误信息
 */
const validateFlow = ({ bus, lf, needInfo = true }) => {
    return new Promise((resolve, reject) => {
        let graph = lf.getGraphData();
        const childIds = graph.nodes.map(item => item.id);
        console.log(childIds);
        // 有连线关系的节点，需要被校验
        let filterConnectNodes = [];
        childIds.forEach(nodeId => {
            // 入边
            const incomingEdges = lf.getNodeIncomingEdge(nodeId);
            // 出边
            const outgoingEdges = lf.getNodeOutgoingEdge(nodeId);
            if (incomingEdges.length || outgoingEdges.length) {
                filterConnectNodes.push(nodeId);
            }
        });
        // 遍历子组件，等待子组件执行完毕，先监听注册，拿到值后再取消事件
        const promises = filterConnectNodes.map(
            (id) =>
                new Promise((resolve, reject) => {
                    const handler = ({ nodeId, validateStatus, errResult }) => {
                        if (nodeId === id) {
                            bus.$off('childValidateDone', handler);
                            // 校验是否通过
                            if (validateStatus) {
                                resolve(nodeId);
                            } else {
                                reject({ nodeId, errResult });
                            }
                        }
                    };
                    bus.$on('childValidateDone', handler);
                })
        );
        bus.$emit('validateFlowChild');
        Promise.all(promises).then((data) => {
            resolve(data);
        }).catch(err => {
            const { nodeId, errResult } = err || {};
            console.log('promisesErr', err);
            reject(nodeId);
            const { properties } = lf.getNodeDataById(nodeId);
            if (needInfo && properties?.nodeName) {
                console.log(errResult, 'errResult');
                message.error(`${properties.nodeName}节点校验未通过，请检查配置`); // 校验未通过，提示错误信息
            }
        });
    });
};

/**
 * 校验边
 *
 * @param {Object} params - 参数对象
 * @param {Object} params.lf - 布局对象
 * @param {Object} params.bus - 事件总线对象
 * @returns {boolean} - 校验通过返回true，否则返回false
 */
const validateEdges = ({ lf }) => {
    const { validateStatus = false, errResult = '' } = validateEdgesRules(lf);
    if (!validateStatus && errResult) {
        const { transformModel, width, height } = lf.graphModel;
        const { x, y } = lf.getNodeModelById(errResult);
        // 移动错误焦点到画布中央
        transformModel.focusOn(x, y, width, height);
        // bus.$emit('setError', [errResult]);
    }
    if (validateStatus) {
        // message.success('校验通过');
        return true;
    }
    return false;
};
export {
    validateFlow,
    validateEdges
};