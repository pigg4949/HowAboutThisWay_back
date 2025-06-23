// src/main/java/com/koreait/howaboutthisway/service/impl/BookmarkServiceImpl.java
package com.HATW.service;

import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.HATW.dto.BookmarkerDTO;
import com.HATW.mapper.BookmarkerMapper;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkerMapper bookmarkerMapper;

    @Override
    public void save(BookmarkerDTO bookmarkDTO) {
        bookmarkerMapper.insert(bookmarkerDTO);
    }

    @Override
    public BookmarkerDTO findByIdx(int idx) {
        return bookmarkerMapper.findByIdx(idx);
    }

    @Override
    public List<BookmarkerDTO> findByUserId(String userId) {
        return bookmarkerMapper.findByUserId(userId);
    }

    @Override
    public void update(BookmarkerDTO bookmarkDTO) {
        bookmarkerMapper.update(bookmarkDTO);
    }

    @Override
    public void deleteByIdx(int idx) {
        bookmarkerMapper.deleteByIdx(idx);
    }
}
