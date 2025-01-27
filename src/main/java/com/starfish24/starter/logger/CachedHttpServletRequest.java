package com.starfish24.starter.logger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
public class CachedHttpServletRequest extends HttpServletRequestWrapper {

    private final ServletInputStream payloadStream;
    private final byte[] payloadBytes;
    private final boolean displayableContent;

    private static final Set<MediaType> VISIBLE_TYPES = new HashSet<>(Arrays.asList(
            MediaType.valueOf("text/*"),
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.APPLICATION_FORM_URLENCODED
    ));

    public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        displayableContent = isContentDisplayable(request);
        this.payloadStream = request.getInputStream();
        this.payloadBytes = this.displayableContent ? StreamUtils.copyToByteArray(request.getInputStream()) : null;
    }

    private boolean isContentDisplayable(HttpServletRequest request) {
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.valueOf(request.getContentType());
        } catch (Exception ignore) {
            return false;
        }
        return VISIBLE_TYPES.stream().anyMatch(mediaType::isCompatibleWith);
    }

    @Override
    public ServletInputStream getInputStream() {
        return this.displayableContent ? new CachedServletInputStream(this.payloadBytes) : payloadStream;
    }

    @Override
    public BufferedReader getReader() {
        InputStream in = this.displayableContent ? new ByteArrayInputStream(this.payloadBytes) : payloadStream;
        return new BufferedReader(new InputStreamReader(in));
    }

    public InputStream getContentInputStream() {
        return new CachedServletInputStream(this.displayableContent ? this.payloadBytes : "[content not displayable]".getBytes());
    }

    public String apiUrl() {
        return this.getMethod() + " " + this.getRequestURI();
    }

    public String fullUrl() {
        return this.getQueryString() != null ? apiUrl() + "?" + this.getQueryString() : apiUrl();
    }
}
