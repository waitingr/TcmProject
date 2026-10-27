package com.example.tcmproject.dto;

import com.example.tcmproject.entity.User;

/** 对外返回的用户资料，永远不包含密码 */
public record ProfileResponse(
        Long id,
        String username,
        String nickname,
        Double height,
        Double weight,
        String bloodType,
        String geneticHistory,
        Double bmi
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getHeight(),
                user.getWeight(),
                user.getBloodType(),
                user.getGeneticHistory(),
                user.getBmi()
        );
    }
}