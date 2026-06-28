package com.compliance.compliance_agent.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRisk {

    private static final Logger log = LoggerFactory.getLogger(TestRisk.class);

    public void test() {
        // 风险1：硬编码 API 密钥
        String apiKey = "sk-abc123def456";

        // 风险2：日志输出未脱敏的手机号
        String phone = "13812345678";
        log.info("用户手机号：" + phone);
        log.info("用户手机号222：" + phone);

        System.out.println("API Key: " + apiKey);
    }
}