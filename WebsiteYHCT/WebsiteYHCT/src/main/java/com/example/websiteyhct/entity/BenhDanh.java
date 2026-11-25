package com.example.websiteyhct.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "BenhDanh")
public class BenhDanh {
    @Id
    @Column(name = "MaBenhDanh",length = 30)
    private  String maBenhDanh;
    @Nationalized
    @Column(name = "TenBenhDanh",length = 200, nullable = false)
    private  String tenBenhDanh;

    public BenhDanh(){ }

    public  String getMaBenhDanh() { return maBenhDanh;}
    public  String getTenBenhDanh() { return tenBenhDanh;}

}
