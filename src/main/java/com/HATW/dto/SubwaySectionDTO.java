package com.HATW.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SubwaySectionDTO {
    private String before_station_name;
    private MarkerDTO before_point;
    private String before_walk_route_json;
    private String after_station_name;
    private MarkerDTO after_point;
    private String after_walk_route_json;
}
