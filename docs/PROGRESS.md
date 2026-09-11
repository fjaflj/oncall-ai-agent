# 项目状态

## 当前版本能力

- 支持 TXT/Markdown 运维文档上传、分片、向量化和 Milvus 检索。
- 支持可选的两阶段 RAG：Milvus 先召回候选文档，再通过 Cross Encoder/Reranker 精排后返回最终 Top-K。
- 支持普通问答、SSE 流式问答和基于会话 ID 的多轮上下文。
- 支持时间、内部文档、Prometheus 告警和 Mock CLS 日志工具。
- 支持通过 MCP profile 接入外部日志工具。
- 支持 Supervisor / Planner / Executor 告警分析流程和 Markdown 报告输出。
- 支持上传路径校验、文件大小限制和索引失败状态反馈。
- 支持 JUnit 回归测试和 GitHub Actions 构建验证。

## 已验证项

执行命令：

```bash
mvn --batch-mode --no-transfer-progress verify
```

当前验证结果：17 个测试通过，0 个失败。单元测试不依赖云密钥或运行中的 Milvus；真实 OpenAI、Prometheus、CLS 和 MCP 端到端链路需要在对应环境中单独验证。

## OpenAI 运行配置

- 对话模型默认使用 `gpt-5.6-terra`，向量模型默认使用 `text-embedding-3-small`。
- API Key 通过本机 `OPENAI_API_KEY` 环境变量提供，支持 `OPENAI_BASE_URL` 兼容网关。
- 新向量集合为 `oncall_knowledge_openai`，默认维度 1536；旧 `biz` 集合保留不动。
- 首次运行需要重新上传 `aiops-docs` 文档，完成 OpenAI 向量重建。
- Rerank 默认关闭；开启后通过 `RAG_RERANK_ENABLED` 和 `RAG_RERANK_API_KEY` 接入兼容 `/v1/rerank` 的服务，默认召回 12 条、精排后返回 3 条。
- Rerank 服务失败时默认回退到 Milvus 向量召回，避免外部精排服务不可用导致知识库整体不可用。

## 后续规划

- 增加认证、权限和接口限流；
- 将会话迁移到 Redis 并增加 TTL；
- 引入文档版本和索引切换机制；
- 增加检索质量、工具调用成功率和报告可靠性评测；
- 增加不同 Embedding、Reranker 模型组合的检索质量评测；
- 在审批和回滚机制完善后，再考虑接入自动化变更执行。
