package com.HATW.util;

import com.HATW.dto.CoordinateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    /** 점-선분 최소 거리 계산 (도 단위) */
    private static double distancePointToSegment(CoordinateDTO p, CoordinateDTO v, CoordinateDTO w) {
        double l2 = distance(v, w); l2 = l2 * l2;
        if (l2 == 0) return distance(p, v);
        double t = ((p.getX() - v.getX()) * (w.getX() - v.getX())
                + (p.getY() - v.getY()) * (w.getY() - v.getY())) / l2;
        t = Math.max(0, Math.min(1, t));
        CoordinateDTO proj = new CoordinateDTO(
                v.getX() + t * (w.getX() - v.getX()),
                v.getY() + t * (w.getY() - v.getY())
        );
        return distance(p, proj);
    }

    /** 두 점 사이 거리(도 단위) 계산 */
    public static double distance(CoordinateDTO a, CoordinateDTO b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.hypot(dx, dy);
    }

    /**
     * GeoJSON FeatureCollection 형태의 경로 검사.
     * features 노드가 없으면 null 반환.
     */
    public static Boolean isPointOnGeoJsonLine(String lineJson, double lon, double lat) throws Exception {
        JsonNode root = mapper.readTree(lineJson);
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) return null;
        List<CoordinateDTO> coords = new ArrayList<>();
        for (JsonNode feature : (ArrayNode) features) {
            for (JsonNode pt : feature.path("geometry").path("coordinates")) {
                coords.add(new CoordinateDTO(pt.get(0).asDouble(), pt.get(1).asDouble()));
            }
        }
        CoordinateDTO target = new CoordinateDTO(lon, lat);
        for (int i = 0; i < coords.size() - 1; i++) {
            if (distancePointToSegment(target, coords.get(i), coords.get(i + 1)) < 0.0001) {
                return true;
            }
        }
        return false;
    }

    /**
     * "lon,lat lon,lat ..." 형태의 linestring 검사
     */
    public static boolean isPointOnLinestring(String linestring, double lon, double lat) {
        String[] tokens = linestring.trim().split("\\s+");
        List<CoordinateDTO> coords = new ArrayList<>();
        for (String token : tokens) {
            String[] xy = token.split(",");
            coords.add(new CoordinateDTO(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
        }
        CoordinateDTO target = new CoordinateDTO(lon, lat);
        for (int i = 0; i < coords.size() - 1; i++) {
            if (distancePointToSegment(target, coords.get(i), coords.get(i + 1)) < 0.0001) {
                return true;
            }
        }
        return false;
    }
}
