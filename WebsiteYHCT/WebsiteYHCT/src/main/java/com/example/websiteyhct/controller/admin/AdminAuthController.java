package com.example.websiteyhct.controller.admin;

import com.example.websiteyhct.service.TraCuuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuthController {

    private final TraCuuService service;

    public AdminAuthController(TraCuuService service) {
        this.service = service;
    }

    @GetMapping("/admin/login")
    public String login(Model model) {
        model.addAttribute("soBaiThuoc", service.demBaiThuoc());
        model.addAttribute("soViThuoc", service.demViThuoc());
        model.addAttribute("soThanhPhan", service.demThanhPhan());
        return "admin/login";
    }
}
