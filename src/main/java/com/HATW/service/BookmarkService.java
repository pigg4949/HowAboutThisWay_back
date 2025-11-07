// src/main/java/com/koreait/howaboutthisway/service/BookmarkService.java
package com.HATW.service;

import java.util.List;
import com.HATW.dto.BookmarkerDTO;

public interface BookmarkService {
    /**
     * 전체 북마크 조회
     * @return 북마크 리스트
     */
    List<BookmarkerDTO> getAllBookmarks();

    /**
     * 사용자 ID로 북마크 목록 조회
     * @param userId 사용자 ID
     * @return 북마크 리스트
     */
    List<BookmarkerDTO> getBookmarksByUserId(Long userId);

    /**
     * 새 북마크를 저장합니다.
     * @param bookmarkDTO userId, address, label 필드를 포함한 DTO
     */
    void createBookmark(BookmarkerDTO bookmarkDTO);

    /**
     * 북마크 정보 수정 (label이나 address 변경)
     * @param bookmarkId 북마크 ID
     * @param bookmarkDTO 변경된 address/label 포함
     */
    void updateBookmark(Long bookmarkId, BookmarkerDTO bookmarkDTO);

    /**
     * 북마크 삭제
     * @param bookmarkId 삭제할 북마크 ID
     */
    void deleteBookmark(Long bookmarkId);
}
