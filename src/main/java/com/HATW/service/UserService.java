// src/main/java/com/koreait/howaboutthisway/service/UserService.java
package com.HATW.service;

import com.HATW.dto.LoginDTO;
import com.HATW.dto.UserDTO;
import jakarta.servlet.http.HttpSession;

public interface UserService {
    /**
     * 신규 회원 가입
     * @param userDTO 회원가입 폼에서 넘어온 데이터 (userId, ssn1, ssn2, name, password 등)
     */
    void register(UserDTO userDTO);

    /**
     * 로그인 검증
     * @param loginDTO 로그인 정보 (userId, password)
     * @param session HTTP 세션
     * @return 검증 성공 시 true, 실패 시 false
     */
    boolean login(LoginDTO loginDTO, HttpSession session);

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return UserDTO
     */
    UserDTO getUserInfo(Long userId);

    /**
     * 사용자 정보 수정
     * @param userId 사용자 ID
     * @param userDTO 수정할 사용자 정보
     */
    void updateUserInfo(Long userId, UserDTO userDTO);

    /**
     * 사용자 삭제
     * @param userId 사용자 ID
     */
    void deleteUser(Long userId);

    /**
     * 아이디 찾기
     * @param userDTO 사용자 정보 (이름, 전화번호 등)
     * @return 찾은 아이디
     */
    String findUsername(UserDTO userDTO);

    /**
     * 비밀번호 재설정 메일/SMS 전송
     * @param userDTO 사용자 정보
     */
    void sendResetPassword(UserDTO userDTO);

    /**
     * 전화번호 인증 코드 검증
     * @param phone 전화번호
     * @param code 인증 코드
     * @return 검증 성공 시 true
     */
    boolean verifyPhoneCode(String phone, String code);
}
