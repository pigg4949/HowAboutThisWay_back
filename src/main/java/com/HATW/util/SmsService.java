package com.HATW.util;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsService {

    private final DefaultMessageService messageService;
    private final String senderPhone;
    private final Map<String, VerificationInfo> verificationMap = new ConcurrentHashMap<>();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_SECONDS = 300; // 5분

    public SmsService(@Value("${coolsms.api-key}") String apiKey,
                      @Value("${coolsms.api-secret}") String apiSecret,
                      @Value("${coolsms.sender}") String senderPhone) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        this.senderPhone = senderPhone;
    }

    public void sendVerificationCode(String phoneNumber) {
        String code = generateCode();
        verificationMap.put(phoneNumber, new VerificationInfo(code, LocalDateTime.now()));

        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(phoneNumber);
        message.setText("[HATW] 인증번호: " + code + " (5분 이내 입력)");

        // messageService.send(message);
    }

    public boolean verifyCode(String phoneNumber, String inputCode) {
        VerificationInfo info = verificationMap.get(phoneNumber);
        return info != null
                && info.code.equals(inputCode)
                && info.createdAt.plusSeconds(EXPIRE_SECONDS).isAfter(LocalDateTime.now());
    }

    private String generateCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(number);
    }

    private static class VerificationInfo {
        private final String code;
        private final LocalDateTime createdAt;

        public VerificationInfo(String code, LocalDateTime createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }
}