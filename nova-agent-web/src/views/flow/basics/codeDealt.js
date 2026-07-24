/*
 * @Author: hewenquan
 * @Date: 2025-08-11 16:01:37
 * @LastEditTime: 2025-11-25 16:32:12
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/basics/codeDealt.js
 * @Description: 节点默认数据
 */

import { uniqueValue } from '@/views/flow/common/common';

// 启动节点业务字段mock值配置
const startMockVars = () => [
    {
        'varName': 'end_user_id',
        'varNameAbbr': 'end_user_id',
        'varType': 'String',
        'id': 'end_user_id',
        'varValue': ''
    },
    {
        'varName': 'paramObject.imSessionId',
        'varNameAbbr': 'imSessionId',
        'varType': 'String',
        'id': 'imSessionId',
        'varValue': ''
    },
    {
        'varName': 'paramObject.talkType',
        'varNameAbbr': 'talkType',
        'varType': 'String',
        'id': 'talkType',
        'varValue': 'NORMAL_TALK',
        'selectOptions': [
            {
                label: 'NORMAL_TALK(会话类型)',
                value: 'NORMAL_TALK'
            },
            {
                label: 'EVENT_TALK(事件类型)',
                value: 'EVENT_TALK'
            },
            {
                label: 'SESSION_EVALUATE(用户评价)',
                value: 'SESSION_EVALUATE'
            }
        ]
    },
    {
        'varName': 'paramObject.showName',
        'varNameAbbr': 'showName',
        'varType': 'String',
        'id': 'showName',
        'varValue': ''
    },
    {
        'varName': 'paramObject.talkContext.orderId',
        'varNameAbbr': 'orderId',
        'varType': 'String',
        'id': 'orderId',
        'varValue': ''
    },
    // biz Id
    {
        'varName': 'paramObject.talkContext.baiduAppOrderInfo.BizID',
        'varNameAbbr': 'BizID',
        'varType': 'Number',
        'id': 'BizID',
        'varValue': ''
    },
];
// code 节点输入默认数据
const codeDefaultInputVars = () => [
    {
        'varName': 'city',
        'varType': 'String',
        'id': uniqueValue(),
        'varValue': 'beijing'
    },
    {
        'varName': 'days',
        'varType': 'Integer',
        'id': uniqueValue(),
        'varValue': 3,
    },
    {
        'varName': 'weather',
        'varType': 'String',
        'id': uniqueValue(),
        'varValue': 'snow',
    }
];
// code 节点输出默认数据
const codeDefaultOutputVars = () => [
    {
        'varName': 'key0',
        'varType': 'String',
        'id': uniqueValue(),
        'zIndex': 1,
    },
    {
        'varName': 'key1',
        'varType': 'String',
        'id': uniqueValue(),
        'zIndex': 1,
    },
    {
        'varName': 'key2',
        'varType': 'ArrayNumber',
        'id': uniqueValue(),
        'zIndex': 1,
    },
];

// code 节点代码默认数据
// eslint-disable-next-line max-len
const codeData = '# 定义一个 main 函数，传入 params 参数。params 中包含了节点配置的输入变量。\n# 需要定义一个字典作为输出变量\n# 引用节点定义的变量：params[\'变量名\']\n# 运行环境 Python3；预置 Package：NumPy\n\ndef main(params):\n\n    # 创建一个字典作为输出变量\n    output_object ={\n    \n        # 引用节点定义的 city 变量\n        "key0":params[\'city\'],\n        \n        # 引用节点定义的 weather，city 和 days 变量\n        # 拼接为字符串赋值到 ”key1“ \n        "key1":"it will " + params[\'weather\'] + " in " + params[\'city\'] + " in the next " + str(params[\'days\']) + " days",\n        \n        # 为 “key2” 赋值一个浮点列表\n        "key2":[1.1, 2.1, 3.0],\n    }\n    \n    # 返回输出字典类型变量 output_object，包含代码节点所需的输出数据\n    return output_object';

// 文本处理节点输入默认数据
const textProcessorInput = () => [
    {
        'varName': 'String1',
        'varType': 'reference',
        'id': uniqueValue(),
        'varValue': null,
        'referenceNodeId': '',
        'referenceVarName': '',
        'referenceVarType': '',
    }
];

// 文本处理节点输出默认数据
const textProcessorOutPut = () => [
    {
        varName: 'output',
        varType: 'String',
        id: uniqueValue(),
    }
];

// 文本处理节点输出默认数据
const rewriteOutPut = () => [
    {
        varName: 'rewrite_query',
        varType: 'String',
        id: uniqueValue(),
    }
];

// 工作流默认输出字段
const workFlowOutPut = () => [
    {
        varName: 'answer',
        varType: 'String',
        id: uniqueValue(),
    }
];
export {
    startMockVars,
    codeDefaultInputVars,
    codeDefaultOutputVars,
    codeData,
    textProcessorOutPut,
    textProcessorInput,
    rewriteOutPut,
    workFlowOutPut
};