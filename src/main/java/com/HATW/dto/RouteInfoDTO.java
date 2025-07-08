package com.HATW.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RouteInfoDTO {
    private Map<String, Object> fare;
    private int totalTime;
    private int totalWalkTime;
    private int totalWalkDistance;
    private int transferCount;
    private int totalDistance;
    private int pathType;
    private List<LegDTO> legs;
}