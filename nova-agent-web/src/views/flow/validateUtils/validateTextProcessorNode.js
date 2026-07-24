/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-08-12 19:13:37
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateTextProcessorNode.js
 * @Description: 消息节点校验
 */
import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateMessageNode = ({ model = {}, lf }) => {
    const propertiesData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [], mode = '', template = '' } = propertiesData;
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
    if (mode === 'CONCAT') {
        if (!template) {
            errResult.push('字符串拼接模板缺失');
            validateStatus = false;
        }
    }
    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateMessageNode;