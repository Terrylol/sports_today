package com.example.teamhistory.controller;

import com.example.teamhistory.entity.User;
import com.example.teamhistory.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public User login(@RequestParam String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setNickname(username); // Default nickname
            user.setVisitCount(1);
            user.setLastVisitDate(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            user.setVisitCount(user.getVisitCount() + 1);
            user.setLastVisitDate(LocalDateTime.now());
            userMapper.updateVisitInfo(user);
        }
        return user;
    }

    @PutMapping("/{id}/team")
    public void updateTeam(@PathVariable Long id, @RequestParam Long teamId) {
        userMapper.updateFavoriteTeam(id, teamId);
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userMapper.findById(id);
    }
}
