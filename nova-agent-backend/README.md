# Nova Agent Backend

基于 DAG 工作流引擎的智能体平台后端服务。

## 技术栈

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

## 项目结构

```
src/main/java/com/nova/agent/
├── config/              # Spring 配置（线程池、Redis、Swagger）
├── constant/            # 常量（上下文变量名、错误码）
├── controller/          # REST API 控制器
│   └── AgentFlowController.java   # 核心 API（20+ 端点）
├── entity/              # 核心实体
│   ├── AgentFlow.java             # ★ 工作流引擎（DAG 构建 & 执行）
│   ├── graph/                     # 15 种工作流节点
│   │   ├── Node.java              # 节点抽象基类
│   │   ├── StartNode.java         # 起始节点
│   │   ├── EndNode.java           # 结束节点
│   │   ├── LLMNode.java           # 大模型调用
│   │   ├── IfNode.java            # 条件分支
│   │   ├── KnowledgeNode.java     # 知识库检索
│   │   ├── ApiNode.java           # HTTP API 调用
│   │   ├── CodeNode.java          # Python 代码沙箱
│   │   ├── McpNode.java           # MCP 协议工具调用
│   │   ├── IntentNode.java        # 意图识别/路由
│   │   ├── MessageNode.java       # 消息输出
│   │   ├── WorkflowNode.java      # 子工作流
│   │   ├── WorkflowAgentNode.java # 工作流 Agent
│   │   ├── MemoryNode.java        # 记忆管理
│   │   ├── QueryRewriteNode.java  # 查询改写
│   │   └── TextProcessorNode.java # 文本处理
│   └── ...
├── enums/               # 枚举定义（NodeType、VarType 等）
├── exception/           # 异常处理 & 全局异常拦截
├── llm/                 # LLM 客户端（支持 fallback 兜底）
├── model/
│   ├── po/              # 持久化对象（agent、agent_execute_log 等 14 张表）
│   └── vo/              # 视图对象（Result、AgentVo 等）
├── repository/          # MyBatis Mapper 接口
├── service/
│   ├── AgentFlowService.java      # 核心业务接口（~30 个方法）
│   └── impl/AgentFlowServiceImpl.java
├── tracking/            # 性能追踪
└── utils/               # 工具类（HttpUtils、ConditionUtils 等）
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.x+
- Maven 3.9+

### 1. 创建数据库

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 2. 配置密码

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
  data:
    redis:
      password:  # Redis 密码，无密码则留空或删除
```

### 3. 启动应用

```bash
# 方式一：IDEA 中直接运行 NovaAgentApplication

# 方式二：命令行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. 验证

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

后端点：
  GET  /api/v1/agent/initInfo          画布初始化信息（节点类型列表、可用模型）
  POST /api/v1/agent/save              创建/更新智能体（有 appId=更新，无=新建）
  POST /api/v1/agent/validate          校验工作流 DAG 合法性
  POST /api/v1/agent/talk              执行工作流（核心接口）
  GET  /api/v1/agent/conversation      生成会话 ID
  POST /api/v1/agent/runningInfo       查询实时执行状态（节点级日志）
  GET  /api/v1/agent/publish           发布智能体（草稿 → 正式版）
  GET  /api/v1/agent/list              分页查询智能体列表
  GET  /api/v1/agent/detail            查询智能体详情
  GET  /api/v1/agent/copy              复制智能体
  GET  /api/v1/agent/delete            删除智能体
  GET  /api/v1/agent/export            导出智能体配置
  POST /api/v1/agent/import            导入智能体配置
  GET  /api/v1/agent/checkPublishAgent 检查是否已发布
  GET  /api/v1/agent/listSupportModels 获取可用模型列表
  POST /api/v1/agent/apiDebug          调试 API 节点
  POST /api/v1/agent/codeDebug         调试代码节点
  POST /api/v1/agent/prepareJson       Prompt 格式转换（JSON ↔ MD）
  GET  /api/v1/agent/verify            校验 WorkflowAgent 依赖关系
  GET  /api/v1/agent/latestPublishedTime 查询最近发布时间
```

## 节点详情

| 节点 | 说明 |
|------|------|
| `StartNode` | 流程入口，接收用户输入并透传 |
| `EndNode` | 流程出口，收集结果并返回 |
| `LLMNode` | 调用大模型 API，支持多模型、Fallback、VL 多模态 |
| `IfNode` | 条件分支，AND/OR 逻辑，16 种运算符 |
| `KnowledgeNode` | 知识库 RAG 检索 |
| `ApiNode` | HTTP API 调用，支持 GET/POST/PUT/DELETE |
| `CodeNode` | Python 代码沙箱执行 |
| `McpNode` | MCP 协议工具调用（SSE+JSON-RPC） |
| `IntentNode` | 意图识别和槽位提取 |
| `MessageNode` | 消息模板输出 |
| `WorkflowNode` | 子流程模板替换 |
| `WorkflowAgentNode` | 嵌套执行另一个 Agent |
| `MemoryNode` | 会话记忆读写 |
| `QueryRewriteNode` | 查询改写（LLM 辅助） |
| `TextProcessorNode` | 文本处理（trim/upper/replace/JSON 提取） |

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

完整建表语句见 `src/main/resources/schema.sql`。

## 运行测试

```bash
# IDEA 中右键 AgentFlowIntegrationTest → Run
# 测试会连接真实数据库，自动创建和清理测试数据
```
