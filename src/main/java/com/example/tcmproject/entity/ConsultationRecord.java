package com.example.tcmproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_records")
@Data
public class ConsultationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // 关键：多对一关联，实现数据隔离
    @JoinColumn(name = "user_id")
    private User user;

    private String symptoms;
    private String diagnosisType;
    private String features;
    private String risks;
    @Column(columnDefinition = "TEXT")
    private String advice;
    private LocalDateTime consultTime = LocalDateTime.now();
}