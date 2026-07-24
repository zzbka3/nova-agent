/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-08-14 14:14:51
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateRewriteNode.js
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
    const { inputVars = [], temperature, rewriteType } = propertiesData;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    if (temperature < 0) {
        validateStatus = false;
        errResult.push('多样性不能为空');
    }
    if (rewriteType < 0) {
        validateStatus = false;
        errResult.push('rewrite_type不能为空');
    }
    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateMessageNode;