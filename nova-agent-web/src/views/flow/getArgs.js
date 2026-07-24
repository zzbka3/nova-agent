/*
 * @Author: hewenquan
 * @Date: 2025-06-30 14:45:35
 * @LastEditTime: 2025-11-10 10:42:03
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/getArgs.js
 * @Description: 获取参数
 */

import { getNodeMap } from '@/views/flow/basics/flowConfig.js';
// 系统参数
let systemArgs = [];
// 业务线额外参数
let extraArgs = [];

/**
 * @description: 设置初始化参数和业务参数
 * @param {*} parseInfo
 * @param {*} workFlowVarsConfig
 * @return {*}
 */
const setInitInfo = ({ parseInfo, workFlowVarsConfig }) => {
    systemArgs = [parseInfo];
    extraArgs = {
        workFlowVarsConfig
    };
};

/**
 * @description: 操作符号集合
 * @return {*}
 */
const opOptions = [
    {
        label: '等于',
        value: 'EQUAL'
    },
    {
        label: '不等于',
        value: 'NOT_EQUAL'
    },
    {
        label: '长度大于等于',
        value: 'LENGTH_GT_EQUAL'
    },
    {
        label: '长度大于',
        value: 'LENGTH_GT'
    },
    {
        label: '长度小于等于',
        value: 'LENGTH_LT_EQUAL'
    },
    {
        label: '长度小于',
        value: 'LENGTH_LT'
    },
    {
        label: '为空',
        value: 'EMPTY'
    },
    {
        label: '不为空',
        value: 'NOT_EMPTY'
    },
    {
        label: '包含',
        value: 'CONTAINS'
    },
    {
        label: '不包含',
        value: 'NOT_CONTAINS'
    },
    {
        label: '大于',
        value: 'GT'
    },
    {
        label: '大于等于',
        value: 'GT_EQUAL'
    },
    {
        label: '小于',
        value: 'LT'
    },
    {
        label: '小于等于',
        value: 'LT_EQUAL'
    },
];
// 节点输出字段暂存，优化渲染速度和编辑读取变量速度
let tempOutputs = {
    // nodeId: [nodeId, title, children]
};
/**
 * @description: 清空输出缓存map
 * @return {*}
 */
const deleteTempOutputs = () => {
    tempOutputs = {};
};
/**
 * 循环获取父节点的输出参数， Properties 下的 outputVars 属性
 *
 * @returns 如果返回一个空数组，代表当前没有父节点的输出
 * [
 *  {
 *      nodeId: string, // 节点id
 *      title: string, // 节点标题
 *      children: [ // 节点子参数
 *          {
 *              varName: string, // 参数名称
 *              varId: string,
 *              referenceNodeId: string, // 父节点id
 *              canSelect: true, // 当前选项是否可以选择
 *               childrenCanSelect: false, // ArrayObject子级不再可选择
 *          }
 *      ]
 *  }
 * ]
 */
