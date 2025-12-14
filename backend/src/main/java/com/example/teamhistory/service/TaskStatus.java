package com.example.teamhistory.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskStatus {
    private String taskId;
    private Long teamId;
    private boolean fullRefresh;
    private volatile String status; // PENDING, RUNNING, COMPLETED, FAILED
    private volatile int progress; // 0-100
    private List<String> logs;
    private long startTime;

    public TaskStatus(String taskId, Long teamId, boolean fullRefresh) {
        this.taskId = taskId;
        this.teamId = teamId;
        this.fullRefresh = fullRefresh;
        this.status = "PENDING";
        this.progress = 0;
        // Use synchronized list for thread safety
        this.logs = Collections.synchronizedList(new ArrayList<>());
        this.startTime = System.currentTimeMillis();
    }

    public void appendLog(String log) {
        this.logs.add(log);
        // 只保留最近 50 条日志
        if (this.logs.size() > 50) {
            this.logs.remove(0);
        }
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public Long getTeamId() { return teamId; }
    public boolean isFullRefresh() { return fullRefresh; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public List<String> getLogs() { return new ArrayList<>(logs); } // Return copy to avoid ConcurrentModificationException
    public long getStartTime() { return startTime; }
}
