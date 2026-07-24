<!--
 * @Author: hewenquan
 * @Date: 2025-07-09 11:02:44
 * @LastEditTime: 2025-11-12 17:25:20
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/apiConfig/index.vue
 * @Description: api 配置
-->
<template>
    <div class="api-config">
        <div class="api-base">
            <div class="api-title">
                基本信息
            </div>
            <div class="api-base-desc">
                <div class="base-desc-item">
                    <div class="base-label">
                        接口地址
                    </div>
                    <a-tooltip
                        :title="urlValue"
                    >
                        <div class="base-value">
                            {{ urlValue || '未填写' }}
                        </div>
                    </a-tooltip>
                </div>
                <div class="base-desc-item">
                    <div class="base-label">
                        请求方式
                    </div>
                    <div
                        class="base-value"
                        :style="{ color: methodColor }"
                    >
                        {{ method }}
                    </div>
                </div>
                <div class="base-desc-item">
                    <div class="base-label">
                        鉴权方式
                    </div>
                    <div class="base-value">
                        {{ authType === 'NO_AUTH' ? '无需鉴权' : authType || '未选择' }}
                    </div>
                </div>
                <div class="base-desc-item">
                    <div class="base-label">
                        调试状态
                    </div>
                    <div
                        class="base-value"
                        :class="{ 'un-pass': debugStatus === 'unPass', 'pass': debugStatus === 'pass' }"
                    >
                        {{ debugStatus === 'unPass' ? '未通过' : debugStatus === 'pass' ? '通过' : '未调试' }}
                    </div>
                </div>
            </div>
            <div
                class="edit-api-btn"
                @click="editApi"
            >
                编辑API
            </div>
        </div>
        <div class="input-container">
            <div>
                <InputComponents
                    ref="editApiInput"
                    :var-type-options="varTypeOptions"
                    :input-value-options="arrArgs"
                    :all-flat-args="allFlatArgs"
                    :input-data="requestVars"
                    :input-tooltip="inputTooltip"
                    :click-node="clickNode"
                    :lf="lf"
                    :body-type="inputBodyType"
                    :need-filter="true"
                    @changeInputData="changeInputData"
                    v-if="requestVars && requestVars.length > 0"
                ></InputComponents>
            </div>
        </div>
        <div class="output-container">
            <outputComponents
                :output-vars="responseVars"
                :is-can-expand="false"
            ></outputComponents>
        </div>
        <div
            class="api-edit-wrapper"
            v-show="isShowApiEdit"
        >
            <div class="api-content">
                <div class="edit-title-wrapper">
                    <span class="edit-title">
                        编辑API
                    </span>
                    <div
                        class="edit-back"
                        @click="isShowApiEdit = false"
                    >
                        <a-icon type="right-circle" />
                    </div>
                </div>
                <edit-api
                    :api-config="currApiConfig"
                    @apiParamsChange="apiParamsChange"
                    :lf="lf"
                    :click-node="clickNode"
                    v-if="Object.keys(currApiConfig).length"
                />
            </div>
        </div>
    </div>
</template>

<script>
import LogicFlow from '@logicflow/core';
import editApi from './editApi.vue';
import InputComponents from '../components/InputComponents';
import { deepClone } from '@baidu/metis-js-util';
import { processTreeData, checkReferenceVarName } from '@/views/flow/common/common';
import { getAllArgs, getAllFlatArgs, deleteTempOutputs } from '@/views/flow/getArgs';
import outputComponents from '../components/outputConfigComponents';
// import { flowRequest } from '@/views/flow/common/request';
// import { verifyRequestJsonSchema } from '@/views/flow/apiList';
import { replaceMockValues } from '@/views/flow/common/common';

