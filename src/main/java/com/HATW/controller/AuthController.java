package com.HATW.controller;

import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import com.HATW.util.PasswordUtil;
import com.HATW.util.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String phoneNumber) {
        smsService.sendVerificationCode(phoneNumber);
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String phoneNumber,
                                             @RequestParam String code) {
        boolean isValid = smsService.verifyCode(phoneNumber, code);
        if (isValid) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증 실패 또는 만료됨");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String phoneNumber,
                                           @RequestParam String code,
                                           @RequestParam String newPassword) {
        if (!smsService.verifyCode(phoneNumber, code)) {
            return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
        }

        UserDTO user = userMapper.findByPhoneNumber(phoneNumber);
        if (user == null) {
            return ResponseEntity.badRequest().body("존재하지 않는 사용자입니다.");
        }

        // 비밀번호 업데이트
        user.setPassword(newPassword);
        String hashedPassword = PasswordUtil.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        userMapper.updateUser(user);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
