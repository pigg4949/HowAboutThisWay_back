package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
import com.HATW.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;

    @Override
    public void submitReport(ReportDTO reportDTO) {
        // 이미지 URL이 이미 설정되어 있다고 가정 (컨트롤러에서 처리)
        if (reportDTO.getStatus() == null) {
            reportDTO.setStatus("PENDING");
        }
        if (reportDTO.getCreatedAt() == null) {
            reportDTO.setCreatedAt(LocalDateTime.now());
        }
        if (reportDTO.getUpdatedAt() == null) {
            reportDTO.setUpdatedAt(LocalDateTime.now());
        }
        reportMapper.insertReport(reportDTO);
    }

    @Override
    public List<ReportDTO> getReportsByUser(Long userId) {
        // userId를 String으로 변환 (UserDTO에서 userId 가져오기)
        var user = userMapper.findByIdx(userId.intValue());
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return reportMapper.findByUserId(user.getUserId());
    }

    @Override
    public void deleteReport(Long reportId, Long userId) {
        // 사용자 확인
        var user = userMapper.findByIdx(userId.intValue());
        if (user == null) {
            throw new SecurityException("사용자를 찾을 수 없습니다.");
        }
        
        // 신고 확인 및 소유자 확인
        ReportDTO report = reportMapper.findByIdx(reportId.intValue());
        if (report == null) {
            throw new IllegalArgumentException("신고를 찾을 수 없습니다.");
        }
        if (!report.getUserId().equals(user.getUserId())) {
            throw new SecurityException("본인의 신고만 삭제할 수 있습니다.");
        }
        
        reportMapper.deleteReport(reportId.intValue(), user.getUserId());
    }
}
