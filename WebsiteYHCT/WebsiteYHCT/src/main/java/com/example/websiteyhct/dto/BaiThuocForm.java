package com.example.websiteyhct.dto;

public class BaiThuocForm {
    private Long maBaiThuocDungChung;
    private String maBaiThuoc;
    private String tenBaiThuoc;
    private String maTheLamSang;
    private Integer maVanBan;
    private Integer trangBatDau;
    private Integer trangKetThuc;

    public BaiThuocForm() {
    }

    public BaiThuocForm(Long maBaiThuocDungChung, String maBaiThuoc, String tenBaiThuoc,
                        String maTheLamSang, Integer maVanBan, Integer trangBatDau, Integer trangKetThuc) {
        this.maBaiThuocDungChung = maBaiThuocDungChung;
        this.maBaiThuoc = maBaiThuoc;
        this.tenBaiThuoc = tenBaiThuoc;
        this.maTheLamSang = maTheLamSang;
        this.maVanBan = maVanBan;
        this.trangBatDau = trangBatDau;
        this.trangKetThuc = trangKetThuc;
    }

    public Long getMaBaiThuocDungChung() {
        return maBaiThuocDungChung;
    }

    public void setMaBaiThuocDungChung(Long maBaiThuocDungChung) {
        this.maBaiThuocDungChung = maBaiThuocDungChung;
    }

    public String getMaBaiThuoc() {
        return maBaiThuoc;
    }

    public void setMaBaiThuoc(String maBaiThuoc) {
        this.maBaiThuoc = maBaiThuoc;
    }

    public String getTenBaiThuoc() {
        return tenBaiThuoc;
    }

    public void setTenBaiThuoc(String tenBaiThuoc) {
        this.tenBaiThuoc = tenBaiThuoc;
    }

    public String getMaTheLamSang() {
        return maTheLamSang;
    }

    public void setMaTheLamSang(String maTheLamSang) {
        this.maTheLamSang = maTheLamSang;
    }

    public Integer getMaVanBan() {
        return maVanBan;
    }

    public void setMaVanBan(Integer maVanBan) {
        this.maVanBan = maVanBan;
    }

    public Integer getTrangBatDau() {
        return trangBatDau;
    }

    public void setTrangBatDau(Integer trangBatDau) {
        this.trangBatDau = trangBatDau;
    }

    public Integer getTrangKetThuc() {
        return trangKetThuc;
    }

    public void setTrangKetThuc(Integer trangKetThuc) {
        this.trangKetThuc = trangKetThuc;
    }
}
