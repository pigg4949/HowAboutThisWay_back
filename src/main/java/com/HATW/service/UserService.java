// src/main/java/com/koreait/howaboutthisway/service/UserService.java
package com.HATW.service;

import com.HATW.dto.UserDTO;

public interface UserService {
    /**
     * 신규 회원 가입
     * @param userDTO 회원가입 폼에서 넘어온 데이터 (userId, ssn1, ssn2, name, password 등)
     */
    void register(UserDTO userDTO);

    /**
     * 로그인 검증
     * @param userId   입력된 아이디
     * @param password 입력된 비밀번호(평문)
     * @return 검증 성공 시 UserDTO(비밀번호 해시/솔트 포함), 실패 시 null
     */
    UserDTO login(String userId, String password);
}
