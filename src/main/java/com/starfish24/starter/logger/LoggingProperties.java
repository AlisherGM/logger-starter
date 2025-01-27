package com.starfish24.starter.logger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "oms.logger")
public class LoggingProperties {
    private boolean enableHeaders = false;
    private Integer requestPayloadLimit = null;
    private Integer responsePayloadLimit = null;
    private Set<String> ignoreLoggingUriRequestContent = new HashSet<>();
    private Set<String> ignoreLoggingUriResponseContent = new HashSet<>();
    private Set<String> ignoreLoggingUri = new HashSet<>();

    public boolean ignoreRequestContent(String uri) {
        return uri != null && this.getIgnoreLoggingUriRequestContent().contains(uri);
    }

    public boolean ignoreResponseContent(String uri) {
        return uri != null && this.getIgnoreLoggingUriResponseContent().contains(uri);
    }
}
