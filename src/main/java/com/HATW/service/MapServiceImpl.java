package com.HATW.service;

import com.HATW.dto.CoordinateDTO;
import com.HATW.dto.CustomRequestDTO;
import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
import com.HATW.util.Config;
import com.HATW.util.GeometryUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ReportMapper reportMapper;

    @Value("${POI.URL}")
    private String POI_URL;
    @Value("${PEDESTRIAN.URL}")
    private String PEDESTRIAN_URL;
    @Value("${TRANSIT.URL}")
    private String TRANSIT_URL;
    @Value("${TMAP.APP.KEY}")
    private String TMAP_APPKEY;


    @Override
    public String searchLocation(String keyword) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String uri = String.format(
                "%s?version=1&format=json&appKey=%s&searchKeyword=%s&searchType=all&page=1&count=10&reqCoordType=WGS84GEO&resCoordType=EPSG3857",
                POI_URL, TMAP_APPKEY, encoded
        );
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("POI 검색 API 에러(status=" + res.statusCode() + "): " + res.body());
        }
        return res.body();
    }

    @Override
    public String getPedestrianRoute(Map<String, Object> params, List<CoordinateDTO> detourList)
            throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("startX", params.get("startX").toString());
        body.addProperty("startY", params.get("startY").toString());
        body.addProperty("endX",   params.get("endX").toString());
        body.addProperty("endY",   params.get("endY").toString());
        body.addProperty("startName", params.get("startName").toString());
        body.addProperty("endName",   params.get("endName").toString());
        body.addProperty("reqCoordType", "WGS84GEO");
        body.addProperty("resCoordType", "WGS84GEO");
        body.addProperty("searchOption", 0);

        // detourList가 비어있지 않으면 passList 추가
        if (detourList != null && !detourList.isEmpty()) {
            String passList = detourList.stream()
                    .map(d -> d.getX() + "," + d.getY())
                    .collect(Collectors.joining("_"));
            body.addProperty("passList", passList);
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(PEDESTRIAN_URL))
                .header("appKey", TMAP_APPKEY)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("보행자 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
        }
        return res.body();
    }

    @Override
    public String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("startX", params.get("startX").toString());
        body.addProperty("startY", params.get("startY").toString());
        body.addProperty("endX", params.get("endX").toString());
        body.addProperty("endY", params.get("endY").toString());
        body.addProperty("format", "json");
        body.addProperty("count", 10);


        System.out.println("[DEBUG] TMAP AppKey for Transit API: '" + TMAP_APPKEY + "'");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(TRANSIT_URL))
                .header("appKey", TMAP_APPKEY)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("====== TMAP Transit API Response ======");
        System.out.println("Status Code: " + res.statusCode());
        System.out.println("Response Body: " + res.body());
        System.out.println("=====================================");

        if (res.statusCode() != 200) {
            throw new IOException("대중교통 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
        }
        return res.body();
    }

    @Override
    public String getCustomPedestrianRoute(CustomRequestDTO req)
            throws IOException, InterruptedException {
        // 1. 사고지점 리스트 세팅
        List<ReportDTO> reports = reportMapper.findApprovedReports();
        List<CoordinateDTO> avoidList = reports.stream()
                .map(r -> new CoordinateDTO(r.getLon(), r.getLat()))
                .collect(Collectors.toList());
        req.setAvoidList(avoidList);

        // 2. params 맵 준비 (출발/도착 등)
        Map<String, Object> params = Map.of(
                "startX", req.getStartX(),
                "startY", req.getStartY(),
                "endX", req.getEndX(),
                "endY", req.getEndY(),
                "startName", req.getStartName(),
                "endName", req.getEndName()
        );

        // 3. 기본 경로 계산
        String baseJson = getPedestrianRoute(params, Collections.emptyList());
        List<CoordinateDTO> route = GeometryUtil.parsePolyline(baseJson);

        // 4. detourList(경유지) 생성 (최대 5개)
        List<CoordinateDTO> detourList = new ArrayList<>();
        for (CoordinateDTO pt : route) {
            boolean isConflict = req.getAvoidList().stream()
                    .anyMatch(av -> GeometryUtil.distance(pt, av) * 111000.0 < req.getAvoidRadius());
            if (isConflict) {
                int idx = route.indexOf(pt);
                CoordinateDTO detour = GeometryUtil.computeDetourPoint(route, idx, req.getAvoidRadius());
                if (detourList.stream().noneMatch(d -> d.getX() == detour.getX() && d.getY() == detour.getY())) {
                    detourList.add(detour);
                    if (detourList.size() >= 5) break;
                }
            }
        }

        // 5. 경유지(passList) 포함해서 최종 경로 재요청
        String finalJson = getPedestrianRoute(params, detourList);
        return finalJson;
    }

    // 재귀적 회피 경로 함수
    // 오버로딩 기법
    private String getCustomRouteSection(
            double startX, double startY, double endX, double endY,
            String startName, String endName, CustomRequestDTO req
    ) throws IOException, InterruptedException {
        return getCustomRouteSection(startX, startY, endX, endY, startName, endName, req, 0);
    }


// 재귀적 회피 경로 함수 (예외 없이 최대한 회피한 경로 반환)
    private String getCustomRouteSection(
            double startX, double startY, double endX, double endY,
            String startName, String endName, CustomRequestDTO req, int depth
    ) throws IOException, InterruptedException {
        int MAX_DEPTH = 10; // 무한루프 방지용
        if (depth > MAX_DEPTH) {
            System.out.println("재귀 깊이 초과! 강제 종료, 현재까지 경로 반환");
            // 예외를 던지지 않고 현재까지 경로 반환
            String json = getPedestrianRoute(Map.of(
                    "startX", startX, "startY", startY,
                    "endX", endX, "endY", endY,
                    "startName", startName, "endName", endName
            ), Collections.emptyList());
            return json;
        }

        String json = getPedestrianRoute(Map.of(
                "startX", startX, "startY", startY,
                "endX", endX, "endY", endY,
                "startName", startName, "endName", endName
        ), Collections.emptyList());
        List<CoordinateDTO> route = GeometryUtil.parsePolyline(json);

        Optional<CoordinateDTO> conflict = route.stream()
                .filter(pt -> req.getAvoidList().stream()
                        .anyMatch(av -> GeometryUtil.distance(pt, av) * 111000.0 < req.getAvoidRadius()))
                .findFirst();

        if (conflict.isEmpty()) {
            return json;
        }

        int conflictIdx = route.indexOf(conflict.get());
        CoordinateDTO detour = GeometryUtil.computeDetourPoint(route, conflictIdx, req.getAvoidRadius());

        // detour 좌표가 충돌점과 같으면 더 이상 회피 불가, 현재까지 경로 반환
        if (conflict.get().getX() == detour.getX() && conflict.get().getY() == detour.getY()) {
            System.out.println("detour 좌표가 충돌점과 동일! 강제 종료, 현재까지 경로 반환");
            return json;
        }

        String part1 = getCustomRouteSection(
                startX, startY, detour.getX(), detour.getY(), startName, "Detour", req, depth + 1
        );
        String part2 = getCustomRouteSection(
                detour.getX(), detour.getY(), endX, endY, "Detour", endName, req, depth + 1
        );

        return GeometryUtil.mergeRoutes(part1, part2);
    }
}