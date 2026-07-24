import { getAllFlatArgs } from '../getArgs';
import { validateVars } from './commonValidate';

const validateKnowledgeNode = ({ model = {}, lf }) => {
    const knowledgeData = model?.properties || {};
    const AllFlatArgs = getAllFlatArgs({ nodeId: model.id, lf });
    // 校验状态
    let validateStatus = true;
    // 错误信息
    let errResult = [];
    const { inputVars = [], knowledgeBaseId = [] } = knowledgeData;
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];

    if (!knowledgeBaseId || knowledgeBaseId.length === 0) {
        errResult.push('知识库缺失');
        validateStatus = false;
    }

    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateKnowledgeNode;