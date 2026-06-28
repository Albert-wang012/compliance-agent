package com.compliance.compliance_agent.controller;

import com.compliance.compliance_agent.agent.ComplianceReport;
import com.compliance.compliance_agent.integration.GitHubApiClient;
import com.compliance.compliance_agent.integration.GitLabApiClient;
import com.compliance.compliance_agent.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Webhook Controller
 */
@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;
    private final GitLabApiClient gitLabApiClient;
    private final GitHubApiClient gitHubApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(WebhookService webhookService,
                             GitLabApiClient gitLabApiClient,
                             GitHubApiClient gitHubApiClient) {
        this.webhookService = webhookService;
        this.gitLabApiClient = gitLabApiClient;
        this.gitHubApiClient = gitHubApiClient;
    }

    // ========== GitLab ==========

    @PostMapping("/gitlab")
    public Map<String, Object> handleGitLabMR(@RequestBody Map<String, Object> payload) {
        Map<String, Object> project = (Map<String, Object>) payload.get("project");
        String projectId = String.valueOf(project.get("id"));

        Map<String, Object> attributes = (Map<String, Object>) payload.get("object_attributes");
        String mrIid = String.valueOf(attributes.get("iid"));
        String sourceBranch = (String) attributes.get("source_branch");

        // 获取代码 diff
        String changesJson = gitLabApiClient.getMrChanges(projectId, mrIid);
        String codeDiff = extractDiff(changesJson);

        // 审查
        ComplianceReport report = webhookService.review(codeDiff, sourceBranch, "金融");

        // 构建评论并发表
        String comment = webhookService.buildReviewComment(report, "GitLab");
        gitLabApiClient.postComment(projectId, mrIid, comment);

        return Map.of("success", true, "score", report.getScore(), "platform", "gitlab");
    }

    // ========== GitHub ==========

    @PostMapping("/github")
    public Map<String, Object> handleGitHubPR(@RequestBody Map<String, Object> payload) {
        log.info("=== 收到 GitHub Webhook 请求 ===");
        log.info("事件类型: {}", payload.get("action"));

        // 从根节点提取 pull_request（兼容 opened 和 synchronize）
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
        if (pullRequest == null) {
            log.error("Webhook Payload 中没有 pull_request 字段");
            return Map.of("success", false, "error", "无效的 Payload");
        }

        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        String fullName = (String) repository.get("full_name");
        String[] parts = fullName.split("/");
        String owner = parts[0];
        String repo = parts[1];

        String prNumber = String.valueOf(pullRequest.get("number"));
        String prTitle = (String) pullRequest.get("title");

        // 获取代码 diff
        String filesJson = gitHubApiClient.getPrFiles(owner, repo, prNumber);
        String codeDiff = extractDiff(filesJson);

        // 审查
        ComplianceReport report = webhookService.review(codeDiff, prTitle, "金融");

        // 构建评论并发表
        String comment = webhookService.buildReviewComment(report, "GitHub");
        gitHubApiClient.postComment(owner, repo, prNumber, comment);

        return Map.of("success", true, "score", report.getScore(), "platform", "github");
    }

    // ========== 公共方法 ==========

    private String extractDiff(String filesJson) {
        try {
            JsonNode root = objectMapper.readTree(filesJson);
            StringBuilder diffBuilder = new StringBuilder();
            for (JsonNode file : root) {
                String filename = file.get("filename").asText();
                String patch = file.has("patch") ? file.get("patch").asText() : "";
                if (!patch.isEmpty()) {
                    diffBuilder.append("=== ").append(filename).append(" ===\n");
                    diffBuilder.append(patch).append("\n\n");
                }
            }
            String result = diffBuilder.toString();
            if (result.isEmpty()) {
                log.warn("所有文件都没有 patch 内容");
            }
            return result;
        } catch (Exception e) {
            log.error("解析 GitHub API 返回的文件列表失败", e);
            return "无法解析代码变更内容";
        }
    }
}