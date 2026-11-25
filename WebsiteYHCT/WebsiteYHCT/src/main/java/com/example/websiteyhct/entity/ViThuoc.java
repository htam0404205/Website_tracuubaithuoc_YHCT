package com.example.websiteyhct.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "ViThuoc")
public class ViThuoc {

    @Id
    @Column(name = "MaDungChung")
    private Long id;

    @Nationalized
    @Column(name = "TenViThuoc", length = 200, nullable = false)
    private String tenViThuoc;

    @Column(name = "MaDuocLieu", length = 30)
    private String maDuocLieu;

    @Column(name = "NguonGocNam")
    private Character nguonGocNam;

    @Column(name = "NguonGocBac")
    private Character nguonGocBac;

    @Nationalized
    @Column(name = "TenKhoaHocDuocLieu", length = 300)
    private String tenKhoaHocDuocLieu;

    @Nationalized
    @Column(name = "TenKhoaHocNguon", length = 500)
    private String tenKhoaHocNguon;


    public ViThuoc() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @jakarta.persistence.Transient
    public Long getMaDungChung() {
        return id;
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
}
