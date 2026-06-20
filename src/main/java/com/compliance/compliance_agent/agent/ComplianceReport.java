package com.compliance.compliance_agent.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 4.1 报告数据结构
 * 审查报告的 DTO，用来把 AI 返回的文本结构化成 JSON
 */
public class ComplianceReport {

    private String rawAnalysis;
    private List<RiskItem> risks = new ArrayList<>();

    public int getScore() {
        if (risks.isEmpty()) return 100;
        long criticalCount = risks.stream().filter(r -> "CRITICAL".equals(r.getLevel())).count();
        long highCount = risks.stream().filter(r -> "HIGH".equals(r.getLevel())).count();
        return Math.max(0, 100 - (int) criticalCount * 25 - (int) highCount * 10);
    }

    public boolean isHighRisk() {
        return risks.stream().anyMatch(r -> "CRITICAL".equals(r.getLevel()) || "HIGH".equals(r.getLevel()));
    }

    // --- getters / setters ---
    public String getRawAnalysis() { return rawAnalysis; }
    public void setRawAnalysis(String rawAnalysis) { this.rawAnalysis = rawAnalysis; }
    public List<RiskItem> getRisks() { return risks; }
    public void setRisks(List<RiskItem> risks) { this.risks = risks; }

    public static class RiskItem {
        private String level;
        private String location;
        private String title;
        private String description;
        private String suggestion;
        private String icon;

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}