/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-09-17 10:47:00
 * @LastEditors: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateLargeModelNode.js
 * @Description: 大模型校验
 */
import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateLargeModelNode = ({ model = {}, lf }) => {
    const largeModelData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const {
        inputVars = [],
        outputVars = [],
        model: largeModel = null,
        userPrompt = '',
        systemPrompt = ''
    } = largeModelData;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    if (!largeModel || largeModel.length === 0) {
        errResult.push('请选择模型');
        validateStatus = false;
    }

    if (!userPrompt || userPrompt.length === 0) {
        errResult.push('用户提示词缺失');
        validateStatus = false;
    }
    if (!systemPrompt || systemPrompt.length === 0) {
        errResult.push('系统提示词缺失');
        validateStatus = false;
    }
    if (outputVars && outputVars.length > 0) {
        outputVars.forEach(element => {
            // 参数名校验
            if (!element?.varName || element?.varName.length === 0) {
                errResult.push('输出参数名缺失');
                validateStatus = false;
            }
            if (element?.varName) {
                const reg = /^[a-zA-Z][a-zA-Z0-9_-]*$/;
                if (!reg.test(element?.varName)) {
                    errResult.push(`输出参数名${element?.varName}格式不正确, 只能输入字母、数字、"_"、"-"，并以字母开头`);
                    validateStatus = false;
                }
            }
            if (!element?.varType) {
                errResult.push('输出参数类型缺失');
                validateStatus = false;
            }
        });
    }

    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateLargeModelNode;