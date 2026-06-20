package com.compliance.compliance_agent.controller;

import com.compliance.compliance_agent.agent.ComplianceReport;
import com.compliance.compliance_agent.agent.ComplianceScanner;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 4.3 创建或更新合规审查接口
 * 前端接口，接收代码变更并返回报告
 */
@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceScanner scanner;

    public ComplianceController(ComplianceScanner scanner) {
        this.scanner = scanner;
    }

    @PostMapping("/scan")
    public Map<String, Object> scan(@RequestBody Map<String, String> request) {
        String codeDiff = request.getOrDefault("codeDiff", "");
        String filePath = request.getOrDefault("filePath", "unknown");
        String projectType = request.getOrDefault("projectType", "金融");

        ComplianceReport report = scanner.scan(codeDiff, filePath, projectType);

        return Map.of(
                "success", true,
                "score", report.getScore(),
                "highRisk", report.isHighRisk(),
                "risks", report.getRisks(),
                "rawAnalysis", report.getRawAnalysis()
        );
    }
}