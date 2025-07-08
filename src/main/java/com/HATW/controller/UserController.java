package com.HATW.controller;

import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import com.HATW.service.UserService;
import com.HATW.util.JwtUtil;
import com.HATW.util.SmsService;
import com.HATW.util.VerificationCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.HATW.util.DuplicateUserIdException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;
    private final VerificationCodeService codeService;


    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }




    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        try {
            userService.register(user);
            return ResponseEntity.ok("회원가입 성공");
        } catch (DuplicateUserIdException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @GetMapping("/check-dup")
    public ResponseEntity<?> checkDuplicate(@RequestParam String userId) {
        if (userService.existsByUserId(userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginReq) {
        // 1) 로그인 시도 (JWT 발급)
        try {
            String token = userService.login(loginReq.getUserId(), loginReq.getPasswordHash());

        if (token == null) {
            return ResponseEntity.status(401).body("로그인 실패");
        }

        // 2) UserDTO에서 isAdmin 가져오기
        UserDTO user = userService.getUserByUserId(loginReq.getUserId());
        Boolean isAdmin = Boolean.TRUE.equals(user.getIsAdmin());

        // 3) 응답 생성
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("isAdmin", isAdmin);

        return ResponseEntity.ok(res);
    } catch (IllegalStateException ex)

    {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    } 

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        UserDTO user = userService.getUserInfoFromToken(token);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String token,
                                    @RequestBody UserDTO user) {
        userService.update(token, user);
        return ResponseEntity.ok("회원정보 수정 완료");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token) {
        userService.delete(token);
        return ResponseEntity.ok("회원탈퇴 완료");
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code, HttpSession session) {
        UserDTO user = userService.kakaoLogin(code);
        session.setAttribute("user", user);
        String jwt = jwtUtil.generateToken(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("userId", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/google/callback")
    public ResponseEntity<?> googleLogin(
            @RequestParam("code") String code,
            HttpSession session) {

        UserDTO user = userService.googleLogin(code);
        session.setAttribute("user", user);

        String jwt = jwtUtil.generateToken(user.getUserId());

        Map<String, Object> resp = new HashMap<>();
        resp.put("token",  jwt);
        resp.put("userId", user.getUserId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        String userId = request.getUserId();
        String phone  = request.getPhone();

        try {
            smsService.sendTemporaryPasswordAndReset(phone, userId);
            return ResponseEntity.ok("✅ 비밀번호 초기화 및 문자 전송 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ 요청 오류: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 서버 오류: 비밀번호는 초기화되었을 수 있으나 문자 전송에 실패했습니다.");
        }
    }

    /** 비밀번호 초기화 요청용 DTO */
    public static class ResetPasswordRequest {
        private String userId;
        private String phone;

        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }
        public String getPhone() {
            return phone;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body("휴대폰 번호는 필수입니다.");
        }

        String code = codeService.generateAndSaveCode(phone);
        smsService.sendSms(phone, "인증번호는 [" + code + "]입니다. 3분 내에 입력해주세요.");
        System.out.println("[DEBUG] 인증번호 전송 대상: " + phone);
        System.out.println("[DEBUG] 전송된 인증번호: " + code);
        return ResponseEntity.ok(Map.of("message", "인증번호 전송 완료", "code", code)); // 개발 중이라면 code 포함
    }

    // 인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String inputCode = request.get("code");

        if (codeService.verifyCode(phone, inputCode)) {
            return ResponseEntity.ok(Map.of("message", "인증 성공"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패 또는 만료");
        }
    }
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody FindIdRequest request) {
        String name = request.getName();
        String phone = request.getPhone();

        try {
            String userId = userService.findUserIdByNameAndPhone(name, phone);
            if (userId != null) {
                return ResponseEntity.ok(Map.of("userId", userId));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 회원 정보가 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류 발생");
        }
    }

    // ✅ 아이디 찾기 요청 DTO
    public static class FindIdRequest {
        private String name;
        private String phone;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @PatchMapping("/{userId}/active")
    public ResponseEntity<Void> updateActive(
            @PathVariable String userId,
            @RequestParam("isActive") boolean isActive,
            @RequestHeader("Authorization") String token) {

        // 서비스 레이어에 1(활성) 또는 0(비활성)로 전달
        userService.setActiveStatus(userId, isActive);
        return ResponseEntity.noContent().build();
    }




}
