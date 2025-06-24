package com.HATW.service;

import com.HATW.dto.MarkerDTO;
import com.HATW.mapper.MarkerMapper;
import com.HATW.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarkerServiceImpl implements MarkerService {

    private final MarkerMapper markerMapper;
    private final ReportMapper reportMapper;

    @Override
    public List<MarkerDTO> getMarkersByTypes(List<Integer> types) {
        return markerMapper.findMarkersByTypes(types);
    }
    @Override
    public List<Map<String, Object>> getReportsByTypes(List<Integer> types) {
        return reportMapper.findByTypes(types);
    }
}
