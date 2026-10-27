package com.example.tcmproject.service;

import com.example.tcmproject.dto.HealthRequest;
import com.example.tcmproject.dto.RegisterRequest;
import com.example.tcmproject.entity.User;
import com.example.tcmproject.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String password = request.password() == null ? "" : request.password();
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("密码长度需为 " + MIN_PASSWORD_LENGTH + "-" + MAX_PASSWORD_LENGTH + " 位");
        }
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("账号已存在，请直接登录");
        }

        String nickname = request.nickname() == null ? "" : request.nickname().trim();
        if (nickname.isEmpty()) {
            nickname = username;
        }
        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    @Transactional
    public User login(String rawUsername, String rawPassword) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        String password = rawPassword == null ? "" : rawPassword;
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("请输入账号和密码");
        }

        User user = userRepository.findByUsername(username);
        if (user == null || !matches(password, user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 兼容早期直接存明文的账号：登录成功后自动升级为 BCrypt
        if (!isBcrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User require(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在或已注销"));
    }

    @Transactional
    public User updateHealth(Long userId, HealthRequest request) {
        User user = require(userId);
        if (request.height() == null || request.weight() == null) {
            throw new IllegalArgumentException("请填写身高和体重");
        }
        if (request.height() < 50 || request.height() > 250) {
            throw new IllegalArgumentException("身高请填写 50-250 cm 之间的数值");
        }
        if (request.weight() < 20 || request.weight() > 300) {
            throw new IllegalArgumentException("体重请填写 20-300 kg 之间的数值");
        }

        user.setHeight(round(request.height(), 1));
        user.setWeight(round(request.weight(), 1));
        double meters = request.height() / 100.0;
        user.setBmi(round(request.weight() / (meters * meters), 1));

        String bloodType = request.bloodType() == null ? "" : request.bloodType().trim();
        user.setBloodType(bloodType.isEmpty() ? null : bloodType.substring(0, Math.min(bloodType.length(), 10)));

        String history = request.geneticHistory() == null ? "" : request.geneticHistory().trim();
        user.setGeneticHistory(history.isEmpty() ? null : history.substring(0, Math.min(history.length(), 500)));
        return userRepository.save(user);
    }

    private String normalizeUsername(String raw) {
        String username = raw == null ? "" : raw.trim();
        if (username.length() < 2 || username.length() > 30) {
            throw new IllegalArgumentException("账号长度需为 2-30 个字符");
        }
        if (username.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("账号不能包含空格");
        }
        return username;
    }

    private boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}