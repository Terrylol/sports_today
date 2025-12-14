package com.example.teamhistory.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String nickname;
    private Long favoriteTeamId;
    private Integer visitCount;
    private LocalDateTime lastVisitDate;
    
    // Transient field for full team details
    private Team favoriteTeam;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getFavoriteTeamId() {
        return favoriteTeamId;
    }

    public void setFavoriteTeamId(Long favoriteTeamId) {
        this.favoriteTeamId = favoriteTeamId;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public LocalDateTime getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(LocalDateTime lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public Team getFavoriteTeam() {
        return favoriteTeam;
    }

    public void setFavoriteTeam(Team favoriteTeam) {
        this.favoriteTeam = favoriteTeam;
    }
}
