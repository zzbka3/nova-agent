package com.nova.agent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Python code sandbox execution service.
 * Calls a separate Python web service to execute user code.
 */
@Slf4j
@Service("pythonRunner")
public class PythonRunner {

    @Value("${third-service.python-sandbox.base-url:http://localhost:8811}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Execute Python code by calling the external sandbox service
     * @param requestBody JSON with "code" and "params" fields
     * @return JSON response string
     */
    public String runCode(String requestBody) {
        try {
            JSONObject request = JSON.parseObject(requestBody);
            log.info("PythonRunner executing code, baseUrl={}", baseUrl);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/execute"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                JSONObject errorResp = new JSONObject();
                errorResp.put("ok", false);
                errorResp.put("error", "Python sandbox returned HTTP " + response.statusCode());
                errorResp.put("result", null);
                errorResp.put("exec_time_ms", 0);
                return errorResp.toJSONString();
            }
        } catch (Exception e) {
            log.error("PythonRunner execution error!", e);
            JSONObject errorResp = new JSONObject();
            errorResp.put("ok", false);
            errorResp.put("error", "Python sandbox error: " + e.getMessage());
            errorResp.put("result", null);
            errorResp.put("exec_time_ms", 0);
            return errorResp.toJSONString();
        }
    }
}
