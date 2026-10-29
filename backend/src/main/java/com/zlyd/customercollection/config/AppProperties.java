package com.zlyd.customercollection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private boolean captchaEnabled = true;
    private Jwt jwt = new Jwt();
    private File file = new File();
    private Mail mail = new Mail();
    private Reminder reminder = new Reminder();
    private DemoUser demoUser = new DemoUser();

    @Data
    public static class Jwt {
        private String secret = "customer-collection-jwt-secret-key-please-change-in-production-2026";
        private long expireHours = 168;
    }

    @Data
    public static class File {
        private String root = "./upload";
        private String urlPrefix = "/upload";
        private long maxImageSize = 10L * 1024 * 1024;
        private long maxFileSize = 50L * 1024 * 1024;
    }

    @Data
    public static class Mail {
        private boolean enabled = false;
        private String from = "no-reply@example.com";
        private int codeExpireMinutes = 10;
    }

    @Data
    public static class Reminder {
        private boolean enabled = true;
        private String cron = "0 0 8 * * *";
    }

    @Data
    public static class DemoUser {
        private boolean enabled = true;
    }
}
