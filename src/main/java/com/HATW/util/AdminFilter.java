package com.HATW.util;

import com.HATW.dto.UserDTO;
import com.HATW.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// isAdmin int [not null, default: 0] "관리자 여부(0 = 일반,1 = 관리자)"
public class AdminFilter implements Filter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AdminFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // CORS 필터가 OPTIONS 요청을 처리하도록 맡기고, AdminFilter에서는 OPTIONS 요청을 그대로 통과시킨다.
        // CorsFilter가 모든 필요한 CORS 헤더를 추가할 것이므로, 여기서 STATUS를 설정하거나 응답을 종료하지 않는다.
        // AdminFilter는 실제 요청 (GET, POST 등)에 대해서만 인증/인가 로직을 수행한다.
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("AdminFilter: OPTIONS 요청 감지. 다음 필터 체인으로 통과.");
            chain.doFilter(request, response); // OPTIONS 요청을 다음 필터 (CorsFilter 등)로 전달
            return; // 현재 필터의 처리를 종료하고, 다음 필터로 완전히 넘김
        }

        System.out.println("AdminFilter: 요청 처리 시작 -> URI: " + req.getRequestURI() + ", 메소드: " + req.getMethod());
        String authHeader = req.getHeader("Authorization");
        System.out.println("AdminFilter: Authorization 헤더: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("AdminFilter: 인증 헤더가 없거나 형식이 올바르지 않습니다.");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 없습니다.");
            return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("AdminFilter: 추출된 토큰: " + token.substring(0, Math.min(token.length(), 20)) + "...");

            UserDTO user = userService.getUserInfoFromToken(authHeader); // 또는 userService.getUserInfoFromToken(token);

            if (user == null) {
                System.out.println("AdminFilter: 토큰에서 사용자 정보를 가져올 수 없습니다. 토큰이 유효하지 않거나 사용자가 존재하지 않습니다.");
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                return;
            }

            System.out.println("AdminFilter: 사용자 정보 조회 성공 -> ID: " + user.getUserId() + ", 관리자 여부: " + user.getIsAdmin());

            if (Boolean.FALSE.equals(user.getIsAdmin())) {
                System.out.println("AdminFilter: 사용자 " + user.getUserId() + "는 관리자가 아닙니다. 접근 거부.");
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근할 수 없습니다.");
                return;
            }

            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);

            System.out.println("AdminFilter: 사용자 " + user.getUserId() + "는 관리자입니다. 다음 필터 체인으로 진행.");
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("AdminFilter: 토큰 유효성 검증 또는 사용자 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 인증 실패: " + e.getMessage());
        }
    }
}
