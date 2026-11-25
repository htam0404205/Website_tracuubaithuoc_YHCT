package com.example.websiteyhct.service;

import com.example.websiteyhct.dto.*;
import com.example.websiteyhct.entity.*;
import com.example.websiteyhct.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TraCuuService {

    private static final int KICH_THUOC_TRANG = 20;

    private final BaiThuocRepository baiThuocRepo;
    private final BenhDanhRepository benhDanhRepo;
    private final TheLamSangRepository theLamSangRepo;
    private final ViThuocRepository viThuocRepo;
    private final ThanhPhanBaiThuocRepository thanhPhanRepo;

    public TraCuuService(BaiThuocRepository baiThuocRepo,
                         BenhDanhRepository benhDanhRepo,
                         TheLamSangRepository theLamSangRepo,
                         ViThuocRepository viThuocRepo, ThanhPhanBaiThuocRepository thanhPhanRepo) {
        this.baiThuocRepo = baiThuocRepo;
        this.benhDanhRepo = benhDanhRepo;
        this.theLamSangRepo = theLamSangRepo;
        this.viThuocRepo = viThuocRepo;
        this.thanhPhanRepo = thanhPhanRepo;
    }

    @Transactional(readOnly = true)
    public Page<BaiThuocDong> timBaiThuoc(String q, String maBenhDanh, String maTheLamSang, int trang) {
        String tuKhoa = (q == null) ? "" : q.trim();
        Pageable pg = PageRequest.of(Math.max(trang, 0), KICH_THUOC_TRANG, Sort.by("tenBaiThuoc"));
        return baiThuocRepo.timKiem(tuKhoa, maBenhDanh, maTheLamSang, pg)
                .map(b -> {
                    var tls = b.getTheLamSang();
                    String tenThe = (tls != null) ? tls.getTenTheLamSang() : "";
                    String tenBenh = (tls != null && tls.getBenhDanh() != null)
                            ? tls.getBenhDanh().getTenBenhDanh() : "";
                    return new BaiThuocDong(
                            b.getId(),
                            b.getMaBaiThuoc(),
                            b.getTenBaiThuoc(),
                            tenThe,
                            tenBenh
                    );
                });
    }

    @Transactional(readOnly = true)
    public Page<ViThuocDong> timViThuoc(String q, int trang) {
        String tuKhoa = (q == null) ? "" : q.trim();
        Pageable pg = PageRequest.of(Math.max(trang, 0), KICH_THUOC_TRANG, Sort.by("tenViThuoc"));
        return viThuocRepo.timTheoTen(tuKhoa, pg)
                .map(v -> new ViThuocDong(
                        v.getId(),
                        v.getTenViThuoc(),
                        v.getMaDuocLieu(),
                        nguonGoc(v),
                        v.getTenKhoaHocNguon()));
    }

    @Transactional(readOnly = true)
    public List<BenhDanh> danhSachBenhDanh() {
        return benhDanhRepo.findAll(Sort.by("tenBenhDanh"));
    }

    @Transactional(readOnly = true)
    public List<TheLamSang> danhSachTheLamSang() {
        return theLamSangRepo.findAllByOrderByTenTheLamSangAsc();
    }

    @Transactional(readOnly = true)
    public long demBaiThuoc() {
        return baiThuocRepo.count();
    }

    @Transactional(readOnly = true)
    public long demViThuoc() {
        return viThuocRepo.count();
    }

    @Transactional(readOnly = true)
    public long demBenhDanh() {
        return benhDanhRepo.count();
    }

    @Transactional(readOnly = true)
    public long demThanhPhan(){
        return thanhPhanRepo.count();
    }

    @Transactional(readOnly = true)
    public List<BenhDanhDong> benhDanhNoiBat(int soLuong) {
        return benhDanhRepo.noiBat(PageRequest.of(0, soLuong));
    }


    @Transactional(readOnly = true)
    public Page<BaiThuocThe> khamPha(String q, String benh, String the, String thanhPhan,
                                     String sort, int trang) {
        Pageable pg = PageRequest.of(Math.max(trang, 0), 12, sapXep(sort));
        Page<BaiThuoc> p = baiThuocRepo.khamPha(q.trim(), benh, the, thanhPhan.trim(), pg);
        return new PageImpl<>(chuyenThanhThe(p.getContent()), pg, p.getTotalElements());
    }

    private List<BaiThuocThe> chuyenThanhThe(List<BaiThuoc> ds) {
        List<Long> ids = ds.stream().map(b -> b.getId()).toList();
        Map<Long, List<String>> tp = new HashMap<>();
        if (!ids.isEmpty()) {
            for (ThanhPhanRut r : thanhPhanRepo.rutGon(ids)) {
                tp.computeIfAbsent(r.baiThuocId(), k -> new ArrayList<>()).add(r.tenGoc());
            }
        }
        return ds.stream().map(b -> {
            List<String> t = tp.getOrDefault(b.getId(), List.of());
            String xemTruoc = String.join(" · ", t.stream().limit(4).toList()) + (t.size() > 4 ? " …" : "");
            return new BaiThuocThe(b.getId(), b.getMaBaiThuoc(), b.getTenBaiThuoc(), loaiBai(b.getMaBaiThuoc()),
                    t.size(), xemTruoc, b.getTheLamSang().getTenTheLamSang(),
                    b.getTheLamSang().getBenhDanh().getTenBenhDanh());
        }).toList();
    }

    // Chỉ nhận các giá trị sắp xếp cho phép (tránh người dùng gõ tên cột bất kỳ vào URL).
// Thêm "id" cuối cùng vì tên bài thuốc có thể trùng nhau (nhiều bài cùng tên), nếu không thì phân trang bị lộn.
    private static Sort sapXep(String sort) {
        return switch (sort) {
            case "ten-giam" -> Sort.by("tenBaiThuoc").descending().and(Sort.by("id"));
            case "ma"       -> Sort.by("maBaiThuoc").and(Sort.by("id"));
            default         -> Sort.by("tenBaiThuoc").and(Sort.by("id"));
        };
    }

    // Suy loại bài từ đuôi mã (U62.392.5.01.CP001 -> CP)
    private static String loaiBai(String ma) {
        int i = ma.lastIndexOf('.');
        String duoi = (i >= 0 ? ma.substring(i + 1) : ma).toUpperCase().replace('Ð', 'Đ');
        if (duoi.startsWith("CP")) return "Bài thuốc cổ phương";
        if (duoi.startsWith("ĐP")) return "Đối pháp lập phương";
        if (duoi.startsWith("NP")) return "Nghiệm phương";   // cần đối chiếu với văn bản QĐ 3080 trước khi nộp
        return "Bài thuốc";
    }

    @Transactional(readOnly = true)
    public List<LuaChon> luaChonBenhDanh() {
        return benhDanhRepo.findAll(Sort.by("tenBenhDanh")).stream()
                .map(b -> new LuaChon(b.getMaBenhDanh(), b.getTenBenhDanh(), "")).toList();
    }

    @Transactional(readOnly = true)
    public List<LuaChon> luaChonTheLamSang() {
        return theLamSangRepo.findAllByOrderByTenTheLamSangAsc().stream()
                .map(t -> new LuaChon(t.getMaTheLamSang(), t.getTenTheLamSang(),
                        t.getBenhDanh().getMaBenhDanh())).toList();
    }

    private static final java.time.format.DateTimeFormatter NGAY =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public BaiThuocChiTiet chiTietBaiThuoc(Long id) {
        BaiThuoc b = baiThuocRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài thuốc"));
        TheLamSang t = b.getTheLamSang();
        BenhDanh bd = t.getBenhDanh();

        List<ThanhPhanDong> tp = thanhPhanRepo.findByBaiThuoc_IdOrderByViTri(id).stream()
                .map(x -> new ThanhPhanDong(x.getViTri(), x.getTenGoc(), x.getTrangThai(),
                        x.getViThuoc() == null ? null : x.getViThuoc().getId(), thongTinThanhPhan(x)))
                .toList();

        VanBanNguon vb = b.getVanBan();
        String trang = null;
        if (b.getTrangBatDau() != null) {
            boolean khac = b.getTrangKetThuc() != null && !b.getTrangKetThuc().equals(b.getTrangBatDau());
            trang = khac ? b.getTrangBatDau() + "–" + b.getTrangKetThuc() : String.valueOf(b.getTrangBatDau());
        }
        String ghiChu = (b.getNgayTao() == null ? "" : "Tạo ngày " + b.getNgayTao().format(NGAY))
                + (b.getNgayCapNhat() == null ? "" : " · Cập nhật lần cuối " + b.getNgayCapNhat().format(NGAY));

        List<BaiThuocThe> lienQuan = chuyenThanhThe(
                baiThuocRepo.findTop2ByTheLamSang_MaTheLamSangAndIdNotOrderByTenBaiThuoc(t.getMaTheLamSang(), id));

        return new BaiThuocChiTiet(b.getId(), b.getMaBaiThuoc(), b.getTenBaiThuoc(), loaiBai(b.getMaBaiThuoc()),
                bd.getMaBenhDanh(), bd.getTenBenhDanh(), t.getMaTheLamSang(), t.getTenTheLamSang(), tp,
                vb != null,
                vb == null ? null : vb.getSoHieu(),
                vb == null ? null : vb.getTenVanBan(),
                vb == null ? null : vb.getCoQuanBanHanh(),
                vb == null || vb.getNgayBanHanh() == null ? null : vb.getNgayBanHanh().format(NGAY),
                b.getNguonUrl(), trang, ghiChu, lienQuan);
    }

    private static String nguonGoc(ViThuoc v) {
        boolean nam = v.getNguonGocNam() != null && v.getNguonGocNam() == 'N';
        boolean bac = v.getNguonGocBac() != null && v.getNguonGocBac() == 'B';
        if (nam && bac) return "Nam, Bắc";
        if (nam) return "Nam";
        if (bac) return "Bắc";
        return "";
    }

    private static String thongTinThanhPhan(ThanhPhanBaiThuoc x) {
        ViThuoc v = x.getViThuoc();
        if (v == null) {
            return "REVIEW".equals(x.getTrangThai()) ? "Cần rà soát với danh mục vị thuốc"
                    : "Chưa khớp danh mục vị thuốc";
        }
        StringBuilder s = new StringBuilder();
        if (v.getTenKhoaHocNguon() != null && !v.getTenKhoaHocNguon().isBlank()) s.append(v.getTenKhoaHocNguon());
        String ng = nguonGoc(v);
        if (!ng.isEmpty()) s.append(s.length() > 0 ? " · " : "").append("Nguồn gốc ").append(ng);
        return s.length() > 0 ? s.toString() : "Đã khớp danh mục vị thuốc";
    }

    @Transactional(readOnly = true)
    public List<BaiThuocThe> timBaiThuocCoDungViThuoc(Long viThuocId) {
        return chuyenThanhThe(thanhPhanRepo.findBaiThuocByViThuocId(viThuocId));
    }

    @Transactional(readOnly = true)
    public List<BaiThuoc> danhSachGanDay() {
        return baiThuocRepo.findAll(PageRequest.of(0, 5, Sort.by("id").ascending())).getContent();
    }
}