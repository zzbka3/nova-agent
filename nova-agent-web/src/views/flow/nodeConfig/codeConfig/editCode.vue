<!--
 * @Author: hewenquan
 * @Date: 2025-08-11 14:38:09
 * @LastEditTime: 2025-08-18 11:39:42
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/nodeConfig/codeConfig/editCode.vue
 * @Description: code 编辑
-->
<template>
    <div
        class="edit-code-wrapper"
    >
        <a-spin
            :spinning="spinning"
        >
            <div class="edit-title-wrapper">
                <span class="edit-title">
                    Python
                </span>
                <div
                    class="edit-back"
                    @click="closeEdit"
                >
                    <a-icon
                        type="arrow-right"
                        class="edit-back-icon"
                    />
                </div>
            </div>
            <myCodeMirror
                class="code-editor"
                :code-data.sync="code"
                @codeDataChange="codeDataChange"
                ref="myCodeMirror"
            />
            <div class="code-test">
                <div class="code-input-wrapper">
                    <div class="code-input-title">
                        <div>
                            输入测试
                        </div>
                        <div class="test-btn">
                            <div
                                class="test-btn-item"
                                @click="fillInput"
                            >
                                填充数据
                            </div>
                            <div
                                class="test-btn-item"
                                @click="runTest"
                            >
                                运行
                            </div>
                        </div>
                    </div>
                    <div class="code-input-preview">
                        <myCodeMirror
                            :code-data.sync="inputTest"
                            cm-mode="application/json"
                            class="code-editor-input"
                        />
                    </div>
                </div>
                <div class="code-input-wrapper">
                    <div class="code-input-title">
                        <div>
                            输出结果
                        </div>
                        <div class="test-btn">
                            <div
                                class="test-btn-item"
                                @click="copy"
                            >
                                复制
                            </div>
                        </div>
                    </div>
                    <div class="code-input-preview">
                        <myCodeMirror
                            v-if="outputResult"
                            :read-only="true"
                            :code-data="outputResult"
                            class="code-editor-input"
                            cm-mode="application/json"
                        />
                        <div
                            v-else
                            class="no-result"
                        >
                            请在左侧填写输入数据，运行后查看输出结果
                        </div>
                    </div>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script>
import myCodeMirror from '@/views/flow/nodeConfig/components/myCodeMirror';
import { copy } from '@baidu/metis-js-util';
import { codeDebug } from '@/views/flow/apiList';
import { flowRequest } from '@/views/flow/common/request';
export default {
    props: {
        codeData: {
            type: String,
            default: ''
        },
        // 输入
        inputVars: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            code: this.codeData, // python代码
            inputTest: '', // 输入测试数据
            outputResult: '', // 输出结果
            spinning: false, // 页面加载loading
        };
    },
    components: {
        myCodeMirror,
    },
    created() {
        this.fillInput();
    },
    methods: {
        codeDataChange(codeData) {
            this.$emit('update:codeData', codeData);
            this.$emit('codeDataChange', codeData);
        },
        /**
        * 关闭编辑模式
        *
        */
        closeEdit() {
            this.$emit('close');
        },
        // 填充数据
        fillInput() {
            let inputTest = {};
            if (!this.inputVars || !this.inputVars.length){
                return;
            }
            const getDefaultValue = (varType, varValue) => {
                switch (varType) {
                    case 'String':
                        return varValue || (Math.random() * 100).toString();
                    case 'Number':
                        return varValue !== '' ? Number(varValue) : Math.round(Math.random() * 100);
                    case 'Integer':
                        return varValue !== '' ? Number(varValue) : Math.round(Math.random() * 100);
                    case 'Boolean':
                        return varValue !== '' ? varValue : Math.random() > 0.5;
                    case 'Any':
                        return varValue !== '' ? varValue : null;
                    case 'Object':
                        return varValue || {};
                    default:
                        return varValue || [];
                }
            };
            this.inputVars.forEach(item => {
                const { varType, varValue = '', referenceVarType, varName } = item || {};
                if (varName) {
                    // 引用类型
                    if (varType === 'reference') {
                        inputTest[varName] = getDefaultValue(referenceVarType);
                    } else {
                        inputTest[varName] = getDefaultValue(varType, varValue);
                    }
                }
            });
            console.log(inputTest, 'inputTest');
            this.inputTest = JSON.stringify(inputTest);
        },
        /**
        * 运行测试
        */
        async runTest() {
            let parseInput = null;
            if (!this.inputTest) {
                this.$message.error('请完善测试数据');
                return;
            }
            try {
                parseInput = JSON.parse(this.inputTest);
            } catch (error) {
                console.log(error);
            }
            // JSON 简单校验
            if (!parseInput) {
                this.$message.error('输入数据格式错误');
                return;
            }
            this.spinning = true;
            // codeDebug
            const data = {
                code: this.code,
                params: parseInput,
            };
            const res = await flowRequest({
                url: codeDebug,
                method: 'post',
                data
            }).catch(err => {
                console.log(err, 'err');
                this.outputResult = JSON.stringify({
                    error: '运行失败'
                });
            });
            this.spinning = false;
            if (res) {
                console.log(res, 'res');
                this.outputResult = res;
            }
        },
        /**
        * 复制方法
        *
        * @returns void
        */
        copy() {
            const res = copy(JSON.stringify(this.outputResult));
            if (res) {
                this.$message.success('复制成功');
            }
        }
    }
};
</script>

<style lang="less" scoped>
.edit-code-wrapper {
    width: calc(100vw - 400px);
    height: 100%;
    position: fixed;
    top: 0px;
    right: 400px;
    background: #070c1480;
    border-right: 1px solid #e8e9eb;
    z-index: 999;
    padding-left: 100px;
    box-sizing: border-box;
    /deep/ .ant-spin-nested-loading {
        height: 100%;
        .ant-spin-spinning {
            max-height: unset;
        }
        .ant-spin-container {
            height: 100%;
        }
        .ant-spin-blur {
            opacity: 1;
        }
        .ant-spin-container::after {
            background: rgba(255, 255, 255, 0.5);
        }
    }
    .edit-title-wrapper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        height: 40px;
        padding: 0 16px;
        background: #2d2e2e;
        .edit-title {
            font-size: 14px;
            font-weight: 500;
            color: #f7f7f9;
        }
        .edit-back {
            border-radius: 4px;
            cursor: pointer;
            padding: 4px 8px;
            &:hover {
                background: #ffffff1a;
            }
            .edit-back-icon {
                color: #f7f7f9;
            }
        }
    }
    .code-editor {
        height: 70%;
    }
    .code-test {
        height: calc(30% - 40px);
        display: flex;
        .code-input-wrapper {
            height: 100%;
            width: 50%;
            border-right: 1px solid #282929;
            box-sizing: border-box;
            .code-input-title {
                display: flex;
                justify-content: space-between;
                align-items: center;
                height: 40px;
                padding: 0 16px;
                background: #282929;
                color: #f7f7f9;
                font-size: 13px;
                .test-btn {
                    display: flex;
                    .test-btn-item {
                        margin: 0 5px;
                        padding: 5px 7px;
                        border-radius: 2px;
                        cursor: pointer;
                        &:hover {
                            background: #333;
                        }
                    }
                }
            }
            .code-input-preview {
                height: calc(100% - 40px);
                background: #282929;
                .code-editor-input {
                    height: 100%;
                }
                .no-result {
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    color: #8b8b8c;
                }
            }
        }
    }
}
</style>