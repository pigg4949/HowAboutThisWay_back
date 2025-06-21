package com.HowAboutThisWay.mapper;

import com.HowAboutThisWay.dto.InquiryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryMapper {

    /**
     * 내 문의사항 목록 조회
     * @param userId 조회할 사용자 아이디
     */
    List<InquiryDTO> findByUserId(@Param("userId") String userId);

    /**
     * 내 문의사항 단일 조회
     * @param idx    문의 PK
     * @param userId 요청한 사용자 아이디 (본인 확인)
     */
    InquiryDTO findByIdxAndUserId(@Param("idx") int idx,
                                  @Param("userId") String userId);

    /**
     * 관리자 전체 조회:
     * 답변 없는 문의를 먼저(createdAt 오래된 순), 답변된 문의는 뒤로
     */
    List<InquiryDTO> findAllForAdmin();

    /**
     * 새 문의사항 등록
     * useGeneratedKeys=true 로 DB 생성 PK(idx)를 DTO.idx에 자동 설정
     */
    void insertInquiry(InquiryDTO inquiry);

    /**
     * 문의사항 업데이트 (관리자 답변 처리)
     * @param inquiry idx, adminResponses, updatedAt 포함
     */
    void updateInquiry(InquiryDTO inquiry);

    /**
     * 내 문의사항 삭제 (혹시 몰라 넣음)
     * @param idx    삭제할 문의 PK
     * @param userId 요청한 사용자 아이디 (본인 확인)
     */
    void deleteInquiry(@Param("idx") int idx,
                       @Param("userId") String userId);
}
