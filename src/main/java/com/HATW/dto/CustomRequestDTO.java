package com.HATW.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CustomRequestDTO {
    // 시작 좌표
    private double startX;
    private double startY;

    // 도착 좌표
    private double endX;
    private double endY;

    // 출발지 도착지 이름
    private String startName;
    private String endName;

    private List<CoordinateDTO> avoidList;
    // 피해야 할 반경 (미터 단위)
    @Min(50)
    private double avoidRadius;
}

