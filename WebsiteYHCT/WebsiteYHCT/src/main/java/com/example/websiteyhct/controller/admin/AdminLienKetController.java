package com.example.websiteyhct.controller.admin;

import com.example.websiteyhct.entity.ThanhPhanBaiThuoc;
import com.example.websiteyhct.entity.ViThuoc;
import com.example.websiteyhct.repository.ThanhPhanBaiThuocRepository;
import com.example.websiteyhct.repository.ViThuocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminLienKetController {

    private final ThanhPhanBaiThuocRepository thanhPhanRepo;
    private final ViThuocRepository viThuocRepo;

    public AdminLienKetController(ThanhPhanBaiThuocRepository thanhPhanRepo, ViThuocRepository viThuocRepo) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.viThuocRepo = viThuocRepo;
    }

    @GetMapping("/admin/lien-ket")
    public String review(@RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "all") String filter,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Page<ThanhPhanBaiThuoc> trang = thanhPhanRepo.timKiemReview(
                keyword.trim(), filter,
                PageRequest.of(Math.max(0, page), 10)
        );

        model.addAttribute("page", trang);
        model.addAttribute("keyword", keyword);
        model.addAttribute("filter", filter);
        model.addAttribute("viThuocList", viThuocRepo.findAll(Sort.by("tenViThuoc")));
        return "admin/lienket/review";
    }

    @PostMapping("/admin/lien-ket/{id}")
    public String capNhatLienKet(@PathVariable Long id,
                                 @RequestParam(required = false) String tenViThuoc,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "all") String filter,
                                 RedirectAttributes ra) {
        Optional<ThanhPhanBaiThuoc> opt = thanhPhanRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("flashError", "Không tìm thấy thành phần ID: " + id);
            return "redirect:/admin/lien-ket?page=" + page + "&filter=" + filter;
        }

        ThanhPhanBaiThuoc tp = opt.get();
        if (tenViThuoc != null && !tenViThuoc.isBlank()) {
            Optional<ViThuoc> vtOpt = viThuocRepo.findFirstByTenViThuoc(tenViThuoc.trim());
            if (vtOpt.isPresent()) {
                tp.setViThuoc(vtOpt.get());
                tp.setTrangThai("MANUAL");
                ra.addFlashAttribute("flashSuccess", "Đã khớp thành phần \"" + tp.getTenGoc() + "\" với vị thuốc \"" + vtOpt.get().getTenViThuoc() + "\"");
            } else {
                tp.setViThuoc(null);
                tp.setTrangThai("NOT_FOUND");
                ra.addFlashAttribute("flashError", "Không tìm thấy vị thuốc \"" + tenViThuoc + "\" trong danh mục");
            }
        } else {
            tp.setViThuoc(null);
            tp.setTrangThai("NOT_FOUND");
            ra.addFlashAttribute("flashSuccess", "Đã hủy liên kết thành phần \"" + tp.getTenGoc() + "\"");
        }

        thanhPhanRepo.save(tp);
        return "redirect:/admin/lien-ket?page=" + page + "&filter=" + filter;
    }
}
