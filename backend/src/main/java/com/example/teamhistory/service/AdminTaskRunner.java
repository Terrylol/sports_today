package com.example.teamhistory.service;

import com.example.teamhistory.entity.HistoryEvent;
import com.example.teamhistory.entity.Team;
import com.example.teamhistory.mapper.HistoryEventMapper;
import com.example.teamhistory.mapper.TeamMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTaskRunner {

    private final AIService aiService;
    private final HistoryEventMapper historyEventMapper;
    private final TeamMapper teamMapper;

    public AdminTaskRunner(AIService aiService, HistoryEventMapper historyEventMapper, TeamMapper teamMapper) {
        this.aiService = aiService;
        this.historyEventMapper = historyEventMapper;
        this.teamMapper = teamMapper;
    }

    @Async
    public void runRefresh(TaskStatus status, Long teamId, boolean fullRefresh) {
        if (status == null) return;

        status.setStatus("RUNNING");
        Team team = teamMapper.findById(teamId);
        
        try {
            LocalDate today = LocalDate.now();
            
            // 如果是全量刷新，先删除当天所有数据
            if (fullRefresh) {
                status.appendLog("正在清理 " + team.getDisplayName() + " 今天的所有历史数据...");
                historyEventMapper.deleteByTeamAndDate(teamId, today.getMonthValue(), today.getDayOfMonth());
            }

            // 定义挖掘范围：1880 年到 2024 年
            int startYear = 1880;
            int endYear = 2024;
            int chunkSize = 10; // 每 10 年一个批次

            int totalChunks = (endYear - startYear) / chunkSize + 1;
            int currentChunk = 0;

            // 获取已存在的年份（如果是增量刷新）
            List<Integer> existingYears = new ArrayList<>();
            if (!fullRefresh) {
                List<HistoryEvent> events = historyEventMapper.findByTeamAndDate(teamId, today.getMonthValue(), today.getDayOfMonth());
                existingYears = events.stream().map(HistoryEvent::getEventYear).collect(Collectors.toList());
            }

            for (int year = startYear; year <= endYear; year += chunkSize) {
                currentChunk++;
                int chunkEnd = Math.min(year + chunkSize - 1, endYear);
                
                // 计算当前批次需要排除的年份
                List<Integer> currentBatchExcluded = new ArrayList<>();
                if (!fullRefresh) {
                    final int finalYear = year;
                    currentBatchExcluded = existingYears.stream()
                            .filter(y -> y >= finalYear && y <= chunkEnd)
                            .collect(Collectors.toList());
                    
                    // 如果这个区间所有年份都填满了，跳过
                    if (currentBatchExcluded.size() >= (chunkEnd - finalYear + 1)) {
                         status.appendLog(String.format("跳过区间 %d-%d (已填满)", year, chunkEnd));
                         status.setProgress((int) ((double) currentChunk / totalChunks * 100));
                         continue;
                    }
                }

                status.appendLog(String.format("正在挖掘 %d-%d 年间的历史...", year, chunkEnd));
                
                // 调用 AI 进行批量挖掘
                List<HistoryEvent> newEvents = aiService.fetchEventsByYearRange(team, year, chunkEnd, currentBatchExcluded);
                
                if (!newEvents.isEmpty()) {
                    for (HistoryEvent event : newEvents) {
                        historyEventMapper.insert(event);
                        status.appendLog("发现事件: " + event.getEventYear() + " - " + event.getTitle());
                    }
                } else {
                    status.appendLog(String.format("区间 %d-%d 未发现新事件", year, chunkEnd));
                }

                // 更新进度
                status.setProgress((int) ((double) currentChunk / totalChunks * 100));
                
                // 稍微休眠一下，避免触发 API 限流
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            status.setStatus("COMPLETED");
            status.setProgress(100);
            status.appendLog("任务完成！");

        } catch (Exception e) {
            e.printStackTrace();
            status.setStatus("FAILED");
            status.appendLog("任务失败: " + e.getMessage());
        }
    }
}
