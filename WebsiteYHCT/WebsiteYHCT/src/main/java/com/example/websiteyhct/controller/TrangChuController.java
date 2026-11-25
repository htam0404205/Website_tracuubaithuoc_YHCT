package com.example.websiteyhct.controller;

import com.example.websiteyhct.dto.BaiThuocThe;
import com.example.websiteyhct.service.TraCuuService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrangChuController {

    private final TraCuuService service;

    public TrangChuController(TraCuuService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String trangChu(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String benhDanh,
            @RequestParam(defaultValue = "") String theLamSang,
            @RequestParam(defaultValue = "") String thanhPhan,
            @RequestParam(defaultValue = "ten") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<BaiThuocThe> trang = service.khamPha(q, benhDanh, theLamSang, thanhPhan, sort, page);

        model.addAttribute("soBaiThuoc", service.demBaiThuoc());
        model.addAttribute("soViThuoc", service.demViThuoc());
        model.addAttribute("soThanhPhan", service.demThanhPhan());

        model.addAttribute("q", q);
        model.addAttribute("benhDanh", benhDanh);
        model.addAttribute("theLamSang", theLamSang);
        model.addAttribute("thanhPhan", thanhPhan);
        model.addAttribute("sort", sort);
        model.addAttribute("dsBenhDanh", service.luaChonBenhDanh());
        model.addAttribute("dsTheLamSang", service.luaChonTheLamSang());

        model.addAttribute("trang", trang);
        model.addAttribute("trangBd", Math.max(0, trang.getNumber() - 2)); // cửa sổ số trang: hiện 5 số
        model.addAttribute("trangKt", Math.max(0, Math.min(trang.getTotalPages() - 1, trang.getNumber() + 2)));
        return "index";
    }
}
