package com.example.teamhistory.mapper;

import com.example.teamhistory.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findById(Long id);
    void insert(User user);
    void updateFavoriteTeam(@Param("userId") Long userId, @Param("teamId") Long teamId);
    void updateVisitInfo(User user);
}
