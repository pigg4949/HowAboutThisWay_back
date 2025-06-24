package com.HATW.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private Integer idx;           // PK
    private String userId;         // 사용자 아이디
    private String ssn1;           // 주민번호 앞자리
    private String ssn2;           // 주민번호 뒷자리
    private String name;           // 이름
    private String passwordHash;   // 비밀번호 해시
    private String phone;          // 전화번호
    private Boolean isActive;      // 활성 여부
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean isAdmin; // 관리자
}
