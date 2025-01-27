package com.starfish24.starter.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

import static com.starfish24.starter.logger.Marker.BUSINESS_FATAL;

@Slf4j
public class OmsResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return super.hasError(clientHttpResponse);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        byte[] body = getResponseBody(clientHttpResponse);
        log.error(BUSINESS_FATAL, "Handled {} {} with body: {}", clientHttpResponse.getStatusCode(), clientHttpResponse.getStatusText(), body);
        super.handleError(clientHttpResponse);
    }
}
