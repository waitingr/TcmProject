package com.example.tcmproject.repository;

import com.example.tcmproject.entity.ConsultRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Repository extends JpaRepository<ConsultRecord, Long> {
    // 关键：定义一个方法，根据用户ID查询记录，并按ID倒序排列（即显示最新的记录）
    List<ConsultRecord> findByUserIdOrderByIdDesc(Long userId);
}