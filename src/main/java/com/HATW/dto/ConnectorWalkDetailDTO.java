package com.HATW.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorWalkDetailDTO {
    private int itineraryIndex;
    private int beforeWalkLegIndex;
    private int afterWalkLegIndex;
    // -- for full-route passList 호출에 쓰일 정보
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private int    searchOption;
    private String passList;  // "lon1,lat1;lon2,lat2;…"
    private String beforeElevatorName; // 출발역 엘리베이터 이름
    private String afterElevatorName;  // 도착역 엘리베이터 이름
    private String beforeElevatorExit;
    private String afterElevatorExit;
    private String beforeElevatorComment;
    private String afterElevatorComment;
}
