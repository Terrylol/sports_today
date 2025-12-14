package com.example.teamhistory.mapper;

import com.example.teamhistory.entity.HistoryEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface HistoryEventMapper {
    void insert(HistoryEvent event);
    List<HistoryEvent> findByDate(@Param("month") Integer month, @Param("day") Integer day);
    List<HistoryEvent> findByTeamAndDate(@Param("teamId") Long teamId, @Param("month") Integer month, @Param("day") Integer day);
    void deleteByTeamAndDate(@Param("teamId") Long teamId, @Param("month") Integer month, @Param("day") Integer day);
}
