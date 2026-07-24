const varTypeOption = [
    { label: 'String', value: 'String' },
    { label: 'Integer', value: 'Integer' },
    { label: 'Boolean', value: 'Boolean' },
    { label: 'Number', value: 'Number' },
    { label: 'ArrayString', value: 'ArrayString' },
    { label: 'ArrayInteger', value: 'ArrayInteger' },
    { label: 'ArrayNumber', value: 'ArrayNumber' },
    { label: 'ArrayBoolean', value: 'ArrayBoolean' },
];
const modelOption = [
    {
        label: '推荐',
        children: [
            {
                label: 'DeepSeek-R1',
                value: 'DeepSeek-R1',
                modelType: 'LLM',
                // eslint-disable-next-line max-len
                img: 'https://agi-dev-platform-web.cdn.bcebos.com/ai_apaas/dist/img/DeepSeek_bd09dab5.png'
            },
            {
                label: 'Qwen3-30B-A3B',
                value: 'Qwen3-30B-A3B',
                modelType: 'VL',
                // eslint-disable-next-line max-len
                img: 'https://agi-dev-platform-web.cdn.bcebos.com/ai_apaas/dist/img/DeepSeek_bd09dab5.png'
            },
            {
                label: 'ERNIE-X1-32K',
                value: 'ERNIE-X1-32K',
                modelType: 'LLM',
                // eslint-disable-next-line max-len
                img: 'https://agi-dev-platform-web.cdn.bcebos.com/ai_apaas/dist/img/DeepSeek_bd09dab5.png'
            }
        ]
    },
];
const modeOption = [
    { label: '极速模式', value: 'speed', descText: '适用于意图明确且简单，对响应速度要求高的场景' },
    { label: '精确模式', value: 'accurate', descText: '适用于多意图复杂场景，支持配置意图例句并进行参数抽取，耗时相对较长' },
];
const memoryModOption = [
     { label: '记忆变量写入', value: 'write', },
     { label: '记忆变量读取', value: 'read', },
];
const varTypeApiBodyOption = [
    { label: 'String', value: 'String' },
    { label: 'Number', value: 'Number' },
    { label: 'Integer', value: 'Integer' },
    { label: 'Boolean', value: 'Boolean' },
    { label: 'Any', value: 'Any' },
    // { label: 'Array', value: 'Array' },
    { label: 'Object', value: 'Object' },

    { label: 'Array<String>',value: 'ArrayString' },
    { label: 'Array<Number>', value: 'ArrayNumber' },
    { label: 'Array<Integer>', value: 'ArrayInteger' },
    { label: 'Array<Boolean>', value: 'ArrayBoolean' },
    { label: 'Array<Any>', value: 'ArrayAny' },
    { label: 'Array<Object>', value: 'ArrayObject' }
];
const varTypeApiBodyEasyOption = [
    { label: 'String', value: 'String' },
    { label: 'Number', value: 'Number' },
    { label: 'Integer', value: 'Integer' },
    { label: 'Boolean', value: 'Boolean' },
    { label: 'Any', value: 'Any' },
];
const varTypeApiArrayOption = [
    { label: 'Array<String>',value: 'ArrayString' },
    { label: 'Array<Number>', value: 'ArrayNumber' },
    { label: 'Array<Integer>', value: 'ArrayInteger' },
    { label: 'Array<Boolean>', value: 'ArrayBoolean' },
    { label: 'Array<Any>', value: 'ArrayAny' },
    { label: 'Array<Object>', value: 'ArrayObject' }
];
export {
    varTypeOption,
    modelOption,
    modeOption,
    varTypeApiBodyOption,
    varTypeApiArrayOption,
    varTypeApiBodyEasyOption,
    memoryModOption,
};