package com.HATW.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubwayConnectorDTO {
    // 첫 연결: 전체 출발지 → 첫 지하철역 엘리베이터
    private String origin_name;
    private MarkerDTO origin_point;
    private String first_station_name;
    private MarkerDTO first_elevator_point;
    private String origin_to_elevator_json;

    // 마지막 연결: 마지막 지하철역 엘리베이터 → 전체 목적지
    private String last_station_name;
    private MarkerDTO last_elevator_point;
    private String destination_name;
    private MarkerDTO destination_point;
    private String elevator_to_destination_json;
}
