/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-10-28 10:39:12
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateWorkFlowNode.js
 * @Description: 工作流节点校验
 */
import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateWorkFlowNode = ({ model = {}, lf }) => {
    console.log('validateWorkFlowNode');
    const messageData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [] } = messageData;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateWorkFlowNode;