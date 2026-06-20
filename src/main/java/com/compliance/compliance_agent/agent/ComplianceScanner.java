package com.compliance.compliance_agent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 4.2 核心扫描服务
 * 核心 Service，串联“检索规范”和“LLM 分析”这两步
 */
@Service
public class ComplianceScanner {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final Map<String, String> RISK_ICONS = Map.of(
            "CRITICAL", "🔴",
            "HIGH", "🟠",
            "MEDIUM", "🟡",
            "LOW", "🟢"
    );

    public ComplianceScanner(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public ComplianceReport scan(String codeDiff, String filePath, String projectType) {
        // 1. 智能检索相关规范
        String searchQuery = buildSearchQuery(codeDiff, filePath);
        String context = vectorStore.similaritySearch(searchQuery)
                .stream()
                .filter(doc -> doc.getScore() >= 0.3)
                .map(doc -> doc.getText())
                .collect(Collectors.joining("\n---\n"));

        // 2. 让大模型进行分析
        String systemPrompt = buildSystemPrompt(projectType, context);
        String analysisResult = chatClient.prompt()
                .system(systemPrompt)
                .user("请分析以下代码变更的合规风险：\n\n```java\n" + codeDiff + "\n```")
                .call()
                .content();

        // 3. 解析成结构化报告
        ComplianceReport report = new ComplianceReport();
        report.setRawAnalysis(analysisResult);
        parseReport(analysisResult, report);
        return report;
    }

    private String buildSearchQuery(String codeDiff, String filePath) {
        StringBuilder query = new StringBuilder("代码安全规范");
        if (codeDiff.contains("password") || codeDiff.contains("secret") || codeDiff.contains("key")) {
            query.append(" 硬编码密钥 敏感信息");
        }
        if (codeDiff.contains("SQL") || codeDiff.contains("Statement")) {
            query.append(" SQL注入 参数化查询");
        }
        if (codeDiff.contains("log") || codeDiff.contains("print")) {
            query.append(" 日志脱敏 敏感信息输出");
        }
        return query.toString();
    }

    private String buildSystemPrompt(String projectType, String context) {
        return """
            你是一位资深代码合规审查专家，专注于%s领域的代码安全与合规性。

            ## 需要检查的风险类别：
            1. 硬编码密钥/凭证/Token
            2. 敏感信息泄露（手机号、身份证、银行卡等）
            3. SQL注入风险
            4. 开源许可证合规问题
            5. 日志脱敏缺失

            ## 相关合规规范参考：
            %s

            ## 回答格式（严格遵守）：
            对于每个发现的风险，请用以下格式：
            RISK|风险等级|文件名:行号|风险标题|风险描述|修复建议

            风险等级必须为：CRITICAL / HIGH / MEDIUM / LOW
            如果没有发现风险，回复：PASS|未发现明显合规风险
            """.formatted(projectType, context);
    }

    private void parseReport(String result, ComplianceReport report) {
        for (String line : result.split("\n")) {
            if (line.startsWith("RISK|")) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    ComplianceReport.RiskItem item = new ComplianceReport.RiskItem();
                    item.setLevel(parts[1].trim());
                    item.setLocation(parts[2].trim());
                    item.setTitle(parts[3].trim());
                    item.setDescription(parts[4].trim());
                    item.setSuggestion(parts[5].trim());
                    item.setIcon(RISK_ICONS.getOrDefault(parts[1].trim(), "⚪"));
                    report.getRisks().add(item);
                }
            }
        }
    }
}