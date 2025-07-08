package com.HATW.service;

import com.HATW.dto.ReportDTO;
import com.HATW.dto.ReportUpdateRequest;
import com.HATW.mapper.ReportMapper;
import com.HATW.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
            // 1) 업로드 폴더 준비
            File imagesDir = new File(uploadDir, "reportImgs");
            if (!imagesDir.exists()) imagesDir.mkdirs();

            // 2) 새 파일명 생성
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf('.'))
                    : "";
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
            String fileName = reportDTO.getUserId() + "_" +
                    timestamp + "_" +
                    reportDTO.getType() + "_" +
                    UUID.randomUUID() + ext;

            // 3) 파일 복사
            Path target = Paths.get(uploadDir, "reportImgs", fileName);
            Files.copy(file.getInputStream(), target, REPLACE_EXISTING);

            // 4) DTO에 경로·기본값 설정
            reportDTO.setImageUrl("/reportImgs/" + fileName);
            reportDTO.setStatus("PENDING");
            reportDTO.setWeight(0);

            // 5) DB 저장
            reportMapper.insertReport(reportDTO);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
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

    @Override
    public List<ReportDTO> findByUserId(String userId) {
        return reportMapper.findByUserId(userId);
    }


    @Override
    public String updateReportImage(Long reportId, String userId, MultipartFile image) {
        ReportDTO report = reportMapper.findById(reportId);
        if (report == null) throw new RuntimeException("제보가 존재하지 않습니다.");
        if (!userId.equals(report.getUserId())) throw new RuntimeException("본인 소유가 아닙니다.");

        // 이전 파일 삭제
        String oldUrl = report.getImageUrl();
        if (StringUtils.hasText(oldUrl)) {
            String oldFile = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
            File toDelete = new File(uploadDir, "reportImgs/" + oldFile);
            if (toDelete.exists()) toDelete.delete();
        }

        // 새 파일 저장
        String ext = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String newFileName = UUID.randomUUID() + (ext != null ? "." + ext : "");
        Path path = Paths.get(uploadDir, "reportImgs", newFileName);
        try {
            Files.copy(image.getInputStream(), path, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
        String newUrl = "/reportImgs/" + newFileName;
        reportMapper.updateImageUrl(reportId, newUrl);
        return newUrl;
    }

    public void updateReport(Long reportId, ReportUpdateRequest request) {
        reportMapper.updateComment(reportId, request.getComment());
    }
}