# 简历与面试说明

## 项目名称

OnCall AI Agent 智能运维助手

## 简历描述

- 设计并实现基于 Spring Boot、Spring AI Alibaba、DashScope 和 Milvus 的智能运维平台，支持运维文档入库、向量检索、多轮问答和 SSE 流式输出。
- 构建 RAG 知识库链路，实现 TXT/Markdown 文档分片、Embedding 向量化、Milvus 相似度检索和来源元数据返回。
- 设计 ReactAgent 工具调用体系，接入内部文档、Prometheus 告警、CLS 日志和时间查询工具，使模型能够根据问题自动选择排障数据源。
- 构建 Supervisor / Planner / Executor 多 Agent 告警诊断流程，根据监控与日志证据生成包含告警详情、根因分析、处理建议和风险评估的 Markdown 报告。
- 完善文件上传安全校验、索引失败状态反馈、Mock 与 MCP 配置隔离，并通过单元测试和 GitHub Actions 建立持续验证流程。

## 技术关键词

Java 17、Spring Boot、Spring AI、DashScope、Milvus、RAG、ReactAgent、Planner-Executor、Prometheus、MCP、SSE、Docker Compose、JUnit 5、GitHub Actions。

## 面试重点

- 为什么文件上传成功不能直接代表知识库索引成功；
- 文档分片的最大长度和重叠长度如何影响召回质量；
- 为什么更换 Embedding 模型需要检查向量维度和 Milvus Collection；
- ReactAgent 工具调用与 Planner / Executor 编排分别解决什么问题；
- 为什么 Prompt 中的重试约束不能替代程序级循环预算；
- 当前系统如何区分 Mock 演示模式和真实 Prometheus / MCP 接入模式。

## 当前边界

系统当前面向告警分析与排障辅助，不执行重启、扩容、回滚等高风险变更。会话目前保存在应用内存中，真实 CLS 查询依赖 MCP 配置，AIOps 报告采用完成后的分块输出。
