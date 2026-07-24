<template>
    <div class="warp-content-item">
        <div class="warp-content-item-title flex-between">
            <div class="warp-content-item-title">
                <a-icon
                    :type="expandedOutput ? 'caret-down' : 'caret-right'"
                    @click.stop="expandOutput"
                />
                <span class="warp-content-item-title-name">输出</span>
            </div>
        </div>
        <div v-if="expandedOutput">
            <div
                v-for="(item, index) in outputList"
                :key="item.varName + index"
            >
                <div class="warp-content-item-content">
                    <a-icon
                        :type="item.expanded ? 'caret-down' : 'caret-right'"
                        @click.stop="item.expanded = !item.expanded"
                        v-if="item.children && item.children.length > 0"
                    />

                    <span class="output-key">{{ item.varName }}</span>
                    <span class="output-type">{{ item.varNameType }}</span>
                </div>

                <div v-if="item.expanded">
                    <div
                        class="item-content-child"
                        v-for="(itemChild, indexChild) in item.children"
                        :key="itemChild.varName + indexChild"
                    >
                        <div class="warp-content-item-content">
                            <a-icon
                                :type="itemChild.expanded ? 'caret-down' : 'caret-right'"
                                @click.stop="itemChild.expanded = !itemChild.expanded"
                                v-if="itemChild.children && itemChild.children.length > 0"
                            />

                            <span class="output-key">{{ itemChild.varName }}</span>
                            <span class="output-type">{{ itemChild.varNameType }}</span>
                        </div>
                        <div v-if="itemChild.expanded">
                            <div
                                class="warp-content-item-content item-content-child"
                                v-for="(itemChildChild, indexChildChild) in itemChild.children"
                                :key="itemChildChild.varName + indexChildChild"
                            >
                                <div class="warp-content-item-content">
                                    <span class="output-key">{{ itemChildChild.varName }}</span>
                                    <span class="output-type">{{ itemChildChild.varNameType }}</span>
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

export default {
    props: {
        outputList: {
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
            this.outputVarsOptions(this.outputList);
        }
    },
    mounted() {
        console.log('outputList2222', this.outputList);
    },
    data() {
        return {
            expandedOutput: true,
            expandedOutputParent: false
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
        },
        expandOutput() {
            this.expandedOutput = !this.expandedOutput;
            this.$emit('expandOutput', this.expandedOutput);
        }
    }
};
</script>

<style lang="less" scoped>
    .warp-content-item {
        padding: 14px;
        color: #5c5f66;
        border-radius: 8px;
        background-color: #f9f9fb;
        line-height: 20px;

        .warp-content-item-title {
            gap: 8px;
            display: flex;
            align-items: center;

            a-icon {
                cursor: pointer;
            }
            img {
                width: 15px;
                height: 15px;
            }
            .warp-content-item-title-name {
                font-weight: bold;
                flex: 1;
                color: #151b26;
            }
            .warp-content-item-title-value {
                display: flex;
                flex: 1;
                min-width: 0;
            }
        }
        .warp-content-item-content {
            display: flex;
            align-items: center;
            margin-top: 4px;
            gap: 8px;

            .warp-content-item-content-left {
                display: flex;
                flex: 1;
                min-width: 0;
            }
            .content-key {
                overflow: hidden;
                max-width: 160px;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .required-tag:after {
                content: "*";
                color: #f33d3d;
                margin-inline-start: 2px;
            }
            .content-key-type {
                height: 20px;
                margin-left: 4px;
                padding: 0 5px;
                white-space: nowrap;
                border-radius: 4px;
                background-color: #e8e9eb;
            }
            .warp-content-item-content-right {
                display: flex;
                flex: 1;
                min-width: 0;

                .content-default{
                    flex-shrink: 0;
                    width: 14px;
                }
                .content-value {
                    display: flex;
                    box-sizing: border-box;
                    width: fit-content;
                    max-width: calc(100% - 8px);
                    padding: 0 4px;
                    border: 1px solid #e8e9eb;
                    border-radius: 4px;
                    background-color: #fff;
                }
            }
            .output-key {
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
            .output-type {
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
            img {
                width: 15px;
                height: 15px;
            }
            .knowledgeName {
                color: #151B26;
                word-break: break-word;
                line-height: 1.67;
                font-size: 12px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                display: inline-block;
                max-width: 100%;
                margin-left: 6px;
            }
        }
        .warp-content-item-content-tips {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 8px;
            background: #fff;
            border-radius: 4px;
            font-size: 12px;
            margin: 4px 0 8px;

            .tips-title {
                margin-bottom: 4px;
                font-size: 12px;
                font-weight: bold;
                color: #84868c;
            }
            .tips-content {
                word-break: break-all;
                -webkit-line-clamp: 2;
                color: #5c5f66;
                display: -webkit-box;
                overflow: hidden;
                -webkit-box-orient: vertical;
                text-overflow: ellipsis;
            }
        }
        .item-content-child {
            padding-left: 22px;
        }
    }
</style>
