package com.example.tcmproject.dto;

import com.example.tcmproject.entity.ConsultationRecord;
import java.time.format.DateTimeFormatter;

/** 前端渲染问诊记录所需的扁平结构，避免直接序列化实体 */
public record RecordResponse(
        Long id,
        Long userId,
        String symptoms,
        String diagnosisType,
        String features,
        String risks,
        String advice,
        String consultTime
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static RecordResponse from(ConsultationRecord record) {
        return new RecordResponse(
                record.getId(),
                record.getUser() == null ? null : record.getUser().getId(),
                record.getSymptoms(),
                record.getDiagnosisType(),
                record.getFeatures(),
                record.getRisks(),
                record.getAdvice(),
                record.getConsultTime() == null ? null : record.getConsultTime().format(FORMATTER)
        );
    }
}