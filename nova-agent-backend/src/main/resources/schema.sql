-- ============================================================
-- Nova Agent Platform - Database Schema
-- Database: nova_agent
-- ============================================================

CREATE DATABASE IF NOT EXISTS nova_agent
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE nova_agent;

-- -----------------------------------------------------------
-- 1. agent - 智能体（已发布版本）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agent` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体唯一标识',
  `name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '智能体名称',
  `config` MEDIUMTEXT COMMENT '工作流配置 JSON (DSL)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=已发布',
  `agent_type` TINYINT NOT NULL DEFAULT 0 COMMENT '智能体类型: 0=工作流, 1=对话',
  `memory_schema` TEXT COMMENT '记忆配置 JSON',
  `reference_turns` INT NOT NULL DEFAULT 5 COMMENT '引用历史对话轮次',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_status` (`status`),
  KEY `idx_agent_type` (`agent_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体表（已发布版本）';

-- -----------------------------------------------------------
-- 2. agent_draft - 智能体草稿
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agent_draft` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体唯一标识',
  `name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '智能体名称',
  `config` MEDIUMTEXT COMMENT '工作流配置 JSON (DSL)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=已发布',
  `agent_type` TINYINT NOT NULL DEFAULT 0 COMMENT '智能体类型',
  `memory_schema` TEXT COMMENT '记忆配置 JSON',
  `reference_turns` INT NOT NULL DEFAULT 5 COMMENT '引用历史对话轮次',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体草稿表';

-- -----------------------------------------------------------
-- 3. agent_execute_log - 工作流执行记录
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agent_execute_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键（execute_id 指向此列）',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体 ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话 ID',
  `request` MEDIUMTEXT COMMENT '用户请求 JSON',
  `response` MEDIUMTEXT COMMENT '执行结果',
  `status` INT NOT NULL DEFAULT 0 COMMENT '执行状态: 0=初始, 1=运行中, 2=完成, 3=异常',
  `total_used_tokens` INT NOT NULL DEFAULT 0 COMMENT '总消耗 tokens',
  `cost_time` INT NOT NULL DEFAULT 0 COMMENT '总耗时(毫秒)',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_conversation` (`app_id`, `conversation_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行记录表';

-- -----------------------------------------------------------
-- 4. agent_node_execute_log - 节点执行日志
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agent_node_execute_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体 ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话 ID',
  `execute_id` BIGINT NOT NULL COMMENT '执行记录 ID（关联 agent_execute_log.id）',
  `node_id` VARCHAR(64) NOT NULL COMMENT '节点 ID',
  `node_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '节点名称',
  `node_type` VARCHAR(32) NOT NULL COMMENT '节点类型: LLM/IF/API/CODE 等',
  `input_vars` MEDIUMTEXT COMMENT '节点入参 JSON',
  `output_vars` MEDIUMTEXT COMMENT '节点出参 JSON',
  `status` INT NOT NULL DEFAULT 0 COMMENT '节点状态: 0=初始, 1=到达, 2=运行中, 3=完成, 4=异常',
  `cost_time` INT NOT NULL DEFAULT 0 COMMENT '节点耗时(毫秒)',
  `used_tokens` INT NOT NULL DEFAULT 0 COMMENT 'Token 消耗',
  `exception` TEXT COMMENT '异常信息',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_execute_id` (`execute_id`),
  KEY `idx_app_conversation` (`app_id`, `conversation_id`),
  KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点执行日志表';

-- -----------------------------------------------------------
-- 5. agent_edge_execute_log - 边执行日志
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agent_edge_execute_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体 ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话 ID',
  `execute_id` BIGINT NOT NULL COMMENT '执行记录 ID',
  `edge_id` VARCHAR(64) NOT NULL COMMENT '边 ID',
  `from_node_id` VARCHAR(64) NOT NULL COMMENT '源节点 ID',
  `target_node_id` VARCHAR(64) NOT NULL COMMENT '目标节点 ID',
  `condition_match` INT NOT NULL DEFAULT -1 COMMENT '条件匹配: -1=未评估, 0=不匹配, 1=匹配',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_execute_id` (`execute_id`),
  KEY `idx_app_conversation` (`app_id`, `conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='边执行日志表';

