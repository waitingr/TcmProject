package com.example.tcmproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "consult_records")
@Data
public class ConsultRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;        // 关联用户ID
    private String consultTime; // 问诊时间
    private String diagType;    // 证型判定
    private String feature;     // 体质特征
    private String risk;        // 健康风险

    @Column(columnDefinition = "TEXT")
    private String advice;      // 调理建议（内容较长，使用TEXT类型）

    // 在保存前自动生成时间字符串
    @PrePersist
    protected void onCreate() {
        this.consultTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}