const getAllParentNodeOutputs = ({ nodeId = '', lf = {} } = {}) => {
    if (!nodeId || !lf) {
        return [];
    }
    // 获取当前节点的父节点
    const incomingNodes = lf.getNodeIncomingNode(nodeId) || [];
    // 没有父节点输出参数
    if (!Array.isArray(incomingNodes) || !incomingNodes.length) {
        return [];
    }
    let outputs = [];
    incomingNodes.forEach(mode => {
        // 是否有缓存，有则直接使用缓存，没有则重新获取
        if (tempOutputs[mode.id] && tempOutputs[mode.id].length) {
            outputs.push(...tempOutputs[mode.id]);
        } else {
            let currentNodeOutputs = [];
            const { outputVars = [], nodeName, readOnlyOutputs } = mode.getProperties() || {};
            const cycleChildren = ({ children, parentArgType = '', parentRealNamePath = '' }) => {
                if (!children || !children.length) {
                    return [];
                }
                return children.map(arg => {
                    // 当参数类型是ArrayObject，则不递归处理子节点
                    if ((arg.varNameType || arg.varType) !== 'ArrayObject') {
                        const defaultConfig = {
                            referenceNodeId: mode.id,
                            ...arg,
                            canSelect: true, // 当前选项是否可以选择
                        };
                        // 父节点卫对象是Object类型时，拼接父节点的realNamePath
                        if (['Object', 'ArrayObject'].includes(parentArgType)) {
                            defaultConfig.realNamePath = `${parentRealNamePath}.${arg.varName}`;
                        } else {
                            defaultConfig.realNamePath = arg.realNamePath || arg.varName;
                        }
                        if (arg.children && arg.children.length) {
                            defaultConfig.children = cycleChildren({
                                children: arg.children,
                                parentArgType: arg.varNameType || arg.varType,
                                parentRealNamePath: defaultConfig.realNamePath || parentRealNamePath || arg.varType
                            });
                            return defaultConfig;
                        } else {
                            defaultConfig.childrenCanSelect = false; // Object子级不再可选择
                            return defaultConfig;
                        }
                    } else {
                        const defaultConfig = {
                            ...arg,
                            referenceNodeId: mode.id,
                            canSelect: true, // 当前选项是否可以选择
                            childrenCanSelect: false, // ArrayObject子级不再可选择
                        };
                        // 父节点卫对象是Object类型时，拼接父节点的realNamePath
                        if (['Object', 'ArrayObject'].includes(parentArgType)) {
                            defaultConfig.realNamePath = `${parentRealNamePath}.${arg.varName}`;
                        } else {
                            defaultConfig.realNamePath = arg.realNamePath || arg.varName;
                        }
                        return defaultConfig;
                    }
                });
            };
            // API 类型节点，输出参数为只读输出参数
            let realOutputs = mode.type === 'API' ? readOnlyOutputs : outputVars;
            if (Array.isArray(realOutputs) && realOutputs.length) {
                currentNodeOutputs.push({
                    title: nodeName || getNodeMap(mode.type),
                    key: mode.id + '-' + mode.type,
                    nodeId: mode.id,
                    children: realOutputs.map(item => {
                        return {
                            ...item,
                            children: cycleChildren({
                                children: item.children,
                                parentArgType: item.varNameType || item.varType,
                                parentRealNamePath: item.varName
                            }),
                            referenceNodeId: mode.id,
                        };
                    })
                });
            }
            const parentOut = getParentNodeOutputs({ nodeId: mode.id, lf });
            if (parentOut && parentOut.length) {
                currentNodeOutputs.push(...parentOut);
            }
            outputs.push(...currentNodeOutputs);
            tempOutputs[mode.id] = currentNodeOutputs;
        }
    });
    return [...outputs];
};

/**
 * 循环获取父节点的输出参数, 通过nodeId去重后返回
 *
 * @returns 如果返回一个空数组，代表当前没有父节点的输出
 */
const getParentNodeOutputs = ({ nodeId = '', lf = {} } = {}) => {
    const allOutPuts = getAllParentNodeOutputs({ nodeId, lf });
    let has = {};
    // 通过节点Id 去重
    const filterOutputs = allOutPuts.reduce((pre, cur) => {
        if (!has[cur.nodeId]) {
            pre.push(cur);
            has[cur.nodeId] = true;
        }
        return pre;
    }, []);
    return filterOutputs;
};

/**
 * 获取当前节点可使用的所有参数
 *  返回所有系统参数
 *          默认都可以选择
 *          比较关系使用getAllCanSelectArgs来获取可选择参数
 * @returns [
 *  {
 *      nodeId,
 *      title,
 *      // 输出变量结合
 *      children: [
 *          {
 *              varId,
 *              varName,
 *              varType,
 *              varValue,
 *              canSelect,
 *              children,
 *              realNamePath,
 *              childrenCanSelect,
 *              referenceNodeId
 *          }
 *      ]
 *  }
 * ]
 */
