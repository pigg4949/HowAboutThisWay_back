package com.HATW.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Config {
    private final String TMAP_URL = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json";
    private final String TMAP_TRANSIT_URL = "https://apis.openapi.sk.com/transit/routes";
    private final String AppKey ="cVOg48Li4s4IgLLcfUSzy8NwU6UNL97g22EZ8OID";


}
