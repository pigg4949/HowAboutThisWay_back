package com.HATW.service;

import com.HATW.mapper.ReportMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.ArrayList;
import okhttp3.*;

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ReportMapper reportMapper;
    private final Gson gson = new Gson();

    @Value("${POI.URL}")
    private String poiUrl;
    @Value("${TMAP.APP.KEY}")
    private String appKey;
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Autowired
    private com.HATW.mapper.MarkerMapper markerMapper;

    // 영어 타입을 한글로 변환하는 함수
    private String getKoreanType(String type) {
        if ("WALK".equals(type)) return "도보";
        if ("BUS".equals(type)) return "버스";
        if ("SUBWAY".equals(type)) return "지하철";
        return type;
    }

    @Override
    public String searchLocation(String keyword) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String uri = String.format(
                "%s?version=1&format=json&appKey=%s&searchKeyword=%s&searchType=all&page=1&count=10&reqCoordType=WGS84GEO&resCoordType=EPSG3857",
                poiUrl, appKey, encoded
        );
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("POI 검색 API 에러(status=" + res.statusCode() + "): " + res.body());
        }
        return res.body();
    }

    @Override
    public String processRouteInstructions(Map<String, Object> request) throws IOException {
        List<Map<String, Object>> descriptions = (List<Map<String, Object>>) request.get("descriptions");

        // OpenAI API 키 확인
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            throw new IOException("OpenAI API 키가 설정되지 않았습니다.");
        }

        System.out.println("[NLP] 자연어 처리 시작 - 안내 단계 수: " + (descriptions != null ? descriptions.size() : 0));

        // 프롬프트: description만을 기반으로 안내문 생성
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 전문적인 네비게이션 안내 시스템입니다. " +
                "사용자에게 명확하고 친근한 경로 안내를 제공하는 것이 목표입니다.\n\n");
        prompt.append("아래는 경로 안내를 위한 실제 데이터(description)입니다. " +
                "반드시 이 데이터를 기반으로 경로 안내문을 만들어야합니다.\n");
        prompt.append("- 안내문은 반드시 한국어로 만들어준다.\n");
        prompt.append("- 각 안내문에는 이동 방향과 교통수단을 표현 할 이모티콘을 자연스럽게 포함한다.\n");
        prompt.append("- 안내문에 '좌회전'이 포함되면 ⬅️ 이모티콘을, '우회전'이 포함되면 ➡️ 이모티콘을 해당 안내문에 자연스럽게 포함한다.\n");
        prompt.append("- 안내문은 너무 길지 않게, 명확하고 간결하게 요약한다.\n");
        prompt.append("- 영어 안내문이 나오면 반드시 한국어로 번역해서 출력한다.\n");
        prompt.append("- 불필요한 출력(위아래 json, 따옴표, 번호, 중괄호 등)은 모두 제거한다.\n");
        prompt.append("- 안내문을 한 줄 씩 JSON 배열 형태로 반환한다.\n");
        prompt.append("description 배열(각 단계별 안내):\n");
        prompt.append(gson.toJson(descriptions)).append("\n");

        // OpenAI API 호출 및 안내문 파싱 (타임아웃 설정 개선)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        String openaiUrl = "https://api.openai.com/v1/chat/completions";
        String reqBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"user\", \"content\": " + gson.toJson(prompt.toString()) + "}\n" +
                "  ],\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"temperature\": 0.3\n" +
                "}";

        RequestBody body = RequestBody.create(mediaType, reqBody);
        Request requestObj = new Request.Builder()
                .url(openaiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + openaiApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        // 재시도 로직 추가
        IOException lastException = null;
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("[NLP] OpenAI API 호출 시도 " + attempt + "/" + maxRetries);

                try (Response response = client.newCall(requestObj).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "응답 본문 없음";
                        System.err.println("[ERROR] OpenAI API 호출 실패 - Status: " + response.code() + ", Body: " + errorBody);
                        throw new IOException("OpenAI API 호출 실패: " + response.code() + " - " + errorBody);
                    }
                    String responseBody = response.body().string();
                    System.out.println("[NLP] OpenAI API 호출 성공 - 응답 길이: " + responseBody.length());

                    // OpenAI 응답에서 안내문 추출 및 파싱 (기존 로직)
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    String content = json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();

                    content = content.trim();
                    JsonElement elem;
                    try {
                        elem = gson.fromJson(content, JsonElement.class);
                        if (!elem.isJsonArray()) {
                            String[] lines = content.split("\n");
                            List<String> instructions = new ArrayList<>();
                            for (String line : lines) {
                                line = line.trim();
                                if (!line.isEmpty()) {
                                    line = line.replaceAll("^\\d+\\.\\s*", "");
                                    line = line.replaceAll("^[\\[\\]\\{\\}\\s]*", "");
                                    line = line.replaceAll("[\\[\\]\\{\\}\\s]*$", "");
                                    if (!line.isEmpty()) {
                                        instructions.add(line);
                                    }
                                }
                            }
                            elem = gson.toJsonTree(instructions);
                        }
                    } catch (Exception e) {
                        String[] lines = content.split("\n");
                        List<String> instructions = new ArrayList<>();
                        for (String line : lines) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                line = line.replaceAll("^\\d+\\.\\s*", "");
                                line = line.replaceAll("^[\\[\\]\\{\\}\\s]*", "");
                                line = line.replaceAll("[\\[\\]\\{\\}\\s]*$", "");
                                if (!line.isEmpty()) {
                                    instructions.add(line);
                                }
                            }
                        }
                        elem = gson.toJsonTree(instructions);
                    }
                    JsonObject result = new JsonObject();
                    result.add("instructions", elem);
                    System.out.println("[NLP] 자연어 처리 완료 - 생성된 안내문 수: " + elem.getAsJsonArray().size());
                    return gson.toJson(result);
                }

            } catch (IOException e) {
                lastException = e;
                System.err.println("[NLP] OpenAI API 호출 실패 (시도 " + attempt + "/" + maxRetries + "): " + e.getMessage());

                if (attempt < maxRetries) {
                    // 재시도 전 잠시 대기 (지수 백오프)
                    long waitTime = (long) Math.pow(2, attempt - 1) * 1000; // 1초, 2초, 4초...
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("재시도 중 인터럽트 발생", ie);
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] processRouteInstructions 오류: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("자연어 처리 중 오류 발생: " + e.getMessage());
            }
        }

        System.err.println("[NLP] OpenAI API 호출 최종 실패 (" + maxRetries + "번 시도 후)");
        throw new IOException("OpenAI API 호출 실패 (최대 재시도 횟수 초과): " +
                (lastException != null ? lastException.getMessage() : "알 수 없는 오류"), lastException);
    }
}