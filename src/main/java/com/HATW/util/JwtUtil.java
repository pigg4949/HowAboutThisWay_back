package com.HATW.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 1. JWT 관련 설정 값 정의
    private final String SECRET = "MySuperSecretKeyForJWTTokenWhichIsVerySecure12345"; // JWT 서명에 사용할 비밀키
    private final long EXPIRATION = 1000 * 60 * 60; // JWT 토큰 유효기간(1시간)
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());  // 서명 생성 및 검증에 필요한 Key 객체 생성

    // 2. JWT 토큰 생성 메서드
    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)                                                 // 토큰 주체(사용자 이름)를 설정
                .claim("userId", userId)                                                  // 사용자 ID를 추가 정보로 저장
                .setIssuedAt(new Date())                                              // 토큰 발급 시각 설정
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))     // 토큰 만료 시각 설정
                .signWith(key, SignatureAlgorithm.HS256)                              // 서명 알고리즘과 서명 키 설정
                .compact();                                                           // JWT 생성 및 직렬화
    }

    // 3. JWT에서 사용자 이름(subject)을 가져오는 메서드
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();                                       // JWT Claims에서 주체(subject) 가져오기
    }

    // 4. JWT에서 사용자 ID를 가져오는 메서드
    public String getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", String.class);                     // JWT Claims에서 id 값 가져오기
    }

    // 5. JWT 토큰의 유효성 검증 메서드
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);                                                       // JWT 파싱하여 유효성 체크
            return true;                                                              // 파싱 성공하면 유효한 토큰으로 판단
        } catch (JwtException | IllegalArgumentException e) {
            return false;                                                             // 실패 시 유효하지 않은 토큰
        }
    }

    // 6. JWT 파싱하여 Claims 객체를 얻는 메서드 (내부사용)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)                                                   // JWT 검증을 위한 서명 키 설정
                .build()
                .parseClaimsJws(token)                                                // JWT 파싱 및 서명 유효성 검사
                .getBody();                                                           // JWT 본문(Claims) 반환
    }

    // 7. HTTP 요청 헤더에서 JWT를 추출하여 사용자 ID를 반환하는 메서드
    public String getUserIdFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");                           // Authorization 헤더 값 추출
        if (bearer != null && bearer.startsWith("Bearer ")) {                         // 헤더값이 있고 Bearer로 시작하면
            String token = bearer.substring(7);                                       // Bearer 뒤의 JWT 추출
            return getUserIdFromToken(token);                                         // JWT에서 사용자 ID 추출 후 반환
        }
        throw new RuntimeException("토큰 문제 있슈~");                                  // JWT 없거나 잘못된 형식이면 예외 발생
    }
}
