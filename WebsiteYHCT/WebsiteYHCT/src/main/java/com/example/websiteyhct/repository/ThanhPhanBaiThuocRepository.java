package com.example.websiteyhct.repository;

import com.example.websiteyhct.dto.ThanhPhanRut;
import com.example.websiteyhct.entity.ThanhPhanBaiThuoc;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ThanhPhanBaiThuocRepository extends JpaRepository<ThanhPhanBaiThuoc,Long> {
    @Query("""

            select new com.example.websiteyhct.dto.ThanhPhanRut(tp.baiThuoc.id, tp.tenGoc)
    from ThanhPhanBaiThuoc tp
    where tp.baiThuoc.id in :ids
    order by tp.baiThuoc.id, tp.viTri
    """)
    List<ThanhPhanRut> rutGon(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = "viThuoc")
    List<ThanhPhanBaiThuoc> findByBaiThuoc_IdOrderByViTri(Long baiThuocId);

    @Query("select distinct tp.baiThuoc from ThanhPhanBaiThuoc tp where tp.viThuoc.id = :viThuocId order by tp.baiThuoc.tenBaiThuoc")
    List<com.example.websiteyhct.entity.BaiThuoc> findBaiThuocByViThuocId(@Param("viThuocId") Long viThuocId);

    @EntityGraph(attributePaths = {"baiThuoc", "viThuoc"})
    @Query("""
        select tp from ThanhPhanBaiThuoc tp
        where (:kw = '' or tp.tenGoc like concat('%', :kw, '%') or tp.baiThuoc.maBaiThuoc like concat('%', :kw, '%'))
          and (:filter = 'all' 
               or (:filter = 'not-found' and tp.viThuoc is null)
               or (:filter = 'matched' and tp.viThuoc is not null))
        order by tp.id desc
    """)
    org.springframework.data.domain.Page<ThanhPhanBaiThuoc> timKiemReview(
            @Param("kw") String kw,
            @Param("filter") String filter,
            org.springframework.data.domain.Pageable pageable);
}
