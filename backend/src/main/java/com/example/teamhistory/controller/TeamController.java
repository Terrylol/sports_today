package com.example.teamhistory.controller;

import com.example.teamhistory.entity.Team;
import com.example.teamhistory.mapper.TeamMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamMapper teamMapper;

    public TeamController(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamMapper.findAll();
    }
}
