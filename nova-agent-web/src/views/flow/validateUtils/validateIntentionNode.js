/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-10 13:20:58
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-09-06 12:27:54
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateIntentionNode.js
 * @Description: 校验意图节点
 */
import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';
const validateIntentionNode = ({ model = {}, lf }) => {
    // eslint-disable-next-line max-len
    const { mode, inputVars, intentItems, model: intentModel, defaultTargetNodes = [] } = model.properties;
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    if (!mode) {
        validateStatus = false;
        errResult.push('请选择模式');
    }
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    let temIntent = [];
    // 是否更新conditionList
    let isUpdateIntent = false;
    // 分支器所有输出边集合
    let allTargetNodes = [];
    // 获取该节点所有输出的边
    const outgoingEdges = lf.getNodeOutgoingEdge(model.id);
    if (!intentItems || intentItems.length === 0) {
        validateStatus = false;
        errResult.push('意图未完善');
    } else {
        const varNameSet = new Set(); // 用于记录 varName
        const duplicateVarNames = new Set(); // 收集重复的
        intentItems.forEach((item, index) => {
            if (!item.intentName) {
                validateStatus = false;
                errResult.push(`意图${index + 1}名称未完善`);
            }
            if (!item.intentDesc) {
                validateStatus = false;
                errResult.push(`意图${index + 1}描述未完善`);
            }
            if (mode === 'accurate') {
                if (item.demos && item.demos.length) {
                    item.demos.forEach((demo) => {
                        if (!demo) {
                            validateStatus = false;
                            errResult.push(`意图${index + 1}例句未完善`);
                        }
                    });
                }
                if (item.extractVars && item.extractVars.length) {
                    item.extractVars.forEach((extractVar) => {
                        if (!extractVar.varName) {
                            validateStatus = false;
                            errResult.push(`意图${index + 1}参数名未完善`);
                        } else {
                            const reg = /^[a-zA-Z][a-zA-Z0-9_]*$/;
                            if (!reg.test(extractVar.varName)) {
                                validateStatus = false;
                                errResult.push(`意图${index + 1}参数名不符合规范`);
                            }
                            if (varNameSet.has(extractVar.varName)) {
                                duplicateVarNames.add(extractVar.varName);
                            } else {
                                varNameSet.add(extractVar.varName);
                            }
                        }
                        if (!extractVar.varType) {
                            validateStatus = false;
                            errResult.push(`意图${index + 1}参数类型未完善`);
                        }
                        if (!extractVar.desc) {
                            validateStatus = false;
                            errResult.push(`意图${index + 1}参数描述未完善`);
                        }
                    });
                }
            }
            // 目标节点校验
        let temTargetNodes = [];
        const { targetNodes = [] } = item || {};
        if (targetNodes && targetNodes.length) {
            targetNodes.map(item => {
                const temTargetNode = outgoingEdges.find(edge => edge.id === item.edgeId);
                if (temTargetNode) {
                    allTargetNodes.push(item);
                    temTargetNodes.push(item);
                }
            });
            if (targetNodes.length !== temTargetNodes.length) {
                isUpdateIntent = true;
                temIntent.push({
                    ...item,
                    targetNodes: temTargetNodes
                });
            } else {
                temIntent.push(item);
            }
        }
        });
        if (duplicateVarNames.size > 0) {
            validateStatus = false;
            errResult.push('参数名重复');
        }
    }
    if (isUpdateIntent) {
        model.setProperties({
            intentItems: [...temIntent]
        });
    }
    if (!intentModel) {
        validateStatus = false;
        errResult.push('请选择模型');
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
export default validateIntentionNode;