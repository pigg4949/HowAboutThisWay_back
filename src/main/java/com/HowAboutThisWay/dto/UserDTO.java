package com.HowAboutThisWay.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer idx;           // PK
    private String userId;         // 사용자 아이디
    private String ssn1;           // 주민번호 앞자리
    private String ssn2;           // 주민번호 뒷자리
    private String name;           // 이름
    private String passwordHash;   // 비밀번호 해시
    private String phone;          // 전화번호
    private Boolean isActive;      // 활성 여부
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
}
