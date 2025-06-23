package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
import com.HATW.service.ReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public void createReport(ReportDTO reportDTO, MultipartFile file) {
        try {
            // 현재시간 포맷 정의 (연월일T시분초)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            String formattedDateTime = LocalDateTime.now().format(formatter);

            // 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 파일명 구성 (userId, 시간, 타입, UUID)
            String fileName = reportDTO.getUserId() + "_" +
                    formattedDateTime + "_" +
                    reportDTO.getType() + "_" +
                    UUID.randomUUID() + extension;

            // 저장 경로 설정
            Path filePath = Paths.get(uploadDir, fileName);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 저장된 파일의 URL 구성
            String fileUrl = "/reportImgs/" + fileName;


            reportDTO.setImageUrl(fileUrl);
            reportDTO.setStatus("PENDING");
            reportDTO.setCreatedAt(LocalDateTime.now());
            reportDTO.setUpdatedAt(LocalDateTime.now());

            // DB 저장
            reportMapper.insertReport(reportDTO);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }
    }

    @Override
    public List<ReportDTO> getReportsByUserId(String userId) {
        return reportMapper.findByUserId(userId);
    }

    @Override
    public List<ReportDTO> getAllReportsForAdmin() {
        return reportMapper.findAllForAdmin();
    }

    @Override
    public List<Map<String, Object>> getReportsByTypes(List<Integer> types) {
        return reportMapper.findByTypes(types);
    }

    @Override
    public void deleteReport(int idx, String userId) {
        reportMapper.deleteReport(idx, userId);
    }

    @Override
    public void updateReportStatus(int idx, String status) {
        reportMapper.updateStatus(idx, status, LocalDateTime.now());
    }
}