const getAllArgs = ({ addArgs = [], nodeId, lf }) => {
    const cycleChildren = ({ children, parentArgType = '', parentRealNamePath = '' }) => {
        return children.map(arg => {
            // 默认的输出参数
            let defaultConfig = {
                ...arg,
                canSelect: true
            };
            // 父节点卫对象是Object类型时，拼接父节点的realNamePath
            // a.b.c.d.e.....
            if (['Object', 'ArrayObject'].includes(parentArgType)) {
                defaultConfig.realNamePath = `${parentRealNamePath}.${arg.varName}`;
            } else {
                defaultConfig.realNamePath = arg.realNamePath || arg.varName;
            }
            // 当参数类型是 ArrayObject，则不递归处理子节点
            if (arg.varType !== 'ArrayObject') {
                // 存在子节点（api和code节点可以配置Object）
                if (arg.children && arg.children.length) {
                    defaultConfig.children = cycleChildren({
                        children: arg.children,
                        parentArgType: arg.varType,
                        parentRealNamePath: defaultConfig.realNamePath || parentRealNamePath || arg.varName
                    });
                    return defaultConfig;
                } else {
                    return defaultConfig;
                }
            } else {
                // ArrayObject子级不再可选择
                defaultConfig.childrenCanSelect = false;
                return defaultConfig;
            }
        });
    };
    // 默认可选择 系统默认参数 + 自定义新增参数
    let allArgs = [...systemArgs, ...addArgs];
    // 节点存在，获取其所有父节点的输出参数
    if (nodeId) {
        const parentOutputs = getParentNodeOutputs({ nodeId, lf });
        allArgs = [...systemArgs, ...addArgs, ...parentOutputs];
    }
    return allArgs.map(arg => {
        return {
            ...arg,
            children: cycleChildren({ children: arg.children })
        };
    });
};

/**
 * @description: 获取当前节点所有可使用参数的平铺结构
 * @param {*} nodeId
 * @param {*} lf
 * @param {*} filterNodeId 根据来源的节点Id过滤
 * @return {*} [{id, varName, varType, varId}]
 */
const getAllFlatArgs = ({ nodeId, lf, filterNodeId = '' }) => {
    // 获取所有可使用参数
    let allArgs = getAllArgs({ nodeId, lf });
    let result = [];
    const cycleChildren = (children) => {
        return children.map(argConfig => {
            result.push(argConfig);
            // 是否存在子级，子级需要递归
            if (argConfig.children && argConfig.children.length) {
                cycleChildren(argConfig.children);
            }
        });
    };
    // 通过 referenceNodeId 来过滤
    if (filterNodeId) {
        allArgs = allArgs.filter(item => item.nodeId === filterNodeId);
    }
    allArgs.forEach(item => {
        cycleChildren(item.children);
    });
    return result;
};

/**
 * @param {*} varType : 当前节点的参数类型
 * @param {*} opOption 当前字段支持的操作符
 * @param {*} addArgs 支持定义新增参数，不再父级输出范围内
 * @param {*} nodeId 节点ID
 * @param {*} lf
 * @return {*} 获取当前节点所有可以被选择的父级输出参数
 */
const getAllCanSelectArgs = ({ varType, opOption, addArgs = [], nodeId, lf } = {}) => {
    // 主要用于 IF 节点
    // 根据输入的字段类型和操作符，获取当前节点可以被选择的参数
    if (varType && argsRules[varType] && argsRules[varType][opOption]) {
        const { argType, argsMap = [] } = argsRules[varType][opOption];
        // 递归获取所有的子级参数
        const cycleChildren = (children) => {
            return children.map(arg => {
                const outArgs = {
                    ...arg,
                    canSelect: argsMap.includes(arg.varType), // 当前选项是否可以选择
                };
                if (arg.children && arg.children.length) {
                    outArgs.children = cycleChildren(arg.children);
                }
                return outArgs;
            });
        };
        return {
            argType,
            argsMap: getAllArgs({ addArgs, nodeId, lf }).map(arg => {
                return {
                    ...arg,
                    children: cycleChildren(arg.children)
                };
            })
        };
    } else {
        // 没有配置参数/参数的操作符号不再定义范围内，返回当前节点的所有参数
        return {
            argsMap: getAllArgs({ addArgs, nodeId, lf })
        };
    }
};

