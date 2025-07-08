package com.HATW.service;

import java.io.IOException;
import java.util.Map;

public interface MapService {
    String searchLocation(String keyword) throws IOException, InterruptedException;
    String processRouteInstructions(Map<String, Object> request) throws IOException;
}
