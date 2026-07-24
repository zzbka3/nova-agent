/*
 * @Author: hewenquan
 * @Date: 2025-10-23 14:23:06
 * @LastEditTime: 2025-10-24 16:56:34
 * @LastEditors: hewenquan
 * @FilePath: /metis-front/src/views/flow/common/getNodeInitNames.js
 * @Description: 保持新增节点的变量名唯一
 */
const getNodeInitNames = (nodeName, type, lf, defaultNodeName) => {
    // nodeName已存在，非初始化创建，直接返回
    if (nodeName) {
        return nodeName;
    }
    const { nodes = [] } = lf.getGraphData();
    // const filterNodes = nodes.filter(item => item.type === type);
    const arr = nodes.map(item => {
        const currentName = item.properties && item.properties.nodeName;
        if (currentName === defaultNodeName) return 1;
        if (currentName && currentName.startsWith(`${defaultNodeName}_`)) {
            const num = parseInt(currentName.split('_')[1], 10);
            return isNaN(num) ? null : num;
        }
        return null;
    }).filter(Boolean);
    const sorted = [...new Set(arr)].sort((a, b) => a - b);
    const set = new Set(sorted);
    let nextNum;
    if (sorted.length === 0 || !set.has(1)) {
        // 没有任何，或第一个被删了 -> 优先补 "defaultNodeName"
        nextNum = 1;
    } else {
        // 从 2 开始寻找第一个缺失的数字
        const max = sorted[sorted.length - 1];
        nextNum = null;
        for (let i = 2; i <= max; i++) {
            if (!set.has(i)) {
                nextNum = i;
                break;
            }
        }
        if (nextNum === null) {
            // 没有缺口 -> 在最大值后 +1
            nextNum = max + 1;
        }
    }
    return nextNum === 1 ? defaultNodeName : `${defaultNodeName}_${nextNum}`;
};

export default getNodeInitNames;