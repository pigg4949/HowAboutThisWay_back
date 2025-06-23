package com.HATW.dao;

import com.HATW.dto.RouteRequestDTO;
import com.HATW.dto.SearchHistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MapDAO {

    private final JdbcTemplate jdbcTemplate;

    public void insertRouteHistory(RouteRequestDTO dto) {
        String sql = "INSERT INTO route_history (start_lat, start_lng, end_lat, end_lng, requested_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                dto.getStartLat(),
                dto.getStartLng(),
                dto.getEndLat(),
                dto.getEndLng(),
                dto.getTimestamp());
    }

    public void insertSearchHistory(SearchHistoryDTO dto) {
        String sql = "INSERT INTO search_history (keyword, search_type, searched_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,
                dto.getKeyword(),
                dto.getSearchType(),
                dto.getTimestamp());
    }
}
