package com.thkim.toyproject.fintrack.infrastructure.stock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kis")
public class KisProperties {
    private String baseUrl;
    private String appKey;
    private String appSecret;
    private String accountNo;
}
