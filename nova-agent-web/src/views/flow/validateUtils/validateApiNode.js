import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateApiNode = ({ model = {}, lf }) => {
    const processData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [], url, debugStatus = 'unPass' } = processData;

    if (inputVars && inputVars.length > 0) {
        const {
            validateVarsStats,
            varsError
        } = validateVars({
            varsData: inputVars,
            AllFlatArgs: AllFlatArgs
        });
        validateStatus = validateVarsStats;
        errResult = [...varsError];
    }
    if (!url || url.length === 0) {
        errResult.push('url缺失');
        validateStatus = false;
    }
    if (!debugStatus || debugStatus === 'unPass') {
        errResult.push('调试未通过，请点击API【发送】按钮调试接口');
        validateStatus = false;
    }

    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateApiNode;