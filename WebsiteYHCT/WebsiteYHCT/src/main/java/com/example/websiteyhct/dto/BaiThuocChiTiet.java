package com.example.websiteyhct.dto;

import java.util.List;

public record BaiThuocChiTiet(
        Long id,
        String maBaiThuoc,
        String tenBaiThuoc,
        String loai,
        String maBenhDanh,
        String tenBenhDanh,
        String maTheLamSang,
        String tenTheLamSang,
        List<ThanhPhanDong> thanhPhan,
        boolean coNguon,
        String nguonSoHieu,
        String nguonTen,
        String nguonCoQuan,
        String nguonNgay,
        String nguonUrl,
        String nguonTrang,
        String ghiChuBanGhi,
        List<BaiThuocThe> lienQuan) {
}
