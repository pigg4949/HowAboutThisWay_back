package com.HATW.service;

import com.HATW.dto.CoordinateDTO;
import com.HATW.dto.CustomRequestDTO;
import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
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


     // 커스텀 보행자 경로: 재귀 함수로 충돌 회피 처리

    @Override
    public String getCustomPedestrianRoute(CustomRequestDTO req)
            throws IOException, InterruptedException {
        // avoidList 초기화: reportMapper에서 조회
        System.out.println("[MapService] getCustomPedestrianRoute 시작, 초기 avoidList 세팅");
        List<ReportDTO> reports = reportMapper.findApprovedReports();
        List<CoordinateDTO> avoidList = reports.stream()
                .map(r -> new CoordinateDTO(r.getLon(), r.getLat()))
                .toList();
        req.setAvoidList(avoidList);

        System.out.println("[MapService] getCustomPedestrianRoute 시작, req=" + gson.toJson(req));
        try {
            String result = getCustomRouteSection(
                    req.getStartX(), req.getStartY(),
                    req.getEndX(),   req.getEndY(),
                    req.getStartName(), req.getEndName(),
                    req,
                    0,
                    new HashSet<>()
            );
            System.out.println("[MapService] getCustomPedestrianRoute 완료, result length=" + (result != null ? result.length() : "null"));
            return result;
        } catch (Exception ex) {
            System.out.println("[MapService] Error in getCustomPedestrianRoute: " + ex);
            ex.printStackTrace();
            throw ex;
        }
    }


     // 재귀적 회피 경로 계산

    private String getCustomRouteSection(
            double startX, double startY,
            double endX,   double endY,
            String startName, String endName,
            CustomRequestDTO req,
            int depth,
            Set<CoordinateDTO> visitedDetours
    ) throws IOException, InterruptedException {
        final int MAX_DEPTH = 10;
        System.out.println(String.format(
                "[MapService] Enter getCustomRouteSection depth=%d, start=(%.6f,%.6f), end=(%.6f,%.6f), visitedDetours=%s",
                depth, startX, startY, endX, endY, visitedDetours
        ));
        try {
            // 1) 깊이 한도 도달 시
            if (depth >= MAX_DEPTH) {
                System.out.println("[MapService] depth limit reached (" + depth + ") → 기본 segment 반환");
                return getPedestrianRoute(
                        Map.of(
                                "startX", startX,
                                "startY", startY,
                                "endX",   endX,
                                "endY",   endY,
                                "startName", startName,
                                "endName",   endName
                        ),
                        Collections.emptyList()
                );
            }

            // 기본 경로 요청
            Map<String, Object> params = Map.of(
                    "startX", startX,
                    "startY", startY,
                    "endX",   endX,
                    "endY",   endY,
                    "startName", startName,
                    "endName",   endName
            );
            System.out.println("[MapService] Requesting segment, params=" + params);
            String json = getPedestrianRoute(params, Collections.emptyList());
            System.out.println("[MapService] Received JSON length=" + (json != null ? json.length() : "null"));

            // 경로 파싱
            List<CoordinateDTO> route = GeometryUtil.parsePolyline(json);
            System.out.println("[MapService] Parsed route size=" + (route != null ? route.size() : "null"));

            // 충돌 지점 검색
            Optional<CoordinateDTO> conflictOpt = route.stream()
                    .filter(pt -> req.getAvoidList().stream()
                            .anyMatch(av -> GeometryUtil.distance(pt, av) * 111000.0 < req.getAvoidRadius()))
                    .findFirst();
            System.out.println("[MapService] conflict found? " + conflictOpt.isPresent());
            if (conflictOpt.isEmpty()) {
                System.out.println("[MapService] No conflict → returning segment");
                return json;
            }

            // 회피 지점 계산
            CoordinateDTO conflict = conflictOpt.get();
            System.out.println("[MapService] conflict at (" + conflict.getX() + "," + conflict.getY() + ")");
            int idx = route.indexOf(conflict);
            CoordinateDTO detour = GeometryUtil.computeDetourPoint(route, idx, req.getAvoidRadius());
            System.out.println("[MapService] computed detour at (" + detour.getX() + "," + detour.getY() + ")");

            boolean same = conflict.getX() == detour.getX() && conflict.getY() == detour.getY();
            if (same || visitedDetours.contains(detour)) {
                System.out.println("[MapService] detour invalid or duplicate → returning segment");
                return json;
            }
            visitedDetours.add(detour);

            // 재귀 호출
            System.out.println("[MapService] recursing part1 and part2");
            String part1 = getCustomRouteSection(
                    startX, startY,
                    detour.getX(), detour.getY(),
                    startName, "Detour",
                    req, depth + 1, visitedDetours
            );
            String part2 = getCustomRouteSection(
                    detour.getX(), detour.getY(),
                    endX, endY,
                    "Detour", endName,
                    req, depth + 1, visitedDetours
            );

            // 병합
            System.out.println("[MapService] merging parts");
            String merged = GeometryUtil.mergeRoutes(part1, part2);
            System.out.println("[MapService] merged JSON length=" + (merged != null ? merged.length() : "null"));
            return merged;
        } catch (Exception ex) {
            System.out.println("[MapService] Exception in getCustomRouteSection at depth=" + depth + ": " + ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}