// src/main/java/com/koreait/howaboutthisway/service/impl/UserServiceImpl.java
package com.HATW.service;

import com.HATW.dto.LoginDTO;
import com.HATW.dto.UserDTO;
import com.HATW.mapper.UserMapper;
import com.HATW.util.SmsService;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final SmsService smsService;

    @Override
    public void register(UserDTO userDTO) {
        // 1) 솔트 생성
        String salt = BCrypt.gensalt();
        // 2) 해시 생성
        String hashed = BCrypt.hashpw(userDTO.getPassword(), salt);

        userDTO.setPasswordSalt(salt);
        userDTO.setPasswordHash(hashed);
        // 기타 컬럼(isActive, createdAt 등)은 DB 기본값에 맡깁니다.
        userMapper.insertUser(userDTO);
    }

    @Override
    public boolean login(LoginDTO loginDTO, HttpSession session) {
        UserDTO user = userMapper.findByUserId(loginDTO.getUserId());
        if (user == null) return false;
        // 비밀번호 검증
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPasswordHash())) {
            return false;
        }
        // 세션에 사용자 ID 저장
        session.setAttribute("loginUserId", user.getIdx());
        return true;
    }

    @Override
    public UserDTO getUserInfo(Long userId) {
        return userMapper.findByIdx(userId.intValue());
    }

    @Override
    public void updateUserInfo(Long userId, UserDTO userDTO) {
        UserDTO existingUser = userMapper.findByIdx(userId.intValue());
        if (existingUser == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        // 비밀번호가 제공된 경우 암호화
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            String salt = BCrypt.gensalt();
            String hashed = BCrypt.hashpw(userDTO.getPassword(), salt);
            userDTO.setPasswordSalt(salt);
            userDTO.setPasswordHash(hashed);
        }
        
        userDTO.setIdx(userId.intValue());
        userMapper.updateUser(userDTO);
    }

    @Override
    public void deleteUser(Long userId) {
        UserDTO user = userMapper.findByIdx(userId.intValue());
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userMapper.deleteByUserId(user.getUserId());
    }

    @Override
    public String findUsername(UserDTO userDTO) {
        UserDTO user = userMapper.findByNameAndPhone(userDTO.getName(), userDTO.getPhone());
        if (user == null) {
            throw new IllegalArgumentException("일치하는 사용자를 찾을 수 없습니다.");
        }
        return user.getUserId();
    }

    @Override
    public void sendResetPassword(UserDTO userDTO) {
        UserDTO user = userMapper.findByUserId(userDTO.getUserId());
        if (user == null || !user.getPhone().equals(userDTO.getPhone())) {
            throw new IllegalArgumentException("일치하는 사용자를 찾을 수 없습니다.");
        }
        // SMS 인증번호 전송
        smsService.sendVerificationCode(user.getPhone());
    }

    @Override
    public boolean verifyPhoneCode(String phone, String code) {
        return smsService.verifyCode(phone, code);
    }
}

