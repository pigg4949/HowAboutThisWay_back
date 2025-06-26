package com.HATW.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RouteRequestDTO {
    /** 출발지 경도 (X) */
    private double startX;
    /** 출발지 위도 (Y) */
    private double startY;
    /** 도착지 경도 (X) */
    private double endX;
    /** 도착지 위도 (Y) */
    private double endY;
    /** 추천 경로 개수 (옵션, 기본 10) */
    private int count = 10;

//    /**
//     * 원본 대중교통 API 응답 JSON
//     */
//    private Map<String, Object> transitJson;
}