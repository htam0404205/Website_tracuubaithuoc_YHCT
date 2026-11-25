package com.example.websiteyhct.dto;

public class ViThuocForm {
    private Long maDungChung;
    private String tenViThuoc;
    private String maDuocLieu;
    private String tenKhoaHocDuocLieu;
    private String tenKhoaHocNguon;
    private Character nguonGocNam;
    private Character nguonGocBac;

    public ViThuocForm() {
    }

    public ViThuocForm(Long maDungChung, String tenViThuoc, String maDuocLieu,
                       String tenKhoaHocDuocLieu, String tenKhoaHocNguon,
                       Character nguonGocNam, Character nguonGocBac) {
        this.maDungChung = maDungChung;
        this.tenViThuoc = tenViThuoc;
        this.maDuocLieu = maDuocLieu;
        this.tenKhoaHocDuocLieu = tenKhoaHocDuocLieu;
        this.tenKhoaHocNguon = tenKhoaHocNguon;
        this.nguonGocNam = nguonGocNam;
        this.nguonGocBac = nguonGocBac;
    }

    public Long getMaDungChung() {
        return maDungChung;
    }

    public void setMaDungChung(Long maDungChung) {
        this.maDungChung = maDungChung;
    }

    public String getTenViThuoc() {
        return tenViThuoc;
    }

    public void setTenViThuoc(String tenViThuoc) {
        this.tenViThuoc = tenViThuoc;
    }

    public String getMaDuocLieu() {
        return maDuocLieu;
    }

    public void setMaDuocLieu(String maDuocLieu) {
        this.maDuocLieu = maDuocLieu;
    }

    public String getTenKhoaHocDuocLieu() {
        return tenKhoaHocDuocLieu;
    }

    public void setTenKhoaHocDuocLieu(String tenKhoaHocDuocLieu) {
        this.tenKhoaHocDuocLieu = tenKhoaHocDuocLieu;
    }

    public String getTenKhoaHocNguon() {
        return tenKhoaHocNguon;
    }

    public void setTenKhoaHocNguon(String tenKhoaHocNguon) {
        this.tenKhoaHocNguon = tenKhoaHocNguon;
    }

    public Character getNguonGocNam() {
        return nguonGocNam;
    }

    public void setNguonGocNam(Character nguonGocNam) {
        this.nguonGocNam = nguonGocNam;
    }

    public Character getNguonGocBac() {
        return nguonGocBac;
    }

    public void setNguonGocBac(Character nguonGocBac) {
        this.nguonGocBac = nguonGocBac;
    }
}
