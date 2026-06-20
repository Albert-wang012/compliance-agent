# 🔍 AI 代码合规审查 Agent

> 基于 Spring AI + RAG + Agent 的企业级 AI 应用实践

## 📌 项目背景

在企业级开发中，代码合规审查（硬编码密钥、SQL注入、敏感信息泄露等）长期依赖人工，效率低且易遗漏。本项目利用 **RAG 检索增强生成** 与 **LLM 大模型** 技术，实现自动化、可追溯的代码合规审查。

## ⚡ 核心功能

- 🔎 **代码变更自动扫描**：接收代码 diff，自动识别潜在合规风险
- 📚 **RAG 增强检索**：基于 Elasticsearch 向量数据库，精准匹配企业合规规范
- 🤖 **Agent 智能分析**：调用阿里云百炼大模型，多维度综合判断风险等级
- 📊 **结构化报告**：输出包含风险等级、修复建议、合规评分的 JSON 报告

## 🛠 技术栈

| 层面 | 技术选型 |
| :--- | :--- |
| 后端框架 | Spring Boot 3.5.x |
| AI 框架 | Spring AI Alibaba 1.0.0-M6.1 |
| 大模型 | 阿里云百炼 (qwen-plus) |
| 向量数据库 | Elasticsearch 8.17.0 |
| 前端 | 原生 HTML + CSS + JavaScript |
| 构建工具 | Maven 3.9+ |
| JDK | 17 |

## 🚀 本地运行

### 前置条件

- JDK 17+
- Maven 3.9+
- Docker Desktop

### 1.克隆项目

git clone https://github.com/Albert-wang012/compliance-agent.git
cd compliance-agent

### 2.启动 Elasticsearch

docker run -d --name es-rag -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.17.0

### 3.配置 API Key

修改 src/main/resources/application.yml，将 sk-你的真实API_Key 替换为你的阿里云百炼 API Key。

### 4.启动项目

mvn spring-boot:run

### 5.访问页面

浏览器打开 http://localhost:8080，粘贴代码变更内容，点击"提交审查"。

## 🧪 测试用例

### 严重风险 (CRITICAL)

String apiKey = "sk-abc123def456";
String sql = "SELECT * FROM users WHERE name = '" + userName + "'";

### 中度风险 (HIGH)

log.info("用户手机号：" + userPhone);

### 轻度风险 (LOW)

try {
int result = 10 / 0;
} catch (Exception e) {
e.printStackTrace();
}

## 📖 项目亮点

- **Java 技术栈的 AI 落地实践**：区别于市面上主流的 Python 方案，本项目展示了如何在 Java 生态中完成 AI Agent 开发
- **完整的 RAG + Agent 链路**：从文档加载、切片、向量化、语义检索到 LLM 综合分析，覆盖企业级 AI 应用的核心流程
- **Prompt Engineering 实践**：通过角色设定、类别约束、格式规范三步优化，实现可编程的结构化输出
- **生产级扩展性**：可集成到 GitLab CI/CD 流水线，通过 Webhook 自动触发审查，结果自动回复到 Merge Request

## 🔧 生产环境扩展方向

- **Webhook 集成**：接收 GitLab/GitHub 的 MR 事件，自动获取代码 diff
- **自动评论**：调用 GitLab/GitHub API，将审查报告自动发表到 MR 评论区
- **规范库管理**：支持从数据库/配置中心动态加载合规规范，支持持续更新
- **多租户支持**：不同项目可配置不同的审查规则和规范库

## 📄 开源协议

MIT License