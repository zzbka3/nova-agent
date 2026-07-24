/*
 * @Author: hewenquan
 * @Date: 2025-07-07 14:05:09
 * @LastEditTime: 2025-11-13 19:30:05
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @FilePath: /metis-front/src/views/flow/basics/lfEvent.js
 * @Description: 画布事件处理集合
 */
/**
 * 处理节点点击事件
 *
 * @param args 节点点击事件携带的参数
 * @param bus 事件总线
 * @returns 是否显示配置对话框
 */
import { validateFlow } from '@/views/flow/validateUtils/lf';
import { Modal } from 'ant-design-vue';
import { deleteTempOutputs } from '@/views/flow/getArgs';

/**
 * @description: 获取当前节点的所有下游节点ID
 * @param {*} lf
 * @param {*} nodeId
 * @param {*} visited 过滤重复ID
 * @return {*}
 */
const getAllDownstreamNodes = (lf, nodeId, visited = new Set()) => {
    if (visited.has(nodeId)) {
        return [];
    }
    visited.add(nodeId);
    const directNodes = lf.getNodeOutgoingNode(nodeId);
    const allNodes = [...directNodes];
    // 递归获取
    directNodes.forEach(node => {
        const downstream = getAllDownstreamNodes(lf, node.id, visited);
        allNodes.push(...downstream);
    });
    return allNodes;
};

/**
 * @description: 获取当前节点的父级节点ID
 * @param {*} lf
 * @param {*} nodeId
 * @param {*} visited
 * @return {*}
 */
const getAllUpstreamNodes = (lf, nodeId, visited = new Set()) => {
        if (visited.has(nodeId)) {
            return [];
        }
        visited.add(nodeId);
        const directNodes = lf.getNodeIncomingNode(nodeId);
        const allNodes = [...directNodes.map(node => node.id)];
        directNodes.forEach(node => {
            const upstream = getAllUpstreamNodes(lf, node.id, visited);
            allNodes.push(...upstream);
        });
        return allNodes;
    };
/**
 * 自定义锚点点击事件处理函数, 删除连线确认弹窗
 *
 * @param {Object} param - 事件参数对象
 * @param {Object} param.node - 锚点节点对象
 * @param {Object} param.lf - LayoutFactory实例
 * @returns {void}
 */
const customAnchorClickEvent = ({ edge = {}, lf, bus }) => {
    Modal.confirm({
        title: '确认删除链接线吗',
        content: '删除后将无法恢复',
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
            const { sourceAnchorId, sourceNodeId, id, targetNodeId } = edge || {};
            if (sourceAnchorId) {
                // 更新 多锚点节点的配置关系，多锚点节点的配置中有targetNodes字段，代表每个锚点的指向
                const splitSourceAnchorId = sourceAnchorId.split('__');
                if (splitSourceAnchorId.length === 3) {
                    const sourceNodeMode = lf.getNodeModelById(sourceNodeId);
                    const { type = '' } = sourceNodeMode || {};
                    // 分支器组件
                    if (type === 'IF') {
                        const { conditionList = [], defaultTargetNodes = [] } = sourceNodeMode.getProperties();
                        if (splitSourceAnchorId[1] === '-1') {
                            // 兜底分支
                            sourceNodeMode.setProperties({
                                defaultTargetNodes: defaultTargetNodes.filter(item => item.edgeId !== id)
                            });
                        } else {
                            // 正常分支
                            // eslint-disable-next-line max-len
                            const targetIndex = conditionList.findIndex(item => item.conditionIndex === +splitSourceAnchorId[1]);
                            if (targetIndex > -1) {
                                const { targetNodes = [] } = conditionList[targetIndex];
                                conditionList[targetIndex].targetNodes =
                                    targetNodes.filter(item => item.edgeId !== id);
                                sourceNodeMode.setProperties({
                                    conditionList
                                });
                            }
                        }
                    }
                    if (type === 'INTENT') {
                        const { intentItems = [], defaultTargetNodes = [] } = sourceNodeMode.getProperties();
                        if (splitSourceAnchorId[1] === '-1') {
                            sourceNodeMode.setProperties({
                                defaultTargetNodes: defaultTargetNodes.filter(item => item.edgeId !== id)
                            });
                        } else {
                            // 正常分支
                            // eslint-disable-next-line max-len
                            const targetIndex = intentItems.findIndex(item => item.intentItemsIndex === +splitSourceAnchorId[1]);
                            if (targetIndex > -1) {
                                const { targetNodes = [] } = intentItems[targetIndex];
                                intentItems[targetIndex].targetNodes =
                                    targetNodes.filter(item => item.edgeId !== id);
                                sourceNodeMode.setProperties({
                                    intentItems
                                });
                            }
                        }
                    }
                }
            }
            // 删除链接线
            lf.deleteEdge(edge.id);
            // 链接线的起始节点输出为默认参数，不需要删除引用关系
            if (sourceNodeId !== '1') {
                // 需要更新引用的节点, 当前节点的所有下游节点
                const needUpdateNodes = [lf.getNodeModelById(targetNodeId), ...getAllDownstreamNodes(lf, targetNodeId)];
                // 更新节点中需要删除的节点ID 参数, 当前节点的所有上游节点
                let needDeleteNodes = [
                    sourceNodeId,
                    ...getAllUpstreamNodes(lf, sourceNodeId)
                ].filter(item => item !== '1');
                deleteALlReference({
                    nodeId: sourceNodeId,
                    lf,
                    operate: 'edge',
                    needUpdateNodes,
                    needDeleteNodes,
                    bus
                });
            }
            deleteTempOutputs();
        },
    });
};

