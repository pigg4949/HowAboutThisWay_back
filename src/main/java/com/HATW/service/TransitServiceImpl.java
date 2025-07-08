package com.HATW.service;

import com.HATW.dto.ConnectorWalkDetailDTO;
import com.HATW.dto.MarkerDTO;
import com.HATW.dto.ElevatorCandidateDTO;
import com.HATW.mapper.MarkerMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TransitServiceImpl implements TransitService {
    private final MarkerMapper markerMapper;
    private final TmapService tmapService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 1) 엘리베이터 경유 경로 강제 처리 (/forPram 첫 번째 단계)
     * 목표: 출발지-도보-지하철-도보-목적지 경로에서
     * - 첫 번째 WALK: 출발지 → 탑승역 엘리베이터로 덮기
     * - 두 번째 WALK: 하차역 엘리베이터 → 목적지로 덮기
     * (중간 WALK 레그는 건드리지 않음)
     */
    public String getRouteWithElevator(String transitJson) throws Exception {
        System.out.println("[ELEVATOR] 엘리베이터 강제 경유 처리 시작");

        ObjectNode root = (ObjectNode) mapper.readTree(transitJson);
        ArrayNode itineraries = (ArrayNode) root.path("metaData").path("plan").path("itineraries");

        int processedCount = 0;
        for (int itinIdx = 0; itinIdx < itineraries.size(); itinIdx++) {
            ObjectNode itin = (ObjectNode) itineraries.get(itinIdx);
            ArrayNode legs = (ArrayNode) itin.path("legs");

            // 지하철 레그 인덱스 찾기
            int subwayStartIdx = -1, subwayEndIdx = -1;
            for (int i = 0; i < legs.size(); i++) {
                String mode = legs.get(i).path("mode").asText("");
                if ("SUBWAY".equalsIgnoreCase(mode)) {
                    if (subwayStartIdx == -1) subwayStartIdx = i;
                    subwayEndIdx = i;
                }
            }
            if (subwayStartIdx == -1) {
                continue; // 지하철이 없으면 스킵
            }

            // 첫 번째 WALK 레그 처리: 출발지 → 탑승역 엘리베이터
            if (subwayStartIdx > 0) {
                ObjectNode firstWalkLeg = (ObjectNode) legs.get(subwayStartIdx - 1);
                if ("WALK".equalsIgnoreCase(firstWalkLeg.path("mode").asText())) {
                    processWalkToElevator(firstWalkLeg, legs.get(subwayStartIdx), true);
                    processedCount++;
                }
            }
            // 두 번째 WALK 레그 처리: 하차역 엘리베이터 → 목적지
            if (subwayEndIdx < legs.size() - 1) {
                ObjectNode secondWalkLeg = (ObjectNode) legs.get(subwayEndIdx + 1);
                if ("WALK".equalsIgnoreCase(secondWalkLeg.path("mode").asText())) {
                    processWalkToElevator(secondWalkLeg, legs.get(subwayEndIdx), false);
                    processedCount++;
                }
            }
        }

        System.out.println("[ELEVATOR] 엘리베이터 강제 경유 처리 완료 - " + processedCount + "개 WALK 레그 수정");
        return mapper.writeValueAsString(root);
    }

    /**
     * WALK 레그를 엘리베이터 경유 경로로 처리
     * @param walkLeg 처리할 WALK 레그
     * @param subwayLeg 연결된 지하철 레그
     * @param isToStation true: 출발지→탑승역, false: 하차역→목적지
     */
    private void processWalkToElevator(ObjectNode walkLeg, JsonNode subwayLeg, boolean isToStation) {
        // 역명 추출
        String stationName = null;
        if (isToStation) {
            // 탑승역: 지하철 레그의 첫 번째 역명
            ArrayNode stationList = (ArrayNode) subwayLeg.path("passStopList").path("stationList");
            if (stationList.size() > 0) {
                stationName = stationList.get(0).path("stationName").asText("");
            }
        } else {
            // 하차역: 지하철 레그의 마지막 역명
            ArrayNode stationList = (ArrayNode) subwayLeg.path("passStopList").path("stationList");
            if (stationList.size() > 0) {
                stationName = stationList.get(stationList.size() - 1).path("stationName").asText("");
            }
        }
        if (stationName == null || stationName.isEmpty()) {
            System.out.println("[엘리베이터 처리] 역명 추출 실패");
            return;
        }
        System.out.println("[ELEVATOR] " + (isToStation ? "탑승역" : "하차역") + " 처리: " + stationName);
        // 엘리베이터 후보 조회
        List<ElevatorCandidateDTO> candidates = findElevatorCandidates(stationName);
        System.out.println("[DEBUG] 엘리베이터 후보 좌표 목록: " + candidates);
        if (candidates.isEmpty()) {
            System.out.println("[ELEVATOR] " + stationName + "역 엘리베이터 없음 - 원본 경로 유지");
            return;
        }
        // WALK 레그의 시작/끝 좌표
        double sx = walkLeg.path("start").path("lon").asDouble();
        double sy = walkLeg.path("start").path("lat").asDouble();
        double ex = walkLeg.path("end").path("lon").asDouble();
        double ey = walkLeg.path("end").path("lat").asDouble();
        // 최적 엘리베이터 찾기
        double minDist = Double.MAX_VALUE;
        ObjectNode bestWalkSection = null;
        ElevatorCandidateDTO bestElevator = null;
        for (ElevatorCandidateDTO elev : candidates) {
            double elevLon = elev.getLon();
            double elevLat = elev.getLat();
            ObjectNode walkRoute;
            if (isToStation) {
                walkRoute = getWalkRouteToElevator(sx, sy, elevLon, elevLat);
            } else {
                walkRoute = getWalkRouteFromElevator(elevLon, elevLat, ex, ey);
            }
            if (walkRoute == null) {
                System.out.println("[ELEVATOR] " + elev.getComment() + " 경로 계산 실패");
                continue;
            }
            double dist = calcSectionDistance(walkRoute);
            if (dist < minDist) {
                minDist = dist;
                bestWalkSection = walkRoute;
                bestElevator = elev;
            }
        }
        if (bestWalkSection != null) {
            walkLeg.set("walkRouteJson", bestWalkSection);
            ArrayNode newSteps = updateStepsFromWalkRoute(bestWalkSection, walkLeg);
            if (newSteps != null) {
                walkLeg.set("steps", newSteps);
            }
            System.out.println("[ELEVATOR] " + stationName + "역 엘리베이터 경로 적용 성공: " + bestElevator.getComment() + " (거리: " + minDist + "m)");
        } else {
            System.out.println("[ELEVATOR] " + stationName + "역 모든 엘리베이터 경로 계산 실패 - 원본 경로 유지");
        }
    }

    // 출발지 → 엘리베이터 경로 요청
    private ObjectNode getWalkRouteToElevator(double sx, double sy, double elevLon, double elevLat) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startX", sx);
            params.put("startY", sy);
            params.put("endX", elevLon);
            params.put("endY", elevLat);
            params.put("startName", "출발지");
            params.put("endName", "엘리베이터");
            params.put("searchOption", 4);
            String walkJson = tmapService.getPedestrianRoute(params);

            if (walkJson == null || walkJson.isEmpty()) {
                System.out.println("[ELEVATOR] Tmap API 응답 없음");
                return null;
            }

            ObjectNode result = (ObjectNode) mapper.readTree(walkJson);

            // 응답 구조 확인
            if (!result.has("features") || result.get("features").size() == 0) {
                System.out.println("[ELEVATOR] Tmap API 응답에 features 없음");
                return null;
            }

            System.out.println("[ELEVATOR] 출발지→엘리베이터 경로 계산 성공");
            return result;
        } catch (Exception e) {
            System.err.println("[엘리베이터 경로] 출발지→엘리베이터 경로 계산 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // 엘리베이터 → 목적지 경로 요청
    private ObjectNode getWalkRouteFromElevator(double elevLon, double elevLat, double ex, double ey) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("startX", elevLon);
            params.put("startY", elevLat);
            params.put("endX", ex);
            params.put("endY", ey);
            params.put("startName", "엘리베이터");
            params.put("endName", "목적지");
            params.put("searchOption", 4);
            String walkJson = tmapService.getPedestrianRoute(params);

            if (walkJson == null || walkJson.isEmpty()) {
                System.out.println("[ELEVATOR] Tmap API 응답 없음");
                return null;
            }

            ObjectNode result = (ObjectNode) mapper.readTree(walkJson);

            // 응답 구조 확인
            if (!result.has("features") || result.get("features").size() == 0) {
                System.out.println("[ELEVATOR] Tmap API 응답에 features 없음");
                return null;
            }

            System.out.println("[ELEVATOR] 엘리베이터→목적지 경로 계산 성공");
            return result;
        } catch (Exception e) {
            System.err.println("[ELEVATOR] 엘리베이터→목적지 경로 계산 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 해당 역의 엘리베이터 후보 리스트 조회
     */
    private List<ElevatorCandidateDTO> findElevatorCandidates(String stationName) {
        List<ElevatorCandidateDTO> result = new ArrayList<>();

        // 1. 정확한 역명으로 조회
        List<com.HATW.dto.MarkerDTO> markers = markerMapper.selectByStationName(stationName);

        // 2. 역명 변형으로 추가 조회
        if (stationName.endsWith("역")) {
            // "역" 제거 후 조회 (예: "사당역" -> "사당")
            String stationNameWithoutStation = stationName.substring(0, stationName.length() - 1);
            List<com.HATW.dto.MarkerDTO> additionalMarkers = markerMapper.selectByStationName(stationNameWithoutStation);
            markers.addAll(additionalMarkers);
        } else {
            // "역" 추가 후 조회 (예: "사당" -> "사당역")
            String stationNameWithStation = stationName + "역";
            List<com.HATW.dto.MarkerDTO> additionalMarkers = markerMapper.selectByStationName(stationNameWithStation);
            markers.addAll(additionalMarkers);
        }

        // 3. 중복 제거 및 DTO 변환
        Set<String> seenComments = new HashSet<>();
        for (com.HATW.dto.MarkerDTO m : markers) {
            String comment = m.getComment();
            if (comment == null || seenComments.contains(comment)) continue;

            seenComments.add(comment);
            String exit = "";
            if (comment.contains(" ")) {
                int idx = comment.lastIndexOf(" ");
                exit = comment.substring(idx + 1);
            }
            result.add(new ElevatorCandidateDTO(stationName, exit, m.getLon(), m.getLat(), comment));
        }

        System.out.println("[ELEVATOR] " + stationName + "역 엘리베이터 " + result.size() + "개 발견");
        return result;
    }

    /**
     * walkRouteJson에서 steps 배열 생성
     */
    private ArrayNode updateStepsFromWalkRoute(ObjectNode walkRoute, ObjectNode walkLeg) {
        try {
            ArrayNode newSteps = mapper.createArrayNode();
            ArrayNode features = (ArrayNode) walkRoute.get("features");

            if (features == null || features.size() == 0) {
                return null;
            }

            // Tmap API 응답의 실제 경로 좌표를 사용하여 상세한 linestring 생성
            for (JsonNode feature : features) {
                if (feature.has("geometry") &&
                        "LineString".equals(feature.path("geometry").path("type").asText())) {

                    ArrayNode coordinates = (ArrayNode) feature.path("geometry").path("coordinates");
                    if (coordinates.size() > 0) {

                        // 모든 좌표를 linestring 형태로 연결
                        StringBuilder linestring = new StringBuilder();
                        for (int i = 0; i < coordinates.size(); i++) {
                            JsonNode coord = coordinates.get(i);
                            double lon = coord.get(0).asDouble();
                            double lat = coord.get(1).asDouble();
                            if (i > 0) linestring.append(" ");
                            linestring.append(lon).append(",").append(lat);
                        }

                        ObjectNode step = mapper.createObjectNode();
                        step.put("streetName", feature.path("properties").path("streetName").asText(""));
                        step.put("distance", feature.path("properties").path("distance").asDouble(0));
                        step.put("description", feature.path("properties").path("description").asText("엘리베이터 경유 경로"));
                        step.put("linestring", linestring.toString());
                        newSteps.add(step);
                    }
                }
            }

            // 만약 features가 없거나 비어있으면 fallback으로 직선 생성
            if (newSteps.size() == 0) {
                System.out.println("[AI 경고] Tmap API 응답에서 features를 찾을 수 없어 직선으로 대체");
                double startLon = walkLeg.path("start").path("lon").asDouble();
                double startLat = walkLeg.path("start").path("lat").asDouble();
                double endLon = walkLeg.path("end").path("lon").asDouble();
                double endLat = walkLeg.path("end").path("lat").asDouble();

                ObjectNode firstStep = mapper.createObjectNode();
                firstStep.put("streetName", "");
                firstStep.put("distance", 0);
                firstStep.put("description", "엘리베이터 경유 경로 (직선)");
                firstStep.put("linestring", startLon + "," + startLat + " " + endLon + "," + endLat);
                newSteps.add(firstStep);
            }

            return newSteps;

        } catch (Exception e) {
            System.err.println("[ELEVATOR] steps 배열 업데이트 실패: " + e.getMessage());
            return null;
        }
    }
    /**
     * FeatureCollection의 전체 거리 계산
     */
    private double calcSectionDistance(ObjectNode section) {
        double total = 0;
        try {
            ArrayNode features = (ArrayNode) section.path("features");
            for (JsonNode feat : features) {
                JsonNode props = feat.path("properties");
                if (props.has("distance")) total += props.get("distance").asDouble();
            }
        } catch (Exception e) {
            return 0;
        }
        return total;
    }

    /**
     * 2) 환승 전후 보행경로 계산 (/forPram 두 번째 단계)
     */
    @Override
    public String connectingTrafficWalkPaths(String transitJson) throws Exception {
        System.out.println("[FORPRAM] 환승 전후 보행경로 계산 시작");
        ObjectNode root = (ObjectNode) mapper.readTree(transitJson);
        ArrayNode itineraries = (ArrayNode) root.path("metaData").path("plan").path("itineraries");
        List<ConnectorWalkDetailDTO> connectorDetails = computeWalkPath(transitJson);

        for (int idx = 0; idx < itineraries.size(); idx++) {
            final int itineraryIndex = idx;
            ObjectNode itin = (ObjectNode) itineraries.get(idx);
            ArrayNode legs = (ArrayNode) itin.path("legs");

            for (int i = 0; i < legs.size(); i++) {
                ObjectNode leg = (ObjectNode) legs.get(i);
                if (!"WALK".equalsIgnoreCase(leg.path("mode").asText())) continue;

                double sx = leg.path("start").path("lon").asDouble();
                double sy = leg.path("start").path("lat").asDouble();
                double ex = leg.path("end").path("lon").asDouble();
                double ey = leg.path("end").path("lat").asDouble();

                // 엘리베이터 후보 조회 - 앞뒤 레그가 지하철인지 확인
                List<MarkerDTO> elevatorCands = new ArrayList<>();
                String stationName = null;

                // 앞 레그가 지하철인 경우
                if (i > 0 && isSubwayMode((ObjectNode) legs.get(i-1))) {
                    // 지하철 레그의 첫 번째 역명 사용
                    ObjectNode prevLeg = (ObjectNode) legs.get(i-1);
                    if (prevLeg.has("passStopList")) {
                        ArrayNode stationList = (ArrayNode) prevLeg.path("passStopList").path("stationList");
                        if (stationList.size() > 0) {
                            stationName = stationList.get(0).path("stationName").asText("");
                        }
                    }
                    if (stationName == null || stationName.isEmpty()) {
                        stationName = leg.path("start").path("name").asText("");
                    }
                    elevatorCands = markerMapper.selectByStationName(stationName);
                }
                // 뒤 레그가 지하철인 경우 (현재 WALK → 지하철 탑승)
                else if (i < legs.size()-1 && isSubwayMode((ObjectNode) legs.get(i+1))) {
                    // 지하철 레그의 첫 번째 역명 사용 (탑승역)
                    ObjectNode nextLeg = (ObjectNode) legs.get(i+1);
                    if (nextLeg.has("passStopList")) {
                        ArrayNode stationList = (ArrayNode) nextLeg.path("passStopList").path("stationList");
                        if (stationList.size() > 0) {
                            stationName = stationList.get(0).path("stationName").asText(""); // 첫 번째 역명 (탑승역)
                        }
                    }
                    if (stationName == null || stationName.isEmpty()) {
                        stationName = leg.path("end").path("name").asText("");
                    }
                    elevatorCands = markerMapper.selectByStationName(stationName);
                }

                if (!elevatorCands.isEmpty()) {
                    MarkerDTO elevator = elevatorCands.get(0);
                    double elevLon = elevator.getLon();
                    double elevLat = elevator.getLat();

                    // 1. 출발지~엘리베이터 (혹은 도착역~엘리베이터)
                    double dist1 = Math.sqrt(Math.pow(sx - elevLon, 2) + Math.pow(sy - elevLat, 2));
                    ObjectNode walkRoute1 = null;
                    if (dist1 < 0.0002) { // 약 20m 이하
                        walkRoute1 = createDirectLineStringFeature(sx, sy, elevLon, elevLat);
                    } else {
                        Map<String, Object> params1 = new HashMap<>();
                        params1.put("startX", sx);
                        params1.put("startY", sy);
                        params1.put("endX", elevLon);
                        params1.put("endY", elevLat);
                        params1.put("startName", leg.path("start").path("name").asText(""));
                        params1.put("endName", elevator.getComment() != null ? elevator.getComment() : "엘리베이터");
                        params1.put("searchOption", 4);
                        String walkJson1 = tmapService.getPedestrianRoute(params1);
                        walkRoute1 = (ObjectNode) mapper.readTree(walkJson1);
                    }

                    // 2. 엘리베이터~지하철(혹은 목적지)
                    double dist2 = Math.sqrt(Math.pow(ex - elevLon, 2) + Math.pow(ey - elevLat, 2));
                    ObjectNode walkRoute2 = null;
                    if (dist2 < 0.0002) {
                        walkRoute2 = createDirectLineStringFeature(elevLon, elevLat, ex, ey);
                    } else {
                        Map<String, Object> params2 = new HashMap<>();
                        params2.put("startX", elevLon);
                        params2.put("startY", elevLat);
                        params2.put("endX", ex);
                        params2.put("endY", ey);
                        params2.put("startName", elevator.getComment() != null ? elevator.getComment() : "엘리베이터");
                        params2.put("endName", leg.path("end").path("name").asText(""));
                        params2.put("searchOption", 4);
                        String walkJson2 = tmapService.getPedestrianRoute(params2);
                        walkRoute2 = (ObjectNode) mapper.readTree(walkJson2);
                    }

                    // feature 합치기
                    ArrayNode mergedFeatures = mapper.createArrayNode();
                    if (walkRoute1 != null && walkRoute1.has("features")) {
                        for (JsonNode f : walkRoute1.path("features")) mergedFeatures.add(f);
                    }
                    if (walkRoute2 != null && walkRoute2.has("features")) {
                        for (JsonNode f : walkRoute2.path("features")) mergedFeatures.add(f);
                    }
                    ObjectNode walkRouteJson = mapper.createObjectNode();
                    walkRouteJson.put("type", "FeatureCollection");
                    walkRouteJson.set("features", mergedFeatures);
                    leg.set("walkRouteJson", walkRouteJson);
                }

                // 엘리베이터 정보 추가
                ConnectorWalkDetailDTO detail = connectorDetails.stream()
                        .filter(d -> d.getItineraryIndex() == itineraryIndex)
                        .findFirst().orElse(null);
                if (detail != null) {
                    itin.put("beforeElevatorName", detail.getBeforeElevatorComment());
                    itin.put("beforeElevatorExit", detail.getBeforeElevatorComment());
                    itin.put("afterElevatorName", detail.getAfterElevatorComment());
                    itin.put("afterElevatorExit", detail.getAfterElevatorComment());
                }

            }
        }
        System.out.println("[FORPRAM] 환승 전후 보행경로 계산 완료");
        return mapper.writeValueAsString(root);
    }

    // 짧은 거리 직접 feature 생성
    private ObjectNode createDirectLineStringFeature(double sx, double sy, double ex, double ey) {
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");
        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "LineString");
        ArrayNode coords = mapper.createArrayNode();
        ArrayNode c1 = mapper.createArrayNode(); c1.add(sx); c1.add(sy);
        ArrayNode c2 = mapper.createArrayNode(); c2.add(ex); c2.add(ey);
        coords.add(c1); coords.add(c2);
        geometry.set("coordinates", coords);
        feature.set("geometry", geometry);
        ObjectNode props = mapper.createObjectNode();
        props.put("description", "직선 연결");
        feature.set("properties", props);
        ObjectNode fc = mapper.createObjectNode();
        fc.put("type", "FeatureCollection");
        ArrayNode arr = mapper.createArrayNode(); arr.add(feature);
        fc.set("features", arr);
        return fc;
    }

    /**
     * 해당 레그가 지하철 모드인지 확인하는 헬퍼 메서드
     */
    private boolean isSubwayMode(ObjectNode leg) {
        if (leg == null) {
            return false;
        }
        String mode = leg.path("mode").asText("");
        return "SUBWAY".equalsIgnoreCase(mode);
    }

    // ===== /forOld 기능 관련 함수들 =====

    /**
     * 모든 보행 레그 재계산 (/forOld 기능)
     */
    @Override
    public String connectingWalkPaths(String transitJson) throws Exception {
        ObjectNode root = (ObjectNode) mapper.readTree(transitJson);
        ArrayNode itineraries = (ArrayNode) root
                .path("metaData")
                .path("plan")
                .path("itineraries");

        for (int itinIdx = 0; itinIdx < itineraries.size(); itinIdx++) {
            ObjectNode itin = (ObjectNode) itineraries.get(itinIdx);
            ArrayNode legs = (ArrayNode) itin.path("legs");

            for (int i = 0; i < legs.size(); i++) {
                ObjectNode leg = (ObjectNode) legs.get(i);
                if (!"WALK".equalsIgnoreCase(leg.path("mode").asText())) continue;

                double sx = leg.path("start").path("lon").asDouble();
                double sy = leg.path("start").path("lat").asDouble();
                double ex = leg.path("end").path("lon").asDouble();
                double ey = leg.path("end").path("lat").asDouble();
                String sName = leg.path("start").path("name").asText("");
                String eName = leg.path("end").path("name").asText("");

                // 1) 출발=도착(0m) 또는 너무 근거리면 직접 FeatureCollection 생성
                double dist = Math.sqrt(Math.pow(sx - ex, 2) + Math.pow(sy - ey, 2));
                if (dist < 0.0002) { // 약 20m 이하
                    leg.set("walkRouteJson", createDirectLineStringFeature(sx, sy, ex, ey));
                    continue;
                }

                // 2) passList 만들기 (BUS와 연계된 경우)
                String passList = null;
                if (i > 0 && "BUS".equalsIgnoreCase(legs.get(i-1).path("mode").asText())) {
                    passList = buildPassList((ObjectNode) legs.get(i-1).path("passStopList"));
                } else if (i + 1 < legs.size() && "BUS".equalsIgnoreCase(legs.get(i+1).path("mode").asText())) {
                    passList = buildPassList((ObjectNode) legs.get(i+1).path("passStopList"));
                }

                Map<String,Object> params = new HashMap<>();
                params.put("startX", sx);
                params.put("startY", sy);
                params.put("endX", ex);
                params.put("endY", ey);
                params.put("startName", sName);
                params.put("endName", eName);
                params.put("searchOption", 30);
                if (passList != null) params.put("passList", passList);

                String walkJson = tmapService.getPedestrianRoute(params);
                ObjectNode walkRoute = (ObjectNode) mapper.readTree(walkJson);
                leg.set("walkRouteJson", walkRoute);
            }

            // 거리/시간 재계산 (기존 로직 유지)
            int totalDist = 0, totalTime = 0, totalWalkDist = 0, totalWalkTime = 0;
            for (JsonNode legNode : legs) {
                ObjectNode leg = (ObjectNode) legNode;
                String mode = leg.path("mode").asText();
                int dist = 0, time = 0;
                JsonNode wj = leg.path("walkRouteJson");
                if (wj.isObject()) {
                    for (JsonNode feat : wj.path("features")) {
                        JsonNode props = feat.path("properties");
                        if (props.has("distance")) dist += props.get("distance").asInt();
                        if (props.has("time")) time += props.get("time").asInt();
                    }
                    leg.put("distance", dist);
                    leg.put("sectionTime", time);
                } else {
                    dist = leg.path("distance").asInt(0);
                    time = leg.path("sectionTime").asInt(0);
                }
                totalDist += dist;
                totalTime += time;
                if ("WALK".equalsIgnoreCase(mode)) {
                    totalWalkDist += dist;
                    totalWalkTime += time;
                }
            }
            itin.put("totalDistance", totalDist);
            itin.put("totalTime", totalTime);
            itin.put("totalWalkDistance", totalWalkDist);
            itin.put("totalWalkTime", totalWalkTime);
        }
        return mapper.writeValueAsString(root);
    }

    /** BUS passStopList 에서 "lon,lat;lon,lat;..." 형태로 합쳐 반환 */
    private String buildPassList(ObjectNode passStopList) {
        ArrayNode stations = (ArrayNode) passStopList.path("stationList");
        List<String> points = new ArrayList<>();
        for (JsonNode s : stations) {
            points.add(s.path("lon").asText() + "," + s.path("lat").asText());
        }
        return String.join(";", points);
    }

    // ===== 기타 유틸리티 함수들 =====

    @Override
    public List<ConnectorWalkDetailDTO> computeWalkPath(String jsonData) throws Exception {
        JsonNode root = mapper.readTree(jsonData);
        ArrayNode itins = (ArrayNode) root.path("metaData").path("plan").path("itineraries");
        List<ConnectorWalkDetailDTO> details = new ArrayList<>();

        for (int itinIdx = 0; itinIdx < itins.size(); itinIdx++) {
            ArrayNode legs = (ArrayNode) itins.get(itinIdx).path("legs");

            // 1-1) 지하철 레그 인덱스 찾기
            int firstSub = -1, lastSub = -1;
            for (int i = 0; i < legs.size(); i++) {
                if ("SUBWAY".equalsIgnoreCase(legs.get(i).path("mode").asText())) {
                    if (firstSub < 0) firstSub = i;
                    lastSub = i;
                }
            }
            if (firstSub < 0) continue;

            // 1-2) 전·후 WALK 레그 인덱스 찾기
            int beforeWalk = -1, afterWalk = -1;
            for (int i = firstSub - 1; i >= 0; i--) {
                if ("WALK".equalsIgnoreCase(legs.get(i).path("mode").asText())) {
                    beforeWalk = i;
                    break;
                }
            }
            for (int i = lastSub + 1; i < legs.size(); i++) {
                if ("WALK".equalsIgnoreCase(legs.get(i).path("mode").asText())) {
                    afterWalk = i;
                    break;
                }
            }
            if (beforeWalk < 0 || afterWalk < 0) continue;

            // 1-2.5) 지하철↔지하철 스킵
            String prevTransit = legs.get(firstSub).path("mode").asText();
            String nextTransit = legs.get(lastSub).path("mode").asText();
            if ("SUBWAY".equalsIgnoreCase(prevTransit) && "SUBWAY".equalsIgnoreCase(nextTransit)) {
                continue;
            }

            // 1-3) connector 시작/끝 좌표
            ObjectNode bwLeg = (ObjectNode) legs.get(beforeWalk);
            double bwEndLon = bwLeg.path("end").path("lon").asDouble();
            double bwEndLat = bwLeg.path("end").path("lat").asDouble();

            ObjectNode awLeg = (ObjectNode) legs.get(afterWalk);
            double awStartLon = awLeg.path("start").path("lon").asDouble();
            double awStartLat = awLeg.path("start").path("lat").asDouble();

            // 1-4) 역 이름 추출
            String fsName = legs.get(firstSub)
                    .path("passStopList").path("stationList").get(0)
                    .path("stationName").asText();
            JsonNode lsList = legs.get(lastSub).path("passStopList").path("stationList");
            String lsName = lsList.get(lsList.size() - 1).path("stationName").asText();

            // 1-5) DB에서 엘리베이터 후보 조회 (comment에서 역명 키워드 검색)
            List<MarkerDTO> fsCands = markerMapper.selectByStationName(fsName);
            List<MarkerDTO> lsCands = markerMapper.selectByStationName(lsName);

            // 1-6) 최적 좌표 탐색 및 comment 파싱
            double bestBeforeX = 0, bestBeforeY = 0;
            int bestBeforeDist = Integer.MAX_VALUE;
            String beforeElevatorName = "";
            String beforeElevatorExit = "";
            for (MarkerDTO cand : fsCands) {
                Map<String,Object> p = new HashMap<>();
                p.put("startX", bwEndLon);
                p.put("startY", bwEndLat);
                p.put("endX",   cand.getLon());
                p.put("endY",   cand.getLat());
                p.put("startName", fsName + "역 앞");
                p.put("endName",   fsName + " 엘리베이터");
                p.put("searchOption", 30);
                String j = tmapService.getPedestrianRoute(p);
                int d = mapper.readTree(j)
                        .path("features").get(0)
                        .path("properties").path("totalDistance").asInt(Integer.MAX_VALUE);
                if (d < bestBeforeDist) {
                    bestBeforeDist = d;
                    bestBeforeX = cand.getLon();
                    bestBeforeY = cand.getLat();
                    // comment 파싱
                    String comment = cand.getComment() != null ? cand.getComment() : "";
                    int lastSpaceIndex = comment.lastIndexOf(" ");
                    if (lastSpaceIndex > 0) {
                        beforeElevatorName = comment.substring(0, lastSpaceIndex).replaceAll("\\(.*?\\)", "") + "역 엘리베이터";
                        beforeElevatorExit = comment.substring(lastSpaceIndex + 1);
                    } else {
                        beforeElevatorName = comment.replaceAll("\\(.*?\\)", "") + "역 엘리베이터";
                        beforeElevatorExit = "";
                    }
                }
            }
            double bestAfterX = 0, bestAfterY = 0;
            int bestAfterDist = Integer.MAX_VALUE;
            String afterElevatorName = "";
            String afterElevatorExit = "";
            for (MarkerDTO cand : lsCands) {
                Map<String,Object> p = new HashMap<>();
                p.put("startX", cand.getLon());
                p.put("startY", cand.getLat());
                p.put("endX",   awStartLon);
                p.put("endY",   awStartLat);
                p.put("startName", lsName + " 엘리베이터");
                p.put("endName",   lsName + "역 앞");
                p.put("searchOption", 30);
                String j = tmapService.getPedestrianRoute(p);
                int d = mapper.readTree(j)
                        .path("features").get(0)
                        .path("properties").path("totalDistance").asInt(Integer.MAX_VALUE);
                if (d < bestAfterDist) {
                    bestAfterDist = d;
                    bestAfterX = cand.getLon();
                    bestAfterY = cand.getLat();
                    // comment 파싱
                    String comment = cand.getComment() != null ? cand.getComment() : "";
                    int lastSpaceIndex = comment.lastIndexOf(" ");
                    if (lastSpaceIndex > 0) {
                        afterElevatorName = comment.substring(0, lastSpaceIndex).replaceAll("\\(.*?\\)", "") + "역 엘리베이터";
                        afterElevatorExit = comment.substring(lastSpaceIndex + 1);
                    } else {
                        afterElevatorName = comment.replaceAll("\\(.*?\\)", "") + "역 엘리베이터";
                        afterElevatorExit = "";
                    }
                }
            }

            // 1-7) ConnectorWalkDetailDTO 생성 및 추가
            if (bestBeforeDist < Integer.MAX_VALUE || bestAfterDist < Integer.MAX_VALUE) {
                ConnectorWalkDetailDTO detail = new ConnectorWalkDetailDTO();
                detail.setItineraryIndex(itinIdx);
                detail.setBeforeElevatorComment(beforeElevatorName);
                detail.setBeforeElevatorExit(beforeElevatorExit);
                detail.setAfterElevatorComment(afterElevatorName);
                detail.setAfterElevatorExit(afterElevatorExit);
                details.add(detail);
            }
        }
        return details;
    }
}