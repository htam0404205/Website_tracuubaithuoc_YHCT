package com.example.websiteyhct.controller.admin;

import com.example.websiteyhct.service.TraCuuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final TraCuuService service;

    public AdminDashboardController(TraCuuService service) {
        this.service = service;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("soBaiThuoc", service.demBaiThuoc());
        model.addAttribute("soViThuoc", service.demViThuoc());
        model.addAttribute("soThanhPhan", service.demThanhPhan());
        model.addAttribute("danhSachGanDay", service.danhSachGanDay());
        return "admin/dashboard";
    }
}
