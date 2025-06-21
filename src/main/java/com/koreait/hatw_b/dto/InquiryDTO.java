package com.koreait.hatw_b.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryDTO {
    private Integer idx;             // PK
    private String userId;           // 문의한 사용자 아이디
    private String content;          // 문의 내용
    private String adminResponses;   // 관리자 답변
    private LocalDateTime createdAt; // 등록일시
    private LocalDateTime updatedAt; // 수정일시
}
