/*
 * @Author: hewenquan
 * @Date: 2025-07-23 10:16:41
 * @LastEditTime: 2025-09-09 14:48:57
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/validateUtils/validateCodeNode.js
 * @Description: 代码节点校验
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
    const { inputVars = [], codeData = '', outputVars = [] } = propertiesData;
    // 输入参数校验
    const {
        validateVarsStats,
        varsError
    } =  validateVars({
        varsData: inputVars,
        AllFlatArgs: AllFlatArgs
    });
    validateStatus = validateVarsStats;
    errResult = [...varsError];
    // 代码校验
    if (!codeData) {
        errResult.push('代码缺失');
        validateStatus = false;
    }
    if (!outputVars || !outputVars.length) {
        errResult.push('输出参数缺失');
        validateStatus = false;
    } else {
        const flatOutputs = [];
        const cycleChildren = (children) => {
            return children.map(argConfig => {
                if (argConfig.children && argConfig.children.length) {
                    flatOutputs.push(argConfig);
                    cycleChildren(argConfig.children);
                } else {
                    flatOutputs.push(argConfig);
                }
            });
        };
        outputVars.forEach(item => {
            flatOutputs.push(item);
            if (item.children && item.children.length) {
                cycleChildren(item.children);
            }
        });
        flatOutputs.forEach(item => {
            const {varName = '', varType = ''} = item;
            if (!varName || varName.length === 0) {
                varsError.push('参数名缺失');
                validateStatus = false;
            }
            if (!varType) {
                varsError.push('参数类型缺失');
                validateStatus = false;
            }
        });
    }
    return {
        validateStatus: validateStatus,
        errResult: errResult
    };
};

export default validateMessageNode;