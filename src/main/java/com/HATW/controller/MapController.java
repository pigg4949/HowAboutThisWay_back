package com.HATW.controller;

import com.HATW.service.MapService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService service;
    private final Gson gson;

//    /**
//     * 지도 뷰 API (APP_KEY 반환)
//     */
//    @PostMapping("/view")
//    public ResponseEntity<String> viewMap() {
//        return ResponseEntity.ok(config.getAppKey());
//    }

    /**
     * POI 검색 (외부 API 호출)
     * keyword: 검색어 한 개
     */
    @GetMapping("/searchLocation")
    public ResponseEntity<String> searchLocation(@RequestParam String keyword) {
        try {
            // 외부 API 호출 결과를 JSON 문자열로 반환
            String jsonResult = service.searchLocation(keyword);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "POI 검색 중 오류")));
        }
    }

    /**
     * 경로 로그 저장 (선택적)
     */
    @PostMapping(value = "/routeLog", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> logRoute(@RequestBody Map<String, Object> params) {
        service.logRoute(params);
        return ResponseEntity.noContent().build();
    }

    /**
     * 보행자 경로 안내
     */
    @PostMapping(value = "/pedestrianRoute", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> pedestrianRoute(@RequestBody Map<String, Object> params) {
        try {
            String json = service.getPedestrianRoute(params);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "서버 경로탐색 호출 중 오류")));
        }
    }

    /**
     * 대중교통 경로 안내
     */
    @PostMapping(value = "/transitRoute", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> transitRoute(@RequestBody Map<String, Object> params) {
        try {
            String json = service.getTransitRoute(params);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "서버 대중교통 호출 중 오류")));
        }
    }
}
