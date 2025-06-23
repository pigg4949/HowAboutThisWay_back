// src/main/java/com/koreait/hatw_b/mapper/BookmarkerMapper.java
package com.HATW.mapper;

import com.HATW.dto.BookmarkerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BookmarkerMapper {

    /**
     * 내 즐겨찾기 목록 조회
     * @param userId 조회할 사용자 아이디
     */
    List<BookmarkerDTO> findByUserId(@Param("userId") String userId);

    /**
     * 내 즐겨찾기 등록
     * useGeneratedKeys=true 로 DB 생성 PK(idx)를 DTO.idx에 채워줌
     */
    void insertBookmarker(BookmarkerDTO bookmarker);

    /**
     * 내 즐겨찾기 수정
     * @param bookmarker idx, userId, address, label, updatedAt 포함
     */
    void updateBookmarker(BookmarkerDTO bookmarker);

    /**
     * 내 즐겨찾기 삭제
     * @param idx    삭제할 즐겨찾기 PK
     * @param userId 요청한 사용자 아이디 (본인 확인)
     */
    void deleteBookmarker(@Param("idx") int idx,
                          @Param("userId") String userId);
}
