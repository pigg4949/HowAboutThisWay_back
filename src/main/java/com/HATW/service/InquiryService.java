package com.HATW.service;

import com.HATW.dto.InquiryDTO;
import java.util.List;

public interface InquiryService {
    /**
     * 관리자용 모든 문의 조회
     */
    List<InquiryDTO> findAllForAdmin();

    /**
     * 사용자 문의 등록
     */
    void insertInquiry(InquiryDTO inquiry);

    /**
     * 문의 수정 (답변 포함)
     */
    void updateInquiry(InquiryDTO inquiry);

    // 로그인 유저가 쓴 문의 가져오기
    List<InquiryDTO> findByUserId(String userId);
}
