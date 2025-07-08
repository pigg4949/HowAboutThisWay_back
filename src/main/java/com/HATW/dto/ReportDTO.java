package com.HATW.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ReportDTO(Double lon, Double lat, String comment) {
        this.lon     = lon;
        this.lat     = lat;
        this.comment = comment;
    }
}
