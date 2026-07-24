/*
 * @Author: v_liuhaohao01 v_liuhaohao01@baidu.com
 * @Date: 2025-07-11 19:24:53
 * @LastEditors: hewenquan
 * @LastEditTime: 2025-10-29 15:25:12
 * @FilePath: /metis-front/src/views/flow/common/modelList.js
 * @Description: 获取模型列表
 */

import { supportModels } from '@/views/flow/apiList.js';
import { flowRequest } from './request.js';
import router from '@/router';
let modelList = [];
let storageProductLine = null;
let categoryModelList = [];
let fetchingPromise = null;
async function getModelList(needCategory = false) {
    const { productLine } = router?.app?._route?.params || {};
    // 同产品线下，如果已有缓存，直接返回
    if (storageProductLine === productLine && modelList.length > 0) {
        return needCategory ? categoryModelList : modelList;
    }
    // 如果正在请求中，等待同一个 Promise
    if (fetchingPromise) {
        await fetchingPromise;
        return needCategory ? categoryModelList : modelList;
    }
    // 发起请求
    fetchingPromise = flowRequest({
        url: supportModels,
        method: 'get'
    }).then((data) => {
        modelList = data;
        storageProductLine = productLine;
        // 模型分类 推荐和默认
        categoryModelList = [{
            modelName: '全部',
            children: data.filter(item => item?.category !== 'recommended')
        }];
        const recommendedModel = data.filter(item => item?.category === 'recommended');
        if (recommendedModel.length) {
            categoryModelList.unshift({
                modelName: '推荐模型',
                children: recommendedModel
            });
        }
    }).catch(() => {
        this.$message.error('获取 agent 详情失败, 请重试');
    }).finally(() => {
        fetchingPromise = null;
    });
    await fetchingPromise;
    return needCategory ? categoryModelList : modelList;
}
let memorySchemaList = [];
const setMemorySchemaList = (list) => {
    memorySchemaList = [{
        title: '记忆变量',
        nodeId: '3',
        key: 'memorySchema',
        disabled: true,
        children: list.map(item => ({
            ...item,
            referenceNodeId: '3'
        })),
    }];
};
export {
    getModelList,
    setMemorySchemaList,
    memorySchemaList
};