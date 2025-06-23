// src/main/java/com/koreait/howaboutthisway/service/impl/UserServiceImpl.java
package com.HATW.service;

import com.HATW.mapper.UserMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import com.HATW.dto.UserDTO;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

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
    public UserDTO login(String userId, String password) {
        UserDTO user = userMapper.findByUserId(userId);
        if (user == null) return null;
        // 비밀번호 검증
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }
}

