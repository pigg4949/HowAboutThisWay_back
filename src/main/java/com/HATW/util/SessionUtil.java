package com.HATW.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionUtil {

    private static final String LOGIN_USER_ID = "loginUserId";

    // 로그인: 세션에 사용자 ID 저장
    public void setLoginUser(HttpServletRequest request, Long userId) {
        HttpSession session = request.getSession(true); // 없으면 생성
        session.setAttribute(LOGIN_USER_ID, userId);
    }

    // 로그아웃: 세션 제거
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 없으면 null
        if (session != null) {
            session.invalidate();
        }
    }

    // 로그인한 사용자 ID 가져오기
    public Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute(LOGIN_USER_ID);
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof String) {
                try {
                    return Long.parseLong((String) userId);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    // 로그인 여부 확인
    public boolean isLoggedIn(HttpServletRequest request) {
        return getLoginUserId(request) != null;
    }
}