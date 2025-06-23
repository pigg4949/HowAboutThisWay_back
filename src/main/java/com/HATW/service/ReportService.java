package com.HATW.service;

import com.HATW.dto.ReportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ReportService {
    void createReport(ReportDTO reportDTO, MultipartFile file);
    List<ReportDTO> getReportsByUserId(String userId);
    List<ReportDTO> getAllReportsForAdmin();
    List<Map<String, Object>> getReportsByTypes(List<Integer> types);
    void deleteReport(int idx, String userId);
    void updateReportStatus(int idx, String status);
}
