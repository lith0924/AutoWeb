package org.example.auto_web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Selenium自动化配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "selenium")
public class SeleniumConfig {
    // Chrome相关配置
    private Chrome chrome = new Chrome();
    // 通用超时配置
    private Timeout timeout = new Timeout();

    // 内部类：Chrome配置
    @Data
    public static class Chrome {
        private String driverPath;
        private boolean headlessMode;
        private boolean closeBrowserAfterExec;
    }

    // 内部类：超时配置
    @Data
    public static class Timeout {
        private int seconds;
        private long waitAfterStep;
    }
}