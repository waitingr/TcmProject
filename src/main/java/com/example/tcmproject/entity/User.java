package com.example.tcmproject.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String username;

    /** 只允许反序列化（注册/登录入参），任何接口都不会把密码序列化出去 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    private Double height;         // 身高 (cm)
    private Double weight;         // 体重 (kg)

    @Column(length = 10)
    private String bloodType;      // 血型

    @Column(length = 500)
    private String geneticHistory; // 遗传病史 / 过敏史

    private Double bmi;            // BMI 指数（由后端统一计算）
}