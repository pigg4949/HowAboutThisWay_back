package com.HATW.service;

import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.HATW.dto.BookmarkerDTO;
import com.HATW.mapper.BookmarkerMapper;

@Service
@RequiredArgsConstructor
public class BookmarkerServiceImpl implements BookmarkerService {
    private final BookmarkerMapper bookmarkerMapper;

    @Override
    public void insertBookmarker(BookmarkerDTO bookmarker) {
        bookmarkerMapper.insertBookmarker(bookmarker);
    }

    @Override
    public List<BookmarkerDTO> findByUserId(String userId) {
        return bookmarkerMapper.findByUserId(userId);
    }

    @Override
    public void deleteBookmarker(int idx, String userId) {
        bookmarkerMapper.deleteBookmarker(idx, userId);
    }
}
