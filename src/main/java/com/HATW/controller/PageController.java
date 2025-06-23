package com.HATW.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Value("${tmap.api.key}")
    private String appKey;

    @GetMapping("/")
    public String showMap(Model model) {
        model.addAttribute("appKey", appKey);
        return "map";  // → templates/map.html
    }
}