package com.starfish24.starter.logger;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggerStarterConfiguration {

    @Bean
    public RequestAndResponseLoggingFilter reqAndRespFilterJB(LoggingProperties properties) {
        return new RequestAndResponseLoggingFilter(properties);
    }

    @Bean
    public RequestAndResponseLoggingInterceptor reqAndRespInterceptorJB() {
        return new RequestAndResponseLoggingInterceptor();
    }

}
