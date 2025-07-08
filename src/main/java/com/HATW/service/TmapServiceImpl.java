package com.HATW.service;

import com.HATW.mapper.ReportMapper;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TmapServiceImpl implements TmapService {

    @Value("${PEDESTRIAN.URL}")
    private String pedestrianUrl;
    @Value("${TMAP.APP.KEY}")
    private String appKey;
    @Value("${TRANSIT.URL}")
    private String transitUrl;

    private HttpClient httpClient = HttpClient.newHttpClient();
    private final ReportMapper reportMapper;

    @Override
    public String getPedestrianRoute(Map<String, Object> params)
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



        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(pedestrianUrl))
                .header("appKey", appKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 429) {
            System.err.println("[ERROR] Tmap 보행자 경로 API 쿼터 초과(429): " + res.body());
            throw new IOException("보행자 경로 API 쿼터 초과(429): " + res.body());
        }
        if (res.statusCode() != 200) {
            System.err.println("[ERROR] 보행자 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
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


        System.out.println("[DEBUG] TMAP AppKey for Transit API: '" + appKey + "'");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(transitUrl))
                .header("appKey", appKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("====== TMAP Transit API Response ======");
        System.out.println("Status Code: " + res.statusCode());
        System.out.println("Response Body: " + res.body());
        System.out.println("=====================================");

        if (res.statusCode() != 200) {
            throw new IOException("대중교통 경로 API 에러(status=" + res.statusCode() + "): " + res.body());
        }
        return res.body();
    }
}
