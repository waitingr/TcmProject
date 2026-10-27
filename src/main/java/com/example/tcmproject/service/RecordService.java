package com.example.tcmproject.service;

import com.example.tcmproject.dto.RecordRequest;
import com.example.tcmproject.dto.RecordResponse;
import com.example.tcmproject.entity.ConsultationRecord;
import com.example.tcmproject.entity.User;
import com.example.tcmproject.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserService userService;

    public RecordService(RecordRepository recordRepository, UserService userService) {
        this.recordRepository = recordRepository;
        this.userService = userService;
    }

    @Transactional
    public RecordResponse save(Long userId, RecordRequest request) {
        User user = userService.require(userId);
        if (isBlank(request.diagnosisType())) {
            throw new IllegalArgumentException("缺少证型结果，暂时无法保存这条记录");
        }

        ConsultationRecord record = new ConsultationRecord();
        record.setUser(user);
        record.setSymptoms(trimTo(request.symptoms(), 1000, "用户未填写症状描述"));
        record.setDiagnosisType(trimTo(request.diagnosisType(), 100, null));
        record.setFeatures(trimTo(request.features(), 500, null));
        record.setRisks(trimTo(request.risks(), 500, null));
        record.setAdvice(trimTo(request.advice(), 20000, null));
        record.setConsultTime(LocalDateTime.now());
        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<RecordResponse> list(Long userId) {
        userService.require(userId);
        return recordRepository.findByUserIdOrderByConsultTimeDesc(userId)
                .stream()
                .map(RecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecordResponse get(Long recordId, Long userId) {
        ConsultationRecord record = recordRepository.findById(recordId)
                .filter(item -> item.getUser() != null && userId != null && userId.equals(item.getUser().getId()))
                .orElseThrow(() -> new NoSuchElementException("记录不存在或无权查看"));
        return RecordResponse.from(record);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimTo(String value, int maxLength, String fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}