/*
 * @Author: hewenquan
 * @Date: 2025-06-26 19:17:14
 * @LastEditTime: 2025-07-28 11:29:31
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/registerFlowNode/commonUtils.js
 * @Description: 通用的工具函数
 */

/**
 * @description: 设置锚点的样式
 * @param {*} style
 * @return {*}
 */
const setAnchorStyle = (style) => {
    // #84868c
    style.stroke = 'rgb(24, 125, 255)';
    style.fill = 'rgb(24, 125, 255)';
    style.r = 3;
    style.hover.r = 8;
    style.hover.fill = 'rgb(24, 125, 255)';
    style.hover.stroke = 'rgb(24, 125, 255)';
    return style;
};

/**
 * @description: 起始节点连线规则，左侧不能作为连线的起点规则
 * @return {*}
 */
const connectedSourceRules = {
    message: '左侧不能作为连线的起点',
    validate: (
        source,
        target,
        sourceAnchor,
    ) => {
        // 报错
        let isValid = true;
        if (sourceAnchor?.name === 'left') {
            isValid = false;
        }
        return isValid;
    }
};
/**
 * @description: 目标节点规则。右侧不能作为连线的终点
 * @return {*}
 */
const connectedTargetRules = {
    message: '节点右侧不能作为连线的终点',
    validate: (
        source,
        target,
        sourceAnchor,
        targetAnchor
    ) => {
        console.log(source, target, sourceAnchor, targetAnchor, 'targetAnchor');
        // 报错
        let isValid = true;
        if (sourceAnchor?.name === 'left') {
            isValid = false;
        }
        if (targetAnchor?.name === 'right') {
            isValid = false;
        }
        return isValid;
    }
};
const intentionOutputList = [
    { varName: 'thought', varType: 'String', varDesc: '意图识别的模型思考过程' },
    { varName: 'classification', varType: 'String', varDesc: '识别的对应意图' },
    { varName: 'classificationID', varType: 'String', varDesc: '识别的对应意图的序号' },
];
// 知识库保存配置字段
const largeModelOutputList = [
    {
        varName: 'OutputList',
        varType: 'ArrayObject',
        varDesc: ''
    }
];
// 知识库输出显示字段
const largeModelViews = [
    {
        varName: 'OutputList',
        varType: 'ArrayObject',
        varDesc: '',
        expanded: false,
        children: [
            { varName: 'segment_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.segment_id' },
            { varName: 'document_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.document_id' },
            { varName: 'dataset_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.dataset_id' },
            { varName: 'score', varType: 'Number', varDesc: '', realNamePath: 'OutputList.score' },
            { varName: 'content', varType: 'String', varDesc: '', realNamePath: 'OutputList.content' },
            { varName: 'document_name', varType: 'String', varDesc: '', realNamePath: 'OutputList.document_name' },
            { varName: 'dataset_name', varType: 'String', varDesc: '', realNamePath: 'OutputList.dataset_name' },
            { varName: 'word_count', varType: 'Integer', varDesc: '', realNamePath: 'OutputList.word_count' },
            // eslint-disable-next-line max-len
            { varName: 'original_chunk_id', varType: 'String', varDesc: '', realNamePath: 'OutputList.original_chunk_id' },
            // eslint-disable-next-line max-len
            { varName: 'original_chunk_offset', varType: 'Number', varDesc: '', realNamePath: 'OutputList.original_chunk_offset' },
            { varName: 'url', varType: 'String', varDesc: '', realNamePath: 'OutputList.url' },
        ]
    },
];
export {
    setAnchorStyle,
    connectedSourceRules,
    connectedTargetRules,
    intentionOutputList,
    largeModelOutputList,
    largeModelViews
};