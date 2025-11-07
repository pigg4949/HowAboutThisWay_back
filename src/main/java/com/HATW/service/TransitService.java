package com.HATW.service;

import com.HATW.dto.ConnectorWalkDetailDTO;

import java.io.IOException;
import java.util.List;

public interface TransitService {
    /**
     * 원본 대중교통 JSON 에서,
     * 각 itinerary 별로 앞/뒤 WALK 레그의 보행자 API 최단+계단회피 경로만 뽑아낸 정보를 리턴
     */
    List<ConnectorWalkDetailDTO> computeWalkPath(String jsonData) throws Exception;

    /**
     * 원본 대중교통 JSON 에 computeConnectorWalkDetails 결과를 덮어쓴 새로운 JSON 반환
     */
    String connectingTrafficWalkPaths(String transitJson) throws Exception;

    String connectingWalkPaths(String transitJson) throws Exception;

    /**
     * 엘리베이터 경유 보행 경로 강제 서비스
     * @param transitJson 대중교통 API 결과(JSON)
     * @return 엘리베이터 경유 경로가 포함된 전체 경로 JSON
     */
    String getRouteWithElevator(String transitJson) throws Exception;
}
