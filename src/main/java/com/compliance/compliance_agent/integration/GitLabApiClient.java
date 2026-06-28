package com.compliance.compliance_agent.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GitLabApiClient {

    private final RestClient restClient;

    // GitLab 地址，根据实际情况修改
    private static final String GITLAB_BASE_URL = "https://gitlab.com/api/v4";

    private static final String GITHUB_BASE_URL = "https://api.github.com";



    // GitLab Personal Access Token（在 GitLab Settings → Access Tokens 中创建）
    private static final String GITLAB_TOKEN = "glpat-你的GitLab_Token";

    public GitLabApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GITLAB_BASE_URL)
                .defaultHeader("PRIVATE-TOKEN", GITLAB_TOKEN)
                .build();
    }

    /**
     * 获取 MR 的代码变更详情
     */
    public String getMrChanges(String projectId, String mrIid) {
        String path = "/projects/" + projectId + "/merge_requests/" + mrIid + "/changes";
        return restClient.get()
                .uri(path)
                .retrieve()
                .body(String.class);
    }

    /**
     * 在 MR 评论区发表评论
     */
    public void postComment(String projectId, String mrIid, String commentBody) {
        String path = "/projects/" + projectId + "/merge_requests/" + mrIid + "/notes";

        restClient.post()
                .uri(path)
                .body(Map.of("body", commentBody))
                .retrieve()
                .toBodilessEntity();
    }
}