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

    /**
     * 제보 처리 상태 업데이트 (관리자용)
     *
     * @param idx       제보 PK
     * @param status    처리 상태 (APPROVED or REJECTED)
     * @param updatedAt 처리 시각
     */
    void updateStatus(@Param("idx") int idx,
                      @Param("status") String status,
                      @Param("updatedAt") LocalDateTime updatedAt);
    List<Map<String, Object>> findByTypes(@Param("types") List<Integer> types);
}
