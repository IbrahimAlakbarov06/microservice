package com.example.client.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Unknown error";
        try (InputStream in = response.body().asInputStream()) {
            message = mapper.readTree(in).path("message").asText(message);
        } catch (Exception ignored) {
        }
        int status = response.status();
        if (status >= 500) {
            return new StorageServerException(message);
        }
        return new ResponseStatusException(HttpStatus.valueOf(status), message);
    }
}