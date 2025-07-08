package com.HATW.service;

import com.HATW.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    void register(UserDTO userDTO);
    String login(String userId, String rawPassword);
    void logout(String token);
    void update(String token, UserDTO user);
    void delete(String token);
    UserDTO getUserInfoFromToken(String token);
    UserDTO kakaoLogin(String code);
    UserDTO googleLogin(String code);
    UserDTO getUserByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean checkUserIdExists(String userId);
    String findUserIdByNameAndPhone(String name, String phone);
    void setActiveStatus(String userId, boolean isActive);
}
