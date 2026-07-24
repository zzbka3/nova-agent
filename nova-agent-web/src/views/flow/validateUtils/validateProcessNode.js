import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateProcessNode = ({ model = {}, lf }) => {
    console.log('validateProcessNode');
    const processData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [] } = processData;
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

export default validateProcessNode;