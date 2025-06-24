
package com.HATW.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookmarkerDTO {
    private Integer idx;           // PK
    private String userId;         // 사용자 아이디
    private String address;        // 즐겨찾기 주소
    private String label;          // 즐겨찾기 별명
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
