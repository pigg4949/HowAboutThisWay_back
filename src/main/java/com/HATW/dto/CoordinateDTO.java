package com.HATW.dto;



import lombok.Data;

@Data
public class CoordinateDTO {
    private double x; // 경도 (lon)
    private double y; // 위도 (lat)

    public CoordinateDTO() {
        // 기본 생성자 명시
    }

    public CoordinateDTO(double x, double y) {
        this.x = x;
        this.y = y;
    }
}