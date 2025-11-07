// src/main/java/com/koreait/howaboutthisway/service/impl/BookmarkServiceImpl.java
package com.HATW.service;

import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.HATW.dto.BookmarkerDTO;
import com.HATW.mapper.BookmarkerMapper;
import com.HATW.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkerMapper bookmarkerMapper;
    private final UserMapper userMapper;

    @Override
    public List<BookmarkerDTO> getAllBookmarks() {
        return bookmarkerMapper.findAll();
    }

    @Override
    public List<BookmarkerDTO> getBookmarksByUserId(Long userId) {
        // userId를 String으로 변환 (UserDTO에서 userId 가져오기)
        var user = userMapper.findByIdx(userId.intValue());
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return bookmarkerMapper.findByUserId(user.getUserId());
    }

    @Override
    public void createBookmark(BookmarkerDTO bookmarkDTO) {
        if (bookmarkDTO.getUserId() == null || bookmarkDTO.getUserId().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        bookmarkerMapper.insertBookmarker(bookmarkDTO);
    }

    @Override
    public void updateBookmark(Long bookmarkId, BookmarkerDTO bookmarkDTO) {
        BookmarkerDTO existing = bookmarkerMapper.findByIdx(bookmarkId.intValue());
        if (existing == null) {
            throw new IllegalStateException("북마크를 찾을 수 없습니다.");
        }
        bookmarkDTO.setIdx(bookmarkId.intValue());
        bookmarkerMapper.updateBookmarker(bookmarkDTO);
    }

    @Override
    public void deleteBookmark(Long bookmarkId) {
        BookmarkerDTO existing = bookmarkerMapper.findByIdx(bookmarkId.intValue());
        if (existing == null) {
            throw new IllegalStateException("북마크를 찾을 수 없습니다.");
        }
        bookmarkerMapper.deleteBookmarker(bookmarkId.intValue(), existing.getUserId());
    }
}
