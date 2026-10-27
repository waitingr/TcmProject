package com.example.tcmproject.controller;

import com.example.tcmproject.dto.*;
import com.example.tcmproject.entity.User;
import com.example.tcmproject.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** 注册 */
    @PostMapping("/register")
    public AuthResult register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return AuthResult.ok(user.getId(), user.getNickname());
    }

    /** 登录 */
    @PostMapping("/login")
    public AuthResult login(@RequestBody LoginRequest request) {
        User user = userService.login(request.username(), request.password());
        return AuthResult.ok(user.getId(), user.getNickname());
    }

    /** 用户资料（含健康档案） */
    @GetMapping("/users/{id}")
    public ProfileResponse profile(@PathVariable Long id) {
        return ProfileResponse.from(userService.require(id));
    }

    /** 保存健康档案：身高、体重、血型、遗传病史，BMI 由后端计算 */
    @PostMapping("/users/{id}/health")
    public ProfileResponse updateHealth(@PathVariable Long id, @RequestBody HealthRequest request) {
        return ProfileResponse.from(userService.updateHealth(id, request));
    }
}