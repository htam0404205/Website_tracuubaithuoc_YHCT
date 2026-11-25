package com.example.websiteyhct.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "ThanhPhanBaiThuoc")
public class ThanhPhanBaiThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaBaiThuocDungChung")
    private BaiThuoc baiThuoc;

    @Column(name = "ViTri", nullable = false)
    private Integer viTri;

    @Nationalized
    @Column(name = "TenGoc", length = 300, nullable = false)
    private String tenGoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDungChungViThuoc")
    private ViThuoc viThuoc;

    @Column(name = "TrangThai", length = 20, nullable = false)
    private String trangThai;

    public ThanhPhanBaiThuoc() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BaiThuoc getBaiThuoc() {
        return baiThuoc;
    }

    public void setBaiThuoc(BaiThuoc baiThuoc) {
        this.baiThuoc = baiThuoc;
    }

    public Integer getViTri() {
        return viTri;
    }

    public void setViTri(Integer viTri) {
        this.viTri = viTri;
    }

    public String getTenGoc() {
        return tenGoc;
    }

    public void setTenGoc(String tenGoc) {
        this.tenGoc = tenGoc;
    }

    public ViThuoc getViThuoc() {
        return viThuoc;
    }

    public void setViThuoc(ViThuoc viThuoc) {
        this.viThuoc = viThuoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
