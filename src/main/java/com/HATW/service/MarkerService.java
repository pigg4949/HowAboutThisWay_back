package com.HATW.service;

import com.HATW.dto.MarkerDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarkerService {
    List<MarkerDTO> getMarkersByTypes(List<Integer> types);
    List<Map<String, Object>> getReportsByTypes(List<Integer> types);


}
