# 项目状态

## 当前版本能力

- 支持 TXT/Markdown 运维文档上传、分片、向量化和 Milvus 检索。
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

当前验证结果：9 个测试通过，0 个失败。测试不依赖云密钥或运行中的 Milvus，真实 DashScope、Prometheus、CLS 和 MCP 端到端链路需要在对应环境中单独验证。

## 后续规划

- 增加认证、权限和接口限流；
- 将会话迁移到 Redis 并增加 TTL；
- 引入文档版本和索引切换机制；
- 增加检索质量、工具调用成功率和报告可靠性评测；
- 在审批和回滚机制完善后，再考虑接入自动化变更执行。
