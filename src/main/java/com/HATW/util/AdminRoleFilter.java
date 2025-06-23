package com.HATW.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AdminRoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);

        // 세션 또는 관리자 권한 없음
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
            return;
        }

        // 관리자 권한 있으므로 다음으로 진행
        chain.doFilter(request, response);
    }
}