package com.HATW.controller;

import com.HATW.dto.RouteRequestDTO;
import com.HATW.dto.RouteResponseDTO;
import com.HATW.service.MapService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService service;
    private final Gson gson;

    @PostMapping("/process")
    public RouteResponseDTO processRoute(@RequestBody RouteRequestDTO request) throws IOException, InterruptedException {
        return service.processRoute(request);
    }

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
