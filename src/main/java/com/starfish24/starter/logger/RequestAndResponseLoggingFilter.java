package com.starfish24.starter.logger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.starfish24.starter.logger.Marker.BUSINESS;
import static com.starfish24.starter.logger.Marker.BUSINESS_FATAL;

@Slf4j
public class RequestAndResponseLoggingFilter extends OncePerRequestFilter {
    private final LoggingProperties loggingProperties;

    public RequestAndResponseLoggingFilter(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiUrl = request.getMethod() + " " + request.getRequestURI();
        if (isAsyncDispatch(request) || loggingProperties.getIgnoreLoggingUri().contains(apiUrl)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    private void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                 FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        CachedHttpServletRequest cachedRequest = new CachedHttpServletRequest(request);
        try {
            logRequest(cachedRequest);
            filterChain.doFilter(cachedRequest, response);
        } finally {
            logResponse(response, cachedRequest, startTime);
            response.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(CachedHttpServletRequest request) {
        if (loggingProperties.isEnableHeaders()) {
            Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                    .map(headerName -> new SimpleEntry<>(headerName, request.getHeader(headerName)))
                    .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (oldValue, newValue) -> newValue));
            log.info(BUSINESS, "{}|> {} request-headers: {}", request.getRemoteAddr(), request.fullUrl(), headers);
        }
        String content = loggingProperties.ignoreRequestContent(request.apiUrl()) ? "[ignored body]"
                : extractContent(request.getContentInputStream(), encoding(request.getCharacterEncoding()), loggingProperties.getRequestPayloadLimit());
        log.info(BUSINESS, "{}|> {} request-body: {}", request.getRemoteAddr(), request.fullUrl(), content);
    }

    private void logResponse(ContentCachingResponseWrapper response, CachedHttpServletRequest request, long startTime) {
        HttpStatus status = HttpStatus.valueOf(response.getStatus());
        String apiUrl = request.apiUrl();
        if (loggingProperties.isEnableHeaders()) {
            Map<String, String> headers = response.getHeaderNames().stream()
                    .map(headerName -> new SimpleEntry<>(headerName, response.getHeader(headerName)))
                    .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (oldValue, newValue) -> newValue));
            log.info(BUSINESS, "{}|< {} response-headers: {}", request.getRemoteAddr(), apiUrl, headers);
        }
        String content = loggingProperties.ignoreResponseContent(apiUrl) ? "[ignored body]"
                : extractContent(response.getContentInputStream(), encoding(response.getCharacterEncoding()), loggingProperties.getResponsePayloadLimit());
        long exTime = System.currentTimeMillis() - startTime;
        String responseMessagePattern = "{}|< {} status: {} ({}ms); response-body: {}";
        if (status.is2xxSuccessful()) {
            log.info(BUSINESS, responseMessagePattern, request.getRemoteAddr(), apiUrl, status, exTime, content);
        } else {
            log.error(BUSINESS_FATAL, responseMessagePattern, request.getRemoteAddr(), apiUrl, status, exTime, content);
        }
    }

    private String extractContent(InputStream content, String charsetName, Integer lengthLimit) {
        try {
            byte[] bodyContent = StreamUtils.copyToByteArray(content);
            String strContent = bodyContent.length == 0 ? "[no body]" : IOUtils.toString(bodyContent, charsetName);
            return lengthLimit == null || lengthLimit > strContent.length() ? strContent : strContent.substring(0, lengthLimit);
        } catch (Exception ex) {
            log.error(BUSINESS_FATAL, "Problem to extract content for logging: {}", ex.getMessage(), ex);
            return "[unreadable body]";
        }
    }

    public String encoding(String currentCharset) {
        return StandardCharsets.ISO_8859_1.name().equals(currentCharset)
                ? StandardCharsets.UTF_8.name() : currentCharset;
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        return (request instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) request : new ContentCachingRequestWrapper(request);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        return (response instanceof ContentCachingResponseWrapper)
                ? (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);
    }
}

