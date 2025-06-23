package com.HATW.controller;

import com.HATW.dto.LoginDTO;
import com.HATW.dto.UserDTO;
import com.HATW.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserDTO userDTO) {
        try {
            userService.register(userDTO);
            return ResponseEntity.status(201).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // Duplicate
        } catch (Exception e) {
            return ResponseEntity.badRequest().build(); // 400
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        try {
            boolean authenticated = userService.login(loginDTO, session);
            if (authenticated) return ResponseEntity.ok(true);
            else return ResponseEntity.status(401).body(false);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo(@SessionAttribute(name = "loginUserId", required = false) Long userId) {
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(userService.getUserInfo(userId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMyInfo(@RequestBody UserDTO userDTO, HttpSession session) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            userService.updateUserInfo(userId, userDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(HttpSession session) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) return ResponseEntity.status(401).build();
        try {
            userService.deleteUser(userId);
            session.invalidate();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody UserDTO userDTO) {
        try {
            String username = userService.findUsername(userDTO);
            return ResponseEntity.ok(username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).build(); // Not found
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/find-password")
    public ResponseEntity<Void> findPassword(@RequestBody UserDTO userDTO) {
        try {
            userService.sendResetPassword(userDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).build(); // 정보 불일치
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<Boolean> verifyPhone(@RequestParam String phone, @RequestParam String code) {
        try {
            boolean verified = userService.verifyPhoneCode(phone, code);
            return ResponseEntity.ok(verified);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}