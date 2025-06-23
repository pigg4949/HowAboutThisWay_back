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
     * 내 제보 목록 조회
     * @param userId 조회할 사용자 아이디
     */
    List<ReportDTO> findByUserId(@Param("userId") String userId);

    /**
     * 내 제보 단일 조회 (본인 확인)
     * @param idx    제보 PK
     * @param userId 사용자 아이디
     */
    ReportDTO findByIdxAndUserId(@Param("idx") int idx,
                                 @Param("userId") String userId);

    /**
     * 관리자 전체 조회
     * PENDING 상태 제보는 상단, 그 외는 하단으로 정렬 (각 그룹 내 오래된 순)
     */
    List<ReportDTO> findAllForAdmin();

    /**
     * 타입별 제보 요약 조회
     * 좌표(lon, lat), 이미지 URL, 코멘트만 반환
     * @param types 제보 타입 (휠체어용, 유모차용 등)
     */
    List<Map<String, Object>> findByTypes(@Param("types") List<Integer> types);


    /**
     * 새 제보 등록
     * useGeneratedKeys=true 로 생성된 PK(idx)를 DTO.idx에 채워줌
     */
    void insertReport(ReportDTO report);

    /**
     * 제보 처리 상태 업데이트 (관리자용)
     * @param idx       제보 PK
     * @param status    처리 상태 (APPROVED or REJECTED)
     * @param updatedAt 처리 시각
     */
    void updateStatus(@Param("idx") int idx,
                      @Param("status") String status,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 제보 삭제 (본인 또는 관리자)
     * @param idx    삭제할 제보 PK
     * @param userId 요청한 사용자 아이디 (관리자는 null을 넘겨도 삭제 가능)
     */
    void deleteReport(@Param("idx") int idx,
                      @Param("userId") String userId);
}
