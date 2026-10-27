package com.example.tcmproject.dto;

/** 前端提交的一次问诊结论 */
public record RecordRequest(
        String symptoms,
        String diagnosisType,
        String features,
        String risks,
        String advice
) {
}