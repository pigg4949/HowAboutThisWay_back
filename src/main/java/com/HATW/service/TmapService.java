package com.HATW.service;

import java.io.IOException;
import java.util.Map;

public interface TmapService {
    String getPedestrianRoute(Map<String, Object> params)
            throws IOException, InterruptedException;
    String getTransitRoute(Map<String, Object> params) throws IOException, InterruptedException;
}
