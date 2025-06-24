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

        // 토큰 존재 유무 판별
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 없습니다.");
            return;
        }
        try {
            // 토큰으로 유저 객체 정보 가져오기
            UserDTO user = userService.getUserInfoFromToken(authHeader);

            if (user == null || Boolean.FALSE.equals(user.getIsAdmin())) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근할 수 없습니다.");
                return;
            }
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);

            chain.doFilter(request, response);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 인증 실패");
        }
    }
}