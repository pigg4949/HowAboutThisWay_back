package com.HATW.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElevatorCandidateDTO {
    private String stationName; // 역명
    private String exitName;    // 출구명 또는 위치 설명
    private double lon;         // 경도
    private double lat;         // 위도
    private String comment;     // 원본 comment(설명)
}