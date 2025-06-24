package com.HATW.service;

import com.HATW.dto.CoordinateDTO;
import com.HATW.dto.CustomRequestDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MapService {
    String searchLocation(String keyword) throws IOException, InterruptedException;
    String getPedestrianRoute(Map<String, Object> params, List<CoordinateDTO> detourList) throws IOException, InterruptedException;
    String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException;
    String getCustomPedestrianRoute(CustomRequestDTO req) throws IOException, InterruptedException;
}
