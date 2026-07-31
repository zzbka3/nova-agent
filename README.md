# Nova Agent

一个基于 DAG 工作流引擎的 Java 智能体平台，支持可视化画布编排与 ReAct 自主规划两种模式。

> 设计灵感来源于 Dify，目前聚焦后端引擎开发，前端部分已有可视化工作流画布框架。

## 项目结构

```
nova-agent/
├── nova-agent-backend/    # 后端 — Spring Boot 工作流引擎
├── nova-agent-web/        # 前端 — Vue 3 可视化工作流画布
└── README.md
```

## 核心特性

- **可视化工作流编排** — 拖拽式画布，支持 15 种节点类型，通过 DAG 图编排复杂 Agent 逻辑
- **大模型集成** — 支持多模型调用、Fallback 兜底、VL 多模态
- **知识库 RAG** — 内置知识库检索节点，支持检索增强生成
- **MCP 协议支持** — 基于 SSE + JSON-RPC 的 MCP 工具调用
- **代码沙箱** — Python 代码安全执行
- **条件分支** — 支持 AND/OR 逻辑，16 种运算符
- **子流程复用** — 支持工作流嵌套调用和 WorkflowAgent 引用
- **草稿/发布** — 智能体支持草稿编辑与正式发布，双版本管理
- **执行追踪** — 节点级执行日志（入参/出参/耗时/tokens）、边分支命中记录
- **调试工具** — 支持 API 节点调试、代码节点调试、Prompt 格式转换
- **ReAct 模式**（规划中）— 大模型自主规划 + Tool/Workflow 受控执行，支持多轮交互与中断恢复

---

## 技术栈

### 后端 (nova-agent-backend)

| 组件 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.3.2 |
| ORM | MyBatis + PageHelper | 3.0.3 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis + Redisson | 7.x / 3.23 |
| 图引擎 | JGraphT | 1.5.2 |
| JSON | FastJSON2 | 2.0.57 |
| 构建 | Maven | 3.9+ |

### 前端 (nova-agent-web)

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 + TypeScript | ^3.4 / ^5.4 |
| 构建 | Vite | ^5.4 |
| 工作流画布 | LogicFlow | ^2.0 |
| UI 组件库 | Ant Design Vue | ^4.2 |
| 图标 | Ant Design Icons Vue | ^7.0 |
| 状态管理 | Pinia | ^2.1 |
| 路由 | Vue Router | ^4.3 |
| HTTP | Axios | ^1.7 |

---

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.x+
- Maven 3.9+
- Node.js 18+（前端）

### 1. 克隆项目

```bash
git clone <repo-url>
cd nova-agent
```

### 2. 初始化数据库

```bash
mysql -u root -p < nova-agent-backend/src/main/resources/schema.sql
```

### 3. 配置后端

创建 `nova-agent-backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
  data:
    redis:
      password:  # Redis 密码，无密码则留空或删除
```

### 4. 启动后端

```bash
cd nova-agent-backend

# IDEA 中直接运行 NovaAgentApplication

# 或命令行启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

服务默认运行在 `http://localhost:8080`。

### 5. 启动前端（可选）

```bash
cd nova-agent-web

npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到后端。

### 6. 验证

```bash
# 检查服务状态
curl http://localhost:8080/api/v1/agent/initInfo

# 创建智能体
curl -X POST http://localhost:8080/api/v1/agent/save \
  -H "Content-Type: application/json" \
  -d '{"name":"测试","config":"{}","agentType":0}'

# 执行工作流
curl -X POST http://localhost:8080/api/v1/agent/talk \
  -H "Content-Type: application/json" \
  -d '{"app_id":"<appId>","conversation_id":"<convId>","query":"你好"}'
```

---

## 核心架构

### 工作流执行流程

```
用户请求 → talk API
  → 查询 Agent 配置（Debug 模式优先读草稿）
  → AgentFlow 解析 JSON DSL → 构建 DAG
  → fire(userInput)
    → 找到 START 节点
    → triggerNode → 线程池异步执行
      → node.execute()
        → fillInputVar()    # 上下文中解析引用变量
        → run()             # 节点核心逻辑
        → fillOutputVar()   # 结果写入输出变量
      → 遍历出边，conditionMatch()  # IfNode 分支路由
      → propagate() 向后传播
    → CountDownLatch.await(3 分钟) + 超时诊断
    → 返回 AgentFlowOutput
