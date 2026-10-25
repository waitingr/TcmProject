package com.example.tcmproject.repository;

import com.example.tcmproject.entity.ConsultationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordRepository extends JpaRepository<ConsultationRecord, Long> {
    // 关键：只查询属于特定用户的数据，保证记录不混乱
    List<ConsultationRecord> findByUserIdOrderByConsultTimeDesc(Long userId);
}