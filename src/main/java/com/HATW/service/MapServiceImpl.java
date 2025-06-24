//package com.HATW.service;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URLEncoder;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class MapServiceImpl implements MapService {
//
//
//    @Value("${AppKey}")
//    private String AppKey;
//    @Value("${POI_URL}")
//    private String POI_URL;
//    @Value("${TMAP_TRANSIT_URL}")
//    private String TMAP_TRANSIT_URL;
//    @Value("${TMAP_PEDESTRIAN_URL}")
//    private String TMAP_PEDESTRIAN_URL;
//
//    private final Gson gson = new Gson();
//    private final HttpClient client = HttpClient.newHttpClient();
//
//    @Override
//    public String searchLocation(String keyword) throws IOException, InterruptedException {
//        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
//        String uri = String.format(
//                "%s?version=1&format=json&appKey=%s&searchKeyword=%s&searchType=all&page=1&count=10&reqCoordType=WGS84GEO&resCoordType=EPSG3857",
//                POI_URL, AppKey, encoded
//        );
//        HttpRequest req = HttpRequest.newBuilder()
//                .uri(URI.create(uri))
//                .header("Accept", "application/json")
//                .GET()
//                .build();
//        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
//        if (res.statusCode() != 200) {
//            throw new IOException("POI 검색 API 에러(status=" + res.statusCode() + "): " + res.body());
//        }
//        return res.body();
//    }
//
//    @Override
//    public void logRoute(Map<String, Object> params) {
//        // TODO: 로그를 DB에 저장하거나 다른 처리 로직 구현
//        System.out.println("[RouteLog] " + gson.toJson(params));
//    }
//
//    @Override
//    public String getPedestrianRoute(Map<String, Object> params) throws IOException, InterruptedException {
//        JsonObject body = new JsonObject();
//        body.addProperty("startX", params.get("startX").toString());
//        body.addProperty("startY", params.get("startY").toString());
//        body.addProperty("endX",   params.get("endX").toString());
//        body.addProperty("endY",   params.get("endY").toString());
//        body.addProperty("startName", params.get("startName").toString());
//        body.addProperty("endName",   params.get("endName").toString());
//        body.addProperty("reqCoordType", "WGS84GEO");
//        body.addProperty("resCoordType", "EPSG3857");
//        body.addProperty("searchOption", 0);
//
//        HttpRequest req = HttpRequest.newBuilder()
//                .uri(URI.create(TMAP_PEDESTRIAN_URL))
//                .header("appKey", AppKey)
//                .header("Content-Type", "application/json; charset=UTF-8")
//                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
//                .build();
//        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
//        if (res.statusCode() != 200) {
//            throw new IOException("보행자 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
//        }
//        return res.body();
//    }
//
//    @Override
//    public String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException {
//        JsonObject body = new JsonObject();
//        body.addProperty("startX", params.get("startX").toString());
//        body.addProperty("startY", params.get("startY").toString());
//        body.addProperty("endX",   params.get("endX").toString());
//        body.addProperty("endY",   params.get("endY").toString());
//        body.addProperty("format", "json");
//        body.addProperty("count", 10);
//
//        System.out.println("[DEBUG] TMAP AppKey for Transit API: '" + AppKey + "'");
//
//        HttpRequest req = HttpRequest.newBuilder()
//                .uri(URI.create(TMAP_TRANSIT_URL))
//                .header("appKey", AppKey)
//                .header("Content-Type", "application/json; charset=UTF-8")
//                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
//                .build();
//        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
//
//        System.out.println("====== TMAP Transit API Response ======");
//        System.out.println("Status Code: " + res.statusCode());
//        System.out.println("Response Body: " + res.body());
//        System.out.println("=====================================");
//
//        if (res.statusCode() != 200) {
//            throw new IOException("대중교통 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
//        }
//        return res.body();
//    }
//}