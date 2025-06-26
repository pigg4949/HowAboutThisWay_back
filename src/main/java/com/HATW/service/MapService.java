package com.HATW.service;

import com.HATW.dto.RouteRequestDTO;
import com.HATW.dto.RouteResponseDTO;

import java.io.IOException;
import java.util.Map;

public interface MapService {
    RouteResponseDTO processRoute(RouteRequestDTO request) throws IOException, InterruptedException;
    String searchLocation(String keyword) throws IOException, InterruptedException;
    String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException;


}
