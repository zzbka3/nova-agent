/*
 * @Author: hewenquan
 * @Date: 2025-07-03 16:52:40
 * @LastEditTime: 2025-08-19 15:12:17
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateEndNode.js
 * @Description: 校验结束节点
 */

import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateEndNode = ({ model = {}, lf }) => {
    const endConfig = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    // 输出参数校验
    const { inputVars = [], mode = 'directVar', answerTemplate = '' } = endConfig;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    // console.log(validateStatus, errResult, 'endNode');
    // 校验消息模版
    if (mode === 'template') {
        if (!answerTemplate) {
            errResult.push('回答模板缺失');
            validateStatus = false;
        }
    }
    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateEndNode;