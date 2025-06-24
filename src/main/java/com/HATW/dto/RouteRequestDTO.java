package com.HATW.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RouteRequestDTO {
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private LocalDateTime timestamp = LocalDateTime.now(); // 생성 시 기본 시간 설정
}
