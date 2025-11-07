package com.HATW.service;

import com.HATW.dto.ReportDTO;
import java.util.List;

public interface AdminService {
    /**
     * 전체 신고 조회
     * @return 신고 리스트
     */
    List<ReportDTO> getAllReports();

    /**
     * 신고 승인
     * @param id 신고 ID
     */
    void approveReport(Long id);

    /**
     * 신고 거부
     * @param id 신고 ID
     */
    void rejectReport(Long id);

    /**
     * 신고에 답변
     * @param id 신고 ID
     * @param reply 답변 내용
     */
    void replyToReport(Long id, String reply);
}
