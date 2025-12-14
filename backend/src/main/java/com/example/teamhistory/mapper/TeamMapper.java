package com.example.teamhistory.mapper;

import com.example.teamhistory.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface TeamMapper {
    void insert(Team team);
    Team findByName(String name);
    Team findById(Long id);
    java.util.List<Team> findAll();
    long count();
}
