package com.example.websiteyhct.controller;

import com.example.websiteyhct.service.TraCuuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BaiThuocController {

    private final TraCuuService service;

    public BaiThuocController(TraCuuService service) { this.service = service; }

    @GetMapping("/bai-thuoc/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        model.addAttribute("bt", service.chiTietBaiThuoc(id));
        return "bai-thuoc-chi-tiet";
    }
}