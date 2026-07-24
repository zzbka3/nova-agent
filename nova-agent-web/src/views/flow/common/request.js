/*
 * @Author: hewenquan
 * @Date: 2025-07-17 19:19:40
 * @LastEditTime: 2025-07-29 15:30:42
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/common/request.js
 * @Description: agent request
 */
import request from '@/utils/http';
import router from '@/router';
const flowRequest = (args) => {
    const { params = {} } = router.app._route || {};
    // 添加自定义请求头
    let headers = {};
    // 请求头添加产品线id
    if (params && params.productLine) {
        headers['PRODUCT-LINE-ID'] = params.productLine;
    }
    return request(args, headers);
};
export {
    flowRequest
};