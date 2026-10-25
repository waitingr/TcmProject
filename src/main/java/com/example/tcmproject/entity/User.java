package com.example.tcmproject.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String nickname;

    // 在 User 类中添加以下字段
    private Double height;       // 身高 (cm)
    private Double weight;       // 体重 (kg)
    private String bloodType;    // 血型
    private String geneticHistory; // 遗传病史
    private Double bmi;          // BMI指数

// 记得生成对应的 Getter 和 Setter 方法
}