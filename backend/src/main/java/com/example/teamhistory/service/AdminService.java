package com.example.teamhistory.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminService {

    private final AdminTaskRunner adminTaskRunner;

    // 存储任务状态: TaskId -> TaskStatus
    private final Map<String, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    public AdminService(AdminTaskRunner adminTaskRunner) {
        this.adminTaskRunner = adminTaskRunner;
    }

    public String startRefreshTask(Long teamId, boolean fullRefresh) {
        String taskId = UUID.randomUUID().toString();
        TaskStatus status = new TaskStatus(taskId, teamId, fullRefresh);
        taskStatusMap.put(taskId, status);

        // 异步执行
        adminTaskRunner.runRefresh(status, teamId, fullRefresh);

        return taskId;
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStatusMap.get(taskId);
    }
    
    public Map<String, TaskStatus> getAllTasks() {
        return taskStatusMap;
    }
}
