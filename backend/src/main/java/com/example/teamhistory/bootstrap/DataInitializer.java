package com.example.teamhistory.bootstrap;

import com.example.teamhistory.entity.HistoryEvent;
import com.example.teamhistory.entity.Team;
import com.example.teamhistory.mapper.HistoryEventMapper;
import com.example.teamhistory.mapper.TeamMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TeamMapper teamMapper;
    private final HistoryEventMapper historyEventMapper;

    public DataInitializer(TeamMapper teamMapper, HistoryEventMapper historyEventMapper) {
        this.teamMapper = teamMapper;
        this.historyEventMapper = historyEventMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (teamMapper.count() == 0) {
            initData();
        }
    }

    private void initData() {
        // 1. 创建英超20强
        List<Team> teams = new ArrayList<>();
        teams.add(new Team("arsenal", "阿森纳", "https://resources.premierleague.com/premierleague/badges/t3.svg"));
        teams.add(new Team("aston_villa", "阿斯顿维拉", "https://resources.premierleague.com/premierleague/badges/t7.svg"));
        teams.add(new Team("bournemouth", "伯恩茅斯", "https://resources.premierleague.com/premierleague/badges/t91.svg"));
        teams.add(new Team("brentford", "布伦特福德", "https://resources.premierleague.com/premierleague/badges/t94.svg"));
        teams.add(new Team("brighton", "布莱顿", "https://resources.premierleague.com/premierleague/badges/t36.svg"));
        teams.add(new Team("burnley", "伯恩利", "https://resources.premierleague.com/premierleague/badges/t90.svg"));
        teams.add(new Team("chelsea", "切尔西", "https://resources.premierleague.com/premierleague/badges/t8.svg"));
        teams.add(new Team("crystal_palace", "水晶宫", "https://resources.premierleague.com/premierleague/badges/t31.svg"));
        teams.add(new Team("everton", "埃弗顿", "https://resources.premierleague.com/premierleague/badges/t11.svg"));
        teams.add(new Team("fulham", "富勒姆", "https://resources.premierleague.com/premierleague/badges/t54.svg"));
        teams.add(new Team("liverpool", "利物浦", "https://resources.premierleague.com/premierleague/badges/t14.svg"));
        teams.add(new Team("luton_town", "卢顿", "https://resources.premierleague.com/premierleague/badges/t102.svg"));
        teams.add(new Team("man_city", "曼城", "https://resources.premierleague.com/premierleague/badges/t43.svg"));
        teams.add(new Team("man_utd", "曼彻斯特联", "https://resources.premierleague.com/premierleague/badges/t1.svg"));
        teams.add(new Team("newcastle", "纽卡斯尔联", "https://resources.premierleague.com/premierleague/badges/t4.svg"));
        teams.add(new Team("nottm_forest", "诺丁汉森林", "https://resources.premierleague.com/premierleague/badges/t17.svg"));
        teams.add(new Team("sheffield_utd", "谢菲尔德联", "https://resources.premierleague.com/premierleague/badges/t49.svg"));
        teams.add(new Team("tottenham", "托特纳姆热刺", "https://resources.premierleague.com/premierleague/badges/t6.svg"));
        teams.add(new Team("west_ham", "西汉姆联", "https://resources.premierleague.com/premierleague/badges/t21.svg"));
        teams.add(new Team("wolves", "狼队", "https://resources.premierleague.com/premierleague/badges/t39.svg"));

        for (Team team : teams) {
            teamMapper.insert(team);
        }

        // 2. 为曼联添加一些初始化数据
        Team manu = teams.stream().filter(t -> t.getName().equals("man_utd")).findFirst().orElseThrow();
        
        // 获取今天的日期，确保用户一打开就能看到数据
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        HistoryEvent todayEvent = new HistoryEvent(
                manu,
                month,
                day,
                2023,
                "今日训练",
                "曼联全队正在卡灵顿基地进行日常训练，备战周末的比赛。",
                HistoryEvent.EventType.OTHER
        );
        historyEventMapper.insert(todayEvent);

        // 更多今日数据（模拟）
        historyEventMapper.insert(new HistoryEvent(
                manu, month, day, 2008, "C罗进球回顾", "2008年的今天，C罗在联赛中打入精彩任意球。", HistoryEvent.EventType.MATCH
        ));
        
        historyEventMapper.insert(new HistoryEvent(
                manu, month, day, 1995, "青训小将首秀", "92班成员在预备队表现出色。", HistoryEvent.EventType.OTHER
        ));

        // 插入一些经典时刻
        historyEventMapper.insert(new HistoryEvent(
                manu, 2, 6, 1958, "慕尼黑空难", "曼联历史上最黑暗的一天。", HistoryEvent.EventType.OTHER
        ));
        
        historyEventMapper.insert(new HistoryEvent(
                manu, 5, 26, 1999, "诺坎普奇迹", "索尔斯克亚绝杀，曼联夺得三冠王。", HistoryEvent.EventType.MATCH
        ));
        
        historyEventMapper.insert(new HistoryEvent(
                manu, 5, 21, 2008, "莫斯科雨夜", "欧冠决赛点球大战击败切尔西，第三次捧起大耳朵杯。", HistoryEvent.EventType.MATCH
        ));
        
         historyEventMapper.insert(new HistoryEvent(
                manu, 8, 17, 1996, "贝克汉姆中场吊射", "对阵温布尔登，贝克汉姆成名之作。", HistoryEvent.EventType.MATCH
        ));

        System.out.println("Initialized " + teams.size() + " teams and sample events.");
    }
}
