
package com.HATW.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookmarkerDTO {
    private Integer idx;           // PK
    private String userId;         // 사용자 아이디
    private String address;        // 즐겨찾기 주소
    private String label;          // 즐겨찾기 별명
    private LocalDateTime createdAt; // 등록일
    private LocalDateTime updatedAt; // 수정일
}
