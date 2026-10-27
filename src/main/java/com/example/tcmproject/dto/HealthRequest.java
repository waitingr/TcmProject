package com.example.tcmproject.dto;

/** 健康档案提交参数（BMI 由后端计算，不接受前端传入） */
public record HealthRequest(
        Double height,
        Double weight,
        String bloodType,
        String geneticHistory
) {
}