package com.example.tcmproject.dto;

public record AuthResult(boolean success, String message, Long userId, String nickname) {

    public static AuthResult ok(Long userId, String nickname) {
        return new AuthResult(true, null, userId, nickname);
    }

    public static AuthResult fail(String message) {
        return new AuthResult(false, message, null, null);
    }
}