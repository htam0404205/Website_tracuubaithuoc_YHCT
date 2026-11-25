package com.example.websiteyhct.repository;

import com.example.websiteyhct.entity.BaiThuoc;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BaiThuocRepository extends JpaRepository<BaiThuoc, Long> {
        @EntityGraph(attributePaths = { "theLamSang", "theLamSang.benhDanh" })
        @Query("""

                                select b from BaiThuoc b
                                where b.tenBaiThuoc like concat('%', :q, '%')
                          and (:benh = '' or b.theLamSang.benhDanh.maBenhDanh = :benh)
                          and (:the = ''  or b.theLamSang.maTheLamSang = :the)
                        """)
        Page<BaiThuoc> timKiem(@Param("q") String q,
                        @Param("benh") String benh,
                        @Param("the") String the,
                        Pageable pageable);

        @EntityGraph(attributePaths = { "theLamSang", "theLamSang.benhDanh" })
        @Query("""

                                select b from BaiThuoc b
                        where b.tenBaiThuoc like concat('%', :q, '%')
                          and (:benh = '' or b.theLamSang.benhDanh.maBenhDanh = :benh)
                          and (:the = ''  or b.theLamSang.maTheLamSang = :the)
                          and (:tp = '' or exists (select 1 from ThanhPhanBaiThuoc t
                                                   where t.baiThuoc = b and t.tenGoc like concat('%', :tp, '%')))
                        """)
        Page<BaiThuoc> khamPha(@Param("q") String q, @Param("benh") String benh,
                        @Param("the") String the, @Param("tp") String tp, Pageable pageable);

        @Override
        @EntityGraph(attributePaths = { "theLamSang", "theLamSang.benhDanh", "vanBan" })
        Optional<BaiThuoc> findById(Long id);

        @EntityGraph(attributePaths = { "theLamSang", "theLamSang.benhDanh" })
        List<BaiThuoc> findTop2ByTheLamSang_MaTheLamSangAndIdNotOrderByTenBaiThuoc(String maTheLamSang, Long id);

        @EntityGraph(attributePaths = { "theLamSang", "theLamSang.benhDanh", "vanBan" })
        @Query("""
            select b from BaiThuoc b
            where (:kw = '' or b.tenBaiThuoc like concat('%', :kw, '%') or b.maBaiThuoc like concat('%', :kw, '%'))
              and (:benh = '' or b.theLamSang.benhDanh.maBenhDanh = :benh)
              and (:the = '' or b.theLamSang.maTheLamSang = :the)
        """)
        Page<BaiThuoc> timKiemAdmin(@Param("kw") String kw, @Param("benh") String benh, @Param("the") String the, Pageable pageable);
}
