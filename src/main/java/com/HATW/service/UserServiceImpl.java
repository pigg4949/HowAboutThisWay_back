package com.HATW.service;

import com.HATW.mapper.UserMapper;
import com.HATW.util.JwtUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import com.HATW.dto.UserDTO;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;
    private final JwtUtil jwtUtil;

    @Override
    public void register(UserDTO user) {
        String hashed =BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
        user.setPasswordHash(hashed);
        mapper.insertUser(user);
    }

    @Override
    public String login(String userId, String passwordHash) {
        UserDTO user = mapper.findByUserId(userId);
        if(user!=null && BCrypt.checkpw(passwordHash, user.getPasswordHash())) {
            return jwtUtil.generateToken(user.getUserId());
        }
        return null;
    }

    @Override
    public void logout(String token) {
        // 클라이언트에서 토큰 제거
    }

    @Override
    public void update(String token, UserDTO user) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);
        UserDTO original = mapper.findByUserId(userId);
        if(original != null) {
            if(StringUtils.hasText(user.getPasswordHash())) {
                String hashed = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
                original.setPasswordHash(hashed);
            }
            if(StringUtils.hasText(user.getName())) {
                original.setName(user.getName());
            }
            mapper.update(original);
        }
    }

    @Override
    public void delete(String token) {
        // "Bearer " 접두어 제거
        String jwt = token.replace("Bearer ", "");
        // 토큰에서 userId 추출
        String userId = jwtUtil.getUserIdFromToken(jwt);
        // 해당 userId로 DB에서 삭제
        mapper.delete(userId);
    }

    @Override
    public UserDTO getUserInfoFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(jwt);
        UserDTO user = mapper.findByUserId(userId);
        if(user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }
}

