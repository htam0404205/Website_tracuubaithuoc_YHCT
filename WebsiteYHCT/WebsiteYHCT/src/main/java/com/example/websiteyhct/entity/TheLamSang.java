package com.example.websiteyhct.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "TheLamSang")
public class TheLamSang {
    @Id
    @Column(name = "MaTheLamSang", length = 30)
    private String maTheLamSang;

    @Nationalized
    @Column( name = "TenTheLamSang", length = 300, nullable = false)
    private String tenTheLamSang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn( name = "MaBenhDanh")
    private BenhDanh benhDanh;

    public TheLamSang(){}
    public String getMaTheLamSang(){return  maTheLamSang;}
    public String getTenTheLamSang() { return tenTheLamSang;}
    public BenhDanh getBenhDanh( ){ return benhDanh;}
}
