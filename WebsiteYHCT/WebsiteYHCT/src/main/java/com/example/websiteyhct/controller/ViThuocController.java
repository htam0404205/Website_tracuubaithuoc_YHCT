package com.example.websiteyhct.controller;

import com.example.websiteyhct.dto.BaiThuocThe;
import com.example.websiteyhct.dto.ViThuocDong;
import com.example.websiteyhct.entity.ViThuoc;
import com.example.websiteyhct.repository.ViThuocRepository;
import com.example.websiteyhct.service.TraCuuService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class ViThuocController {

    private final TraCuuService service;
    private final ViThuocRepository viThuocRepo;

    public ViThuocController(TraCuuService service, ViThuocRepository viThuocRepo) {
        this.service = service;
        this.viThuocRepo = viThuocRepo;
    }

    /** Danh sách vị thuốc (tìm kiếm) */
    @GetMapping("/vi-thuoc")
    public String danhSach(@RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<ViThuocDong> trang = service.timViThuoc(q, page);
        model.addAttribute("q", q);
        model.addAttribute("page", trang);
        model.addAttribute("trangBd", Math.max(0, trang.getNumber() - 2));
        model.addAttribute("trangKt", Math.max(0, Math.min(trang.getTotalPages() - 1, trang.getNumber() + 2)));
        return "vithuoc/list";
    }

    /** Chi tiết vị thuốc */
    @GetMapping("/vi-thuoc/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        ViThuoc v = viThuocRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vị thuốc"));
        List<BaiThuocThe> baiThuocList = service.timBaiThuocCoDungViThuoc(id);
        model.addAttribute("viThuoc", v);
        model.addAttribute("baiThuocList", baiThuocList);
        return "vithuoc/detail";
    }
}
