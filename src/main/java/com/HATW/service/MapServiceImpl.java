package com.HATW.service;

import com.HATW.dto.LegDTO;
import com.HATW.dto.RouteInfoDTO;
import com.HATW.dto.RouteRequestDTO;
import com.HATW.dto.RouteResponseDTO;
import com.HATW.dto.MarkerDTO;
import com.HATW.mapper.MarkerMapper;
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

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private final MarkerMapper markerMapper;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${POI.URL}")
    private String POI_URL;
    @Value("${TRANSIT.URL}")
    private String TRANSIT_URL;
    @Value("${TMAP.APP.KEY}")
    private String TMAP_APPKEY;
    @Value("${MATRIX.URL}")
    private String MATRIX_URL;

    @Override
    public String searchLocation(String keyword) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException("searchLocation 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public String getTransitRoute(Map<String, Object> params) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("startX", params.get("startX").toString());
            body.addProperty("startY", params.get("startY").toString());
            body.addProperty("endX",   params.get("endX").toString());
            body.addProperty("endY",   params.get("endY").toString());
            body.addProperty("format", "json");
            body.addProperty("count",  10);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(TRANSIT_URL))
                    .header("appKey", TMAP_APPKEY)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new IOException("대중교통 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
            }
            return res.body();
        } catch (Exception e) {
            throw new RuntimeException("getTransitRoute 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RouteResponseDTO processRoute(RouteRequestDTO req) {
        try {
            Map<String, Object> params = Map.of(
                    "startX", req.getStartX(),
                    "startY", req.getStartY(),
                    "endX",   req.getEndX(),
                    "endY",   req.getEndY(),
                    "count",  req.getCount()
            );
            Map<String, Object> root = gson.fromJson(getTransitRoute(params), Map.class);

            Object planRaw = root.containsKey("metaData")
                    ? ((Map<String, Object>) root.get("metaData")).get("plan")
                    : root.get("plan");
            List<Map<String, Object>> itineraries = planRaw instanceof List
                    ? (List<Map<String, Object>>) planRaw
                    : (List<Map<String, Object>>) ((Map<String, Object>) planRaw).get("itineraries");

            List<RouteInfoDTO> routes = new ArrayList<>();
            for (Map<String, Object> plan : itineraries) {
                List<Map<String, Object>> legsJson = (List<Map<String, Object>>) plan.get("legs");
                boolean hasSubway = legsJson.stream()
                        .anyMatch(l -> "SUBWAY".equals(l.get("mode")));

                List<LegDTO> legsDto = new ArrayList<>();
                for (int i = 0; i < legsJson.size(); i++) {
                    Map<String, Object> cur = legsJson.get(i);
                    String mode = (String) cur.get("mode");
                    String next = i + 1 < legsJson.size()
                            ? (String) legsJson.get(i + 1).get("mode")
                            : null;

                    LegDTO leg = new LegDTO();
                    leg.setMode(mode);
                    leg.setSectionTime(((Number) cur.get("sectionTime")).intValue());
                    leg.setDistance(((Number) cur.get("distance")).intValue());
                    leg.setStart((Map<String, Object>) cur.get("start"));
                    leg.setEnd((Map<String, Object>) cur.get("end"));

                    if (hasSubway && "WALK".equals(mode) && "SUBWAY".equals(next)) {
                        String station = ((Map<String, Object>) cur.get("end")).get("name") + "역";
                        leg.setSegments(findBestMatrix(cur, station, true));
                    } else if (hasSubway && "SUBWAY".equals(mode) && "WALK".equals(next)) {
                        Map<String, Object> nx = legsJson.get(i + 1);
                        String station = ((Map<String, Object>) nx.get("start")).get("name") + "역";
                        leg.setSegments(findBestMatrix(cur, station, false));
                    } else {
                        leg.setSegments((List<Map<String, Object>>) cur.get("steps"));
                    }

                    Map<String, Object> extra = new HashMap<>();
                    for (String k : List.of("route", "routeId", "routeColor", "service", "type", "passStopList")) {
                        if (cur.containsKey(k)) extra.put(k, cur.get(k));
                    }
                    leg.setExtra(extra);
                    legsDto.add(leg);
                }

                RouteInfoDTO info = new RouteInfoDTO();
                info.setFare((Map<String, Object>) plan.get("fare"));
                info.setTransferCount(((Number) plan.get("transferCount")).intValue());
                info.setTotalTime(((Number) plan.get("totalTime")).intValue());
                info.setTotalWalkTime(((Number) plan.get("totalWalkTime")).intValue());
                info.setTotalWalkDistance(((Number) plan.get("totalWalkDistance")).intValue());
                info.setTotalDistance(((Number) plan.get("totalDistance")).intValue());
                info.setPathType(((Number) plan.get("pathType")).intValue());
                info.setLegs(legsDto);
                routes.add(info);
            }

            return new RouteResponseDTO(routes);
        } catch (Exception e) {
            throw new RuntimeException("processRoute 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> findBestMatrix(
            Map<String, Object> legJson,
            String station,
            boolean isWalkToSubway) {
        try {
            List<Map<String, Object>> originalSteps =
                    (List<Map<String, Object>>) legJson.get("steps");

            Map<String, Object> pt = isWalkToSubway
                    ? (Map<String, Object>) legJson.get("start")
                    : (Map<String, Object>) legJson.get("end");
            double fromLat = ((Number) pt.get("lat")).doubleValue();
            double fromLon = ((Number) pt.get("lon")).doubleValue();

            List<MarkerDTO> cand = markerMapper.findElevatorsByStation(station);
            if (cand.isEmpty()) {
                return originalSteps;
            }

            double bestCost = Double.MAX_VALUE;
            List<Map<String, Object>> bestFeat = originalSteps;
            for (MarkerDTO m : cand) {
                String uri = String.format(
                        "%s?version=1&appKey=%s&startX=%.6f&startY=%.6f&endX=%.6f&endY=%.6f",
                        MATRIX_URL, TMAP_APPKEY, fromLon, fromLat, m.getLon(), m.getLat()
                );
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .GET()
                        .build();
                HttpResponse<String> res = client.send(
                        req, HttpResponse.BodyHandlers.ofString());
                Map<String, Object> mat = gson.fromJson(res.body(), Map.class);

                List<Map<String, Object>> features =
                        (List<Map<String, Object>>) mat.get("features");
                if (features == null || features.isEmpty()) {
                    continue;
                }

                double cost = features.stream()
                        .mapToDouble(f -> ((Number) ((Map) f.get("properties")).get("distance")).doubleValue())
                        .sum();
                if (cost < bestCost) {
                    bestCost = cost;
                    bestFeat = features;
                }
            }

            return bestFeat;
        } catch (Exception e) {
            throw new RuntimeException("findBestMatrix 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }
}