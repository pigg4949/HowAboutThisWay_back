package com.HATW.controller;

import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import com.HATW.service.UserService;
import com.HATW.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService service;
    private final JwtUtil jwtUtil;

//    public UserController(UserService service) {
//        this.service = service;
//    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        service.register(user);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO loginRequest) {
        String token = service.login(loginRequest.getUserId(), loginRequest.getPasswordHash());
        if (token != null) {
            return ResponseEntity.ok().body(token);
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        service.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        UserDTO user = service.getUserInfoFromToken(token);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String token, @RequestBody UserDTO user)  {
        service.update(token, user);
        return ResponseEntity.ok("회원정보 수정 완료");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token) {
        service.delete(token);
        return ResponseEntity.ok("회원탈퇴 완료");
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code, HttpSession session) {
        UserDTO user = service.kakaoLogin(code);
        session.setAttribute("user", user);
        String jwt = jwtUtil.generateToken(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("userId", user.getUserId());
        return ResponseEntity.ok(response);
    }

}