// 所有参数类型支持的运算符集合
const argsRules = {
    // 输入字段类型
    String: {
        // 比较操作符
        EQUAL: {
            argsMap: ['String'], // 可选参数数据类型集合
            argType: 'String', // 比较变量支持的数据类型，右侧第一个数据选择
        },
        NOT_EQUAL: {
            argsMap: ['String'],
            argType: 'String'
        },
        LENGTH_GT_EQUAL: {
            argsMap: ['String', 'Integer'],
            argType: 'Integer'
        },
        LENGTH_GT: {
            argsMap: ['String', 'Integer'],
            argType: 'Integer'
        },
        LENGTH_LT_EQUAL: {
            argsMap: ['String', 'Integer'],
            argType: 'Integer'
        },
        LENGTH_LT: {
            argsMap: ['String', 'Integer'],
            argType: 'Integer'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
        CONTAINS: {
            argsMap: ['String'],
            argType: 'String'
        },
        NOT_CONTAINS: {
            argsMap: ['String'],
            argType: 'String'
        },
    },
    Integer: {
        EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        NOT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        Empty: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
        GT: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        GT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        LT: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        LT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
    },
    Number: {
        EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        NOT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        Empty: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
        GT: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        GT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        LT: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
        LT_EQUAL: {
            argsMap: ['Integer', 'Number'],
            argType: 'Integer'
        },
    },
    Boolean: {
        Empty: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
        EQUAL: {
            argsMap: ['Boolean'],
            argType: 'Boolean'
        },
        NOT_EQUAL: {
            argsMap: ['Boolean'],
            argType: 'Boolean'
        },
    },
    ArrayString: {
        CONTAINS: {
            argsMap: ['String'],
            argType: 'String'
        },
        NOT_CONTAINS: {
            argsMap: ['String'],
            argType: 'String'
        },
        LENGTH_GT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_GT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
    },
    ArrayInteger: {
        CONTAINS: {
            argsMap: ['Integer'],
            argType: 'Integer'
        },
        NOT_CONTAINS: {
            argsMap: ['Integer'],
            argType: 'Integer'
        },
        LENGTH_GT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_GT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
    },
    ArrayNumber: {
        CONTAINS: {
            argsMap: ['Number'],
            argType: 'Number'
        },
        NOT_CONTAINS: {
            argsMap: ['Number'],
            argType: 'Number'
        },
        LENGTH_GT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_GT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT_EQUAL: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        LENGTH_LT: {
            argsMap: ['Integer', 'ArrayString', 'ArrayInteger', 'ArrayNumber', 'ArrayBoolean', 'ArrayObject'],
            argType: 'Integer'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
    },
    ArrayObject: {
        LENGTH_GT_EQUAL: {
            argsMap: ['ArrayObject', 'ArrayInteger', 'ArrayString'],
            argType: 'Integer'
        },
        LENGTH_GT: {
            argsMap: ['ArrayObject', 'ArrayInteger', 'ArrayString'],
            argType: 'Integer'
        },
        LENGTH_LT_EQUAL: {
            argsMap: ['ArrayObject', 'ArrayInteger', 'ArrayString'],
            argType: 'Integer'
        },
        LENGTH_LT: {
            argsMap: ['ArrayObject', 'ArrayInteger', 'ArrayString'],
            argType: 'Integer'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        },
    },
    Object: {
        CONTAINS: {
            argsMap: ['String'],
            argType: 'String'
        },
        NOT_CONTAINS: {
            argsMap: ['String', 'Number', 'Boolean', 'Integer'],
            argType: 'String'
        },
        EMPTY: {
            argsMap: [],
            argType: ''
        },
        NOT_EMPTY: {
            argsMap: [],
            argType: ''
        }
    }
};

/**
 * 获取当前入参可以选择的条件关系
 *
 * @param {{argType: string}} options - 参数类型
 * @returns string[] - 返回当前参数类型可以选择的条件关系
 */
const getArgOptions = ({ varType }) => {
    const targetOptions = argsRules[varType];
    if (!targetOptions) {
        return [];
    }
    const targetOptionsKeys = Object.keys(targetOptions);
    return opOptions.filter(item => targetOptionsKeys.includes(item.value));
};

export {
    systemArgs,
    getParentNodeOutputs,
    getAllArgs,
    getAllFlatArgs,
    getAllCanSelectArgs,
    opOptions,
    getArgOptions,
    argsRules,
    setInitInfo,
    extraArgs,
    deleteTempOutputs
};