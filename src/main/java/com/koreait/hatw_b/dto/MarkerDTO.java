package com.koreait.hatw_b.dto;

import lombok.Data;

@Data
public class MarkerDTO {
    private Integer idx;     // PK
    private Integer type;    // 마커 타입
    private Double lon;      // 경도
    private Double lat;      // 위도
    private String address;  // 주소(지도 표시용)
    private String comment;  // 설명(지도 표시용)
    private Integer weight;  // 가중치(알고리즘용)
}
