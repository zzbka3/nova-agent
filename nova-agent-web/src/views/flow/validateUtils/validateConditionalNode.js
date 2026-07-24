/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-04 13:15:40
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-09-09 10:22:30
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateConditionalNode.js
 * @Description: 校验分支器节点
 */
import { getAllFlatArgs } from '../getArgs';
import { isEmpty } from '@/views/flow/common/common';
const validateConditionalNode = ({ model = {}, lf }) => {
    const {conditionList = [], defaultTargetNodes = []} = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    let temConditionLis = [];
    // 是否更新conditionList
    let isUpdateConditionList = false;
    // 分支器所有输出边集合
    let allTargetNodes = [];
    // 获取该节点所有输出的边
    const outgoingEdges = lf.getNodeOutgoingEdge(model.id);
    conditionList.forEach((item, index) => {
        const { innerConditions = [], targetNodes = [] } = item || {};
        if (innerConditions.length === 0) {
            validateStatus = false;
            errResult.push('条件配置未完成');
        }
        innerConditions.forEach((innerItem, innerIndex) => {
            const { left = {}, right = {}, op = '' } = innerItem;
            // eslint-disable-next-line max-len
            const { varType: leftVarType, referenceVarName: leftReferenceVarName, referenceNodeId: leftReferenceNodeId } = left || {};
            // 判断左侧是否为引用类型
            if (leftVarType === 'reference') {
                const leftVarInParent = AllFlatArgs.find((ele) =>
                    // eslint-disable-next-line max-len
                    (ele.realNamePath || ele.varName) === leftReferenceVarName && ele.referenceNodeId === leftReferenceNodeId
                );
                if (!leftVarInParent) {
                    validateStatus = false;
                    errResult.push(`条件${index + 1}中，引用变量未定义`);
                }
            }
            if (op !== 'EMPTY' && op !== 'NOT_EMPTY') {
                // eslint-disable-next-line max-len
                const { varType: rightVarType, referenceVarName: rightReferenceVarName, referenceNodeId: rightReferenceNodeId, varValue: rightVarValue } = right || {};
                // 判断右侧是否为引用类型
                if (rightVarType === 'reference') {
                    const rightVarInParent = AllFlatArgs.find((ele) =>
                        // eslint-disable-next-line max-len
                        (ele.realNamePath || ele.varName) === rightReferenceVarName && ele.referenceNodeId === rightReferenceNodeId
                    );
                    if (!rightVarInParent) {
                        validateStatus = false;
                        errResult.push(`条件${index + 1}中，引用变量${leftReferenceVarName || '<未定义变量>'}的比较变量未引用`);
                    }
                } else {
                    // 右侧非引用类型，需要判断是否有值
                    if (isEmpty(rightVarValue)) {
                        validateStatus = false;
                        errResult.push(`条件${index + 1}中，引用变量${leftReferenceVarName || '<未定义变量>'}比较变量值不能为空`);
                    }
                }
            }
            // 判断操作符是否存在
            if (!op) {
                validateStatus = false;
                errResult.push(`条件${innerIndex + 1}中，条件关系不能为空`);
            }
        });
        // 目标节点校验
        let temTargetNodes = [];
        if (targetNodes && targetNodes.length) {
            targetNodes.map(item => {
                const temTargetNode = outgoingEdges.find(edge => edge.id === item.edgeId);
                if (temTargetNode) {
                    allTargetNodes.push(item);
                    temTargetNodes.push(item);
                }
            });
            if (targetNodes.length !== temTargetNodes.length) {
                isUpdateConditionList = true;
                temConditionLis.push({
                    ...item,
                    targetNodes: temTargetNodes
                });
            } else {
                temConditionLis.push(item);
            }
        }
    });
    if (isUpdateConditionList) {
        model.setProperties({
            conditionList: [...temConditionLis]
        });
    }
    // 校验默认分支边连接线
    if (defaultTargetNodes && defaultTargetNodes.length) {
        const temDefaultTargetNodes = [];
        defaultTargetNodes.map(item => {
            const temTargetNode = outgoingEdges.find(edge => edge.id === item.edgeId);
            if (temTargetNode) {
                allTargetNodes.push(item);
                temDefaultTargetNodes.push(item);
            }
        });
        // 默认分支线存在差异
        if (defaultTargetNodes.length !== temDefaultTargetNodes.length) {
            model.setProperties({
                defaultTargetNodes: [...temDefaultTargetNodes]
            });
        }
    }
    if (outgoingEdges.length !== allTargetNodes.length) {
        // 没有被使用的线，需要全部删除
        outgoingEdges.forEach(item => {
            if (!allTargetNodes.some(edge => edge.edgeId === item.id)) {
                lf.deleteEdge(item.id);
            }
        });
    }

    return { validateStatus, errResult };
};
export default validateConditionalNode;