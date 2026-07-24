<template>
    <div
        class="connect-config-item"
    >
        <div class="config-item-title">
            <div class="flex-center">
                <span>输出</span>
                <!-- <a-tooltip title="输入描述">
                    <a-icon
                        type="question-circle"
                    />
                </a-tooltip> -->
            </div>
            <!-- <a-icon type="sliders" /> -->
        </div>
        <div class="config-item-content-title">
            参数名
        </div>
        <div class="config-item-content">
            <div
                v-for="sub in outputVars"
                :key="sub.varName"
                class="config-item-content-item-warp"
            >
                <div class="config-item-content-item">
                    <a-icon
                        :type="sub.expanded ? 'caret-down' : 'caret-right'"
                        @click.stop="sub.expanded = !sub.expanded"
                        v-if="sub.children && sub.children.length > 0"
                    />
                    <span
                        class="config-item-content-key"
                    >
                        {{ sub.varName }}
                    </span>
                    <span
                        class="config-item-content-value"
                    >{{ sub.originalVarType || sub.varType }}</span>
                </div>

                <div
                    class="output-child"
                    v-if="sub.expanded"
                >
                    <div
                        v-for="(subChild, indexChild) in sub.children"
                        :key="`${subChild.varName}_${indexChild}`"
                    >
                        <div
                            class="config-item-content-item"
                        >
                            <span
                                class="item-default"
                            ></span>

                            <a-icon
                                :type="subChild.expanded ? 'caret-down' : 'caret-right'"
                                @click.stop="subChild.expanded = !subChild.expanded"
                                v-if="subChild.children && subChild.children.length > 0"
                            />
                            <span
                                class="config-item-content-key"
                            >{{ subChild.varName }}</span>
                            <span
                                class="config-item-content-value"
                            >{{ subChild.originalVarType || sub.varType }}</span>
                        </div>

                        <div
                            class="output-child"
                            v-if="subChild.expanded"
                        >
                            <div
                                v-for="(subChildChild, indexChildChild) in subChild.children"
                                :key="`${subChildChild.varName}_${indexChildChild}`"
                            >
                                <div
                                    class="config-item-content-item"
                                >
                                    <span
                                        class="item-default"
                                    ></span>

                                    <a-icon
                                        :type="subChild.expanded ? 'caret-down' : 'caret-right'"
                                        @click.stop="subChild.expanded = !subChild.expanded"
                                        v-if="subChildChild.children && subChildChild.children.length > 0"
                                    />
                                    <span
                                        class="config-item-content-key"
                                    >{{ subChildChild.varName }}</span>
                                    <span
                                        class="config-item-content-value"
                                    >{{ subChildChild.originalVarType || sub.varType }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { deepClone } from '@baidu/metis-js-util';

export default {
    props: {
        outputVars: {
            type: Array,
            default: () => []
        },
        isCanExpand: {
            type: Boolean,
            default: true
        }
    },
    watch: {
        outputVars() {
            this.currOutputVars = deepClone(this.outputVars);
            this.outputVarsOptions(this.currOutputVars);
        }
    },
    data() {
        return {
            currOutputVars: this.outputVars,
        };
    },
    methods: {
        outputVarsOptions(array) {
            return array.map((item, index) => {
                // 创建新对象（保持原对象不变性）
                this.$set(item, 'expanded', false);
                this.$set(item, 'id', index + 1);

                return item;
            });

        }
    }
};
</script>

<style lang="less" scoped>
.output-child {
    padding-left: 20px;
}
.connect-config-item {
    display: block;
    padding: 10px 0;
    border-bottom: 1px solid #e8e9eb;
    font-size: 12px;

    .config-item-minus {
        margin-left: 10px;
    }
}
.config-item-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    font-weight: bold;

    span {
        margin-right: 10px;
        font-weight: bold;
        color: #151b26;
        font-size: 14px;
    }
}
/deep/ .ant-form-item {
    margin-bottom: 0;
}
/deep/ .ant-form-item-label > label::after {
    display: none;
}
.config-item-content-title {
    display: flex;
    align-items: center;
    color: #84868c;
    gap: 4px;
    margin-bottom: 4px;
    margin-top: 10px;
}
.config-item-content-item {
    display: flex;
    align-items: baseline;
    gap: 4px;
    color: #151b26;
    margin-bottom: 5px;

    .config-item-content-key {
    }
    .varType-select {
        flex: 0 0 102px;
        width: 102px
    }
}

.config-item-content {
    cursor: pointer;
    user-select: none;
    line-height: 22px;
    position: relative;
    word-break: break-word;
    margin-bottom: 4px;
    .config-item-content-item {
        .config-item-content-key {
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        .config-item-content-value {
            display: flex;
            align-items: center;
            flex-shrink: 0;
            margin-left: 4px;
            padding: 0 5px;
            background-color: #e8e9eb;
            border-radius: 4px;
            white-space: nowrap;
            height: 20px;
            color: #5c5f66;
            line-height: 20px;
        }

    }
}

.form-item-warp {
    padding: 0 12px;
    background: #f9f9fb;
    display: flex;
    align-items: center;
    margin-bottom: 0;

    /deep/ .ant-input-number {
        width: auto;
    }
    .value-type {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 16px;
        height: 16px;
        color: #144bcc;
        border-radius: 4px;
        background: #d4e5ff;
        font-size: 12px;
        font-weight: bold;
        margin-right: 4px;
    }
    .value-values {
        color: #5c5f66;
        font-size: 12px;
        font-weight: 500;
        line-height: 20px;
    }
    .value-limit {
        color: #84868c;
        font-size: 12px;
        font-weight: 400;
        line-height: 20px;
    }
}

/deep/ .ant-form label {
    font-size: 12px !important;
}

.config-item-plus {
    font-size: 16px;
    color: #2468f2;
    cursor: pointer;
}

.config-item-content-knowledge {
    display: flex;
    overflow: auto;
    flex-direction: column;
    box-sizing: border-box;
    max-height: 398px;
    padding: 0 1px;
    gap: 8px;

    .knowledge-item {
        position: relative;
        display: flex;
        align-items: center;
        flex: 1;
        justify-content: space-between;
        box-sizing: border-box;
        height: 32px;
        padding: 6px;
        border: 1px solid rgba(212, 214, 217, .3);
        border-radius: 6px;

        .item-left {
            flex: 1;
            min-width: 0;

            img {
                width: 20px;
                height: 20px;
            }
            span {
                min-width: 0;
                margin-left: 8px;
                color: #151b26;
                font-weight: 500;
                line-height: 22px;
            }
        }
        img {
            width: 16px;
            height: 16px;
            cursor: pointer;
        }
    }
}
.output-child {
    padding-left: 16px;
}
.originalVarType {
    display: inline-block;
    height: 20px;
    line-height: 20px;
    margin-left: 4px;
    padding: 0 5px;
    white-space: nowrap;
    border-radius: 4px;
    background-color: #e8e9eb;
}
.item-default {
    width: 12px;
}
</style>
