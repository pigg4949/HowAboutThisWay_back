package com.HATW.controller;

import com.HATW.service.MapService;
import com.HATW.service.TmapService;
import com.HATW.service.TransitService;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapController {
    private final TmapService tmapService;
    private final TransitService transitService;
    private final MapService  mapService;
    private final Gson gson = new Gson();

    /**
     * POI 검색 (외부 API 호출)
     * keyword: 검색어 한 개
     */
    @GetMapping("/searchLocation")
    public ResponseEntity<String> searchLocation(@RequestParam String keyword) {
        try {
            // 외부 API 호출 결과를 JSON 문자열로 반환
            String jsonResult = mapService.searchLocation(keyword);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(gson.toJson(Map.of("error", "POI 검색 중 오류")));
        }
    }

    /**
     * 1) 환승 전후 보행경로 계산 후 반환 (/forPram) - 엘리베이터 경유 강제
     */
    @PostMapping("/forPram")
    public ResponseEntity<String> getTransitWithConnector(@RequestBody Map<String,Object> params) {
        try {
            System.out.println("[FORPRAM] /forPram 엔드포인트 호출됨");

            // 1단계: Tmap 대중교통 API 호출
            String rawJson = tmapService.getTransitRoute(params);
            // 2단계: 엘리베이터 강제 경유 처리
            String withElevator = transitService.getRouteWithElevator(rawJson);
            // 3단계: 환승 전후 보행경로 계산
            String enriched = transitService.connectingTrafficWalkPaths(withElevator);
            return ResponseEntity.ok(enriched);
        } catch (Exception e) {
            System.err.println("[FORPRAM] 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 2) 모든 보행 레그 재계산 후 반환 (/forOld)
     */
    @PostMapping("/forOld")
    public ResponseEntity<String> getTransitAllWalk(@RequestBody Map<String,Object> params) {
        try {
            String rawJson = tmapService.getTransitRoute(params);
            String enriched = transitService.connectingWalkPaths(rawJson);
            return ResponseEntity.ok(enriched);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/editRouteGuidance")
    public ResponseEntity<String> processRouteInstructions(@RequestBody Map<String, Object> request) {
        try {
            String result = mapService.processRouteInstructions(request);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
