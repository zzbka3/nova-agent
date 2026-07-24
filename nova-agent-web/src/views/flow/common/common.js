/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-07 18:50:21
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-09-08 10:48:42
 * @FilePath: /metis-front/src/views/flow/common/common.js
 * @Description: 公用方法
 */
/**
 * 处理树形数据
 * @param {rawData} rawData 原始数据
 * @param {h} h 渲染函数
 * @return {*}
*/
const processTreeData = (rawData, h) => {
    return rawData.map(group => ({
        ...group,
        disabled: true,
        children: processChildren(group.children, h)
    }));
};
/**
 * 处理树形数据
 * @param {children} children 子节点
 * @param {h} h 渲染函数
 * @return {*}
*/
const processChildren = (children, h) => {
    return children.map(item => {
      const newItem = {
        ...item,
        disabled: item.canSelect === false,
        title: renderTitle(h, item.varName, item.originalVarType || item.varType),
        key: item.referenceNodeId ?
            item.referenceNodeId + '___' + (item.realNamePath || item.varName) : (item.realNamePath || item.varName)
      };
      if (item.children && item.children.length > 0) {
        newItem.children = processChildren(item.children, h);
      }
      return newItem;
    });
};

/**
 * 渲染树形数据标题
 * @param {h} h 渲染函数
 * @param {title} title 标题
 * @param {type} type 类型
 * @return {*}
 */
const renderTitle = (h, title, type) => {
    return h(
        'span',
        {
            style: {
                width: '100%',
            },
        },
        [
            h('span', {}, title),
            h(
                'span',
                {
                    style: {
                        marginLeft: '5px',
                        padding: '3px 5px',
                        color: '#B0B2B6',
                        backgroundColor: '#E8E9EB',
                        borderRadius: '3px',
                        fontWeight: '400',
                    },
                    class: {
                        'arg-type': true,
                    }
                },
                type
            ),
        ]
    );
};

/**
 * @description: 递归序列化json对象为最终提交数据结构
 * @param {object/array} array 需要递归的对象（数组）
 * @return {*}
 * */
const replaceMockValues = (array) => {
    return array.map((item) => {
        // 创建新对象（保持原对象不变性）
        let newItem = {
            field: item.field,
            type: item.type,
            mockValue: item.mockValue,
            id: item.id
        };

        // 由于子集中object和array类型，只会出现在父级是object的可能，且array不会有子集，所以只处理object和array
        if (item.type === 'Object' || item.type.includes('array')) {
            let mockValue = JSON.stringify(item.children);
            newItem.mockValue = mockValue;
        }

        return newItem;
    });
};

/**
 * 防抖
 * @param {object} debounce
 */
const debounce = (fn, delay) => {
    let timer = null; // 声明计时器
    return function () {
        let context = this;
        let args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    };
};

/**
* 检查引用变量名是否被删除，如果被删除替换为空
*
* @param {Object} param - 包含输入变量和所有扁平化参数的对象
* @param {Array} param.inputVars - 输入变量的数组
* @param {Array} param.allFlatArgs - 所有扁平化参数的数组
*/
const checkReferenceVarName = ({inputVars, allFlatArgs}) => {
    const result = [];
    if (!inputVars || !Array.isArray(inputVars) || !inputVars.length) {
        return result;
    }
    inputVars.forEach(inputVar => {
        const { varType, referenceVarName, referenceNodeId } = inputVar || {};
        // 如果变量类型不是引用类型，则直接添加到结果中
        if (varType !== 'reference') {
            result.push(inputVar);
        } else {
            // 引用类型
            const flatArg = allFlatArgs.find(arg => {
                const {varName, realNamePath} = arg || {};
                return referenceNodeId === arg.referenceNodeId &&
                    (realNamePath === referenceVarName || varName === referenceVarName);
            });
            // 如果找到，则不需要将referenceVarName 等置空
            if (flatArg) {
                result.push(inputVar);
            } else {
                result.push({
                    ...inputVar,
                    referenceNodeId: '',
                    referenceVarName: null,
                    referenceVarType: null,
                    varValue: null
                });
            }
        }
    });
    return result;
};

        // 生成唯一值
const uniqueValue = () => {
    return Date.now().toString(36) + Math.random().toString(36);
};

/**
* 检查传入的值是否为空。
*
* @param value 要检查的值。
* @returns 如果值为空（空字符串、null或undefined），则返回true；否则返回false。
*/
const isEmpty = (value) => {
    if (value === '') {
        return true;
    }
    if (value === null) {
        return true;
    }
    if (value === undefined) {
        return true;
    }
    return false;
};

export {
    processTreeData,
    debounce,
    replaceMockValues,
    checkReferenceVarName,
    uniqueValue,
    isEmpty
};