```

### 节点类型

| 节点 | 说明 |
|------|------|
| `StartNode` | 流程入口，接收用户输入并透传 |
| `EndNode` | 流程出口，收集结果并返回 |
| `LLMNode` | 大模型调用，支持多模型、Fallback、VL 多模态 |
| `IfNode` | 条件分支，AND/OR 逻辑，16 种运算符 |
| `KnowledgeNode` | 知识库 RAG 检索 |
| `ApiNode` | HTTP API 调用，支持 GET/POST/PUT/DELETE |
| `CodeNode` | Python 代码沙箱执行 |
| `McpNode` | MCP 协议工具调用（SSE + JSON-RPC） |
| `IntentNode` | 意图识别和槽位提取 |
| `MessageNode` | 消息模板输出 |
| `WorkflowNode` | 子流程模板替换 |
| `WorkflowAgentNode` | 嵌套执行另一个 Agent |
| `MemoryNode` | 会话记忆读写 |
| `QueryRewriteNode` | 查询改写（LLM 辅助） |
| `TextProcessorNode` | 文本处理（trim/upper/replace/JSON 提取） |

### API 概览

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/v1/agent/initInfo` | 画布初始化信息（节点类型列表、可用模型） |
| POST | `/api/v1/agent/save` | 创建/更新智能体 |
| POST | `/api/v1/agent/validate` | 校验工作流 DAG 合法性 |
| POST | `/api/v1/agent/talk` | **执行工作流（核心接口）** |
| GET | `/api/v1/agent/conversation` | 生成会话 ID |
| POST | `/api/v1/agent/runningInfo` | 查询实时执行状态（节点级日志） |
| GET | `/api/v1/agent/publish` | 发布智能体（草稿 → 正式版） |
| GET | `/api/v1/agent/list` | 分页查询智能体列表 |
| GET | `/api/v1/agent/detail` | 查询智能体详情 |
| GET | `/api/v1/agent/copy` | 复制智能体 |
| GET | `/api/v1/agent/delete` | 删除智能体 |
| GET | `/api/v1/agent/export` | 导出智能体配置 |
| POST | `/api/v1/agent/import` | 导入智能体配置 |
| GET | `/api/v1/agent/checkPublishAgent` | 检查是否已发布 |
| GET | `/api/v1/agent/listSupportModels` | 获取可用模型列表 |
| POST | `/api/v1/agent/apiDebug` | 调试 API 节点 |
| POST | `/api/v1/agent/codeDebug` | 调试代码节点 |
| POST | `/api/v1/agent/prepareJson` | Prompt 格式转换（JSON ↔ MD） |
| GET | `/api/v1/agent/verify` | 校验 WorkflowAgent 依赖关系 |
| GET | `/api/v1/agent/latestPublishedTime` | 查询最近发布时间 |

---

## 数据库表

| 表名 | 用途 |
|------|------|
| `agent` | 已发布的智能体 |
| `agent_draft` | 智能体草稿 |
| `agent_execute_log` | 工作流执行记录 |
| `agent_node_execute_log` | 节点执行日志（入参/出参/耗时/tokens） |
| `agent_edge_execute_log` | 边执行日志（分支命中记录） |
| `conversation` | 会话 |
| `account` | 账号 |
| `token` | API Token |
| `support_llm` | 支持的模型列表 |
| `dictionary` | 字典配置 |
| `workflow_node_dependency` | 工作流节点依赖关系 |

完整建表语句见 `nova-agent-backend/src/main/resources/schema.sql`。

---

## 路线图

### 已完成

- [x] DAG 工作流引擎（JGraphT）
- [x] 15 种工作流节点类型
- [x] 可视化工作流画布（LogicFlow）
- [x] 节点执行追踪与日志
- [x] 智能体草稿/发布版本管理
- [x] 多模型 LLM 调用 + Fallback
- [x] MCP 协议支持
- [x] 知识库 RAG 检索
- [x] Python 代码沙箱
- [x] 导入/导出智能体配置

### 规划中 — ReAct 模式

- [ ] AutoAgent 与 ReAct 主循环
- [ ] Planner 结构化输入输出协议
- [ ] Tool Registry + Node Adapter（将现有节点封装为 Tool）
- [ ] Workflow 多入参/多出参 + 变量命名空间隔离
- [ ] 中断恢复（WAIT_USER / WAIT_EVENT）
- [ ] 统一 Session、Memory、Observation 状态管理
- [ ] 权限、审计、调试与成本控制

详细设计文档见 `nova-agent-backend/迭代计划.md`。

---

## 运行测试

```bash
cd nova-agent-backend

# IDEA 中右键 AgentFlowIntegrationTest → Run
# 测试会连接真实数据库，自动创建和清理测试数据
```

---

## 贡献

目前项目处于早期开发阶段，部分节点预留了接口但尚未完成实现。欢迎提交 Issue 和 PR 参与贡献。

---

## License

[MIT](LICENSE)
