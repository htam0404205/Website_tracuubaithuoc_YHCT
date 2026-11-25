package com.example.websiteyhct.controller.admin;

import com.example.websiteyhct.dto.BaiThuocForm;
import com.example.websiteyhct.entity.BaiThuoc;
import com.example.websiteyhct.entity.ThanhPhanBaiThuoc;
import com.example.websiteyhct.entity.TheLamSang;
import com.example.websiteyhct.entity.VanBanNguon;
import com.example.websiteyhct.entity.ViThuoc;
import com.example.websiteyhct.repository.BaiThuocRepository;
import com.example.websiteyhct.repository.BenhDanhRepository;
import com.example.websiteyhct.repository.ThanhPhanBaiThuocRepository;
import com.example.websiteyhct.repository.TheLamSangRepository;
import com.example.websiteyhct.repository.VanBanNguonRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminBaiThuocController {

    private final BaiThuocRepository baiThuocRepo;
    private final TheLamSangRepository theLamSangRepo;
    private final BenhDanhRepository benhDanhRepo;
    private final VanBanNguonRepository vanBanRepo;
    private final ThanhPhanBaiThuocRepository thanhPhanRepo;
    private final ViThuocRepository viThuocRepo;

    public AdminBaiThuocController(BaiThuocRepository baiThuocRepo,
                                  TheLamSangRepository theLamSangRepo,
                                  BenhDanhRepository benhDanhRepo,
                                  VanBanNguonRepository vanBanRepo,
                                  ThanhPhanBaiThuocRepository thanhPhanRepo,
                                  ViThuocRepository viThuocRepo) {
        this.baiThuocRepo = baiThuocRepo;
        this.theLamSangRepo = theLamSangRepo;
        this.benhDanhRepo = benhDanhRepo;
        this.vanBanRepo = vanBanRepo;
        this.thanhPhanRepo = thanhPhanRepo;
        this.viThuocRepo = viThuocRepo;
    }

    @GetMapping("/admin/bai-thuoc")
    public String danhSach(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "") String maBenhDanh,
                           @RequestParam(defaultValue = "") String maTheLamSang,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<BaiThuoc> trang = baiThuocRepo.timKiemAdmin(
                keyword.trim(), maBenhDanh, maTheLamSang,
                PageRequest.of(Math.max(0, page), 10, Sort.by("id").descending())
        );

        model.addAttribute("page", trang);
        model.addAttribute("keyword", keyword);
        model.addAttribute("paramBenhDanh", maBenhDanh);
        model.addAttribute("paramTheLamSang", maTheLamSang);
        model.addAttribute("dsBenhDanh", benhDanhRepo.findAll(Sort.by("tenBenhDanh")));
        model.addAttribute("dsTheLamSang", theLamSangRepo.findAllByOrderByTenTheLamSangAsc());
        return "admin/baithuoc/list";
    }

    @GetMapping("/admin/bai-thuoc/new")
    public String taoMoi(Model model) {
        model.addAttribute("form", new BaiThuocForm());
        model.addAttribute("theLamSangList", theLamSangRepo.findAllByOrderByTenTheLamSangAsc());
        model.addAttribute("vanBanList", vanBanRepo.findAll());
        return "admin/baithuoc/form";
    }

    @GetMapping("/admin/bai-thuoc/{id}/edit")
    public String chinhSua(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<BaiThuoc> opt = baiThuocRepo.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("flashError", "Không tìm thấy bài thuốc ID: " + id);
            return "redirect:/admin/bai-thuoc";
        }

        BaiThuoc b = opt.get();
        BaiThuocForm form = new BaiThuocForm(
                b.getId(),
                b.getMaBaiThuoc(),
                b.getTenBaiThuoc(),
                b.getTheLamSang() != null ? b.getTheLamSang().getMaTheLamSang() : null,
                b.getVanBan() != null ? b.getVanBan().getId() : null,
                b.getTrangBatDau(),
                b.getTrangKetThuc()
        );

        model.addAttribute("form", form);
        model.addAttribute("theLamSangList", theLamSangRepo.findAllByOrderByTenTheLamSangAsc());
        model.addAttribute("vanBanList", vanBanRepo.findAll());
        model.addAttribute("thanhPhanList", thanhPhanRepo.findByBaiThuoc_IdOrderByViTri(id));
        model.addAttribute("viThuocList", viThuocRepo.findAll(Sort.by("tenViThuoc")));
        return "admin/baithuoc/form";
    }

    @PostMapping("/admin/bai-thuoc")
    public String luuBaiThuoc(@ModelAttribute BaiThuocForm form, RedirectAttributes ra) {
        BaiThuoc b;
        if (form.getMaBaiThuocDungChung() != null) {
            b = baiThuocRepo.findById(form.getMaBaiThuocDungChung()).orElse(new BaiThuoc());
            b.setNgayCapNhat(LocalDateTime.now());
        } else {
            b = new BaiThuoc();
            // Gán ID mới (theo id lớn nhất + 1)
            long nextId = baiThuocRepo.count() > 0 ? (baiThuocRepo.findAll(Sort.by("id").descending()).get(0).getId() + 1) : 1L;
            b.setId(nextId);
            b.setNgayTao(LocalDateTime.now());
            b.setNgayCapNhat(LocalDateTime.now());
        }

        b.setMaBaiThuoc(form.getMaBaiThuoc());
        b.setTenBaiThuoc(form.getTenBaiThuoc());

        if (form.getMaTheLamSang() != null && !form.getMaTheLamSang().isBlank()) {
            TheLamSang tls = theLamSangRepo.findById(form.getMaTheLamSang()).orElse(null);
            b.setTheLamSang(tls);
        }

        if (form.getMaVanBan() != null) {
            VanBanNguon vb = vanBanRepo.findById(form.getMaVanBan()).orElse(null);
            b.setVanBan(vb);
        } else {
            b.setVanBan(null);
        }

        b.setTrangBatDau(form.getTrangBatDau());
        b.setTrangKetThuc(form.getTrangKetThuc());

        baiThuocRepo.save(b);
        ra.addFlashAttribute("flashSuccess", "Đã lưu thành công bài thuốc " + b.getTenBaiThuoc());
        return "redirect:/admin/bai-thuoc";
    }

    @PostMapping("/admin/bai-thuoc/{id}/delete")
    public String xoaBaiThuoc(@PathVariable Long id, RedirectAttributes ra) {
        List<ThanhPhanBaiThuoc> tps = thanhPhanRepo.findByBaiThuoc_IdOrderByViTri(id);
        if (!tps.isEmpty()) {
            thanhPhanRepo.deleteAll(tps);
        }
        baiThuocRepo.deleteById(id);
        ra.addFlashAttribute("flashSuccess", "Đã xóa bài thuốc thành công");
        return "redirect:/admin/bai-thuoc";
    }

    @PostMapping("/admin/bai-thuoc/{id}/thanh-phan")
    public String themThanhPhan(@PathVariable Long id,
                                @RequestParam Integer viTri,
                                @RequestParam String tenGoc,
                                @RequestParam(required = false) String tenViThuoc,
                                RedirectAttributes ra) {
        BaiThuoc b = baiThuocRepo.findById(id).orElse(null);
        if (b == null) {
            ra.addFlashAttribute("flashError", "Không tìm thấy bài thuốc");
            return "redirect:/admin/bai-thuoc";
        }

        ThanhPhanBaiThuoc tp = new ThanhPhanBaiThuoc();
        tp.setBaiThuoc(b);
        tp.setViTri(viTri != null ? viTri : 1);
        tp.setTenGoc(tenGoc.trim());

        if (tenViThuoc != null && !tenViThuoc.isBlank()) {
            Optional<ViThuoc> vtOpt = viThuocRepo.findFirstByTenViThuoc(tenViThuoc.trim());
            if (vtOpt.isPresent()) {
                tp.setViThuoc(vtOpt.get());
                tp.setTrangThai("EXACT");
            } else {
                tp.setViThuoc(null);
                tp.setTrangThai("NOT_FOUND");
            }
        } else {
            tp.setViThuoc(null);
            tp.setTrangThai("NOT_FOUND");
        }

        thanhPhanRepo.save(tp);
        ra.addFlashAttribute("flashSuccess", "Đã thêm thành phần: " + tenGoc);
        return "redirect:/admin/bai-thuoc/" + id + "/edit";
    }

    @PostMapping("/admin/bai-thuoc/{id}/thanh-phan/{tpId}/delete")
    public String xoaThanhPhan(@PathVariable Long id, @PathVariable Long tpId, RedirectAttributes ra) {
        thanhPhanRepo.deleteById(tpId);
        ra.addFlashAttribute("flashSuccess", "Đã xóa thành phần bài thuốc");
        return "redirect:/admin/bai-thuoc/" + id + "/edit";
    }
}
