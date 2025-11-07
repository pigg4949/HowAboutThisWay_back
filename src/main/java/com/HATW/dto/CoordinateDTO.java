package com.HATW.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * GeometryUtil에서 사용하는 좌표 DTO
 */
@Getter
@Setter
@AllArgsConstructor
public class CoordinateDTO {
    private double x;
    private double y;
}

