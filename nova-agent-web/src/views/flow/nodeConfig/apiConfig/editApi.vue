<!--
 * @Author: hewenquan
 * @Date: 2025-07-09 13:55:47
 * @LastEditTime: 2025-09-08 14:42:17
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/apiConfig/editApi.vue
 * @Description: API 编辑页面
-->
<template>
    <a-spin
        :spinning="spinning"
    >
        <div class="edit-api-wrapper">
            <div class="header-wrapper">
                <a-form
                    :form="inputForm"
                    class="header-form"
                >
                    <a-form-item
                        class="header-form-item"
                    >
                        <a-input
                            placeholder="请输入有效的访问地址"
                            :max="500"
                            :style="{ fontSize: '12px' }"
                            @input="urlChange"
                            v-decorator="['url', {
                                rules: [
                                    { required: true, message: '请输入有效的访问地址' }
                                ]
                            }]"
                        >
                            <a-select
                                slot="addonBefore"
                                class="url-select"
                                default-value="GET"
                                v-model="requestMethod"
                                :style="{ width: '90px', fontSize: '12px' }"
                                @change="requestMethodChange"
                            >
                                <a-select-option
                                    v-for="item in methodsOptions"
                                    :key="item.value"
                                >
                                    <span
                                        class="url-select-option"
                                        :style="{ color: item.color }"
                                    >
                                        {{ item.label }}
                                    </span>
                                </a-select-option>
                            </a-select>
                        </a-input>
                    </a-form-item>
                    <a-button
                        type="primary"
                        class="send-btn"
                        @click="sendApi"
                    >
                        发送
                    </a-button>
                </a-form>
            </div>
            <div class="method-wrapper">
                <div class="method-header">
                    <a-radio-group
                        v-model="methodType"
                        button-style="solid"
                        size="small"
                    >
                        <a-radio-button value="request">
                            请求
                        </a-radio-button>
                        <a-radio-button value="response">
                            响应
                        </a-radio-button>
                    </a-radio-group>
                </div>
                <div
                    class="method-content"
                    v-if="methodType === 'request'"
                >
                    <a-tabs
                        v-model="tabsValue"
                        size="small"
                        @change="tabsChange"
                    >
                        <a-tab-pane
                            key="params"
                            :tab="`Params(${paramsDataLength || 0})`"
                        >
                        </a-tab-pane>
                        <a-tab-pane
                            key="headers"
                            :tab="`Headers(${headersDataLength || 0})`"
                        >
                        </a-tab-pane>
                        <a-tab-pane
                            key="authorization"
                            tab="Authorization"
                        >
                        </a-tab-pane>
                        <a-tab-pane
                            key="body"
                            :tab="`Body(${bodyDataLength || 0})`"
                            :disabled="!isHaveBody"
                        >
                        </a-tab-pane>
                    </a-tabs>
                </div>
            </div>
            <div
                class="params-wrapper"
                v-show="methodType === 'request'"
            >
                <div
                    class="params-header"
                >
                    <div v-show="tabsValue === 'params'">
                        Query Params
                    </div>
                    <div v-show="tabsValue === 'headers'">
                        Headers
                    </div>
                    <div v-show="tabsValue === 'body'">
                        <a-radio-group
                            class="body-radio-group"
                            v-model="bodyInfo.bodyType"
                            size="small"
                            @change="bodyTypeChange"
                        >
                            <a-radio
                                value="json"
                            >
                                json
                            </a-radio>
                            <a-radio
                                value="form_data"
                            >
                                form_data
                            </a-radio>
                            <a-radio
                                value="x_www_form_urlencoded"
                            >
                                x_www_form_urlencoded
                            </a-radio>
                        </a-radio-group>
                    </div>
                </div>

                <div
                    class="params-content"
                >
                    <div v-show="tabsValue === 'params'">
                        <ApiParamsComponents
                            :carrier="'params'"
                            :input-data="paramsDataList"
                            :max-id="maxId"
                            @apiParamsChange="(params, singleData, optionMethod) =>
                                paramsChange(params, singleData, optionMethod, 'params')"
                            v-if="paramsDataList && paramsDataList.length > 0"
                            :lf="lf"
                        ></ApiParamsComponents>
                    </div>
                    <div v-show="tabsValue === 'headers'">
                        <ApiParamsComponents
                            :carrier="'headers'"
                            :input-data="headersDataList"
                            :max-id="maxId"
                            @apiParamsChange="(params, singleData, optionMethod) =>
                                paramsChange(params, singleData, optionMethod, 'header')"
                            v-if="headersDataList && headersDataList.length > 0"
                            :lf="lf"
                        ></ApiParamsComponents>
                    </div>
                    <div v-show="tabsValue === 'authorization'">
                        <a-form
                            :form="authorizationForm"
                            class="authorization-form"
                        >
                            <a-form-item
                                size="small"
                                class="authorization-form-item"
                                label="鉴权方式"
                                :required="true"
                            >
                                <a-radio-group
                                    v-model="authInfo.authType"
                                    size="small"
                                    @change="authTypeChange"
                                >
                                    <a-radio
                                        value="NO_AUTH"
                                    >
                                        无需鉴权
                                    </a-radio>
                                    <a-radio
                                        value="API_KEY"
                                    >
                                        API Key
                                    </a-radio>
                                    <!-- 本期不做 -->
                                    <!-- <a-radio
                                        value="OAUTH"
                                    >
                                        OAuth
                                    </a-radio> -->
                                </a-radio-group>
                            </a-form-item>
                            <div v-show="authInfo.authType === 'API_KEY'">
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="密钥位置"
                                    :required="true"
                                >
                                    <a-radio-group
                                        v-model="authInfo.authPos"
                                        size="small"
                                        @change="authPosChange"
                                    >
                                        <a-radio
                                            value="Header"
                                        >
                                            Header
                                        </a-radio>
                                        <a-radio
                                            value="Query"
                                        >
                                            Query
                                        </a-radio>
                                        <a-radio
                                            value="Cookie"
                                        >
                                            Cookie
                                        </a-radio>
                                    </a-radio-group>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="密钥参数名"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入密钥参数名"
                                        :max-length="64"
                                        @input="inputItemChange($event, 'tokenFieldName')"
                                        v-decorator="[
                                            'tokenFieldName',
                                            { rules: [{ required: true, message: '请输入密钥参数名' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenFieldNameLength} / 64` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="密钥值"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入密钥值"
                                        :max-length="20000"
                                        @input="inputItemChange($event, 'token')"
                                        v-decorator="[
                                            'token',
                                            { rules: [{ required: true, message: '请输入密钥值' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenLength} / 20000` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                            </div>
                            <div v-show="authInfo.authType === 'OAUTH'">
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="grant_type"
                                    :required="true"
                                >
                                    <a-select
                                        class="url-select"
                                        v-model="grantType"
                                        @change="inputItemChange($event, 'grantType')"
                                    >
                                        <a-select-option
                                            v-for="item in grantTypeOptions"
                                            :key="item.value"
                                        >
                                            <span>
                                                {{ item.label }}
                                            </span>
                                        </a-select-option>
                                    </a-select>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="client_id"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入客户端ID"
                                        :max-length="64"
                                        @input="inputItemChange($event, 'client_id')"
                                        v-decorator="[
                                            'client_id',
                                            { rules: [{ required: true, message: '请输入客户端ID' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenFieldNameLength} / 64` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="client_secret"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入客户端密钥"
                                        :max-length="20000"
                                        @input="inputItemChange($event, 'client_secret')"
                                        v-decorator="[
                                            'client_secret',
                                            { rules: [{ required: true, message: '请输入客户端密钥' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenLength} / 20000` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="scope"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入scope"
                                        :max-length="20000"
                                        @input="inputItemChange($event, 'scope')"
                                        v-decorator="[
                                            'scope',
                                            { rules: [{ required: false, message: '请输入scope' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenLength} / 20000` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item"
                                    label="authorization_url"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入authorization_url"
                                        :max-length="20000"
                                        @input="inputItemChange($event, 'authorization_url')"
                                        v-decorator="[
                                            'authorization_url',
                                            { rules: [{ required: true, message: '请输入authorization_url' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenLength} / 20000` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                                <a-form-item
                                    size="small"
                                    class="authorization-form-item item-wrap"
                                    label="authorization_content_type"
                                    :required="true"
                                >
                                    <a-input
                                        placeholder="请输入authorization_content_type"
                                        :max-length="20000"
                                        @input="inputItemChange($event, 'authorization_content_type')"
                                        v-decorator="[
                                            'authorization_content_type',
                                            { rules: [{ required: true, message: '请输入authorization_content_type' }]}
                                        ]"
                                    >
                                        <span
                                            slot="suffix"
                                            class="suffix-text"
                                        >
                                            {{ `${tokenLength} / 20000` }}
                                        </span>
                                    </a-input>
                                </a-form-item>
                            </div>
                        </a-form>
                    </div>
                    <div
                        v-show="tabsValue === 'body'"
                    >
                        <div v-show="bodyInfo.bodyType === 'json'">
                            <ApiParamsComponents
                                :carrier="'body-json'"
                                :format-type="bodyInfo.bodyType"
                                :input-data="bodyJsonDataList"
                                :max-id="maxId"
                                @apiParamsChange="(params, singleData, optionMethod) =>
                                    paramsChange(params, singleData, optionMethod, 'json')"
                                v-if="bodyJsonDataList && bodyJsonDataList.length > 0"
                                :lf="lf"
                            ></ApiParamsComponents>
                        </div>
                        <div v-show="bodyInfo.bodyType === 'form_data'">
                            <ApiParamsComponents
                                :carrier="'body-form-data'"
                                :format-type="bodyInfo.bodyType"
                                :input-data="bodyFormDataList"
                                :max-id="maxId"
                                @apiParamsChange="(params, singleData, optionMethod) =>
                                    paramsChange(params, singleData, optionMethod, 'formData')"
                                v-if="bodyFormDataList && bodyFormDataList.length > 0"
                                :lf="lf"
                            ></ApiParamsComponents>
                        </div>
                        <div v-show="bodyInfo.bodyType === 'x_www_form_urlencoded'">
                            <ApiParamsComponents
                                :carrier="'body-form-urlencoded'"
                                :format-type="bodyInfo.bodyType"
                                :input-data="bodyFormUrlencodedDataList"
                                :max-id="maxId"
                                @apiParamsChange="(params, singleData, optionMethod) =>
                                    paramsChange(params, singleData, optionMethod, 'urlencoded')"
                                v-if="bodyFormUrlencodedDataList && bodyFormUrlencodedDataList.length > 0"
                                :lf="lf"
                            ></ApiParamsComponents>
                        </div>
                    </div>
                </div>
            </div>
            <div
                class="params-wrapper"
                v-show="methodType === 'response'"
            >
                <div
                    class="params-header"
                >
                    <a-radio-group
                        class="body-radio-group"
                        v-model="responseType"
                        size="small"
                        @change="responseTypeChange"
                    >
                        <a-radio
                            value="json"
                        >
                            json
                        </a-radio>
                        <a-radio
                            value="string"
                        >
                            string
                        </a-radio>
                    </a-radio-group>
                </div>
                <div
                    class="params-content"
                >
                    <div v-show="responseType === 'json'">
                        <ApiParamsComponents
                            :carrier="'response-json'"
                            :input-data="responseDataList"
                            @apiParamsChange="outputJsonChange"
                            v-if="responseDataList && responseDataList.length > 0"
                            :lf="lf"
                            :click-node="clickNode"
                        ></ApiParamsComponents>
                    </div>
                    <div v-show="responseType === 'string'">
                        <ApiParamsComponents
                            :carrier="'response-string'"
                            :input-data="responseStringDataList"
                            @apiParamsChange="outputStringChange"
                            v-if="responseStringDataList && responseStringDataList.length > 0"
                            :lf="lf"
                        ></ApiParamsComponents>
                    </div>
                </div>
            </div>
            <div class="response-wrapper">
                <div class="response-header">
                    <span>响应结果</span>
                    <span class="response-result">
                        <div class="result">
                            <a-icon
                                class="success-icon"
                                type="check-circle"
                                theme="filled"
                                v-show="debugStatus === 'pass'"
                            />
                            <a-icon
                                class="fail-icon"
                                type="close-circle"
                                theme="filled"
                                v-show="debugStatus === 'unPass'"
                            />
                            <span
                                :class="{
                                    'fail-text': debugStatus === 'unPass', 'success-text': debugStatus === 'pass'
                                }"
                            >
                                {{ debugStatus === 'unPass' ? '未通过' : debugStatus === 'pass' ? '通过' : '未调试' }}
                            </span>

                        </div>
                        <div class="result-status">
                            <span>请求状态码：</span>
                            <span
                                class="result-status-code"
                                :style="{
                                    // eslint-disable-next-line max-len
                                    color: debugStatus === 'unPass' ? '#f33e3e' : debugStatus === 'pass' ? '#30bf13' : ''
                                }"
                            >
                                {{ debugStatusValue }}
                            </span>
                        </div>
                        <div
                            class="result-detail"
                            v-if="false"
                        >
                            <span>content type：</span>
                            <span class="result-detail-content">
                                application/x-gzip
                            </span>
                        </div>
                    </span>
                </div>
                <!-- 暂时屏蔽 -->
                <div
                    class="response-content"
                >
                    <div
                        class="content-item"
                        v-show="debugStatus === 'pass'"
                    >
                        <div
                            class="content-item-title"
                        >
                            接口输出参数
                        </div>
                        <div
                            class="editor-left"
                            ref="editorLeft"
                        >
                        </div>
                    </div>
                    <div
                        class="content-item"
                        v-show="debugStatus === 'pass'"
                    >
                        <div
                            class="content-item-title"
                        >
                            节点输出参数
                        </div>
                        <div
                            class="editor-right"
                            ref="editorRight"
                        >
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </a-spin>
</template>

<script>
import ApiParamsComponents from '../components/apiParamsComponents.vue';
import { flowRequest } from '@/views/flow/common/request';
import { verifyRequestJsonSchema, apiDebug } from '@/views/flow/apiList';
import { deepClone } from '@baidu/metis-js-util';
import CodeMirror from 'codemirror';
import 'codemirror/lib/codemirror.css';
import 'codemirror/mode/javascript/javascript'; // 引入 JavaScript 模式
import { replaceMockValues } from '@/views/flow/common/common';
import LogicFlow from '@logicflow/core';

export default {
    props: {
        // API 配置数据
        apiConfig: {
            type: Object,
            default: () => ({})
        },
        clickNode: {
            type: Object,
            default: () => ({})
        },
        lf: {
            type: LogicFlow,
            required: true
        },
    },
    computed: {
        // body类型是否可选
        isHaveBody() {
            let bool = false;
            if (this.requestMethod === 'POST' || this.requestMethod === 'PUT' || this.requestMethod === 'PATCH') {
                bool = true;
            }
            return bool;
        },
        // tokenFieldName  input框展示已经输入字符长度
        tokenFieldNameLength() {
            let tokenFieldName = this.authorizationForm.getFieldValue('tokenFieldName');
            return tokenFieldName ? tokenFieldName.length : 0;
        },
        // token  input框展示已经输入字符长度
        tokenLength() {
            let token = this.authorizationForm.getFieldValue('token');
            return token ? token.length : 0;
        },
        currApiConfig() {
            return this.$deepClone(this.apiConfig);
        },
        // 调试状态
        debugStatusValue() {
            let statusValue = '';
            if (this.debugStatus === 'unPass') {
                statusValue = '1';
            } else if (this.debugStatus === 'pass') {
                statusValue = this.debugData?.status || '0';
            }
            return statusValue;
        },

    },
    components: {
        ApiParamsComponents
    },
    data() {
        return {
            inputForm: this.$form.createForm(this),
            authorizationForm: this.$form.createForm(this),
            methodsOptions: [
                {
                    label: 'GET',
                    value: 'GET',
                    color: '#30bf13'
                },
                {
                    label: 'POST',
                    value: 'POST',
                    color: '#ff9326'
                },
                {
                    label: 'PUT',
                    value: 'PUT',
                    color: '#2468f2'
                },
                {
                    label: 'PATCH',
                    value: 'PATCH',
                    color: '#6421d9'
                },
                {
                    label: 'DELETE',
                    value: 'DELETE',
                    color: '#cc292e'
                },
                {
                    label: 'HEAD',
                    value: 'HEAD',
                    color: '#2468f2'
                },
                {
                    label: 'OPTIONS',
                    value: 'OPTIONS',
                    color: '#2468f2'
                }
            ], // 请求方法配置项
            url: null, // 请求地址
            requestMethod: 'GET', // 请求方法
            activeKey: 'resquest', // tab栏下标
            isPass: false, // 响应是否通过
            tabsValue: 'params', // tab切换
            grantTypeOptions: [
                {
                    label: 'Client Credentials',
                    value: 'Client Credentials'
                }
            ],
            grantType: 'Client Credentials',
            varTypeOptions: [
                { label: '引用', value: 'reference' },
            ],
            methodType: 'request', // 请求/响应
            responseType: 'json', // 响应类型
            currApiParams: [], // api参数
            requestConfig: {}, // request配置
            responseConfig: {}, // response配置
            authInfo: {
                authType: 'NO_AUTH', // 鉴权类型
            }, // 鉴权配置
            bodyInfo: {
                bodyType: 'json',
            }, // body配置
            paramsDataList: [], // params参数配置
            headersDataList: [], // headers参数配置
            bodyDataList: [], // body参数配置
            bodyFormDataList: [], // form-data参数配置
            bodyFormUrlencodedDataList: [], // x-www-form-urlencoded参数配置
            bodyJsonDataList: [], // json参数配置
            jsonResult: [], // json结果
            responseDataList: [], // 响应Json参数
            responseStringDataList: [
                {
                    field: 'result',
                    value: '',
                    type: 'string'
                }
            ], // 响应String参数
            outputJsonDataList: [], // 输出Json参数
            debugStatus: '', // 调试结果
            debugData: {}, // 调试数据
            editorLeft: null, // 响应json真实展示
            editorRight: null, // 响应json配置展示
            responseJson: '', // 响应json
            bodyDataLength: 0, // body数据长度
            paramsDataLength: 0, // params数据长度
            headersDataLength: 0, // headers数据长度
            currInputVars: [], // 当前输入参数
            spinning: false, // 页面加载中
            maxId: 0, // 最大id
            formatConfigAll: {}, // 格式化后的完整配置
        };
    },

    mounted() {
        this.init();
    },
    methods: {
        init() {
            this.formatConfigAll = deepClone(this.apiConfig);
            this.computeLength(this.apiConfig?.requestConfig);
            this.maxId = this.currMaxId();
            this.url = this.apiConfig?.url || '';

            this.requestMethod = this.apiConfig?.method || 'GET';
            this.requestConfig = this.apiConfig?.requestConfig || {};
            this.responseConfig = this.apiConfig?.responseConfig || {};

            this.responseType = this.apiConfig?.responseConfig?.type || 'json';
            this.bodyInfo = this.apiConfig?.requestConfig?.bodyInfo || {
                bodyType: 'json',
            };
            this.authInfo = this.apiConfig?.requestConfig?.authInfo || {
                authType: 'NO_AUTH', // 鉴权类型
                authPos: 'Header', // 密钥位置
                tokenFieldName: '', // 密钥名称
                token: '', // 密钥
            };
            // this.currInputVars = this.apiConfig?.inputVars || [];
            this.currInputVars = this.apiConfig?.inputVarsAll || [];

            // 请求参数params
            let tempParamsData = this.apiConfig?.requestConfig?.queryParams || [];
            let emptyData = {
                field: '',
                mockvalue: '',
                type: 'String',
            };
            this.paramsDataList = tempParamsData;
            this.paramsDataList = [...this.paramsDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];

            // 请求参数headers
            let tempHeadersData = this.apiConfig?.requestConfig?.headers || [];
            this.headersDataList = tempHeadersData;
            this.headersDataList = [...this.headersDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];

            // 请求参数body(formData)
            let tempBodyFormData = this.apiConfig?.requestConfig?.bodyInfo?.formData || [];
            this.bodyFormDataList = tempBodyFormData;
            this.bodyFormDataList = [...this.bodyFormDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];

            // 请求参数body(x-www-form-urlencoded)
            let tempBodyFormUrlencodedData = this.apiConfig?.requestConfig?.bodyInfo?.formUrlencodedData || [];
            this.bodyFormUrlencodedDataList = tempBodyFormUrlencodedData;
            this.bodyFormUrlencodedDataList = [...this.bodyFormUrlencodedDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];

            // 请求参数body(json)
            let tempBodyJsonData = this.apiConfig?.requestConfig?.bodyInfo?.jsonData || [];
            this.$set(emptyData, 'id', this.uniqueValue());
            this.bodyJsonDataList = tempBodyJsonData;
            this.bodyJsonDataList = [...this.bodyJsonDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];
            this.bodyJsonDataList.map(item => {
                if (item.mockValue && item.mockValue !== '' && item.type === 'Object') {
                    this.$set(item, 'children', JSON.parse(item.mockValue) || []);
                }
            });
            this.bodyJsonDataList = this.replaceKeys(this.bodyJsonDataList);

            // 请求参数authorization
            this.$nextTick(() => {
                this.inputForm.setFieldsValue({
                    ['url']: `${this.url || ''}`,
                });
                this.authorizationForm.setFieldsValue({
                    ['tokenFieldName']: `${this.authInfo?.tokenFieldName || ''}`,
                    ['token']: `${this.authInfo?.token || ''}`,
                });
            });

            // 响应参数body(json)
            let tempResponseJsonData = this.apiConfig?.responseConfig?.jsonData || [];
            this.responseDataList = tempResponseJsonData;
            this.responseDataList = [...this.responseDataList, {
                ...emptyData,
                id: this.uniqueValue()
            }];

        },
        /**
         * @description: 递归给object类型添加展开收起属性
         * @param {object/array} obj 需要添加的对象（数组）
         * @return {*}
         * */
        replaceKeys(array) {
            return array.map((item) => {
                if (item.type === 'Object') {
                    this.$set(item, 'expandedVars', true);
                    this.replaceKeys(item.children);
                }
                return item;
            });
        },

        requestMethodChange() {
            if (
                (this.requestMethod === 'GET' ||
                this.requestMethod === 'DELETE' ||
                this.requestMethod === 'HEAD' ||
                this.requestMethod === 'OPTIONS') &&
                this.tabsValue === 'body'
            ) {
                this.tabsValue = 'params';
            }
            this.change();
        },
        urlChange(e) {
            this.$set(this, 'url', e?.target?.value ? e?.target?.value : e);
            this.change();
        },
        bodyTypeChange() {
            this.$set(this.requestConfig, 'bodyInfo', this.bodyInfo);
            this.change();
        },
        // 鉴权方式变更对数据的特殊处理
        authTypeChange() {
            if (this.authInfo?.authType === 'NO_AUTH') {
                this.$delete(this.authInfo, 'authPos');
            } else {
                this.$set(this.authInfo, 'authPos', 'Header');
            }
            this.$set(this.requestConfig, 'authInfo', this.authInfo);

            this.change();
        },
        authPosChange() {
            this.$set(this.requestConfig, 'authInfo', this.authInfo);
            this.change();
        },
        inputItemChange(event, key) {
            const newVal = event.target ? event.target.value : event;
            this.$set(this.authInfo, key, newVal);
            this.$set(this.requestConfig, 'authInfo', this.authInfo);
            this.change();
        },
        /**
         * @description: 请求参数所有数据变动处理
         * @param {object/array} params 当前操作数据的完整数据列表
         * @param {object} singleData 当前操作数据
         * @param {object} optionMethod 当前操作类型（delete是删除，其他是修改或者新增）
         * @param {object} changeType 当前操作数据的请求参数类型（params、header、json、formData、urlencoded）
         * @return {*}
         * */
        paramsChange(params, singleData, optionMethod, changeType) {
            // 如果当前操作是删除，要判断是否是请求参数中的空值，是空值的话，输入参数列表不存在该数据，不做处理，非空数据进行删除
            if (singleData?.field?.length > 0 || singleData?.value?.length > 0) {
                if (this.currInputVars?.length > 0) {
                    // 在输入参数列表中查找当前操作数据所对应id的下标
                    const intputIndex = this.currInputVars.findIndex(
                        item => item.id === singleData.id,
                    );
                    if (intputIndex > -1) {
                        // 当操作数据在输入参数列表中存在，如果当前操作是删除，则删除输入参数列表中的对应数据，否则就是修改对应数据
                        if (optionMethod === 'delete') {
                            this.currInputVars.splice(intputIndex, 1);
                        } else {
                            this.$set(this.currInputVars[intputIndex], 'varName', singleData.field);
                            this.$set(this.currInputVars[intputIndex], 'varNameType', singleData.type);
                            this.$set(this.currInputVars[intputIndex], 'originalVarType', singleData.type);
                            // 输入参数类型改变，清除已经选中的变量
                            if (optionMethod === 'varTypeChange') {
                                this.$set(this.currInputVars[intputIndex], 'referenceVarId', '');
                                this.$set(this.currInputVars[intputIndex], 'referenceVarName', '');
                                this.$set(this.currInputVars[intputIndex], 'referenceVarType', '');
                                this.$set(this.currInputVars[intputIndex], 'referenceTreeData', null);
                                this.$set(this.currInputVars[intputIndex], 'referenceNodeId', '');
                                this.$set(this.currInputVars[intputIndex], 'varValue', '');
                                this.$set(this.currInputVars[intputIndex], 'varType', 'reference');
                            }
                        }
                    } else {
                        let data = {
                            varName: singleData.field,
                            varValue: null,
                            varType: 'reference',
                            varNameType: singleData.type,
                            id: singleData.id,
                            originalVarType: singleData.type,
                            requestType: changeType,
                        };
                        this.currInputVars.push(data);
                    }

                } else {
                    let data = {
                        varName: singleData.field,
                        varValue: null,
                        varType: 'reference',
                        varNameType: singleData.type,
                        id: singleData.id,
                        originalVarType: singleData.type,
                        requestType: changeType,

                    };
                    this.currInputVars.push(data);
                }
            }

            let tempRequestType = '';

            // 请求参数各种不同类型的数据处理
            if (changeType === 'json') {
                this.$set(this.bodyInfo, 'jsonData', params);
                this.$set(this.requestConfig, 'bodyInfo', this.bodyInfo);

            } else if (changeType === 'formData') {
                this.$set(this.bodyInfo, 'formData', params);
                this.$set(this.requestConfig, 'bodyInfo', this.bodyInfo);

            } else if (changeType === 'urlencoded') {
                this.$set(this.bodyInfo, 'formUrlencodedData', params);
                this.$set(this.requestConfig, 'bodyInfo', this.bodyInfo);

            } else {
                // 非body类型请求参数的数据处理
                if (changeType === 'params') {
                    tempRequestType = 'queryParams';
                } else if (changeType === 'header') {
                    tempRequestType = 'headers';
                }

                this.$set(this.requestConfig, tempRequestType, params);
            }
            this.change();

        },
        // json类型响应参数变更
        outputJsonChange(params) {
            this.outputJsonDataList = params;
            this.$set(this.responseConfig, 'jsonResult', this.outputJsonDataList);
            this.$set(this.responseConfig, 'type', 'json');
            this.$set(this.responseConfig, 'jsonData', this.outputJsonDataList);

            this.change();
        },
        // string类型响应参数变更
        outputStringChange(params) {
            let list = [];
            if (params?.length > 0) {
                params.forEach(element => {
                    let data = {
                        type: element.type,
                        field: element.field,
                    };
                    list.push(data);
                });
            }
            this.$set(this.responseConfig, 'type', 'string');
            this.$set(this.responseConfig, 'jsonResult', list);
            this.$set(this.responseConfig, 'jsonData', this.outputJsonDataList);
            this.change();

        },
        // 响应类型变更处理
        responseTypeChange() {
            // let list = [];
            let params = [];

            if (this.responseType === 'json') {
                this.outputJsonDataList = this.apiConfig?.responseConfig?.jsonData;
                if (this.outputJsonDataList?.length > 0) {
                    params = this.outputJsonDataList;
                } else {
                    let data = {
                        type: 'String',
                        field: '',
                        mockValue: '',
                        id: this.uniqueValue()
                    };
                    params = [data];
                }
                this.$set(this.responseConfig, 'jsonData', params);

            } else {
                let data = {
                    type: 'String',
                    field: 'result',
                    mockValue: '',
                    id: this.uniqueValue(),
                };
                params = [data];
            }

            this.$set(this.responseConfig, 'type', this.responseType);
            this.$set(this.responseConfig, 'jsonResult', params);

            this.change();
        },
        // 数据变动后，对整体数据进行处理
        async change() {
            let newRequestConfig = deepClone(this.requestConfig);
            let newResponseConfig = deepClone(this.responseConfig);

            // 请求参数数据格式化
            if (Object.keys(newRequestConfig).length > 0) {
                if (newRequestConfig?.bodyInfo && newRequestConfig?.bodyInfo?.jsonData?.length > 0) {
                    let newJsonData = await replaceMockValues(newRequestConfig?.bodyInfo?.jsonData);
                    this.$set(newRequestConfig.bodyInfo, 'jsonData', newJsonData);
                    let tempJsonData = this.clearParams(newRequestConfig?.bodyInfo?.jsonData || [], 'jsonData');
                    this.$set(newRequestConfig.bodyInfo, 'jsonData', tempJsonData);
                    let tempFormData = this.clearParams(newRequestConfig?.bodyInfo?.formData || [], 'formData');
                    this.$set(newRequestConfig.bodyInfo, 'formData', tempFormData);
                    let tempFormUrlencodedData =
                        this.clearParams(newRequestConfig?.bodyInfo?.formUrlencodedData || [], 'formUrlencodedData');
                    this.$set(newRequestConfig.bodyInfo, 'formUrlencodedData', tempFormUrlencodedData);

                }
                let tempParams = this.clearParams(newRequestConfig?.queryParams || [], 'params');
                this.$set(newRequestConfig, 'queryParams', tempParams);
                let tempHeaders = this.clearParams(newRequestConfig?.headers || [], 'headers');
                this.$set(newRequestConfig, 'headers', tempHeaders);

            }


            // 响应参数数据格式化
            let tempResponseJsonData = this.clearParams(newResponseConfig?.jsonResult || [], 'responseJsonData');
            this.$set(newResponseConfig, 'jsonResult', tempResponseJsonData);
            if (newResponseConfig?.jsonData?.length > 0) {
                let tempResponseJson = newResponseConfig?.jsonData;
                tempResponseJson = this.clearParams(tempResponseJson || [], 'responseJsonData');
                this.$set(newResponseConfig, 'jsonData', tempResponseJson);
            }

            let params = {
                requestConfig: newRequestConfig,
                method: this.requestMethod,
                url: this.url,
                responseConfig: newResponseConfig,
                debugStatus: this.debugStatus,
                inputVars: this.currInputVars,
            };
            this.formatConfigAll = deepClone(params);

            this.computeLength(this.requestConfig);
            this.$emit('apiParamsChange', params);
        },

        // 发送调试请求
        async sendApi() {

            let validate = await this.inputForm.validateFields();
            if (!validate) {
                this.$message.error('请填写必填字段');
                return;
            }
            if (!this.formatConfigAll?.responseConfig.type) {
                this.$message.error('请配置响应参数');
                return;
            }

            this.spinning = true;

            let bodyData = [];
            if (this.formatConfigAll?.requestConfig && this.formatConfigAll?.requestConfig?.bodyInfo) {
                bodyData = this.formatConfigAll?.requestConfig?.bodyInfo?.jsonData || [];
            }
            // 有body-json类型时，先校验json格式
            if (bodyData && bodyData?.length > 0) {
                const responseData = await flowRequest({
                    url: verifyRequestJsonSchema,
                    method: 'post',
                    data: bodyData,
                }).catch(() => {
                    this.$message.error('校验请求字段格式失败, 请重试');
                });
                console.log(responseData);
            }

            let newRequestConfig = deepClone(this.formatConfigAll?.requestConfig);
            let newResponseConfig = deepClone(this.formatConfigAll?.responseConfig);
            // 暂时屏蔽字符串true，false转boolean类型
            // if (newRequestConfig?.bodyInfo) {
            // eslint-disable-next-line max-len
            //     newRequestConfig.bodyInfo.jsonData = this.handleBooleanData(newRequestConfig.bodyInfo.jsonData || []);
            // }

            // 发送api调试
            let newConfigData = {
                requestConfig: newRequestConfig,
                method: this.requestMethod,
                url: this.url,
                responseConfig: newResponseConfig
            };
            const apiDebugResponse = await flowRequest({
                url: apiDebug,
                method: 'post',
                data: newConfigData,
            }).catch(() => {
                this.$message.error('api调试未通过, 请重试');
            });
            this.spinning = false;

            if (!apiDebugResponse || apiDebugResponse === false) {
                this.debugStatus = 'unPass';
            } else {
                // 调试通过后，处理调试结果数据以及响应参数并展示完整的返回结果
                this.debugStatus = 'pass';
                this.debugData = deepClone(apiDebugResponse);
                this.responseJson = JSON.stringify(this.debugData, null, 4);
                this.$nextTick(() => {
                    if (this.editorLeft) {
                        this.editorLeft.setValue(this.responseJson);
                    } else {
                        this.editorLeft = CodeMirror(this.$refs.editorLeft, {
                            mode: 'javascript',
                            theme: 'default',
                            lineNumbers: true,
                            value: this.responseJson,
                            viewportMargin: Infinity,  // 允许滚动
                            height: '240px'
                        });

                    }
                });

                let responseJsonNode = {};
                newResponseConfig.jsonResult.forEach(element => {
                    let keyValue = element.field;
                    if (element.type === 'Object' && element !== null) {
                        this.$set(responseJsonNode, keyValue, {});
                        element.children.forEach(elementChild => {
                            let keyValueChild = elementChild.field;
                            if (apiDebugResponse[keyValue]) {
                                // eslint-disable-next-line max-len
                                this.$set(responseJsonNode[keyValue], keyValueChild, apiDebugResponse[keyValue][keyValueChild]);
                            } else {
                                this.$set(responseJsonNode[keyValue], keyValueChild, null);
                            }
                        });
                    } else {
                        this.$set(responseJsonNode, keyValue, apiDebugResponse[keyValue] || '');
                    }
                });

                // 响应结果右侧展示节点配置的输出结果
                responseJsonNode = JSON.stringify(responseJsonNode, null, 4);
                this.$nextTick(() => {
                    if (this.editorRight) {
                        this.editorRight.setValue(responseJsonNode);
                    } else {
                        this.editorRight = CodeMirror(this.$refs.editorRight, {
                            mode: 'javascript',
                            theme: 'default',
                            lineNumbers: true,
                            value: responseJsonNode,
                            viewportMargin: Infinity,  // 允许滚动
                            height: '240px'
                        });
                    }

                });
            }
            this.change();
        },
        // 暂时屏蔽字符串true，false转boolean类型
        handleBooleanData(array) {
            return array.map(item => {
                if (item.type === 'Boolean' && (item.mockValue === 'true' || item.mockValue === 'false')) {
                    // 对boolean类型进行处理，由于组件只支持0，1或者字符串'true'，'false'，所以需要转换为true，false
                    item.mockValue = item.mockValue.toLowerCase() === 'true';
                }
                if (item.children && item.children?.length > 0) {
                    item.children = this.handleBooleanData(item.children);
                }
                return item;
            });
        },
        // 删除不同类型的参数空参
        clearParams(array) {
            let tempArray = deepClone(array);
            if (!array || array?.length === 0) {
                return;
            }

            tempArray = tempArray.filter(element =>
                (element?.field && element?.field?.length > 0) ||
                (element?.mockValue && element?.mockValue?.length > 0)
            );
            return tempArray;
        },

        // 计算在删除空参后的数组长度
        computeLength(requestArr) {
            let tempParams = this.clearParams(requestArr?.queryParams, 'params');
            this.paramsDataLength = tempParams?.length;
            let tempHeaders = this.clearParams(requestArr?.headers, 'headers');
            this.headersDataLength = tempHeaders?.length;
            let tempBodyData = [];
            if (requestArr?.bodyInfo) {
                if (requestArr.bodyInfo.bodyType === 'json') {
                    tempBodyData = this.clearParams(requestArr?.bodyInfo?.jsonData, 'jsonData');
                } else if (requestArr.bodyInfo.bodyType === 'form_data') {
                    tempBodyData = this.clearParams(requestArr?.bodyInfo?.formData, 'formData');
                } else if (requestArr.bodyInfo.bodyType === 'x_www_form_urlencoded') {
                    tempBodyData = this.clearParams(requestArr?.bodyInfo?.formUrlencodedData, 'formUrlencodedData');
                }
                this.bodyDataLength = tempBodyData?.length;
            }
        },
        // 计算最大id
        currMaxId() {
            let paramsArr = this.apiConfig?.requestConfig?.queryParams || [];
            let headersArr = this.apiConfig?.requestConfig?.headers || [];
            let bodyArr = this.apiConfig?.requestConfig?.bodyInfo?.jsonData || [];

            let allArr = [...paramsArr, ...headersArr, ...bodyArr];

            if (allArr && allArr.length > 0) {
                const maxAge = Math.max(...allArr.map(item => item.id));
                return maxAge || 0;
            } else {
                return 0;
            }
        },
        tabsChange() {
            this.maxId = this.currMaxId();
        },
        // 生成唯一值
        uniqueValue() {
            return Date.now().toString(36) + Math.random().toString(36);
        }
    }
};
</script>

<style lang="less" scoped>
.edit-api-wrapper {
    display: flex;
    flex-direction: column;
    height: 100%;

    .header-wrapper {
        height: 68px;
        margin-bottom: 8px;
        padding: 8px 16px;

        .header-form {
            display: flex;
            margin-bottom: 8px;
            gap: 8px;
        }

        .header-form-item {
            flex: 1;

            .url-select {
                width: 90px;
            }
        }

        .send-btn {
            width: 72px;
            background: #2468F2;
            margin-top: 4px;
        }
    }

    .method-wrapper {
        height: 30px;
        display: flex;
        align-items: flex-end;
        flex-shrink: 0;
        justify-content: flex-start;
        padding: 0 16px;

        .method-header {
            padding: 2px;
            border-radius: 8px;
            background: #74748024;
            display: flex;

            /deep/ .ant-radio-button-wrapper {
                background: transparent;
                border: none;
                color: #84868c;
                font-size: 12px;
                padding: 0 12px;
            }
            /deep/ .ant-radio-group-solid .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled) {
                background: #fff !important;
                color: #151b26;
                border-radius: 8px;
            }
            /deep/ .ant-radio-button-wrapper:not(:first-child)::before {
                display: none;
            }
        }
        .method-content {
            margin-left: 20px;

            /deep/ .ant-tabs-tab {
                padding: 0;
                height: 30px;
                line-height: 30px;
                font-size: 12px;
            }
            /deep/ .ant-tabs-bar {
                margin-bottom: 0;
            }
        }
    }

    .params-wrapper {
        padding: 12px 16px;
        height: calc(100vh - 68px - 340px - 40px);
        overflow-y: auto;

        .params-header {

        }
    }

    .response-wrapper {
        height: 340px;

        .response-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            height: 40px;
            box-sizing: border-box;
            border-top: 1px solid #e8e9eb;
            border-bottom: 1px solid #e8e9eb;
            font-size: 12px;
            line-height: 20px;
            color: #151b26;
            font-weight: 500;
            padding: 0 16px;

            .response-result {
                display: flex;
                gap: 16px;
                font-size: 12px;
                color: #84868c;

                .result {
                    display: flex;
                    gap: 4px;
                    align-items: center;

                    .fail-icon {
                        font-size: 14px;
                        color: #f33e3e;
                    }
                    .success-icon {
                        font-size: 14px;
                        color: #30bf13;
                    }
                    .success-text {
                        color: #30bf13;
                        margin-left: 4px;
                    }
                    .fail-text {
                        color: #f33e3e;
                        margin-left: 4px;
                    }
                }
                .result-status {
                    display: flex;
                    gap: 4px;

                    .result-status-code {
                        color: #f33e3e;
                    }
                }
                .result-detail {
                    display: flex;
                    gap: 4px;

                    .result-detail-content {
                        color: #000;
                    }
                }
            }
        }

        .response-content {
            overflow-y: auto;
            height: 100%;
            text-align: left;
            color: #f33e3e;
            font-size: 12px;
            display: flex;

            .content-item {
                width: 50%;
            }
        }
    }

    .authorization-form-item {
        display: flex;
        gap: 8px;
        font-size: 12px;

        /deep/ .ant-form-item-label {
            width: 140px;
            text-align: left;
        }
        /deep/ .ant-form-item-required::before  {
            display: none;
        }
        /deep/ .ant-form-item-required::after {
            display: inline-block;
            margin-right: 4px;
            color: #f5222d;
            font-size: 14px;
            font-family: SimSun, sans-serif;
            line-height: 1;
            content: '*';
        }

        /deep/ .ant-form-item-control-wrapper {
            flex: 1;
        }
        /deep/ .ant-radio-wrapper {
            width: 130px;
            margin-right: 24px;
        }
        .suffix-text {
            color: #84868C;
            opacity: .9;
        }
    }
    .item-wrap {
        /deep/ .ant-form-item-label {
            line-height: 18px;
            white-space: normal;
        }
    }

    .params-content {
        margin-top: 10px;
    }

    .content-item-title {
        padding: 5px 15px;
        color: #151b26;
        font-size: 12px;
    }

    /deep/ .CodeMirror {
        height: 240px !important;
    }
}

</style>