-- -----------------------------------------------------------
-- 6. conversation - 会话
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体 ID',
  `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话 ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation` (`app_id`, `conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- -----------------------------------------------------------
-- 7. account - 账号
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `account` (
  `account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号 ID',
  `account_name` VARCHAR(128) NOT NULL COMMENT '账号名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_account_name` (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号表';

-- -----------------------------------------------------------
-- 8. token - API 凭证
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `token` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `token` VARCHAR(256) NOT NULL COMMENT 'Token 字符串',
  `account_id` BIGINT NOT NULL COMMENT '关联账号 ID',
  `permission` VARCHAR(64) NOT NULL DEFAULT 'read' COMMENT '权限标识',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API Token 表';

-- -----------------------------------------------------------
-- 9. support_llm - 支持的 LLM 模型
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `support_llm` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `llm_code` VARCHAR(64) NOT NULL COMMENT '模型编码',
  `display_name` VARCHAR(128) NOT NULL COMMENT '展示名称',
  `model_server` VARCHAR(256) NOT NULL COMMENT '模型服务地址',
  `model_type` VARCHAR(32) NOT NULL DEFAULT 'LLM' COMMENT '模型类型: LLM/VL/EMBEDDING',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_llm_code` (`llm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支持的 LLM 模型表';

-- -----------------------------------------------------------
-- 10. account_llm_quota - 账号 LLM 配额
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `account_llm_quota` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account_id` BIGINT NOT NULL COMMENT '账号 ID',
  `llm_code` VARCHAR(64) NOT NULL COMMENT '模型编码',
  `quota_limit` BIGINT NOT NULL DEFAULT 0 COMMENT '配额上限',
  `used` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_llm` (`account_id`, `llm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号 LLM 配额表';

-- -----------------------------------------------------------
-- 11. dictionary - 字典表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` VARCHAR(128) NOT NULL COMMENT '字典编码',
  `value` MEDIUMTEXT COMMENT '字典值（JSON 格式）',
  `description` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- -----------------------------------------------------------
-- 12. workflow_node_dependency - 工作流节点依赖关系
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `workflow_node_dependency` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_agent_id` VARCHAR(64) NOT NULL COMMENT '父智能体 ID',
  `child_agent_id` VARCHAR(64) NOT NULL COMMENT '子智能体 ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_agent_id`),
  KEY `idx_child` (`child_agent_id`),
  UNIQUE KEY `uk_parent_child` (`parent_agent_id`, `child_agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流节点依赖关系表';

-- -----------------------------------------------------------
-- 13. llm_flow_stats - LLM 流量统计
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `llm_flow_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `llm_code` VARCHAR(64) NOT NULL COMMENT '模型编码',
  `account_id` BIGINT NOT NULL DEFAULT 0 COMMENT '账号 ID',
  `tokens` INT NOT NULL DEFAULT 0 COMMENT 'Token 数量',
  `invoke_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_llm_code` (`llm_code`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_invoke_time` (`invoke_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM 流量统计表';

-- -----------------------------------------------------------
-- 14. stream_agent_invoke_log - 流式调用日志
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `stream_agent_invoke_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `invoke_id` VARCHAR(64) NOT NULL COMMENT '调用 ID',
  `app_id` VARCHAR(64) NOT NULL COMMENT '智能体 ID',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_invoke_id` (`invoke_id`),
  KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流式调用日志表';

-- ============================================================
-- 初始化种子数据
-- ============================================================

-- 插入默认管理员账号
INSERT INTO `account` (`account_id`, `account_name`, `status`) VALUES
(1, 'admin', 1)
ON DUPLICATE KEY UPDATE `account_name` = VALUES(`account_name`);

-- 插入默认 Token
INSERT INTO `token` (`token`, `account_id`, `permission`, `expire_time`) VALUES
('default-token-nova-agent', 1, 'all', '2099-12-31 23:59:59')
ON DUPLICATE KEY UPDATE `account_id` = VALUES(`account_id`);

-- 插入字典数据：知识库节点输出 Schema
INSERT INTO `dictionary` (`code`, `value`, `description`) VALUES
('KNOWLEDGE_NODE_OUT_SCHEMA', '{"field":"OutputList","type":"ArrayObject","children":[{"field":"segment_id","type":"String"},{"field":"document_id","type":"String"},{"field":"dataset_id","type":"String"},{"field":"content","type":"String"},{"field":"document_name","type":"String"},{"field":"dataset_name","type":"String"},{"field":"word_count","type":"String"}]}', '知识库节点输出字段结构')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);
