package com.oggo.planmaker.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {
	private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);
	private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

	@Value("${openai.api.key}")
	private String apiKey;

	private final RestTemplate restTemplate;

	public OpenAIService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(30))
				.setReadTimeout(Duration.ofSeconds(60)).build();
	}

    @Async
    @Cacheable(value = "itineraries", key = "#prompt")
    public CompletableFuture<String> generateItinerary(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 유용한 여행 추천 도우미입니다."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 2500);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // OpenAI API 요청
            Map<String, Object> response = restTemplate.postForObject(OPENAI_API_URL, request, Map.class);
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null && message.containsKey("content")) {
                    return CompletableFuture.completedFuture((String) message.get("content"));
                }
            }
            log.warn("Unexpected response format from OpenAI API: {}", response);
            return CompletableFuture.completedFuture("");
        } catch (Exception e) {
            log.error("Error while calling OpenAI API: " + e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage()));
        }
    }
}