package com.HATW.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.HATW.dto.GoogleUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleUtil {

    @Value("${oauth.google.user-info-url}")
    private String userInfoUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleUtil() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleUserDTO getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());

            return GoogleUserDTO.builder()
                    .email(json.path("email").asText(null))
                    .name(json.path("name").asText(null))
                    .picture(json.path("picture").asText(null))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve Google user info", e);
        }
    }
}