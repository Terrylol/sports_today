package com.example.teamhistory.controller;

import com.example.teamhistory.service.AdminService;
import com.example.teamhistory.service.TaskStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/check-auth")
    public String checkAuth() {
        return "Authenticated";
    }

    @PostMapping("/refresh")
    public Map<String, String> startRefresh(@RequestParam Long teamId, @RequestParam boolean fullRefresh) {
        String taskId = adminService.startRefreshTask(teamId, fullRefresh);
        return Map.of("taskId", taskId);
    }

    @GetMapping("/task/{taskId}")
    public TaskStatus getTaskStatus(@PathVariable String taskId) {
        return adminService.getTaskStatus(taskId);
    }

    @GetMapping("/tasks")
    public Map<String, TaskStatus> getAllTasks() {
        return adminService.getAllTasks();
    }
}
