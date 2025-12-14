package com.example.teamhistory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryEvent {
    private Long id;
    private Team team;
    private Integer eventMonth; // 1-12
    private Integer eventDay;   // 1-31
    private Integer eventYear;  // 发生年份，例如 1999
    private String title;
    private String description;
    private String imageUrl; // 事件配图
    private EventType type; // MATCH, TRANSFER, BIRTHDAY, OTHER

    public enum EventType {
        MATCH, TRANSFER, BIRTHDAY, OTHER
    }

    public HistoryEvent(Team team, Integer eventMonth, Integer eventDay, Integer eventYear, String title, String description, EventType type) {
        this.team = team;
        this.eventMonth = eventMonth;
        this.eventDay = eventDay;
        this.eventYear = eventYear;
        this.title = title;
        this.description = description;
        this.type = type;
    }
}
