package com.HATW.service;

import com.HATW.dto.ReportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReportService {
    void createReport(ReportDTO reportDTO, MultipartFile file);
    List<ReportDTO> getAllReports();
    void updateReportStatus(int idx, String status);
    // 이하는 전부 front단엔 없는 기능
    //List<Map<String, Object>> getReportsByTypes(List<Integer> types);
    // 이용자가 등록된 본인 제보에 관여할 여지가 없으므로, 삭제 예정
    //List<ReportDTO> getReportsByUserId(String userId);
    //void deleteReport(int idx, String userId);

}
