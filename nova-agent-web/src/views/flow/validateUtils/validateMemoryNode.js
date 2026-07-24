import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';
import { memorySchemaList } from '@/views/flow/common/modelList';

const validateProcessNode = ({ model = {}, lf }) => {
    console.log('validateProcessNode');
    const processData = model?.properties || {};
    let AllFlatArgs = [];
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [], mod, readVars = [] } = processData;
    if (mod === 'write') {
        AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
        const {
            validateVarsStats,
            varsError
        } =  validateVars({
            varsData: inputVars,
            AllFlatArgs: AllFlatArgs
        });
        validateStatus = validateVarsStats;
        errResult = [...varsError];
    } else {
        AllFlatArgs = memorySchemaList[0].children;
        const {
            validateVarsStats,
            varsError
        } =  validateVars({
            varsData: readVars,
            AllFlatArgs: AllFlatArgs
        });
        validateStatus = validateVarsStats;
        errResult = [...varsError];
    }

    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateProcessNode;