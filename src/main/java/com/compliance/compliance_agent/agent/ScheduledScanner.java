package com.compliance.compliance_agent.agent;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledScanner {

    private final ComplianceScanner scanner;

    public ScheduledScanner(ComplianceScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * 每天上午 10 点自动巡检一次
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void dailyScan() {
        System.out.println("⏰ 定时巡检开始...");

        // 实际项目中，这里会：
        // 1. 调用 GitHub/GitLab API，获取仓库最新代码
        // 2. 遍历所有文件，对关键文件进行扫描
        // 3. 发现风险后发送企业微信告警
        // 当前 Demo：模拟一次扫描
        ComplianceReport report = scanner.scan(
                "模拟的代码变更内容",
                "定时巡检",
                "金融"
        );

        if (report.isHighRisk()) {
            System.out.println("⚠️ 巡检发现高风险项，评分：" + report.getScore());
        } else {
            System.out.println("✅ 巡检完成，未发现风险");
        }
    }
}