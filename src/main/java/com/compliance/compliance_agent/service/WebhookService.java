package com.compliance.compliance_agent.service;

import com.compliance.compliance_agent.agent.ComplianceReport;
import com.compliance.compliance_agent.agent.ComplianceScanner;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final ComplianceScanner scanner;

    public WebhookService(ComplianceScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * 执行代码审查
     */
    public ComplianceReport review(String codeDiff, String filePath, String projectType) {
        return scanner.scan(codeDiff, filePath, projectType);
    }

    /**
     * 构建 Markdown 格式的审查评论
     */
    public String buildReviewComment(ComplianceReport report, String platform) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🤖 AI 代码合规审查报告\n\n");
        sb.append("**审查平台：**").append(platform).append("\n\n");
        sb.append("**综合评分：**").append(report.getScore()).append("/100\n\n");

        if (report.isHighRisk()) {
            sb.append("⚠️ **本次提交存在高风险项，建议修复后再合并**\n\n");
        }

        if (report.getRisks().isEmpty()) {
            sb.append("✅ 未发现合规风险，代码审查通过。\n");
        } else {
            for (ComplianceReport.RiskItem risk : report.getRisks()) {
                sb.append("---\n");
                sb.append(risk.getIcon()).append(" **[").append(risk.getLevel()).append("]** ");
                sb.append(risk.getTitle()).append("\n\n");
                sb.append("**问题描述：**").append(risk.getDescription()).append("\n\n");
                sb.append("**修复建议：**").append(risk.getSuggestion()).append("\n");
            }
        }
        return sb.toString();
    }
}