package com.HATW.service;

import com.HATW.dto.ReportDTO;

import java.util.List;

public interface ReportService {
    /**
     * 신고 제출
     * @param reportDTO 신고 정보
     */
    void submitReport(ReportDTO reportDTO);

    /**
     * 사용자별 신고 조회
     * @param userId 사용자 ID
     * @return 신고 리스트
     */
    List<ReportDTO> getReportsByUser(Long userId);

    /**
     * 신고 삭제
     * @param reportId 신고 ID
     * @param userId 사용자 ID
     */
    void deleteReport(Long reportId, Long userId);
}
