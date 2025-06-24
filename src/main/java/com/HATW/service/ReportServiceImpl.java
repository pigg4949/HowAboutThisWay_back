package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.mapper.ReportMapper;
import com.HATW.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final String uploadDir;

    public ReportServiceImpl(ReportMapper reportMapper, JwtUtil jwtUtil,
                             @Value("${file.upload-dir}") String uploadDir) {
        this.reportMapper = reportMapper;
        this.uploadDir = uploadDir;
    }

    @Override
    public void createReport(ReportDTO reportDTO, MultipartFile file) {
        try {
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs(); // 경로가 없으면 자동 생성
            }

            // 파일 확장자 안전하게 추출
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 파일명 구성 (userId_시간_타입_UUID.확장자)
            String formattedDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
            String fileName = reportDTO.getUserId() + "_" +
                    formattedDateTime + "_" +
                    reportDTO.getType() + "_" +
                    UUID.randomUUID() + extension;

            // 파일 저장 경로
            Path filePath = Paths.get(uploadDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 접근 가능한 URL 설정
            String fileUrl = "/reportImgs/" + fileName;

            // DTO 세팅 후 저장
            reportDTO.setImageUrl(fileUrl);
            reportDTO.setStatus("PENDING");

            reportMapper.insertReport(reportDTO);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
        }
    }

    @Override
    public void updateReportStatus(int idx, String status) {
        reportMapper.updateStatus(idx, status, LocalDateTime.now());
    }

    @Override
    public List<ReportDTO> getAllReports() {
        return reportMapper.findAllForAdmin();
    }

}