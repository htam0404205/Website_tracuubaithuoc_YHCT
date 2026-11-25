package com.example.websiteyhct.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;

@Entity
@Table(name = "VanBanNguon")
public class VanBanNguon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaVanBan")
    private Integer id;

    @Column(name = "LoaiNguon", length = 20, nullable = false)
    private String loaiNguon;

    @Nationalized @Column(name = "SoHieu", length = 80)
    private String soHieu;

    @Nationalized @Column(name = "TenVanBan", length = 500, nullable = false)
    private String tenVanBan;

    @Nationalized @Column(name = "CoQuanBanHanh", length = 200)
    private String coQuanBanHanh;

    @Column(name = "NgayBanHanh")
    private LocalDate ngayBanHanh;

    @Nationalized @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    public VanBanNguon() { }

    public Integer getId() { return id; }
    public Integer getMaVanBan() { return id; }
    public String getLoaiNguon() { return loaiNguon; }
    public String getSoHieu() { return soHieu; }
    public String getTenVanBan() { return tenVanBan; }
    public String getCoQuanBanHanh() { return coQuanBanHanh; }
    public LocalDate getNgayBanHanh() { return ngayBanHanh; }
    public String getGhiChu() { return ghiChu; }
}