package com.HATW.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.HATW.dto.KakaoUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoUtil {

    @Value("${oauth.kakao.user-info-url}")
    private String userInfoUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KakaoUtil() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public KakaoUserDTO getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode kakaoAccount = root.path("kakao_account");
            JsonNode properties = root.path("properties");

            return KakaoUserDTO.builder()
                    .email(kakaoAccount.path("email").asText(null))
                    .nickname(properties.path("nickname").asText(null))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve Kakao user info", e);
        }
    }
}