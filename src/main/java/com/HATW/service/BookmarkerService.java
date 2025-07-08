// src/main/java/com/koreait/howaboutthisway/service/BookmarkService.java
package com.HATW.service;

import java.util.List;
import com.HATW.dto.BookmarkerDTO;

public interface BookmarkerService {

    void insertBookmarker(BookmarkerDTO bookmarkDTO);

    List<BookmarkerDTO> findByUserId(String userId);

    void deleteBookmarker(int idx, String userId);

}
