package com.HATW.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 토큰 유효성 검사
                if (jwtTokenProvider.validateToken(token)) {
                    // 토큰에서 사용자 ID 추출
                    String userId = jwtTokenProvider.getUserIdFromToken(token);
                    // 요청에 사용자 정보 설정
                    httpRequest.setAttribute("authenticatedUserId", userId);
                } else {
                    throw new SecurityException("Invalid JWT token");
                }
            } catch (Exception e) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "JWT Authentication Failed: " + e.getMessage());
                return;
            }
        }

        chain.doFilter(request, response);
    }
}