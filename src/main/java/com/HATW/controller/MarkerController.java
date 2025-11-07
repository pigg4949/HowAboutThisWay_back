package com.HATW.controller;

import com.HATW.dto.MarkerDTO;

import com.HATW.service.MarkerService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MarkerController {


    private final MarkerService service;
    private final Gson gson;

    // 고정 마커 조회
    @GetMapping("/markers")
    public ResponseEntity<List<MarkerDTO>> getMarkersByTypes(@RequestParam("types") List<Integer> types) {
        List<MarkerDTO> markers = service.getMarkersByTypes(types);
        return ResponseEntity.ok(markers);
    }

    // 제보 마커 조회
    // 승인된 사용자 제보 마커 조회
    @GetMapping("/reports")
    public ResponseEntity<List<Map<String, Object>>> getReportsByTypes(@RequestParam("types") List<Integer> types) {
        return ResponseEntity.ok(service.getReportsByTypes(types));
    }


}
