package com.HATW.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class UserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 인증된 사용자 ID (JwtAuthenticationFilter 또는 LoginCheckFilter에서 주입했다고 가정)
        Object authenticatedUserId = httpRequest.getAttribute("authenticatedUserId");

        if (authenticatedUserId == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }

        // 요청 경로 또는 파라미터에 포함된 사용자 ID 추출
        String path = httpRequest.getRequestURI(); // 예: /api/bookmarks/user/3
        Long pathUserId = extractUserIdFromPath(path);

        if (pathUserId == null) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "userId가 필요합니다.");
            return;
        }

        // userId 일치 검사
        if (!authenticatedUserId.toString().equals(pathUserId.toString())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "다른 사용자의 데이터에는 접근할 수 없습니다.");
            return;
        }

        // 통과
        chain.doFilter(request, response);
    }

    // 경로에서 userId 추출
    private Long extractUserIdFromPath(String path) {
        // 예시: /api/bookmarks/user/3 → 마지막 숫자만 추출
        String[] parts = path.split("/");
        try {
            for (int i = 0; i < parts.length; i++) {
                if ("user".equals(parts[i]) && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1]);
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}