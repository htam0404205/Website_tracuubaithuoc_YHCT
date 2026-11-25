package com.example.websiteyhct.repository;


import com.example.websiteyhct.entity.ViThuoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViThuocRepository extends JpaRepository<ViThuoc,Long> {
    @Query("select v from ViThuoc v where v.tenViThuoc like concat('%', :q, '%')")
    Page<ViThuoc> timTheoTen(@Param("q") String q, Pageable pageable);

    java.util.Optional<ViThuoc> findFirstByTenViThuoc(String tenViThuoc);

    @Query("""
        select v from ViThuoc v
        where (:kw = '' or v.tenViThuoc like concat('%', :kw, '%') or v.maDuocLieu like concat('%', :kw, '%'))
          and (:nguon = '' 
               or (:nguon = 'nam' and v.nguonGocNam = 'N')
               or (:nguon = 'bac' and v.nguonGocBac = 'B'))
    """)
    Page<ViThuoc> timKiemAdmin(@Param("kw") String kw, @Param("nguon") String nguon, Pageable pageable);
}
