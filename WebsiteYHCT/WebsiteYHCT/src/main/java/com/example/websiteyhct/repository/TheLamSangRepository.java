package com.example.websiteyhct.repository;


import com.example.websiteyhct.entity.TheLamSang;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheLamSangRepository extends JpaRepository<TheLamSang, String> {
    @EntityGraph(attributePaths = "benhDanh")
    List<TheLamSang> findAllByOrderByTenTheLamSangAsc();
}
