package com.example.websiteyhct.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "BaiThuoc")
public class BaiThuoc {

    @Id
    @Column(name = "MaBaiThuocDungChung")
    private Long id;

    @Nationalized
    @Column(name = "MaBaiThuoc", length = 40, nullable = false)
    private String maBaiThuoc;

    @Nationalized
    @Column(name = "TenBaiThuoc", length = 300, nullable = false)
    private String tenBaiThuoc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaTheLamSang")
    private TheLamSang theLamSang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaVanBan")
    private VanBanNguon vanBan;

    @Nationalized
    @Column(name = "NguonUrl", length = 500)
    private String nguonUrl;

    @Column(name = "TrangBatDau")
    private Integer trangBatDau;

    @Column(name = "TrangKetThuc")
    private Integer trangKetThuc;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    public BaiThuoc() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public Long getMaBaiThuocDungChung() {
        return id;
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

    public TheLamSang getTheLamSang() {
        return theLamSang;
    }

    public void setTheLamSang(TheLamSang theLamSang) {
        this.theLamSang = theLamSang;
    }

    public VanBanNguon getVanBan() {
        return vanBan;
    }

    public void setVanBan(VanBanNguon vanBan) {
        this.vanBan = vanBan;
    }

    public String getNguonUrl() {
        return nguonUrl;
    }

    public void setNguonUrl(String nguonUrl) {
        this.nguonUrl = nguonUrl;
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

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
