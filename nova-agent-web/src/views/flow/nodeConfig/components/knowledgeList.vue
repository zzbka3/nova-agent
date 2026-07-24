<template>
    <div class="knowledge-list-warp">
        <div class="knowledge-list-warp-header">
            <div>
                <a-input-search
                    placeholder="搜索知识库关键词"
                    style="width: 240px"
                    @search="onSearch"
                />
            </div>
            <a-button
                type="primary"
                class="creatBtn"
            >
                创建知识库
            </a-button>
        </div>

        <div class="knowledge-list-warp-content">
            <div
                class="knowledge-list-warp-content-item"
                v-for="(i, index) in knowledgeAllList"
                :key="i.id"
            >
                <div class="item-icon">
                    <img src="@/assets/knowledge_file_icon.png" />
                </div>
                <div class="item-content">
                    <div
                        class="item-content-title"
                    >
                        {{ i.knowledgeName }}
                    </div>
                    <div class="item-content-info">
                        <span>id: {{ i.id }}</span>
                        <!-- <span>共享资源</span> -->
                        <!-- <div class="item-content-info-modelinfo">
                            <img src="@/assets/knowledge_file_model_icon.png" />
                            <span>multilingual-embedding-AppBuilder</span>
                        </div> -->
                    </div>
                </div>
                <a-button
                    :class="i.isSelect ? 'item-delete-btn' : 'item-add-btn'"
                    type="text"
                    @click="itemHandleClick(i, index)"
                >
                    {{ i.isSelect ? '移除' : '添加' }}
                </a-button>
            </div>
            <div v-if="!knowledgeAllList.length">
                <a-empty />
            </div>
        </div>
    </div>
</template>

<script>
import { knowledgeList } from '@/views/flow/apiList';
import request from '@/utils/http';
export default {
    props: {
        knowledgeSelectedList: {
            type: Array,
            default: () => [],
        }
    },
    data() {
        return {
            isAddItem: false,
            knowledgeAllList: [
                // {
                //     knowledgename: '测试知识库文档一测试知识库文档一测试知识库文档一测试知识库文档一测试知识库文档一测试知识库文档一',
                //     id: '822d3981-f5a4-4bbe-88f4-cb8a7aef3e09'
                // }
            ],
        };
    },
    computed: {
        productLine() {
            return this.$route.params.productLine;
        },
    },
    watch: {
        knowledgeSelectedList(val) {
            this.isAddItem = val.length > 0;
        }
    },
    mounted() {
        this.isAddItem = this.knowledgeSelectedList.length > 0;
    },
    created() {
        this.initKnowledgeList();
    },
    methods: {
        onSearch(value) {
            this.initKnowledgeList(value);
        },
        /**
         * @description: 获取知识库列表
         * @return {*}
         */
        async initKnowledgeList(query = '') {
            const res = await request({
                url: knowledgeList,
                method: 'post',
                data: {
                    productId: this.productLine,
                    param: query,
                }
            });
            if (res) {
                this.knowledgeAllList = res.map(item => {
                    return {
                        ...item,
                        isSelect: this.knowledgeSelectedList.some(select => select.id === item.id),
                    };
                });
            }
        },
        itemHandleClick(item, index) {
            const { isSelect } = item || {};
            this.knowledgeAllList[index].isSelect = !isSelect;
            this.$emit('itemHandleClick', !isSelect, item);
        }
    }
};
</script>

<style lang="less" scoped>
.knowledge-list-warp {
    padding: 0 16px;
    min-height: 400px;
}
.knowledge-list-warp-header {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;

    .creatBtn {
        color: #fff;
        background: #2468F2;
        font-size: 14px;
    }
}
.knowledge-list-warp-content {
    margin-top: 12px;

    .knowledge-list-warp-content-item {
        display: flex;
        overflow: hidden;
        padding: 12px 0;
        border-bottom: 1px solid #e8e9eb;
        justify-content: flex-end;
        align-items: center;

        .item-icon {
            width: 40px;
            height: 40px;
            border-radius: 20px;
            overflow: hidden;
            img {
                width: 100%;
                height: 100%;
            }
        }
        .item-content {
            display: flex;
            overflow: hidden;
            flex: 1;
            flex-direction: column;
            padding: 0 16px;

            .item-content-title {
                color: #192338;
                font-weight: 600;
                display: inline-block;
                max-width: 100%;
                vertical-align: bottom;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .item-content-info {
                display: flex;
                align-items: center;
                gap: 16px;
                white-space: nowrap;
                color: #151b26;
                font-size: 12px;
                font-weight: 400;

                .item-content-info-modelinfo {
                    overflow: hidden;
                    display: flex;
                    align-items: center;

                    img {
                        width: 16px;
                        height: 16px;
                        border-radius: 4px;
                        margin-right: 4px;
                    }
                    span {
                        overflow: hidden;
                        flex: 1;
                        color: #151b26;
                        font-size: 14px;
                        line-height: 20px;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                        word-break: break-word;
                    }
                }
            }
        }
        .item-delete-btn {
            color: rgb(243, 62, 62);
            font-size: 14px;
            border-color: rgb(243, 62, 62);
        }
        .item-add-btn {
            color: #151B26;
            font-size: 14px;
            border-color: #d4d6d9;
        }
    }
}
</style>