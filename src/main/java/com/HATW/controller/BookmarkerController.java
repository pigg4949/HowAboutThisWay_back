package com.HATW.controller;

import com.HATW.dto.BookmarkerDTO;
import com.HATW.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkerController {

    @Autowired
    private BookmarkService bookmarkerService;

    @GetMapping
    public ResponseEntity<List<BookmarkerDTO>> getAllBookmarks() {
        try {
            List<BookmarkerDTO> bookmarks = bookmarkerService.getAllBookmarks();
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookmarkerDTO>> getBookmarksByUserId(@PathVariable Long userId) {
        try {
            List<BookmarkerDTO> bookmarks = bookmarkerService.getBookmarksByUserId(userId);
            if (bookmarks.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Void> createBookmark(@RequestBody BookmarkerDTO bookmarkDTO) {
        try {
            bookmarkerService.createBookmark(bookmarkDTO);
            return ResponseEntity.status(201).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{bookmarkId}")
    public ResponseEntity<Void> updateBookmark(
            @PathVariable Long bookmarkId,
            @RequestBody BookmarkerDTO bookmarkDTO) {
        try {
            bookmarkerService.updateBookmark(bookmarkId, bookmarkDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long bookmarkId) {
        try {
            bookmarkerService.deleteBookmark(bookmarkId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}