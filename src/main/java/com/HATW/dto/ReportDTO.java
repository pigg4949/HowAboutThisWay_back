package com.HATW.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportDTO {
    private int idx;
    private String userId;
    private int type;
    private double lon;     // 경도(X)
    private double lat;     // 위도(Y)
    private String comment;
    private String imageUrl;
    private String status;  // APPROVED 상태만 사용할 예정
    private int weight;     // 우회 우선순위 등에 활용 가능
}
