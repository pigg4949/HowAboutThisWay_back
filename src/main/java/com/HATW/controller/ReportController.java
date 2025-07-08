package com.HATW.controller;

import com.HATW.dto.ReportDTO;
import com.HATW.dto.ReportUpdateRequest;
import com.HATW.dto.UserDTO;
import com.HATW.service.ReportService;
import com.HATW.service.ReportServiceImpl;
import com.HATW.service.UserService;
import com.HATW.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

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
    @GetMapping("/admin/pending")
    public ResponseEntity<List<ReportDTO>> getAllReports(@RequestHeader("Authorization") String token) {
        List<ReportDTO> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            System.out.println("ReportController: No reports found.");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reports);
    }

    /**
     * 관리자: 제보 상태 변경 (승인 or 거절)
     */
    @PutMapping("/admin/{idx}/status")
    public ResponseEntity<Void> changeReportStatus(@PathVariable int idx,
                                                   @RequestBody Map<String, String> request) {
        String status = request.get("status"); // status는 "APPROVED" 또는 "REJECTED"
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest().build(); // 잘못된 값 처리
        }

        reportService.updateReportStatus(idx, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 내 제보 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReportDTO>> getMyReports(
            @RequestHeader("Authorization") String token) {

        UserDTO user = userService.getUserInfoFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ReportDTO> reports = reportService.findByUserId(user.getUserId());
        if (reports.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reports);
    }

    @PutMapping(value = "/{reportId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateReportImage(
            @PathVariable Long reportId,
            @RequestHeader("Authorization") String authorization,
            @RequestParam("image") MultipartFile image) {
        // 토큰에서 userId 추출
        String token = authorization.replace("Bearer ", "");
        String userId = jwtUtil.getUserIdFromToken(token);

        // 서비스에 위임 (본인 소유 확인, 이미지 교체, DB 업데이트)
        String newImageUrl = reportService.updateReportImage(reportId, userId, image);

        return ResponseEntity.ok(newImageUrl); // 새 이미지 URL 반환
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId, @RequestBody ReportUpdateRequest request) {
        reportService.updateReport(reportId, request);
        return ResponseEntity.ok().build();
    }
}
