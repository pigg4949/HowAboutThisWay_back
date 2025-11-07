package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ReportMapper reportMapper;

    @Override
    public List<ReportDTO> getAllReports() {
        return reportMapper.findAllForAdmin();
    }

    @Override
    public void approveReport(Long id) {
        ReportDTO report = reportMapper.findByIdx(id.intValue());
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다.");
        }
        if ("APPROVED".equals(report.getStatus())) {
            throw new IllegalStateException("이미 승인된 신고입니다.");
        }
        reportMapper.updateStatus(id.intValue(), "APPROVED", java.time.LocalDateTime.now());
    }

    @Override
    public void rejectReport(Long id) {
        ReportDTO report = reportMapper.findByIdx(id.intValue());
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다.");
        }
        if ("REJECTED".equals(report.getStatus())) {
            throw new IllegalStateException("이미 거부된 신고입니다.");
        }
        reportMapper.updateStatus(id.intValue(), "REJECTED", java.time.LocalDateTime.now());
    }

    @Override
    public void replyToReport(Long id, String reply) {
        ReportDTO report = reportMapper.findByIdx(id.intValue());
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다.");
        }
        // 답변 저장 로직 (ReportMapper에 reply 필드가 있다고 가정)
        // 실제 구현은 ReportMapper에 reply 필드 업데이트 메서드가 필요합니다
        reportMapper.updateStatus(id.intValue(), report.getStatus(), java.time.LocalDateTime.now());
    }
}
