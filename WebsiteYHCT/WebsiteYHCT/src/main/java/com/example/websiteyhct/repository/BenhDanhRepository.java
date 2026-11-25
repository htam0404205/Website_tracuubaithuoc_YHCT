package com.example.websiteyhct.repository;

import com.example.websiteyhct.dto.BenhDanhDong;
import  com.example.websiteyhct.entity.BenhDanh;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;


public interface BenhDanhRepository extends JpaRepository<BenhDanh, String> {

    List<BenhDanh> findAllByOrderByTenBenhDanhAsc();

    @Query("""
        select new com.example.websiteyhct.dto.BenhDanhDong(bd.maBenhDanh, bd.tenBenhDanh)
        from BaiThuoc b join b.theLamSang t join t.benhDanh bd
        group by bd.maBenhDanh, bd.tenBenhDanh
        order by count(b) desc, bd.tenBenhDanh
        """)
    List<BenhDanhDong> noiBat(Pageable pageable);
}