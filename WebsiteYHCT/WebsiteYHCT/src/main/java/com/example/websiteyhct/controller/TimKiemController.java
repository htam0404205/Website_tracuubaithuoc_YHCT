package com.example.websiteyhct.controller;

import com.example.websiteyhct.service.TraCuuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TimKiemController {

    private final TraCuuService service;

    public TimKiemController(TraCuuService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "baithuoc") String type,
                         @RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "") String benhDanh,
                         @RequestParam(defaultValue = "") String theLamSang,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        boolean laViThuoc = "vithuoc".equals(type);
        model.addAttribute("type", laViThuoc ? "vithuoc" : "baithuoc");
        model.addAttribute("q", q);
        model.addAttribute("benhDanh", benhDanh);
        model.addAttribute("theLamSang", theLamSang);

        if (laViThuoc) {
            model.addAttribute("page", service.timViThuoc(q, page));
        } else {
            model.addAttribute("page", service.timBaiThuoc(q, benhDanh, theLamSang, page));
        }
        return "search";
    }
}
