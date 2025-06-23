// src/main/java/com/koreait/howaboutthisway/service/BookmarkService.java
package com.HATW.service;

import java.util.List;
import com.HATW.dto.BookmarkerDTO;

public interface BookmarkService {
    /**
     * 새 즐겨찾기를 저장합니다.
     * @param bookmarkDTO userId, address, label 필드를 포함한 DTO
     */
    void save(BookmarkerDTO bookmarkDTO);

    /**
     * 특정 즐겨찾이(idx) 조회
     * @param idx 즐겨찾이 고유번호
     * @return 해당 즐겨찾이 정보 (없으면 null)
     */
    BookmarkerDTO findByIdx(int idx);

    /**
     * 사용자(userId)별 즐겨찾이 목록 조회
     * @param userId 조회할 사용자 아이디
     * @return 즐겨찾이 리스트
     */
    List<BookmarkerDTO> findByUserId(String userId);

    /**
     * 즐겨찾이 정보 수정 (label이나 address 변경)
     * @param bookmarkDTO idx, 변경된 address/label 포함
     */
    void update(BookmarkerDTO bookmarkDTO);

    /**
     * 즐겨찾이 삭제
     * @param idx 삭제할 즐겨찾이 고유번호
     */
    void deleteByIdx(int idx);
}
