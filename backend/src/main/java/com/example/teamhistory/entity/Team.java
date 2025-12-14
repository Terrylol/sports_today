package com.example.teamhistory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Team {
    private Long id;
    private String name; // 例如：Manchester United
    private String displayName; // 例如：曼联
    private String logoUrl; // 球队 Logo

    public Team(String name, String displayName, String logoUrl) {
        this.name = name;
        this.displayName = displayName;
        this.logoUrl = logoUrl;
    }
}
