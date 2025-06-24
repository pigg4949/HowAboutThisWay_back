package com.HATW.controller;

import com.HATW.dto.ReportDTO;
import com.HATW.service.ReportService;
import com.HATW.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    /**
     * 사용자 제보 등록
     * 폼 데이터: type, lon, lat, comment, weight (선택)
     * 파일: image
     * 토큰에서 userId 추출
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createReport(
            @RequestHeader("Authorization") String authorization,
            @ModelAttribute ReportDTO reportDTO,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest request) {
        // 토큰에서 userId 꺼내기
        String token = authorization.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(token);
        reportDTO.setUserId(userId);

        // 파일 존재 여부 확인
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 제보 생성
        reportService.createReport(reportDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 관리자: 모든 제보 조회
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<ReportDTO> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reports);
    }

    /**
     * 관리자: 제보 승인
     */
    @PostMapping("/admin/{idx}/approve")
    public ResponseEntity<Void> approveReport(@PathVariable int idx) {
        reportService.updateReportStatus(idx, "APPROVED");
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자: 제보 거절
     */
    @PostMapping("/admin/{idx}/reject")
    public ResponseEntity<Void> rejectReport(@PathVariable int idx) {
        reportService.updateReportStatus(idx, "REJECTED");
        return ResponseEntity.ok().build();
    }
}
