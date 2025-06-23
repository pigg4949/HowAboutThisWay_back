package com.HATW.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchHistoryDTO {
    private String keyword;
    private String searchType;
    private LocalDateTime timestamp = LocalDateTime.now(); // 기본값 설정
}
