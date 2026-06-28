package com.compliance.compliance_agent.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 企业微信通知
 */
@Component
public class WeChatNotifier {

    private final RestClient restClient;

    // 企业微信机器人 Webhook 地址
    private static final String WECHAT_WEBHOOK_URL = System.getenv("WECHAT_WEBHOOK_KEY");

    public WeChatNotifier() {
        this.restClient = RestClient.create();
    }

    /**
     * 发送 Markdown 格式的告警通知
     */
    public void sendMarkdown(String content) {
        Map<String, Object> body = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of("content", content)
        );

        restClient.post()
                .uri(WECHAT_WEBHOOK_URL)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 发送合规审查告警
     */
    public void sendComplianceAlert(String projectName, String mrTitle, int score, boolean isHighRisk) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🤖 AI 代码合规审查告警\n\n");
        sb.append("**项目：**").append(projectName).append("\n\n");
        sb.append("**MR/PR：**").append(mrTitle).append("\n\n");
        sb.append("**合规评分：**").append(score).append("/100\n\n");

        if (isHighRisk) {
            sb.append("⚠️ **高风险！建议立即修复后再合并**\n\n");
        } else {
            sb.append("✅ 审查通过，代码合规\n");
        }

        sendMarkdown(sb.toString());
    }
}