/**
 * 自定义删除节点
 *
 * @param {Object} isEditName 是否在编辑节点名称，编辑节点名称是，删除键不代表删除节点
 */
const customBackEvent = ({ isEditName, lf, bus }) => {
    const { nodes = [], edges = [] } = lf.getSelectElements(true);
    lf.clearSelectElements();
    if (edges.length) {
        edges.forEach((edge) => customAnchorClickEvent({ edge, lf }));
    }
    if (!isEditName && nodes.length) {
        bus.$emit('node:click', null);
        Modal.confirm({
            title: '确认删除节点吗',
            content: '删除后将无法恢复',
            okText: '确认',
            cancelText: '取消',
            onOk: () => {
                deleteTempOutputs();
                nodes.forEach((node) => {
                    const needUpdateNodes = getAllDownstreamNodes(lf, node.id);
                    // 更新节点中需要删除的节点ID 参数, 当前节点的所有上游节点
                    let needDeleteNodes = [
                        node.id,
                        ...getAllUpstreamNodes(lf, node.id), node.id
                    ].filter(item => item !== '1');
                    lf.deleteNode(node.id);
                    deleteALlReference({
                        nodeId: node.id,
                        lf,
                        operate: 'node',
                        needUpdateNodes,
                        needDeleteNodes,
                        bus
                    });
                });
            },
        });
    }
};

/**
* 删除指定节点的所有引用
*
* @param {Object} options 参数对象
* @param {string} options.nodeId 节点ID
* @param {Object} options.lf 逻辑流对象
*/
const deleteALlReference = ({ lf, needUpdateNodes = [], needDeleteNodes = [], bus, nodeId }) => {
    // 备份需要删除的节点
    const originDeleteNodes = JSON.parse(JSON.stringify(needDeleteNodes));
    needUpdateNodes.forEach(item => {
        const { properties = {}, id, type } = item || {};
        const { inputVars = [], conditionList, inputVarsAll = [] } = properties;
        const nodeModel = lf.getNodeModelById(id);
        // 连接关系删除，每个需要更新的节点需用动态获取被删除的节点ID
        if (nodeId) {
            const currentTargetNodeIds = getAllUpstreamNodes(lf, id);
            needDeleteNodes = originDeleteNodes.filter(item => !currentTargetNodeIds.includes(item));
        }
        if (type === 'IF') {
            // 是否需要更新
            let needUpdate = false;
            const newConditionList = conditionList.map(item => {
                const { innerConditions } = item || {};
                const newInnerConditions = innerConditions.map(innerItem => {
                    let { left, right } = innerItem || {};
                    const defaultConfig = {
                        varType: 'reference',
                        referenceVarName: '',
                        referenceVarType: '',
                        referenceNodeId: ''
                    };
                    if (left.varType === 'reference' && needDeleteNodes.includes(left.referenceNodeId)) {
                        left = {
                            ...left,
                            ...defaultConfig
                        };
                        needUpdate = true;
                    }
                    if (right.varType === 'reference' && needDeleteNodes.includes(right.referenceNodeId)) {
                        needUpdate = true;
                        right = {
                            ...right,
                            ...defaultConfig
                        };
                    }
                    return {
                        ...innerItem,
                        left: left,
                        right: right
                    };
                });
                return {
                    ...item,
                    innerConditions: newInnerConditions
                };
            });
            if (needUpdate) {
                nodeModel.setProperties({
                    ...properties,
                    conditionList: newConditionList,
                });
            }
        } else {
            if (!inputVars.some(item => needDeleteNodes.includes(item.referenceNodeId))) {
                return;
            }
            const updateInputs = (vars) => {
                const dealInputVars = [];
                vars.forEach(item => {
                    const { varType, referenceNodeId } = item || {};
                    if (varType === 'reference' && needDeleteNodes.includes(referenceNodeId)) {
                        dealInputVars.push({
                            ...item,
                            referenceVarId: '',
                            referenceVarName: '',
                            referenceVarType: '',
                            varValue: '',
                            referenceNodeId: ''
                        });
                    } else {
                        dealInputVars.push(item);
                    }
                });
                return dealInputVars;
            };
            const dealInputVars = updateInputs(inputVars);
            // api 节点需要同时更新 inputVarsAll
            if (inputVarsAll.some(item => needDeleteNodes.includes(item.referenceNodeId))) {
                const dealInputVarsAll = updateInputs(inputVarsAll);
                nodeModel.setProperties({
                    ...properties,
                    inputVars: dealInputVars,
                    inputVarsAll: dealInputVarsAll
                });
            } else {
                nodeModel.setProperties({
                    ...properties,
                    inputVars: dealInputVars,
                });
            }
        }
        // 删除引用后触发节点校验飘红
        setTimeout(() => {
            validateFlow({
                bus,
                lf,
                needInfo: false
            }).catch(err => {
                console.log(err);
            });
        }, 0);
    });
};

