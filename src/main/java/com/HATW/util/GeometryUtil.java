package com.HATW.util;

import com.HATW.dto.CoordinateDTO;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {
    // 두 좌표 간 거리(도 단위)
    public static double distance(CoordinateDTO a, CoordinateDTO b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.hypot(dx, dy);
    }

    /**
     * 경로에서 충돌지점(conflictIdx)에서 회피 반경(radius, 미터 단위)만큼 수직 방향으로 이동한 detour 좌표를 계산한다.
     * detour 계산은 1도 ≈ 111,000m를 이용해 미터를 도 단위로 변환하여 적용한다.
     */
    public static CoordinateDTO computeDetourPoint(
            List<CoordinateDTO> fullRoute, int conflictIdx, double radius) {
        CoordinateDTO conflict = fullRoute.get(conflictIdx);

        CoordinateDTO ref;
        if (conflictIdx < fullRoute.size() - 1) {
            ref = fullRoute.get(conflictIdx + 1);
        } else if (conflictIdx > 0) {
            ref = fullRoute.get(conflictIdx - 1);
        } else {
            ref = conflict;
        }

        double dx = ref.getX() - conflict.getX();
        double dy = ref.getY() - conflict.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 1;

        // 수직 벡터 (오른쪽)
        double perpX = -dy / len;
        double perpY = dx / len;

        // 오른쪽 detour
        double detourX1 = conflict.getX() + perpX * (radius / 111000.0);
        double detourY1 = conflict.getY() + perpY * (radius / 111000.0);

        // 왼쪽 detour
        double detourX2 = conflict.getX() - perpX * (radius / 111000.0);
        double detourY2 = conflict.getY() - perpY * (radius / 111000.0);

        // 두 detour 중, 실제로 사고지점 반경을 벗어나는 쪽을 선택(혹은 랜덤, 혹은 둘 다 시도)
        // 여기서는 오른쪽 우선, 필요시 왼쪽도 시도
        CoordinateDTO detour = new CoordinateDTO();
        detour.setX(detourX1);
        detour.setY(detourY1);

        System.out.println("detour X: " + detourX1 + ", Y: " + detourY1 + " (오른쪽)");
        System.out.println("detour X: " + detourX2 + ", Y: " + detourY2 + " (왼쪽)");

        return detour;
    }

    // 이하 기존 코드 동일 (경로 파싱, 병합 등)
    public static List<CoordinateDTO> parsePolyline(String json) {
        List<CoordinateDTO> result = new ArrayList<>();
        try {
            JsonArray features = JsonParser
                    .parseString(json)
                    .getAsJsonObject()
                    .getAsJsonArray("features");

            for (JsonElement el : features) {
                JsonObject feature = el.getAsJsonObject();
                String geomType = feature.getAsJsonObject("geometry").get("type").getAsString();
                if ("LineString".equals(geomType)) {
                    JsonArray coords = feature.getAsJsonObject("geometry").getAsJsonArray("coordinates");
                    for (JsonElement coordEl : coords) {
                        JsonArray xy = coordEl.getAsJsonArray();
                        CoordinateDTO pt = new CoordinateDTO();
                        pt.setX(xy.get(0).getAsDouble());
                        pt.setY(xy.get(1).getAsDouble());
                        result.add(pt);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            System.out.println("geometry parsePolyline ERROR: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public static String mergeRoutes(String part1, String part2) {
        // features 배열 합치고 totalTime,totalDistance 재계산
        // (간단 샘플: 두 JSON의 features만 합친 뒤, 나머지는 part1 것으로)
        JsonObject j1 = JsonParser.parseString(part1).getAsJsonObject();
        JsonObject j2 = JsonParser.parseString(part2).getAsJsonObject();
        JsonArray f1 = j1.getAsJsonArray("features");
        JsonArray f2 = j2.getAsJsonArray("features");

        JsonArray merged = new JsonArray();
        f1.forEach(merged::add);
        f2.forEach(merged::add);
        j1.add("features", merged);
        System.out.println("geometry mergeRoutes in or start");
        return j1.toString();
    }
}