package com.HATW.service;

import java.io.IOException;
import java.util.Map;

public interface MapService {
    String searchLocation(String keyword) throws IOException, InterruptedException;
    void logRoute(Map<String, Object> params);
    String getPedestrianRoute(Map<String, Object> params) throws IOException, InterruptedException;
    String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException;
}
