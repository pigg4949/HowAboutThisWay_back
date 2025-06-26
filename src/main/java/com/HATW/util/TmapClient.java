package com.HATW.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Map;

@Component
public class TmapClient {

    /** application.properties 에 정의된 값 주입 */
    @Value("${TMAP.APP.KEY}")
    private String appKey;

    @Value("${MATRIX.URL}")
    private String matrixUrl;

    @Value("${PEDESTRIAN.URL}")
    private String pedestrianUrl;

    @Value("${TRANSIT.URL}")
    private String transitUrl;

    @Value("${POI.URL}")
    private String poiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 대중교통 경로 검색 API 호출
     * @param reqBody Transit API 요청 바디(Map)
     * @return API 응답 JSON을 Map 으로 변환
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTransitRoute(Map<String, Object> reqBody) {
        String url = transitUrl + "?version=1&appKey=" + appKey;
        return restTemplate.postForObject(url, reqBody, Map.class);
    }

    /**
     * 보행자 경로 안내 API 호출
     * @param reqBody Pedestrian API 요청 바디(Map)
     * @return API 응답 JSON을 Map 으로 변환
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPedestrianRoute(Map<String, Object> reqBody) {
        String url = pedestrianUrl + "&appKey=" + appKey;
        return restTemplate.postForObject(url, reqBody, Map.class);
    }

    /**
     * Matrix API 호출 (배열 형태의 GeoJSON features 반환)
     * @param fromLat 출발지 위도
     * @param fromLon 출발지 경도
     * @param toLat   도착지 위도
     * @param toLon   도착지 경도
     * @return API 응답 Map (키 "features" 에 List<Map> 형태의 LineString 리스트)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMatrix(double fromLat, double fromLon,
                                         double toLat, double toLon) {
        String url = matrixUrl
                + "?version=1"
                + "&appKey=" + appKey
                + "&startX=" + fromLon
                + "&startY=" + fromLat
                + "&endX=" + toLon
                + "&endY=" + toLat;
        return restTemplate.getForObject(url, Map.class);
    }

    /**
     * POI 검색 API 호출 (필요 시 사용)
     * @param reqBody POI API 요청 바디(Map)
     * @return API 응답 JSON을 Map 으로 변환
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> searchPoi(Map<String, Object> reqBody) {
        String url = poiUrl + "?version=1&appKey=" + appKey;
        return restTemplate.postForObject(url, reqBody, Map.class);
    }
}