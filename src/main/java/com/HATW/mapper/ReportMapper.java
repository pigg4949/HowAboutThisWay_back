package com.HATW.mapper;

import com.HATW.dto.ReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /**
     * 관리자 전체 조회
     * PENDING 상태 제보는 상단, 그 외는 하단으로 정렬 (각 그룹 내 오래된 순)
     */
    List<ReportDTO> findAllForAdmin();

    /**
     * 새 제보 등록
     * useGeneratedKeys=true 로 생성된 PK(idx)를 DTO.idx에 채워줌
     */
    void insertReport(ReportDTO report);


//     제보 처리 상태 업데이트 (관리자용)
    void updateStatus(@Param("idx") int idx,
                      @Param("status") String status,
                      @Param("updatedAt") LocalDateTime updatedAt);
    List<Map<String, Object>> findByTypes(@Param("types") List<Integer> types);

    // 로그인유저가 작성한 제보 가져오기
    List<ReportDTO> findByUserId(@Param("userId") String userId);


    // 신고된 위험 지점 전체 조회
    List<ReportDTO> findApprovedReports();

    //제보수정
    ReportDTO findById(@Param("reportId") Long reportId);
    void updateImageUrl(@Param("reportId") Long reportId, @Param("imageUrl") String imageUrl);
    void updateComment(@Param("reportId") Long reportId, @Param("comment") String comment);
}
