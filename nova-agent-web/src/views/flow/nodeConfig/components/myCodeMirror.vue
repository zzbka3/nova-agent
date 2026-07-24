<template>
    <codemirror
        ref="myCm"
        v-model="editorValue"
        :options="cmOptions"
        @changes="onCmCodeChanges"
        @blur="onCmBlur"
        @keydown.native="onKeyDown"
        @mousedown.native="onMouseDown"
        @paste.native="OnPaste"
        class="code-mirror-test"
    >
    </codemirror>
</template>

<script>
import { codemirror } from 'vue-codemirror';
window.jsonlint = require('jsonlint-mod');
import 'codemirror/lib/codemirror.css';
import 'codemirror/keymap/sublime';
import 'codemirror/mode/javascript/javascript.js';
import 'codemirror/mode/xml/xml.js';
import 'codemirror/mode/htmlmixed/htmlmixed.js';
import 'codemirror/mode/css/css.js';
import 'codemirror/mode/yaml/yaml.js';
import 'codemirror/mode/sql/sql.js';
import 'codemirror/mode/python/python.js';
import 'codemirror/mode/markdown/markdown.js';
import 'codemirror/addon/hint/show-hint.css';
import 'codemirror/addon/hint/show-hint.js';
import 'codemirror/addon/hint/javascript-hint.js';
import 'codemirror/addon/hint/xml-hint.js';
import 'codemirror/addon/hint/css-hint.js';
import 'codemirror/addon/hint/html-hint.js';
import 'codemirror/addon/hint/sql-hint.js';
import 'codemirror/addon/hint/anyword-hint.js';
import 'codemirror/addon/lint/lint.css';
import 'codemirror/addon/lint/lint.js';
import 'codemirror/addon/lint/json-lint';
import 'codemirror/addon/selection/active-line';
import 'codemirror/addon/hint/show-hint.js';
import 'codemirror/addon/hint/anyword-hint.js';
import 'codemirror/addon/lint/javascript-lint.js';
import 'codemirror/addon/fold/foldcode.js';
import 'codemirror/addon/fold/foldgutter.js';
import 'codemirror/addon/fold/foldgutter.css';
import 'codemirror/addon/fold/brace-fold.js';
import 'codemirror/addon/fold/xml-fold.js';
import 'codemirror/addon/fold/comment-fold.js';
import 'codemirror/addon/fold/markdown-fold.js';
import 'codemirror/addon/fold/indent-fold.js';
import 'codemirror/addon/edit/closebrackets.js';
import 'codemirror/addon/edit/closetag.js';
import 'codemirror/addon/edit/matchtags.js';
import 'codemirror/addon/edit/matchbrackets.js';
import 'codemirror/addon/selection/active-line.js';
import 'codemirror/addon/search/jump-to-line.js';
import 'codemirror/addon/dialog/dialog.js';
import 'codemirror/addon/dialog/dialog.css';
import 'codemirror/addon/search/searchcursor.js';
import 'codemirror/addon/search/search.js';
import 'codemirror/addon/display/autorefresh.js';
import 'codemirror/addon/selection/mark-selection.js';
import 'codemirror/addon/search/match-highlighter.js';
import 'codemirror/theme/monokai.css';
import 'codemirror/theme/base16-light.css';
export default {
    components: { codemirror },
    props: {
        // 编辑器主题
        cmTheme: {
            type: String,
            default: 'monokai'
        },
        // 代码格式
        cmMode: {
            type: String,
            default: 'python'
        },
        // 缩进格数
        cmIndentUnit: {
            type: Number,
            default: 4
        },
        // 是否只读
        readOnly: {
            type: Boolean,
            default: false
        },
        // json自动格式化
        autoFormatJson: {
            type: Boolean,
            default: true
        },
        // 代码数据
        codeData: {
            type: String,
            default: ''
        }
    },
    watch: {
        codeData(val) {
            if (val !== this.editorValue) {
                this.editorValue = val;
                // JSON 模式下，自动格式化
                if (this.cmOptions.mode === 'application/json') {
                    if (this.enableAutoFormatJson) {
                        this.editorValue = this.formatStrInJson(val);
                    }
                }
            }
        }
    },
    data() {
        return {
            editorValue: '', // 编辑器内容
            cmOptions: {
                readOnly: this.readOnly,
                theme: this.cmTheme,  // 主题
                mode: this.cmMode,  // 代码格式
                tabSize: 4,  // tab的空格个数
                indentUnit: !this.cmIndentUnit ? 2 : this.cmIndentUnit,  // 一个块（编辑语言中的含义）应缩进多少个空格
                autocorrect: true,  // 自动更正
                spellcheck: true,  // 拼写检查
                lint: true,  // 检查格式
                lineNumbers: true,  // 是否显示行数
                lineWrapping: true, // 是否自动换行
                styleActiveLine: true,  // line选择是是否高亮
                keyMap: 'sublime',  // sublime编辑器效果
                matchBrackets: true,  // 括号匹配
                autoCloseBrackets: true,  // 在键入时将自动关闭括号和引号
                matchTags: { bothTags: true },  // 将突出显示光标周围的标签
                foldGutter: true,  // 可将对象折叠，与下面的gutters一起使用
                gutters: [
                    'CodeMirror-lint-markers',
                    'CodeMirror-linenumbers',
                    'CodeMirror-foldgutter'
                ],
                highlightSelectionMatches: {
                    minChars: 2,
                    style: 'matchhighlight',
                    showToken: true
                },
            },
            // json编辑模式下，输入框失去焦点时是否自动格式化，true 开启， false 关闭
            enableAutoFormatJson: this.autoFormatJson,
        };
    },
    created() {
        this.editorValue = this.codeData;
        try {
            if (!this.editorValue) {
                this.cmOptions.lint = false;
                return;
            }
            // JSON 模式下，自动格式化
            if (this.cmOptions.mode === 'application/json') {
                if (!this.enableAutoFormatJson) {
                    return;
                }
                this.editorValue = this.formatStrInJson(this.editorValue);
            }
        } catch (e) {
            console.log('初始化codemirror出错：' + e);
        }
    },
    methods: {
        /**
        * 重置代码编辑器lint设置
        *
        * @description 如果编辑器中没有内容，则先禁用lint，然后在下一个tick时重新启用lint。
        * 如果编辑器中有内容，则先禁用lint，然后在下一个tick时重新启用lint。
        */
        resetLint() {
            if (!this.$refs.myCm.codemirror.getValue()) {
                this.$nextTick(() => {
                    this.$refs.myCm.codemirror.setOption('lint', false);
                });
                return;
            }
            this.$refs.myCm.codemirror.setOption('lint', false);
            this.$nextTick(() => {
                this.$refs.myCm.codemirror.setOption('lint', true);
            });
        },
        /**
        * 设置 Codemirror 编辑器选项
        *
        * @param {string} key 要设置的选项名称
        * @param {any} value 要设置的值
        */
        setCodeOption(key, value) {
            if (key) {
                this.$refs.myCm.codemirror.setOption(key, value);
            }
        },
        /**
        * 将字符串格式的 JSON 转换为格式化的 JSON 字符串
        *
        * @param strValue 字符串格式的 JSON 数据
        * @returns 格式化的 JSON 字符串
        */
        formatStrInJson(strValue) {
            return JSON.stringify(
                JSON.parse(strValue),
                null,
                this.cmIndentUnit
            );
        },
        /**
        * 当代码编辑器内容发生变化时触发的回调函数
        *
        * @param cm CodeMirror 实例
        */
        onCmCodeChanges(cm) {
            this.editorValue = cm.getValue();
            this.resetLint();
            this.$nextTick(() => {
                this.$emit('update:codeData', this.editorValue);
                this.$emit('codeDataChange', this.editorValue);
            });
        },
        // 失去焦点时处理函数
        onCmBlur(cm) {
            try {
                let editorValue = cm.getValue();
                if (this.cmOptions.mode === 'application/json' && editorValue) {
                    if (this.enableAutoFormatJson) {
                        this.editorValue = this.formatStrInJson(editorValue);
                    }
                }
            } catch (e) {
                // 啥也不做
            }
        },
        /**
        * 处理键盘按下事件，自动补全提示
        *
        * @param event 键盘事件对象
        */
        onKeyDown(event) {
            const keyCode = event.keyCode || event.which || event.charCode;
            const keyCombination =
                event.ctrlKey || event.altKey || event.metaKey;
            if (!keyCombination && keyCode > 64 && keyCode < 123) {
                this.$refs.myCm.codemirror.showHint({ completeSingle: false });
            }
        },
        // 按下鼠标时事件处理函数
        onMouseDown() {
            this.$refs.myCm.codemirror.closeHint();
        },
        // 替换选中的问题或者光标处插入文本
        replaceSelection(content) {
            if (content) {
                this.$refs.myCm.codemirror.replaceSelection(content);
            }
        },
        // 黏贴事件处理函数
        OnPaste() {
            if (this.cmOptions.mode === 'application/json') {
                try {
                    this.editorValue = this.formatStrInJson(this.editorValue);
                } catch (e) {
                    // 啥都不做
                }
            }
        },
    }
};
</script>
<style lang="less" scoped>
.code-mirror-test {
    text-align: left;
    /deep/ .CodeMirror {
        height: 100%;
    }
}
</style>