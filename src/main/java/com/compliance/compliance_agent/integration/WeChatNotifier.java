package com.compliance.compliance_agent.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 企业微信通知
 */
@Component
public class WeChatNotifier {

    private static final Logger log = LoggerFactory.getLogger(WeChatNotifier.class);

    private final RestClient restClient;

    // 企业微信机器人 Webhook 地址
    private static final String WECHAT_WEBHOOK_KEY = System.getenv("WECHAT_WEBHOOK_KEY");

    public WeChatNotifier() {
        this.restClient = RestClient.create();
    }

    /**
     * 发送 Markdown 格式的告警通知
     */
    public void sendMarkdown(String content) {
        // 从环境变量获取 Key，如果没有配置则跳过发送
        if (WECHAT_WEBHOOK_KEY == null || WECHAT_WEBHOOK_KEY.isEmpty()) {
            log.warn("企业微信 Webhook Key 未配置，跳过告警发送");
            return;
        }

        String url = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=" + WECHAT_WEBHOOK_KEY;

        Map<String, Object> body = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of("content", content)
        );

        restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("企业微信告警已发送");
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