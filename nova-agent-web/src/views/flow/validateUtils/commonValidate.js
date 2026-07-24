/*
 * @Author: hewenquan
 * @Date: 2025-07-18 11:27:22
 * @LastEditTime: 2025-10-16 17:15:42
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/commonValidate.js
 * @Description: 通用的校验方法
 */
/**
 * 验证变量配置的有效性
 *
 * @param {Object} varsConfig - 变量配置对象
 * @param {Array} [varsConfig.varsData=[]] - 变量数据数组
 * @param {Array} [varsConfig.AllFlatArgs=[]] - 所有扁平化参数数组
 * @returns {void}
 */
const validateVars = (varsConfig) => {
    // 校验状态
    let validateVarsStats = true;
    // 错误信息
    let varsError = [];
    const { varsData = [], AllFlatArgs = [] } = varsConfig || {};
    if (!Array.isArray(varsData) || varsData.length === 0) {
        varsError.push('变量配置缺失');
        validateVarsStats = false;
    } else {
        varsData.forEach(element => {
            const { varName, varValue, referenceVarName, varType, referenceNodeId } = element || {};
            // 参数名校验
            if (!varName || varName.length === 0) {
                varsError.push('参数名不能为空');
                validateVarsStats = false;
            }
            if (varName) {
                const reg = /^[a-zA-Z][a-zA-Z0-9_-]*$/;
                if (!reg.test(varName)) {
                    varsError.push(`参数名${varName}格式不正确, 只能输入字母、数字、"_"、"-"，并以字母开头`);
                    validateVarsStats = false;
                }
            }
            // 引用类型校验
            if (varType === 'reference') {
                // 非空校验
                if (!referenceVarName || referenceVarName.length === 0) {
                    varsError.push(`参数 ${varName || '<未命名参数>'} 的引用参数不能为空`);
                    validateVarsStats = false;
                }
                if (referenceVarName && referenceNodeId) {
                    const findTarget = AllFlatArgs.filter(item => {
                        const { varName, realNamePath } = item || {};
                        return referenceVarName === varName || referenceVarName === realNamePath;
                    });
                    // 引用字段是否在可选变量中校验
                    if (!findTarget || !findTarget.some(item => item.referenceNodeId === referenceNodeId)) {
                        varsError.push(`参数${varName || '<未命名参数>'}的引用字段不存在`);
                        validateVarsStats = false;
                    }
                }
            } else {
                // 非引用类型校验非空
                if (!varValue || varValue?.length === 0) {
                    varsError.push(`参数 ${varName || '<未命名参数>'}的 ${varType} 值不能为空`);
                    validateVarsStats = false;
                }
            }
        });
    }
    return {
        validateVarsStats,
        varsError
    };
};

export {
    validateVars
};