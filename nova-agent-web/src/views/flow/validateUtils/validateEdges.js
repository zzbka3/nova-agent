/*
 * @Author: hewenquan
 * @Date: 2025-07-03 17:09:29
 * @LastEditTime: 2025-11-07 10:24:38
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateEdges.js
 * @Description: 校验画布所有节点的连线是否正确
 */
/**
 * @description: 校验节点连线是否正确
 * 1、必须有起始节点
 * 2、必须有结束节点
 * 3、所有锚点必须有连线
 * 4、所有连线的最终点必须为结束节点
 * 5、连线必须为首尾相连
 * 6、开始节点不能直接连接到结束节点
 * @return {*}
 */

import { message } from 'ant-design-vue';
const validateEdges = (lf) => {
    const { nodes = [], edges = [] } = lf.getGraphData();
    if (!Array.isArray(nodes) || !nodes.length) {
        message.error('请先配置节点');
        return {
            validateStatus: false,
            errResult: ''
        };
    }
    if (!Array.isArray(edges) || !edges.length) {
        message.error('请完善节点之间的连线');
        return {
            validateStatus: false,
            errResult: nodes[0].id
        };
    }
    // * 1、必须有起始节点
    const startNode = nodes.find(node => node.type === 'START');
    if (!startNode) {
        message.error('必须配置开始节点');
        return {
            validateStatus: false,
            errResult: ''
        };
    }
    // * 2、必须有结束节点
    const endNode = nodes.find(node => node.type === 'END');
    if (!endNode) {
        message.error('必须配置结束节点');
        return {
            validateStatus: false,
            errResult: ''
        };
    }
    // * 6、开始节点不能直接连接到结束节点
    // todo
    for (let index = 0; index < edges.length; index++) {
        const edge = edges[index];
        if (edge.sourceNodeId === startNode.id && edge.targetNodeId === endNode.id) {
            message.error('开始节点不能直接连接到结束节点');
            return {
                validateStatus: false,
                errResult: ''
            };
        }
    }
    // * 3、所有锚点必须有连线
    // 4、如果节点 起始终止节点都没有连线，可以跳过校验
    const allNodeIds = nodes.map(item => item.id);
    let filterConnectNodes = [];
    allNodeIds.forEach(nodeId => {
        // 入边
        const incomingEdges = lf.getNodeIncomingEdge(nodeId);
        // 出边
        const outgoingEdges = lf.getNodeOutgoingEdge(nodeId);
        if (incomingEdges.length || outgoingEdges.length) {
            filterConnectNodes.push(nodeId);
        }
    });
    for (let index = 0; index < filterConnectNodes.length; index++) {
        const nodeId = filterConnectNodes[index];
        const model = lf.getNodeModelById(nodeId);
        const nodeAnchors = model.anchors;
        // 是否有锚点没有线连
        let hasANchorNotConnected = false;
        if (nodeAnchors && nodeAnchors.length) {
            nodeAnchors.forEach(anchor => {
                const hasEdge = edges.find(edge => {
                    return edge.sourceAnchorId === anchor.id || edge.targetAnchorId === anchor.id;
                });
                if (!hasEdge) {
                    hasANchorNotConnected = true;
                }
            });
        } else {
            hasANchorNotConnected = true;
        }
        if (hasANchorNotConnected) {
            const { nodeName = '' } = model.getProperties() || {};
            let warnMsg = `请完善节点 ${nodeName} 的锚点没有连线`;
            message.error(warnMsg);
            return {
                validateStatus: false,
                errResult: nodeId
            };
        }
    }
    // * 4、所有连线的最终点必须为结束节点
    // 好像不用判断，锚点都连了，那肯定能到结束节点
    return {
        validateStatus: true,
        errResult: ''
    };
};

export default validateEdges;