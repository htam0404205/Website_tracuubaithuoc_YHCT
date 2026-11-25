package com.example.websiteyhct.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {

    @GetMapping("/nguon-du-lieu")
    public String nguonDuLieu() {
        return "about";
    }

    @GetMapping("/ve-thu-vien")
    public String veThuVien() {
        return "ve-thu-vien";
    }
}