export default {
    props: {
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        }
    },
    components: {
        editApi,
        InputComponents,
        outputComponents
    },
    data() {
        return {
            currApiConfig: {},
            varTypeOptions: [
                { label: '引用', value: 'reference' },
                { label: 'String', value: 'String' },
                { label: 'Array<String>', value: 'ArrayString' },
                { label: 'Array<Number>', value: 'ArrayNumber' },
                { label: 'Array<Integer>', value: 'ArrayInteger' },
            ], // 输入参数类型
            arrArgs: [],
            allFlatArgs: [],
            requestVars: [], // 输入参数列表
            responseVars: [], // 输出参数列表
            inputVars: [],
            inputTooltip: '',
            apiParams: [], // api参数
            outputVars: [],
            isShowApiEdit: false, // 是否展示api编辑抽屉
            isConfigLoading: false, // api配置页是否加载
        };
    },
    computed: {
        // 监听点击节点数据
        propertiesData() {
            return this.clickNode.properties || {};
        },
        // 鉴权类型
        authType() {
            return this.currApiConfig?.requestConfig?.authInfo?.authType || null;
        },
        // url地址
        urlValue() {
            return this.currApiConfig?.url || '';
        },
        // 请求方式
        method() {
            return this.currApiConfig?.method || null;
        },
        // 调试状态
        debugStatus() {
            return this.currApiConfig?.debugStatus || null;
        },
        // 请求方式对应的颜色
        methodColor() {
            let color = '';
            switch (this.currApiConfig?.method) {
                case 'GET':
                    color = '#30bf13';
                    break;
                case 'POST':
                    color = '#ff9326';
                    break;
                case 'PUT':
                    color = '#2468f2';
                    break;
                case 'PATCH':
                    color = '#6421d9';
                    break;
                case 'DELETE':
                    color = '#cc292e';
                    break;
                case 'HEAD':
                    color = '#2468f2';
                    break;
                case 'OPTIONS':
                    color = '#2468f2';
                    break;
                default:
                    break;
            }
            return color;
        },
        // body类型
        inputBodyType() {
            return this.currApiConfig?.requestConfig?.bodyInfo?.bodyType || null;
        }

    },
    watch: {
        // 监听点击节点数据
        propertiesData() {
            this.init();
        }
    },
    mounted() {
        this.init();
    },
    methods: {
        init() {
            const { id } = this.clickNode;
            const argsMap = getAllArgs({ nodeId: id, lf: this.lf });
            this.arrArgs = processTreeData(argsMap, this.$createElement);
            this.allFlatArgs = getAllFlatArgs({ nodeId: id, lf: this.lf });

            const {
                requestConfig,
                responseConfig,
                url,
                method,
                debugStatus,
                inputVars,
                readOnlyOutputs,
                inputVarsAll
            } = this.propertiesData;

            console.log('inputVars', inputVars, requestConfig, inputVarsAll);
            console.log('responseConfig', readOnlyOutputs);

            this.$nextTick(() => {
                let currList = [];
                if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
                    // post、put、patch请求方式下对应展示的输入参数
                    if (requestConfig?.queryParams?.length > 0) {
                        requestConfig.queryParams.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });
                    }
                    if (requestConfig?.headers?.length > 0) {
                        requestConfig.headers.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });
                    }
                    if (requestConfig?.bodyInfo?.jsonData?.length > 0) {
                        requestConfig.bodyInfo.jsonData.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });

                    }
                    if (requestConfig?.bodyInfo?.formData?.length > 0) {
                        requestConfig.bodyInfo.formData.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });

                    }
                    if (requestConfig?.bodyInfo?.formUrlencodedData?.length > 0) {
                        requestConfig.bodyInfo.formUrlencodedData.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });
                    }

                    if (requestConfig?.bodyInfo?.bodyType === 'json') {
                        currList = currList.filter(
                            item => (item.requestType !== 'urlencoded' && item.requestType !== 'formData')
                        );

                    } else if (requestConfig?.bodyInfo?.bodyType === 'form_data') {
                        currList = currList.filter(
                            item => (item.requestType !== 'json' && item.requestType !== 'urlencoded')
                        );

                    } else if (requestConfig?.bodyInfo?.bodyType === 'x_www_form_urlencoded') {
                        currList = currList.filter(
                            item => (item.requestType !== 'json' && item.requestType !== 'formData')
                        );

                    }

                } else {

                    // get、delete、head、options请求方式下对应展示的输入参数
                    if (requestConfig?.headers?.length > 0) {
                        requestConfig.headers.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });

                    }
                    if (requestConfig?.queryParams?.length > 0) {
                        requestConfig.queryParams.forEach(element => {
                            const targetValue = inputVarsAll.find(item => item.id === element.id);
                            if (targetValue) {
                                currList.push(targetValue);
                            }
                        });
                    }
                }
                this.requestVars = checkReferenceVarName({
                    inputVars: deepClone(currList),
                    allFlatArgs: this.allFlatArgs
                });
                this.$nextTick(() => {
                    if (this.$refs.editApiInput) {
                        this.$refs.editApiInput.apiInputChange(this.requestVars);
                    }
                });
                this.responseVars = readOnlyOutputs;
                console.log('responseVars', this.responseVars);

                this.$set(this.currApiConfig, 'url', url);
                this.$set(this.currApiConfig, 'method', method);
                this.$set(this.currApiConfig, 'debugStatus', debugStatus);
                this.$set(this.currApiConfig, 'requestConfig', requestConfig);
                this.$set(this.currApiConfig, 'responseConfig', responseConfig);
                this.$set(this.currApiConfig, 'inputVars', this.requestVars);
                this.$set(this.currApiConfig, 'outputVars', this.responseVars);
                this.$set(this.currApiConfig, 'inputVarsAll', inputVarsAll);
            });

        },
        /**
         * @description: 递归赋值
         * @param {object/array} obj 需要递归的对象（数组）
         * @return {*}
         * */
        deepAssign(arr) {
            return arr.map((item) => {
                if (item.type.includes('Array') && item.type !== 'ArrayObject') {
                    if (item?.value && this.isJSON(item.value)) {
                        let children = [];
                        let tempChildren = JSON.parse(item.value);
                        for (let indexChild = 0; indexChild < tempChildren.length; indexChild++) {
                            const element = tempChildren[indexChild];
                            let child = {};
                            this.$set(child, 'id', `${item.id}_${indexChild + 1}`);
                            this.$set(child, 'field', `${item.field}<item>`);
                            this.$set(child, 'type', item.type);
                            this.$set(child, 'value', element);

                            children.push(child);
                        }
                        this.$set(item, 'children', children);
                    }
                } else if (item.type === 'ArrayObject') {
                    if (item?.value && this.isJSON(item.value)) {
                        let children = [];
                        let tempChildren = JSON.parse(item.value);
                        for (let indexChild = 0; indexChild < tempChildren.length; indexChild++) {
                            const element = tempChildren[indexChild];
                            let child = element;
                            this.$set(child, 'value', child['mockValue']);
                            children.push(child);
                        }
                        this.$set(item, 'children', children);
                    }
                }

                // 如果当前项有子数组，递归处理
                if (item.children && Array.isArray(item.children)) {
                    this.deepAssign(item.children, item.id);
                }
                return item;
            });
        },


        // 打开编辑api配置
        editApi() {
            this.isShowApiEdit = true;
        },
        // 输入参数数据变更
        changeInputData(value) {
            this.requestVars = value;
            this.updateLf();
        },
        // api编辑数据变更
        apiParamsChange(params) {
            this.currApiConfig = deepClone(params);
            let currOutList = [];
            if (params?.responseConfig?.jsonResult?.length > 0) {
                currOutList = this.outputListOptions(params.responseConfig.jsonResult);
            }
            currOutList = currOutList.filter(item =>
                (item?.varName && item?.varName?.length > 0)
            );


            if (params.method === 'POST' || params.method === 'PUT' || params.method === 'PATCH') {
                if (params?.requestConfig?.bodyInfo?.bodyType === 'x_www_form_urlencoded') {
                    this.requestVars = params.inputVars.filter(item =>
                        ('urlencoded' === item?.requestType ||
                            'header' === item?.requestType ||
                            'params' === item?.requestType)
                    );
                } else if (params?.requestConfig?.bodyInfo?.bodyType === 'form_data') {
                    this.requestVars = params.inputVars.filter(item =>
                        ('formData' === item?.requestType ||
                            'header' === item?.requestType ||
                            'params' === item?.requestType)
                    );
                } else {
                    // InputComponents 组件没有值更新操作，所以这里需要清空下值，然后在更新，刷新组件状态
                    this.requestVars = [];
                    this.$nextTick(() => {
                        this.requestVars = params.inputVars.filter(item =>
                            ('json' === item?.requestType ||
                                'header' === item?.requestType ||
                                'params' === item?.requestType
                            )
                        );
                    });
                }
            } else {
                this.requestVars = params.inputVars.filter(item =>
                    ('header' === item?.requestType || 'params' === item?.requestType)
                );
            }


            this.responseVars = deepClone(currOutList);
            setTimeout(() => {
                this.$nextTick(() => {
                    if (this.$refs.editApiInput) {
                        this.$refs.editApiInput.apiInputChange(this.requestVars);
                    }
                    // 预存全量输入参数
                    this.$set(this.currApiConfig, 'inputVarsAll', params.inputVars);
                    this.$set(this.currApiConfig, 'inputVars', this.requestVars);
                    this.$set(this.currApiConfig, 'outputVars', this.responseVars);
                    this.updateLf();
                    deleteTempOutputs();
                });
            }, 0);

        },
        /**
         * @description: 更新逻辑流图
         * @return {*}
         */
        async updateLf() {
            const { id } = this.clickNode;
            const edgeModel = this.lf.getNodeModelById(id);
            console.log('updateLf1', this.currApiConfig);
            const {
                requestConfig, responseConfig, url, method, debugStatus, inputVarsAll
            } = this.currApiConfig;
            let newRequestConfig = deepClone(requestConfig);
            let newResponseConfig = deepClone(responseConfig);

            // 所有类型完成输入参数的值在修改时同步数据
            let newInputVarsAll = inputVarsAll.map(item => {
                this.requestVars.find(sub => {
                    if (sub.id === item.id) {
                        item = sub;
                    }
                });
                return item;
            });
            edgeModel.setProperties({
                requestConfig: newRequestConfig,
                responseConfig: newResponseConfig,
                url,
                method,
                debugStatus,
                inputVars: this.requestVars,
                outputVars: [],
                readOnlyOutputs: this.responseVars,
                inputVarsAll: newInputVarsAll
            });
            this.$nextTick(() => {
                edgeModel.updatePath();
            });
        },
        // json类型数据格式化
        async verifyJson(data) {
            let bodyData = data || [];

            let newBodyData = replaceMockValues(bodyData) || [];
            return newBodyData;
        },
        // 遍历处理输出参数
        outputListOptions(array) {
            return array.map((item) => {
                // 创建新对象（保持原对象不变性）
                let tempChildren = [];
                if (item.type === 'Object' && item.mockValue) {
                    this.$set(item, 'children', JSON.parse(item.mockValue));
                }
                if (item?.children?.length > 0) {
                    tempChildren = this.outputListOptions(item.children);
                }

                let data = null;
                data = {
                    varName: item.field,
                    varValue: '',
                    varType: 'String',
                    varNameType: item.type,
                    id: item.id,
                    originalVarType: item.type,
                    children: tempChildren,
                    expanded: false,
                };

                return data;
            });
        },

    }
};
</script>

