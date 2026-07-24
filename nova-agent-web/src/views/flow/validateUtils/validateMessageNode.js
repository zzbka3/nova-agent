/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-09-09 14:48:56
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateMessageNode.js
 * @Description: 消息节点校验
 */
import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateMessageNode = ({ model = {}, lf }) => {
    console.log('validateMessageNode');
    const messageData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [], mode = 'directVar', answerTemplate = '' } = messageData;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
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

export default validateMessageNode;