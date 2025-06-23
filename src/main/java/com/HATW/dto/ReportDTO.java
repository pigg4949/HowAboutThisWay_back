package com.HATW.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportDTO {
    private Integer idx;             // PK
    private String userId;           // 제보자 사용자 아이디
    private Integer type;            // 제보 타입 (단차, 보도 폭 좁음 등)
    private Double lon;              // 경도
    private Double lat;              // 위도
    private String comment;          // 제보 내용
    private String imageUrl;         // 업로드된 이미지 URL
    private String status;           // 처리 상태 (PENDING, APPROVED, REJECTED)
    private Integer weight;          // 가중치 (알고리즘용)
    private LocalDateTime createdAt; // 등록일시
    private LocalDateTime updatedAt; // 수정일시
}
