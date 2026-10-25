package com.example.tcmproject.controller;

import com.example.tcmproject.entity.*;
import com.example.tcmproject.repository.*;
import com.example.tcmproject.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TcmController {

    @Autowired private UserRepository userRepository;
    @Autowired private RecordRepository recordRepository;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User newUser) {
        Map<String, Object> res = new HashMap<>();
        // 1. 检查账号是否已存在
        if (userRepository.findByUsername(newUser.getUsername()) != null) {
            res.put("success", false);
            res.put("message", "账号已存在，请直接登录");
            return res;
        }
        // 2. 保存到数据库 (包含你设置的 nickname)
        userRepository.save(newUser);
        res.put("success", true);
        return res;
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername());
        Map<String, Object> res = new HashMap<>();

        if (user != null && user.getPassword().equals(loginUser.getPassword())) {
            res.put("success", true);
            res.put("userId", user.getId());
            // 关键：必须把 nickname 返回，前端才能显示你设置的名字
            res.put("nickname", user.getNickname());
        } else {
            res.put("success", false);
            res.put("message", "账号或密码错误");
        }
        return res;
    }

    // 保存问诊记录 (保持不变)
    @PostMapping("/records/save")
    public String save(@RequestBody ConsultationRecord record, @RequestParam Long userId) {
        userRepository.findById(userId).ifPresent(record::setUser);
        recordRepository.save(record);
        return "保存成功";
    }

    // 调出当前用户的个人中心数据 (保持不变)
    @GetMapping("/records/my")
    public List<ConsultationRecord> getMyRecords(@RequestParam Long userId) {
        return recordRepository.findByUserIdOrderByConsultTimeDesc(userId);
    }

    // 在 TcmController 中添加
    @PostMapping("/user/health")
    public String updateHealth(@RequestParam Long userId, @RequestBody User healthData) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setHeight(healthData.getHeight());
            user.setWeight(healthData.getWeight());
            user.setBloodType(healthData.getBloodType());
            user.setGeneticHistory(healthData.getGeneticHistory());
            user.setBmi(healthData.getBmi());
            userRepository.save(user);
        });
        return "更新成功";
    }
}