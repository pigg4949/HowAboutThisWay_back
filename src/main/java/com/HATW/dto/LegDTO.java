package com.HATW.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class LegDTO {
    private String mode;
    private int sectionTime;
    private int distance;
    private Map<String, Object> start;
    private Map<String, Object> end;
    private List<Map<String, Object>> segments;
    private Map<String, Object> extra;
}
