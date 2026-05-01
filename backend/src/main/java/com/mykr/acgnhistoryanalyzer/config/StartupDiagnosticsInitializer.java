package com.mykr.acgnhistoryanalyzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class StartupDiagnosticsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(StartupDiagnosticsInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String datasourceUrl = environment.getProperty("spring.datasource.url", "未配置");
        String datasourceUsername = environment.getProperty("spring.datasource.username", "未配置");
        String datasourcePassword = environment.getProperty("spring.datasource.password", "");
        String llmProvider = environment.getProperty("app.llm.provider", "mock");
        String llmModel = environment.getProperty("app.llm.model", "未配置");
        String llmApiKey = environment.getProperty("app.llm.api-key", "");

        log.info("ACGN History Analyzer 正在启动：数据库 {}，用户 {}", datasourceUrl, datasourceUsername);
        if (datasourcePassword == null || datasourcePassword.isBlank()) {
            log.warn("数据库密码为空。若本机 MySQL root 需要密码，请设置环境变量 DB_PASSWORD，或修改本地 application.properties。");
        } else {
            log.info("数据库密码已配置（已隐藏）。如需换环境，请使用 DB_URL、DB_USERNAME、DB_PASSWORD 覆盖。");
        }

        if ("deepseek".equalsIgnoreCase(llmProvider)) {
            if (llmApiKey == null || llmApiKey.isBlank()) {
                log.warn("当前 LLM Provider 为 DeepSeek，但未检测到 API Key。可设置 DEEPSEEK_API_KEY，或在前端 AI 分析页配置运行时 Key。");
            } else {
                log.info("LLM 已配置为 DeepSeek，模型 {}，API Key 已隐藏。", llmModel);
            }
        } else {
            log.info("LLM 当前为预览模式，模型 {}。", llmModel);
        }
    }
}
