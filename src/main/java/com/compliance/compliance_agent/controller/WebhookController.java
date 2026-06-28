package com.compliance.compliance_agent.controller;

import com.compliance.compliance_agent.agent.ComplianceReport;
import com.compliance.compliance_agent.integration.GitHubApiClient;
import com.compliance.compliance_agent.integration.GitLabApiClient;
import com.compliance.compliance_agent.service.WebhookService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook Controller
 */
@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final WebhookService webhookService;
    private final GitLabApiClient gitLabApiClient;
    private final GitHubApiClient gitHubApiClient;

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
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        String fullName = (String) repository.get("full_name");
        String[] parts = fullName.split("/");
        String owner = parts[0];
        String repo = parts[1];

        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
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

    private String extractDiff(String json) {
        // 简化处理，实际用 Jackson 解析
        return json;
    }
}