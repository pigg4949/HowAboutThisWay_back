package com.HATW.service;

import com.HATW.mapper.UserMapper;
import com.HATW.util.JwtUtil;
import com.HATW.util.KakaoAuthUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import com.HATW.dto.UserDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void register(UserDTO user) {
        String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashed);
        mapper.insertUser(user);
    }

    @Override
    public String login(String userId, String passwordHash) {
        UserDTO user = mapper.findByUserId(userId);
        if (user != null && BCrypt.checkpw(passwordHash, user.getPasswordHash())) {
            return jwtUtil.generateToken(user.getUserId());
        }
        return null;
    }

    @Override
    public void logout(String token) {
        // 클라이언트에서 토큰 제거
    }

    @Override
    public void update(String token, UserDTO user) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);
        UserDTO original = mapper.findByUserId(userId);
        if (original != null) {
            if (StringUtils.hasText(user.getPasswordHash())) {
                String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
                original.setPasswordHash(hashed);
            }
            if (StringUtils.hasText(user.getName())) {
                original.setName(user.getName());
            }
            mapper.update(original);
        }
    }

    @Override
    public void delete(String token) {
        // "Bearer " 접두어 제거
        String jwt = token.replace("Bearer ", "");
        // 토큰에서 userId 추출
        String userId = jwtUtil.getUserIdFromToken(jwt);
        // 해당 userId로 DB에서 삭제
        mapper.delete(userId);
    }

    @Override
    public UserDTO getUserInfoFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);
        UserDTO user = mapper.findByUserId(userId);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }

    @Override
    public UserDTO kakaoLogin(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 액세스 토큰 요청
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);



        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", KakaoAuthUtil.CLIENT_ID);
        tokenParams.add("redirect_uri", KakaoAuthUtil.REDIRECT_URI);
        tokenParams.add("code", code);

        System.out.println("카카오 토큰 요청: code=" + code + ", redirect_uri=" + KakaoAuthUtil.REDIRECT_URI + ", client_id=" + KakaoAuthUtil.CLIENT_ID);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        String tokenResponse = restTemplate.postForObject(KakaoAuthUtil.TOKEN_URL, tokenRequest, String.class);
        String accessToken = parseAccessToken(tokenResponse);

        // 2. 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(
                KakaoAuthUtil.USERINFO_URL,
                HttpMethod.GET,
                userRequest,
                String.class
        );

        return handleUserResponse(userResponse.getBody());
    }

    private String parseAccessToken(String response) {
        try {
            JsonNode tokenJson = objectMapper.readTree(response); // ObjectMapper 사용
            return tokenJson.path("access_token").asText();
        } catch (IOException e) {
            throw new RuntimeException("카카오 토큰 파싱 실패", e);
        }
    }

    private UserDTO handleUserResponse(String body) {
        try {
            JsonNode kakaoJson = objectMapper.readTree(body);
            JsonNode account = kakaoJson.path("kakao_account");

            String kakaoId = kakaoJson.path("id").asText();
            String email = account.path("email").asText("");
            String nickname = account.path("profile").path("nickname").asText("카카오유저");

            String userId = "kakao_" + kakaoId;

            UserDTO user = mapper.findByUserId(userId); // 기존 메서드 활용

            if (user == null) {
                UserDTO joinUser = new UserDTO();
                joinUser.setUserId(userId);
                joinUser.setPasswordHash("");
                joinUser.setName(nickname);
                joinUser.setSsn1("SOCIAL");
                joinUser.setSsn2("SOCIAL");
                joinUser.setPhone("SOCIAL");
                joinUser.setIsActive(true);
                joinUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                mapper.insertKakaoUser(joinUser); // insertKakaoUser는 XML에 정의 필요
                user = mapper.findByUserId(userId);
            }

            return user;

        } catch (IOException e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패", e);
        }
    }

}