<style lang="less" scoped>
.api-config {
    position: relative;
    .api-config-wrapper {
        position: relative;
    }
    .api-title {
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: 500;
    }
    .api-base {
        color: rgb(21, 27, 38);
        font-size: 12px;
        border-bottom: 1px solid #e8e9eb;
        padding: 16px 0;
        .api-base-desc {
            .base-desc-item {
                display: flex;
                align-content: center;
                margin-bottom: 12px;
                .base-label {
                    margin-right: 60px;
                    white-space: nowrap;
                    color: #5c5f66;
                }
                .base-value {
                    color: #151B26;
                    word-break: break-word;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    max-width: 100%;
                }
                .un-pass {
                    color: #f33e3e;
                }
                .pass {
                    color: #30bf13;
                }
            }
        }
        .edit-api-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 28px;
            color: #151b26;
            border: 1px solid #e8e9eb;
            font-size: 12px;
            border-radius: 6px;
            cursor: pointer;
            &:hover {
                color: #528EFF;
                border-color: #528EFF;
                background: rgb(255, 255, 255);
            }
        }
    }
    .input-container {
        border-bottom: 1px solid #e8e9eb;
        padding: 16px 0;
    }
    .output-container {
        padding: 16px 0;
    }
    .api-edit-wrapper {
        width: calc(100vw - 400px);
        height: 100%;
        position: fixed;
        top: 0px;
        right: 400px;
        background: #070c1480;
        z-index: 999;
        padding-left: 100px;
        box-sizing: border-box;
        .api-content {
            display: flex;
            flex-direction: column;
            height: 100%;
            background: #fff;
            border-right: 1px solid #e8e9eb;
        }
        .edit-title-wrapper {
            display: flex;
            align-items: center;
            justify-content: space-between;
            height: 40px;
            padding: 0 16px;
            border-bottom: 1px solid #e8e9eb;
            .edit-title {
                font-size: 14px;
                font-weight: 500;
                color: #151b26;
            }
            .edit-back {
                border-radius: 4px;
                cursor: pointer;
                padding: 4px 8px;
                &:hover {
                    background: #0000000d;
                }
            }
        }
    }
}
</style>