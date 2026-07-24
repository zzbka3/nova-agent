<template>
    <div class="knowledge-list-components-warp">
        <div
            class="connect-config-item"
            :style="{ borderBottom: 'none', marginBottom: 0, pandingBottom: 0 }"
        >
            <div class="config-item-title">
                <div class="flex-center">
                    <span>知识库</span>
                    <!-- <a-tooltip title="输入描述">
                        <a-icon
                            type="question-circle"
                        />
                    </a-tooltip> -->
                </div>
                <div @click="openList">
                    <a-icon
                        class="config-item-plus"
                        type="plus"
                    />
                </div>
            </div>
            <div class="config-item-content">
                <div
                    class="config-item-content-knowledge"
                    v-if="currKnowledgeDataList.length > 0"
                >
                    <div
                        class="knowledge-item"
                        v-for="item in currKnowledgeDataList"
                        :key="item.id"
                    >
                        <div class="item-left">
                            <img src="@/assets/knowledge_file.png" />
                            <span class="knowledgeName">
                                {{ item.knowledgeName || '' }}
                            </span>
                        </div>
                        <div
                            class="item-right"
                            @mouseover="showDelete(true)"
                            @mouseleave="showDelete(false)"
                        >
                            <img
                                src="@/assets/knowledge_file_delete.png"
                                @click="deleteItem(item)"
                                v-if="isShowDelete"
                            />
                            <img
                                src="@/assets/knowledge-file_success.png"
                                v-else
                            />
                        </div>
                    </div>
                </div>
                <div
                    class="config-item-content-knowledge"
                    v-else
                >
                    <div
                        class="knowledge-item"
                        style="justify-content: center"
                        @click="openList"
                    >
                        未添加知识库
                    </div>
                </div>
            </div>
        </div>
        <a-modal
            v-model="isShowKnowledgeList"
            v-if="isShowKnowledgeList"
            title="添加知识库"
            width="700px"
            :footer="null"
        >
            <template slot="title">
                <div class="knowledge-list-warp-top">
                    <div class="flex-between">
                        <span class="knowledge-list-warp-title">添加知识库</span>
                        <a-icon type="exclamation-circle" />
                        <span class="knowledge-list-warp-title-tips">已添加1个 / 共计可添加 50 个</span>
                    </div>
                    <a-icon type="close" />
                </div>
            </template>
            <knowledgeList
                class="knowledge-list-warp"
                @itemHandleClick="itemHandleClick"
                :knowledge-selected-list="currKnowledgeDataList"
            ></knowledgeList>
        </a-modal>
    </div>
</template>

<script>
import knowledgeList from './knowledgeList';
import { deepClone } from '@baidu/metis-js-util';

export default {
    props: {
        knowledgeDataList: {
            type: Array,
            default: () => ([])
        },
    },
    components: {
        knowledgeList
    },
    data() {
        return {
            inputType: 'String',
            replaceFields: { // 树形控件字段映射
                title: 'varName',
                value: 'varName',
                children: 'children'
            },
            dataForm: {
                key: 'query',
                inputType: 'String',
                inputValue: ''
            },
            currInputData: this.inputData,
            isShowKnowledgeList: false,
            currKnowledgeDataList: [],
            isShowDelete: false,
        };
    },
    watch: {
        knowledgeDataList(val) {
            this.currKnowledgeDataList = deepClone(val);
            console.log('val', val);
        }
    },
    mounted() {
        this.currKnowledgeDataList = deepClone(this.knowledgeDataList);
    },
    methods: {
        onSearch() {},
        openList() {
            this.isShowKnowledgeList = true;
        },
        closeList() {
            this.isShowKnowledgeList = false;
        },
        inputValueChange() {
            this.change();
        },
        selectValueChange() {
            this.change();
        },
        // 文件数据改变更新
        change() {
            const value = this.currKnowledgeDataList;
            console.log('value', JSON.stringify(value), value.length);
            // let valueList = [value];
            this.$emit('update:value', value);
            this.$emit('change', value);
        },
        itemHandleClick(isAddItem, item) {
            console.log('isAddItem', isAddItem, item);
            if (isAddItem) {
                this.currKnowledgeDataList.push(item);
            } else {
                for (let index = 0; index < this.currKnowledgeDataList.length; index++) {
                    const element = this.currKnowledgeDataList[index];
                    if (element.id === item.id) {
                        this.currKnowledgeDataList.splice(index, 1);
                        break;
                    }
                }
            }
            this.change();

        },
        showDelete(value) {
            this.isShowDelete = value;
        },
        deleteItem(item) {
            for (let index = 0; index < this.currKnowledgeDataList.length; index++) {
                const element = this.currKnowledgeDataList[index];
                console.log('element', element);
                if (element.id === item.id) {
                    this.currKnowledgeDataList.splice(index, 1);
                    break;
                }
            }
            this.change();
        }
    }
};
</script>

<style lang="less" scoped>
.flex-center {
    display: flex;
    align-items: center;
}
.knowledge-list-components-warp {
    .connect-config-item {
        display: block;
        padding: 0;

        .config-item-minus {
            margin-left: 10px;
        }
    }

    .config-item-header {
        display: flex;
        align-items: center;
        color: #84868c;
        gap: 4px;
        margin-bottom: 4px;

        .header-key {
            width: 98px;
        }
        .header-type {
            flex: 0 0 102px;
        }
    }

    .config-item-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: bold;

        span {
            margin-right: 10px;
            font-weight: bold;
            color: #151b26;
            font-size: 14px;
        }
    }
    .config-item-content {
        cursor: pointer;
        user-select: none;
        line-height: 22px;
        position: relative;
        word-break: break-word;
        margin-bottom: 4px;
    }
    .config-item-content-item {
        display: flex;
        align-items: baseline;
        gap: 4px;
        color: #151b26;

        .config-item-content-key {
            width: 98px;
        }
        .config-item-content-key:after  {
            content: "*";
            color: #f33d3d;
            margin-inline-start: 2px;
        }
        .varType-select {
            flex: 0 0 102px;
            width: 102px
        }
        .varValue {
            width: 142px;
        }
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
                display: flex;
                img {
                    width: 20px;
                    height: 20px;
                }
                .knowledgeName {
                    display: inline-block;
                    min-width: 0;
                    max-width: 280px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    margin-left: 8px;
                    color: #151b26;
                    font-weight: 500;
                    line-height: 22px;
                }
            }

        }
    }
    .config-item-plus {
        font-size: 16px;
        color: #2468f2;
        cursor: pointer;
    }
    .item-right {
        cursor: pointer;

        img {
            width: 16px;
            height: 16px;
        }
    }
}
</style>