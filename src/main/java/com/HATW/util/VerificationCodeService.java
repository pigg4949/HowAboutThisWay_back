package com.HATW.util;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();

    public String generateAndSaveCode(String phone) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
        codeStore.put(phone, new CodeInfo(code, System.currentTimeMillis()));
        return code;
    }

    public boolean verifyCode(String phone, String inputCode) {
        CodeInfo info = codeStore.get(phone);
        if (info == null) return false;

        long now = System.currentTimeMillis();
        long elapsed = (now - info.timestamp) / 1000;

        return info.code.equals(inputCode) && elapsed <= 180; // 3분
    }

    private static class CodeInfo {
        String code;
        long timestamp;

        CodeInfo(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}
