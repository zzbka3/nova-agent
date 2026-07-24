/*
 * @Author: hewenquan
 * @Date: 2025-06-13 10:11:14
 * @LastEditTime: 2025-12-09 10:32:21
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @FilePath: /metis-front/src/views/flow/basics/flowConfig.js
 * @Description: config
 */

// 原始节点
const originalNode = [
    {
        desc: '开始/结束',
        children: [
            {
                text: '开始',
                type: 'START',
                icon: require('@/views/flow/image/start.png')
            },
            {
                type: 'END',
                text: '结束',
                icon: require('@/views/flow/image/end.png')
            }
        ]
    }
];
// 可配置节点
const nodeList = [
    {
        desc: '工具引入',
        children: [
            {
                type: 'API',
                text: 'API',
                icon: require('@/views/flow/image/api.png')
            },
            {
                type: 'WORKFLOW_AGENT',
                text: '工作流',
                icon: require('@/views/flow/image/agent.png')
            }
        ]
    },
    {
        desc: '业务逻辑',
        children: [
            {
                text: '分支器',
                type: 'IF',
                icon: require('@/views/flow/image/branch.png')
            },
            {
                type: 'INTENT',
                text: '意图识别',
                icon: require('@/views/flow/image/intention.png')
            },
            {
                type: 'CODE',
                text: '代码',
                icon: require('@/views/flow/image/code.png')
            },
        ]
    },
    {
        desc: '信息&知识',
        children: [
            {
                type: 'KNOWLEDGE',
                text: '知识库',
                icon: require('@/views/flow/image/knowledge.png')
            },
            {
                type: 'LLM',
                text: '大模型',
                icon: require('@/views/flow/image/largeModel.png')
            },
            {
                type: 'TEXT_PROCESSOR',
                text: '文本处理',
                icon: require('@/views/flow/image/text.png')
            },
            // {
            //     type: 'REWRITE',
            //     text: 'query多轮改写',
            //     icon: require('@/views/flow/image/rewrite.png')
            // },
            {
                type: 'WORKFLOW',
                text: '流程组件',
                icon: require('@/views/flow/image/workFlow.png')
            },
            {
                type: 'MEMORY',
                text: '记忆变量',
                icon: require('@/views/flow/image/memory.png')
            },
        ]
    },
    {
        desc: '输入&输出',
        children: [
            {
                type: 'MESSAGE',
                text: '消息节点',
                icon: require('@/views/flow/image/message.png')
            }
        ]
    }
];
// 工作流Agent配置默认值
const defaultNodeData = {
    'nodes': [
        {
            'id': '1',
            'type': 'START',
            'x': 471,
            'y': 400,
            'properties': {
                'width': 360,
                'height': 300,
                'nodeName': '开始'
            }
        },
        {
            'id': '2',
            'type': 'END',
            'x': 1435,
            'y': 400,
            'properties': {
                'width': 400,
                'height': 150,
                'nodeName': '结束',
            }
        }
    ],
    'edges': [
    ]
};
// 自主规划Agent配置默认值
const defaultAutonomyAgent = {
    'promptText': '',
    'promptType': 0,
    'temperature': 0.01,
    'modelNames': 'ernie-4.5-turbo-128k',
    'varList': []
};
/**
 * 获取节点映射
 *
 * @returns 返回节点映射对象
 */
const getNodeMap = (nodeType) => {
    let nodeMap = {};
    [...originalNode, ...nodeList].forEach(item => {
        item.children.forEach(child => {
            nodeMap[child.type] = child.text;
        });
    });
    if (nodeType) {
        return nodeMap[nodeType] || '未知节点类型';
    } else {
        return nodeMap;
    }
};

/**
 * @description: 流程组件映射
 * @return {*}
 */
const workFlowMap = {
    materialId: '物料卡',
    imageUrlList: '图片链接',
    videoList: '视频链接',
    standardId: '标准问id',
    standardAnswerId: '标准问答案id',
    turnToManualFlow: '人工客服',
    openOrderList: '调起订单',
    openWordList: '调起词条',
    showEvaluate: '邀评',
    showHalf: '弹起浮层',
    sugStandardId: '标准问id',
    sugList: '气泡json配置',
    questionCategoryIds: '打标-产品问题类型',
    agentContainerAutoSwitchBuzName: 'bot'
};
export {
    defaultNodeData,
    nodeList,
    getNodeMap,
    originalNode,
    workFlowMap,
    defaultAutonomyAgent
};