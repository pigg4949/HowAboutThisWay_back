package com.HATW.service;

import com.HATW.dto.UserDTO;

public interface UserService {
    void register(UserDTO userDTO);
    String login(String userId, String passwordHash);
    void logout(String token);
    void update(String token, UserDTO user);
    void delete(String token);
    UserDTO getUserInfoFromToken(String token);
    UserDTO kakaoLogin(String code);
}
