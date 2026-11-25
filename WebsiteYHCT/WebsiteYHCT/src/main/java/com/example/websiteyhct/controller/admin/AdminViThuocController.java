package com.example.websiteyhct.controller.admin;

import com.example.websiteyhct.dto.ViThuocForm;
import com.example.websiteyhct.entity.ViThuoc;
import com.example.websiteyhct.repository.ThanhPhanBaiThuocRepository;
import com.example.websiteyhct.repository.ViThuocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminViThuocController {

    private final ViThuocRepository viThuocRepo;
    private final ThanhPhanBaiThuocRepository thanhPhanRepo;

    public AdminViThuocController(ViThuocRepository viThuocRepo, ThanhPhanBaiThuocRepository thanhPhanRepo) {
        this.viThuocRepo = viThuocRepo;
        this.thanhPhanRepo = thanhPhanRepo;
    }

    @GetMapping("/admin/vi-thuoc")
    public String danhSach(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "") String nguonGoc,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<ViThuoc> trang = viThuocRepo.timKiemAdmin(
                keyword.trim(), nguonGoc,
                PageRequest.of(Math.max(0, page), 10, Sort.by("id").descending())
        );

        model.addAttribute("page", trang);
        model.addAttribute("keyword", keyword);
        model.addAttribute("nguonGoc", nguonGoc);
        return "admin/vithuoc/list";
    }

    @GetMapping("/admin/vi-thuoc/new")
    public String taoMoi(Model model) {
        model.addAttribute("form", new ViThuocForm());
        return "admin/vithuoc/form";
    }

    @GetMapping("/admin/vi-thuoc/{id}/edit")
    public String chinhSua(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<ViThuoc> opt = viThuocRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("flashError", "Không tìm thấy vị thuốc ID: " + id);
            return "redirect:/admin/vi-thuoc";
        }
        ViThuoc v = opt.get();
        ViThuocForm form = new ViThuocForm(
                v.getId(),
                v.getTenViThuoc(),
                v.getMaDuocLieu(),
                v.getTenKhoaHocDuocLieu(),
                v.getTenKhoaHocNguon(),
                v.getNguonGocNam(),
                v.getNguonGocBac()
        );
        model.addAttribute("form", form);
        return "admin/vithuoc/form";
    }

    @PostMapping("/admin/vi-thuoc")
    public String luuViThuoc(@ModelAttribute ViThuocForm form, RedirectAttributes ra) {
        ViThuoc v;
        if (form.getMaDungChung() != null) {
            v = viThuocRepo.findById(form.getMaDungChung()).orElse(new ViThuoc());
        } else {
            v = new ViThuoc();
            long nextId = viThuocRepo.count() > 0 ? (viThuocRepo.findAll(Sort.by("id").descending()).get(0).getId() + 1) : 1L;
            v.setId(nextId);
        }

        v.setTenViThuoc(form.getTenViThuoc());
        v.setMaDuocLieu(form.getMaDuocLieu());
        v.setTenKhoaHocDuocLieu(form.getTenKhoaHocDuocLieu());
        v.setTenKhoaHocNguon(form.getTenKhoaHocNguon());
        v.setNguonGocNam(form.getNguonGocNam());
        v.setNguonGocBac(form.getNguonGocBac());

        viThuocRepo.save(v);
        ra.addFlashAttribute("flashSuccess", "Đã lưu vị thuốc: " + v.getTenViThuoc());
        return "redirect:/admin/vi-thuoc";
    }

    @PostMapping("/admin/vi-thuoc/{id}/delete")
    public String xoaViThuoc(@PathVariable Long id, RedirectAttributes ra) {
        var dsBaiThuoc = thanhPhanRepo.findBaiThuocByViThuocId(id);
        if (!dsBaiThuoc.isEmpty()) {
            ra.addFlashAttribute("flashError", "Không thể xóa vị thuốc đang được sử dụng trong " + dsBaiThuoc.size() + " bài thuốc");
            return "redirect:/admin/vi-thuoc";
        }
        viThuocRepo.deleteById(id);
        ra.addFlashAttribute("flashSuccess", "Đã xóa vị thuốc thành công");
        return "redirect:/admin/vi-thuoc";
    }
}
