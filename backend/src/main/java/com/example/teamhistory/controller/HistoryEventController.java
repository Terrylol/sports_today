package com.example.teamhistory.controller;

import com.example.teamhistory.entity.HistoryEvent;
import com.example.teamhistory.mapper.HistoryEventMapper;
import com.example.teamhistory.service.AIService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*") // 允许所有来源跨域，生产环境请限制
public class HistoryEventController {

    private final HistoryEventMapper historyEventMapper;
    private final AIService aiService;

    public HistoryEventController(HistoryEventMapper historyEventMapper, AIService aiService) {
        this.historyEventMapper = historyEventMapper;
        this.aiService = aiService;
    }

    @GetMapping("/today")
    public List<HistoryEvent> getTodayEvents(@RequestParam(required = false) Long teamId) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();
        
        if (teamId != null) {
            return historyEventMapper.findByTeamAndDate(teamId, month, day);
        }
        
        // MVP 版本：返回所有球队在今天的历史事件
        return historyEventMapper.findByDate(month, day);
    }

    @PostMapping("/fetch-ai")
    public List<HistoryEvent> fetchFromAI(@RequestParam Long teamId) {
        return aiService.fetchAndSaveEventsFromAI(teamId);
    }
}
