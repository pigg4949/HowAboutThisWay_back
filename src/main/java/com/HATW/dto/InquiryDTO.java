package com.HATW.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class InquiryDTO {
    private Integer idx;             // PK
    private String userId;           // 문의한 사용자 아이디
    private String content;          // 문의 내용
    private String adminResponses;   // 관리자 답변
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
