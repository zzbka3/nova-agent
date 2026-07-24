package com.nova.agent.utils;

import com.alibaba.fastjson.JSON;
import com.nova.agent.entity.graph.ApiNode;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
public class HttpUtils {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Invoke an external API
     */
    public static String invokeApi(String url, String method,
                                   List<ApiNode.ApiQueryParam> queryParams,
                                   List<ApiNode.ApiQueryHeader> headers,
                                   String jsonBody,
                                   ApiNode.AuthInfo authInfo) throws Exception {
        // Build URL with query params
        StringBuilder urlBuilder = new StringBuilder(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            boolean first = !url.contains("?");
            for (ApiNode.ApiQueryParam param : queryParams) {
                urlBuilder.append(first ? "?" : "&");
                urlBuilder.append(param.getField()).append("=")
                        .append(param.getRealValue() != null ? param.getRealValue() : "");
                first = false;
            }
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .timeout(Duration.ofSeconds(30));

        // Set headers
        if (headers != null) {
            for (ApiNode.ApiQueryHeader header : headers) {
                if (header.getRealValue() != null) {
                    requestBuilder.header(header.getField(), String.valueOf(header.getRealValue()));
                }
            }
        }
        requestBuilder.header("Content-Type", "application/json");

        // Set auth
        if (authInfo != null && authInfo.getToken() != null) {
            requestBuilder.header("Authorization", "Bearer " + authInfo.getToken());
        }

        // Set method and body
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);
        switch (method.toUpperCase()) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(bodyPublisher);
            case "PUT" -> requestBuilder.PUT(bodyPublisher);
            case "DELETE" -> requestBuilder.DELETE();
            default -> requestBuilder.POST(bodyPublisher);
        }

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Simple POST request
     */
    public static String doPost(String url, String body, String authToken) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30));
            if (authToken != null && !authToken.isEmpty()) {
                builder.header("Authorization", authToken);
            }
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            log.error("HTTP POST error: url={}", url, e);
            throw new RuntimeException("HTTP POST failed: " + e.getMessage(), e);
        }
    }
}
