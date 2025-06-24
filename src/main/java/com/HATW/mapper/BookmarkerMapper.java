// src/main/java/com/koreait/hatw_b/mapper/BookmarkerMapper.java
package com.HATW.mapper;

import com.HATW.dto.BookmarkerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BookmarkerMapper {
    List<BookmarkerDTO> findByUserId(@Param("userId") String userId);
    void insertBookmarker(BookmarkerDTO bookmarker);
    void deleteBookmarker(@Param("idx") int idx,
                          @Param("userId") String userId);
}