/**
* 根据节点ID，和 字段ID 更新引用变量名
*
* @param {Object} options - 参数对象
* @param {number} options.nodeId - 节点ID
* @param {string} options.lf - 左侧文件名
* @param {number} options.varNameId - 参数ID
* @param {string} options.updateVarName - 更新后的变量名
*/
const updateReferenceVarNameById = ({ nodeId, lf, varNameId, updateVarName, updateVarType = '' }) => {
    if (!lf || !nodeId) {
        return;
    }
    const { nodes = [] } = lf.getGraphData();
    nodes.forEach(item => {
        const { properties = {}, id, type } = item || {};
        const { inputVars = [], conditionList, readVars = [], outputVars = [], inputVarsAll = [] } = properties;
        const nodeModel = lf.getNodeModelById(id);
        if (type === 'IF') {
            let needUpdate = false;
            const newConditionList = conditionList.map(item => {
                const { innerConditions } = item || {};
                const newInnerConditions = innerConditions.map(innerItem => {
                    let { left, right } = innerItem || {};
                    if (left.referenceVarId === varNameId
                        && left.referenceNodeId === nodeId
                        && (left.referenceVarName !== updateVarName || !!updateVarType)) {
                        needUpdate = true;
                        let newReferenceVarName = left.referenceVarName;
                        if (newReferenceVarName.includes('.')) {
                            const splitReferenceVarName = newReferenceVarName.split('.');
                            splitReferenceVarName[splitReferenceVarName.length - 1] = updateVarName;
                            newReferenceVarName = splitReferenceVarName.join('.');
                        } else {
                            newReferenceVarName = updateVarName;
                        }
                        left = {
                            ...left,
                            referenceVarName: newReferenceVarName,
                            varValue: updateVarName,
                        };
                        if (updateVarType) {
                            left.referenceVarType = updateVarType;
                        }
                    }
                    if (right.referenceVarId === varNameId
                        && right.referenceNodeId === nodeId
                        && (right.referenceVarName !== updateVarName || !!updateVarType)) {
                        needUpdate = true;
                        let newReferenceVarName = right.referenceVarName;
                        if (newReferenceVarName.includes('.')) {
                            const splitReferenceVarName = newReferenceVarName.split('.');
                            splitReferenceVarName[splitReferenceVarName.length - 1] = updateVarName;
                            newReferenceVarName = splitReferenceVarName.join('.');
                        } else {
                            newReferenceVarName = updateVarName;
                        }
                        right = {
                            ...right,
                            referenceVarName: newReferenceVarName,
                            varValue: updateVarName
                        };
                        if (updateVarType) {
                            right.referenceVarType = updateVarType;
                        }
                    }
                    return {
                        ...innerItem,
                        left: left,
                        right: right
                    };
                });
                return {
                    ...item,
                    innerConditions: newInnerConditions
                };
            });
            if (needUpdate) {
                nodeModel.setProperties({
                    ...properties,
                    conditionList: newConditionList,
                });
            }
        } else if (type === 'MEMORY') {
            // 记忆变量更新
            // 判断是否需要修改写入数据的参数名和引用值
            let inputVarsUpdated = inputVars.some(item => {
                if (nodeId === '3') {
                    return item.varNameId === varNameId
                        && item.varName !== updateVarName;
                } else {
                    return item.referenceVarId === varNameId
                        && item.referenceNodeId === nodeId;
                }
            });
            // 判断是否需要修改输出数据
            let outputVarsUpdated = inputVars.some(item =>
                item.varNameId === varNameId
                && item.varName !== updateVarName
            );
            // 判断是否需要修改读取数据
            let readVarsUpdated = readVars.some(item =>
                item.referenceVarId === varNameId
                && item.referenceNodeId === nodeId
            );
            let updateInputVars;
            let updateReadVars;
            let updateOutputVars;
            if (inputVarsUpdated) {
                // 更新写入变量
                updateInputVars = inputVars.map(item => {
                    // eslint-disable-next-line max-len
                    const { referenceVarId, referenceVarName, referenceTreeData, varNameId: nameId, varName } = item || {};
                    const { value } = referenceTreeData || {};
                    // 更新写入变量参数名
                    if (nodeId === '3') {
                        if (nameId === varNameId) {
                            let newVarName = varName;
                            newVarName = updateVarName;
                            return {
                                ...item,
                                varName: newVarName
                            };
                        }
                    } else {
                        // 更新写入变量引用值
                        if (referenceVarId === varNameId) {
                            // referenceVarName 可能是多层级的结构 a.b.c.d， 需要替换最后一级为真实改变数据
                            let newReferenceVarName = referenceVarName;
                            let newValue = value;
                            if (referenceVarName.includes('.')) {
                                const splitReferenceVarName = referenceVarName.split('.');
                                splitReferenceVarName[splitReferenceVarName.length - 1] = updateVarName;
                                newReferenceVarName = splitReferenceVarName.join('.');
                            } else {
                                newReferenceVarName = updateVarName;
                            }
                            if (value.includes('.')) {
                                const splitReferenceValue = value.split('.');
                                splitReferenceValue[splitReferenceValue.length - 1] = updateVarName;
                                newValue = splitReferenceValue.join('.');
                                referenceTreeData.value = newValue;
                            } else {
                                newValue = updateVarName;
                                referenceTreeData.value = newValue;
                            }
                            const updateConfig = {
                                ...item,
                                referenceVarName: newReferenceVarName,
                                varValue: updateVarName,
                                referenceTreeData: {
                                    ...referenceTreeData,
                                },
                            };
                            return updateConfig;
                        }
                    }
                    return item;
                });
            }
            if (readVarsUpdated) {
                // 修改读取数据
                updateReadVars = readVars.map(item => {
                    const { referenceVarId, referenceVarName, referenceTreeData } = item || {};
                    const { value } = referenceTreeData || {};
                    if (referenceVarId === varNameId) {
                        // referenceVarName 可能是多层级的结构 a.b.c.d， 需要替换最后一级为真实改变数据
                        let newReferenceVarName = referenceVarName;
                        let newValue = value;
                        if (referenceVarName.includes('.')) {
                            const splitReferenceVarName = referenceVarName.split('.');
                            splitReferenceVarName[splitReferenceVarName.length - 1] = updateVarName;
                            newReferenceVarName = splitReferenceVarName.join('.');
                        } else {
                            newReferenceVarName = updateVarName;
                        }
                        if (value.includes('.')) {
                            const splitReferenceValue = value.split('.');
                            splitReferenceValue[splitReferenceValue.length - 1] = updateVarName;
                            newValue = splitReferenceValue.join('.');
                            referenceTreeData.value = newValue;
                        } else {
                            newValue = updateVarName;
                            referenceTreeData.value = newValue;
                        }
                        const updateConfig = {
                            ...item,
                            referenceVarName: newReferenceVarName,
                            varValue: updateVarName,
                            referenceTreeData: {
                                ...referenceTreeData,
                            },
                        };
                        return updateConfig;
                    }
                    return item;
                });
            }
            if (outputVarsUpdated) {
                // 修改输出数据参数名
                updateOutputVars = outputVars.map(item => {
                    // eslint-disable-next-line max-len
                    const { varNameId: nameId, varName, id: nodeNameId } = item || {};
                    if (nodeId === '3') {
                        if (nameId === varNameId) {
                            let newVarName = varName;
                            newVarName = updateVarName;
                            updateReferenceVarNameById({
                                nodeId: id,
                                lf: lf,
                                varNameId: nodeNameId,
                                updateVarName: updateVarName
                            });
                            return {
                                ...item,
                                varName: newVarName
                            };
                        }
                    }
                    return item;
                });
            }
            nodeModel.setProperties({
                ...properties,
                inputVars: updateInputVars,
                readVars: updateReadVars,
                outputVars: updateOutputVars
            });
        } else {
            // 寻找目标引用字段，引用id 和节点ID 一致时, 当前值不等于更新值时才更新
            let canUpdated = inputVars.some(item =>
                item.referenceVarId === varNameId
                && item.referenceNodeId === nodeId
                && item.referenceVarName !== updateVarName
            );
            // 节点类型修改
            if (updateVarType) {
                canUpdated = true;
            }
            if (canUpdated) {
                const updateInputs = (vars) => {
                    return vars.map(item => {
                        const { referenceVarId, referenceVarName } = item || {};
                        if (referenceVarId === varNameId) {
                            // referenceVarName 可能是多层级的结构 a.b.c.d， 需要替换最后一级为真实改变数据
                            let newReferenceVarName = referenceVarName;
                            if (referenceVarName.includes('.')) {
                                const splitReferenceVarName = referenceVarName.split('.');
                                splitReferenceVarName[splitReferenceVarName.length - 1] = updateVarName;
                                newReferenceVarName = splitReferenceVarName.join('.');
                            } else {
                                newReferenceVarName = updateVarName;
                            }
                            const updateConfig = {
                                ...item,
                                referenceVarName: newReferenceVarName,
                                varValue: updateVarName
                            };
                            if (updateVarType) {
                                updateConfig.referenceVarType = updateVarType;
                            }
                            return updateConfig;
                        }
                        return item;
                    });
                };
                const updateInputVars = updateInputs(inputVars);
                // api 节点需要同时更新 inputVarsAll
                if (inputVarsAll && inputVarsAll.length) {
                    const updateInputVarsAll = updateInputs(inputVarsAll);
                    nodeModel.setProperties({
                        ...properties,
                        inputVars: updateInputVars,
                        inputVarsAll: updateInputVarsAll
                    });
                } else {
                    nodeModel.setProperties({
                        ...properties,
                        inputVars: updateInputVars,
                    });
                }
            }
        }
    });
};

