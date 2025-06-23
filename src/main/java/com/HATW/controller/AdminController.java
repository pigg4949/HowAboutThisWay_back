package com.HATW.controller;

import com.HATW.dto.ReportDTO;
import com.HATW.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/reports")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        try {
            List<ReportDTO> reports = adminService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reports/{id}/approve")
    public ResponseEntity<Void> approveReport(@PathVariable Long id) {
        try {
            adminService.approveReport(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reports/{id}/reject")
    public ResponseEntity<Void> rejectReport(@PathVariable Long id) {
        try {
            adminService.rejectReport(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reports/{id}/reply")
    public ResponseEntity<Void> replyToReport(@PathVariable Long id, @RequestBody String reply) {
        try {
            if (reply == null || reply.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            adminService.replyToReport(id, reply);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}