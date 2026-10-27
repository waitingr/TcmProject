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

    @Column(length = 1000)
    private String symptoms;

    @Column(length = 100)
    private String diagnosisType;

    @Column(length = 500)
    private String features;

    @Column(length = 500)
    private String risks;

    @Column(columnDefinition = "TEXT")
    private String advice;

    private LocalDateTime consultTime = LocalDateTime.now();
}