package com.HATW.dto;

import lombok.Data;
import java.util.List;

@Data
public class RouteResponseDTO {
    private List<RouteInfoDTO> routes;

    public RouteResponseDTO(List<RouteInfoDTO> routes) {
        this.routes = routes;
    }
}