package com.starfish24.starter.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.starfish24.starter.logger.Marker.BUSINESS;
import static com.starfish24.starter.logger.Marker.BUSINESS_FATAL;


@Slf4j
public class RequestAndResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(request, response, startTime);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        String apiMethod = request.getMethod() + " " + request.getURI();
        log.info(BUSINESS, "{} |>  request-body: {}", apiMethod, new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(HttpRequest request, ClientHttpResponse response, long startTime) throws IOException {
        String apiMethod = request.getMethod() + " " + request.getURI();
        String content = "attachment".equals(response.getHeaders().getContentDisposition().getType()) ? "FILE"
                : StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        long exTime = System.currentTimeMillis() - startTime;
        String responseMessagePattern = "{} |< status: {}{} ({}ms); response-body: {}";
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info(BUSINESS, responseMessagePattern, apiMethod, response.getStatusCode(), response, exTime, content);
        } else {
            log.warn(BUSINESS_FATAL, responseMessagePattern, apiMethod, response.getStatusCode(), response.getStatusText(), exTime, content);
        }
    }
}