/**
* 根据节点ID和变量名ID删除节点中的引用变量名
*
* @param {Object} params 参数对象
* @param {string} params.nodeId 节点ID
* @param {Object} params.lf 逻辑流对象
* @param {string} params.varNameId 变量名ID
* @param {string} params.updateVarName 更新后的变量名
* @returns {void} 无返回值
*/
const deleteReferenceVarNameById = ({ nodeId, lf, varNameId }) => {
    if (!lf || !nodeId || !varNameId) {
        return;
    }
    const { nodes = [] } = lf.getGraphData();
    nodes.forEach(item => {
        const { properties = {}, id, type } = item || {};
        const { inputVars = [], conditionList, readVars = [], outputVars = [], inputVarsAll = [] } = properties;
        const nodeModel = lf.getNodeModelById(id);
        if (type === 'IF') {
            let needUpdate = false;
            const newConditionList = conditionList.map(item => {
                const { innerConditions } = item || {};
                const newInnerConditions = innerConditions.map(innerItem => {
                    let { left, right, op } = innerItem || {};
                    if (left.referenceVarId === varNameId && left.referenceNodeId === nodeId) {
                        needUpdate = true;
                        left = {
                            ...left,
                            referenceVarName: '',
                            varValue: '',
                            referenceVarId: '',
                            referenceVarType: '',
                            referenceNodeId: '',
                        };
                        op = '';
                        right = {
                            ...right,
                            referenceVarName: '',
                            varValue: '',
                            referenceVarId: '',
                            referenceVarType: '',
                            referenceNodeId: '',
                        };
                    }
                    if (right.referenceVarId === varNameId && right.referenceNodeId === nodeId) {
                        needUpdate = true;
                        right = {
                            ...right,
                            referenceVarName: '',
                            varValue: '',
                            referenceVarId: '',
                            referenceVarType: '',
                            referenceNodeId: '',
                        };
                    }
                    return {
                        ...innerItem,
                        left: left,
                        op: op,
                        right: right
                    };
                });
                return {
                    ...item,
                    innerConditions: newInnerConditions
                };
            });
            if (needUpdate) {
                nodeModel.setProperties({
                    ...properties,
                    conditionList: newConditionList,
                });
            }
        } else if (type === 'MEMORY') {
            // 判断写入变量是否被删除/写入变量引用值是否被删除
            let inputVarsUpdated = inputVars.some(item => {
                if (nodeId === '3') {
                    return item.varNameId === varNameId;
                } else {
                    return item.referenceVarId === varNameId
                        && item.referenceNodeId === nodeId;
                }
            });
            // 判断输出变量是否被删除
            let outputVarsUpdated = inputVars.some(item =>
                item.varNameId === varNameId
            );
            // 判断读取的引用变量是否被删除
            let readVarsUpdated = readVars.some(item =>
                item.referenceVarId === varNameId
                && item.referenceNodeId === nodeId
            );
            let updateInputVars;
            let updateReadVars;
            let updateOutputVars;
            // 更新写入变量
            if (inputVarsUpdated) {
                updateInputVars = inputVars.map(item => {
                    // eslint-disable-next-line max-len
                    const { referenceVarId, varNameId: nameId } = item || {};
                    // 更新参数名
                    if (nodeId === '3') {
                        if (nameId === varNameId) {
                            return {
                                ...item,
                                varName: '',
                                varDesc: ''
                            };
                        }
                    } else {
                        // 更新引用值
                        if (referenceVarId === varNameId) {
                            if (referenceVarId === varNameId) {
                                return {
                                    ...item,
                                    referenceVarName: '',
                                    varValue: '',
                                    referenceVarId: '',
                                    referenceVarType: '',
                                    referenceNodeId: '',
                                };
                            }
                        }
                    }
                    return item;
                });
            }
            // 更新读取变量
            if (readVarsUpdated) {
                updateReadVars = readVars.map(item => {
                    const { referenceVarId } = item || {};
                    if (referenceVarId === varNameId) {
                        return {
                            ...item,
                            referenceVarName: '',
                            varValue: '',
                            referenceVarId: '',
                            referenceVarType: '',
                            referenceNodeId: '',
                            referenceTreeData: {},
                        };
                    }
                    return item;
                });
            }
            // 更新输出变量
            if (outputVarsUpdated) {
                updateOutputVars = outputVars.map(item => {
                    // eslint-disable-next-line max-len
                    const { varNameId: nameId } = item || {};
                    if (nodeId === '3') {
                        if (nameId === varNameId) {
                            return {
                                ...item,
                                varName: '',
                                varDesc: ''
                            };
                        }
                    }
                    return item;
                });
            }
            nodeModel.setProperties({
                ...properties,
                inputVars: updateInputVars,
                readVars: updateReadVars,
                outputVars: updateOutputVars
            });
        } else {
            // 寻找目标引用字段，引用id 和节点ID 一致时, 当前值不等于更新值时才更新
            const canDeleted = inputVars.some(item =>
                item.referenceVarId === varNameId && item.referenceNodeId === nodeId
            );
            if (canDeleted) {
                const updateInputs = (vars) => {
                    return vars.map(item => {
                        const { referenceVarId } = item || {};
                        if (referenceVarId === varNameId) {
                            return {
                                ...item,
                                referenceVarName: '',
                                varValue: '',
                                referenceVarId: '',
                                referenceVarType: '',
                                referenceNodeId: '',
                            };
                        }
                        return item;
                    });
                };
                const updateInputVars = updateInputs(inputVars);
                // api 节点需要同时更新 inputVarsAll
                if (inputVarsAll && inputVarsAll.length) {
                    const updateInputVarsAll = updateInputs(inputVarsAll);
                    nodeModel.setProperties({
                        ...properties,
                        inputVars: updateInputVars,
                        inputVarsAll: updateInputVarsAll
                    });
                } else {
                    nodeModel.setProperties({
                        ...properties,
                        inputVars: updateInputVars,
                    });
                }
            }
        }
    });
};
export {
    customAnchorClickEvent,
    customBackEvent,
    deleteALlReference,
    updateReferenceVarNameById,
    deleteReferenceVarNameById
};