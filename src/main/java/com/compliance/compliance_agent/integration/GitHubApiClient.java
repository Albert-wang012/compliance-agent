package com.compliance.compliance_agent.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GitHubApiClient {

    private final RestClient restClient;

    private static final String GITHUB_BASE_URL = "https://api.github.com";

    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");


    public GitHubApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(GITHUB_BASE_URL)
                .defaultHeader("Authorization", "token " + GITHUB_TOKEN)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    /**
     * 获取 Pull Request 的文件变更列表
     * @param owner 仓库所有者（GitHub 用户名）
     * @param repo  仓库名
     * @param prNumber PR 编号
     */
    public String getPrFiles(String owner, String repo, String prNumber) {
        String path = "/repos/" + owner + "/" + repo + "/pulls/" + prNumber + "/files";
        return restClient.get()
                .uri(path)
                .retrieve()
                .body(String.class);
    }

    /**
     * 在 PR 评论区发表评论
     * @param owner 仓库所有者
     * @param repo  仓库名
     * @param prNumber PR 编号
     * @param commentBody 评论内容（Markdown 格式）
     */
    public void postComment(String owner, String repo, String prNumber, String commentBody) {
        String path = "/repos/" + owner + "/" + repo + "/issues/" + prNumber + "/comments";

        restClient.post()
                .uri(path)
                .body(Map.of("body", commentBody))
                .retrieve()
                .toBodilessEntity();
    }
}