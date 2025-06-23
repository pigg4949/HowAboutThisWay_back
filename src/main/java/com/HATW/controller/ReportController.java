package com.HATW.controller;

import com.HATW.dto.ReportDTO;
import com.HATW.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> submitReport(@ModelAttribute ReportDTO reportDTO) {
        try {
            reportService.submitReport(reportDTO);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build(); // 400 or 500 handled internally
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReportDTO>> getMyReports(@SessionAttribute(name = "loginUserId", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        try {
            List<ReportDTO> reports = reportService.getReportsByUser(userId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long reportId,
            @SessionAttribute(name = "loginUserId", required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            reportService.deleteReport(reportId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}