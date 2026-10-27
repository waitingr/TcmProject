package com.example.tcmproject.controller;

import com.example.tcmproject.dto.RecordRequest;
import com.example.tcmproject.dto.RecordResponse;
import com.example.tcmproject.service.RecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /** 保存一次问诊结论 */
    @PostMapping
    public RecordResponse save(@RequestParam Long userId, @RequestBody RecordRequest request) {
        return recordService.save(userId, request);
    }

    /** 当前用户的问诊记录，按时间倒序 */
    @GetMapping
    public List<RecordResponse> list(@RequestParam Long userId) {
        return recordService.list(userId);
    }

    /** 记录详情，校验归属 */
    @GetMapping("/{id}")
    public RecordResponse detail(@PathVariable Long id, @RequestParam Long userId) {
        return recordService.get(id, userId);
    }
}