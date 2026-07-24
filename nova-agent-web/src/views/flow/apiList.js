/*
 * @Author: hewenquan
 * @Date: 2025-07-11 14:23:14
 * @LastEditTime: 2026-04-23 10:48:47
 * @LastEditors: v_yangxing06 v_yangxing06@baidu.com
 * @FilePath: /metis-front/src/views/flow/apiList.js
 * @Description: 接口列表
 */
// agent 列表
const getAgentList = '/api/agent-manager/proxy/v1/agent/list';
// 新建会话
const conversation = '/api/agent-manager/proxy/v1/agent/conversation';
// 校验agent
const validate = '/api/agent-manager/proxy/v1/agent/validate';
// 执行状态查询接口
const runningInfo = '/api/agent-manager/proxy/v1/agent/runningInfo';
// 调试对话
const talk = '/api/agent-manager/proxy/v1/agent/talk';
// 文件上传
const fileUpload = '/api/agent-manager/proxy/v1/agent/fileUpload';
// 获取详情
const getAgentDetail = '/api/agent-manager/proxy/v1/agent/detail';
// 保存接口
const saveAgent = '/api/agent-manager/proxy/v1/agent/save';
// 发布
const publish = '/api/agent-manager/proxy/v1/agent/publish';
// 复制
const copyAgent = '/api/agent-manager/proxy/v1/agent/copy';
// 导出
const exportAgent = '/api/agent-manager/proxy/v1/agent/export';
// 导入
const importAgent = '/api/agent-manager/proxy/v1/agent/import';
// 删除
const deleteAgent = '/api/agent-manager/proxy/v1/agent/delete';
// API调试
const apiDebug = '/api/agent-manager/proxy/v1/agent/apiDebug';
// 大模型列表查询
const supportModels = '/api/agent-manager/proxy/v1/agent/listSupportModels';
// API节点请求JSON格式校验
const verifyRequestJsonSchema = '/api/agent-manager/proxy/v1/agent/verifyRequestJsonSchema';
// API节点响应JSON格式校验
const verifyResponseJsonSchema = '/api/agent-manager/proxy/v1/agent/verifyResponseJsonSchema';
// 获取初始化信息
const initInfo = '/api/agent-manager/proxy/v1/agent/initInfo';
// 知识库 列表
// const knowledgeList = '/api/agent-manager/proxy/rc/agent/knowledge/list';
const knowledgeList = '/data/accessCfg/knowledge/list';
// 代码节点调试
const codeDebug = '/api/agent-manager/proxy/v1/agent/codeDebug';

// 校验流程agent 是否可以添加
const verifyWorkFlow = '/api/agent-manager/proxy/v1/agent/verify';
const streamTalk = '/api/agent-manager/proxy/stream/v1/agent/stream-talk';
// 获取模型列表
const getAutonomyAgentModels = '/api/agent-manager/proxy/v1/agent/listSupportModelsForIndependentPlanning';
export {
    getAgentList,
    conversation,
    validate,
    runningInfo,
    talk,
    fileUpload,
    getAgentDetail,
    saveAgent,
    publish,
    copyAgent,
    exportAgent,
    importAgent,
    deleteAgent,
    apiDebug,
    supportModels,
    verifyRequestJsonSchema,
    verifyResponseJsonSchema,
    initInfo,
    knowledgeList,
    codeDebug,
    verifyWorkFlow,
    streamTalk,
    getAutonomyAgentModels
};