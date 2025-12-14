package com.example.teamhistory.service;

import com.example.teamhistory.entity.HistoryEvent;
import com.example.teamhistory.entity.Team;
import com.example.teamhistory.mapper.HistoryEventMapper;
import com.example.teamhistory.mapper.TeamMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class AIService {

    private final HistoryEventMapper historyEventMapper;
    private final TeamMapper teamMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${siliconflow.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String MODEL_NAME = "deepseek-ai/DeepSeek-V3";

    public AIService(HistoryEventMapper historyEventMapper, TeamMapper teamMapper) {
        this.historyEventMapper = historyEventMapper;
        this.teamMapper = teamMapper;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<HistoryEvent> fetchAndSaveEventsFromAI(Long teamId) {
        Team team = teamMapper.findById(teamId);
        if (team == null) {
            throw new RuntimeException("Team not found");
        }

        // 1. 获取已存在的年份
        LocalDate today = LocalDate.now();
        List<HistoryEvent> existingEvents = historyEventMapper.findByTeamAndDate(teamId, today.getMonthValue(), today.getDayOfMonth());
        List<Integer> existingYears = existingEvents.stream()
                .map(HistoryEvent::getEventYear)
                .distinct()
                .collect(Collectors.toList());

        // 2. 调用 SiliconFlow API 获取数据 (传入已存在的年份)
        List<HistoryEvent> newEvents = callSiliconFlowAPI(team, existingYears);
        
        // 3. 再次过滤 (双重保险) 并保存到数据库
        List<HistoryEvent> savedEvents = new ArrayList<>();
        for (HistoryEvent event : newEvents) {
            if (!existingYears.contains(event.getEventYear())) {
                historyEventMapper.insert(event);
                savedEvents.add(event);
                existingYears.add(event.getEventYear()); // 防止本次批量返回中有重复年份
            }
        }
        
        return savedEvents;
    }

    public List<HistoryEvent> fetchEventsByYearRange(Team team, int startYear, int endYear, List<Integer> excludedYears) {
        LocalDate today = LocalDate.now();
        String prompt = buildRangePrompt(team, today, startYear, endYear, excludedYears);
        
        return callAIWithPrompt(team, today, prompt);
    }

    private List<HistoryEvent> callSiliconFlowAPI(Team team, List<Integer> excludedYears) {
        LocalDate today = LocalDate.now();
        String prompt = buildPrompt(team, today, excludedYears);
        return callAIWithPrompt(team, today, prompt);
    }

    private List<HistoryEvent> callAIWithPrompt(Team team, LocalDate today, String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful assistant."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAIResponse(response.getBody(), team, today);
            } else {
                System.err.println("API call failed: " + response.getStatusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private String buildPrompt(Team team, LocalDate date, List<Integer> excludedYears) {
        String excludedYearsStr = excludedYears.isEmpty() ? "无" : excludedYears.toString();
        
        return String.format(
                "Role: 你是一个精通英超历史的足球数据专家。\n" +
                "Task:今天是 %d月%d日。请挖掘关于英超球队\"%s\"在历史上今天发生的 1 到 3 个重要事件（比赛胜利、球员转会、名宿生日、重要记录等）。\n" +
                "Important: 请务必**排除**以下年份，不要返回这些年份的事件： %s。\n" +
                "Format: 请**只**返回一个合法的 JSON 数组 (Array)，不要包含 Markdown 格式（如 ```json），数组中每个对象的格式要求如下：\n" +
                "[\n" +
                "  {\n" +
                "    \"year\": 2004,\n" +
                "    \"title\": \"事件标题（简短有力）\",\n" +
                "    \"description\": \"事件的详细描述（50-100字左右，生动有趣）\",\n" +
                "    \"imageUrl\": \"相关的图片URL（必须是真实有效的HTTPS链接，优先维基百科或新闻图，如果找不到填空字符串）\",\n" +
                "    \"type\": \"MATCH\" // 枚举值: MATCH(比赛), TRANSFER(转会), BIRTHDAY(生日), OTHER(其他)\n" +
                "  }\n" +
                "]",
                date.getMonthValue(), date.getDayOfMonth(), team.getDisplayName(), excludedYearsStr
        );
    }

    private String buildRangePrompt(Team team, LocalDate date, int startYear, int endYear, List<Integer> excludedYears) {
        String excludedYearsStr = excludedYears.isEmpty() ? "无" : excludedYears.toString();

        return String.format(
                "Role: 你是一个精通英超历史的足球数据专家。\n" +
                "Task:今天是 %d月%d日。请挖掘关于英超球队\"%s\"在 %d 年到 %d 年之间，历史上今天发生的 1 到 3 个重要事件。\n" +
                "Important: 请务必**排除**以下年份，不要返回这些年份的事件： %s。\n" +
                "Format: 请**只**返回一个合法的 JSON 数组 (Array)，不要包含 Markdown 格式（如 ```json），数组中每个对象的格式要求如下：\n" +
                "[\n" +
                "  {\n" +
                "    \"year\": 2004,\n" +
                "    \"title\": \"事件标题（简短有力）\",\n" +
                "    \"description\": \"事件的详细描述（50-100字左右，生动有趣）\",\n" +
                "    \"imageUrl\": \"相关的图片URL（必须是真实有效的HTTPS链接，优先维基百科或新闻图，如果找不到填空字符串）\",\n" +
                "    \"type\": \"MATCH\" // 枚举值: MATCH(比赛), TRANSFER(转会), BIRTHDAY(生日), OTHER(其他)\n" +
                "  }\n" +
                "]",
                date.getMonthValue(), date.getDayOfMonth(), team.getDisplayName(), startYear, endYear, excludedYearsStr
        );
    }

    private List<HistoryEvent> parseAIResponse(String responseBody, Team team, LocalDate today) {
        List<HistoryEvent> events = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            String content = contentNode.asText();

            // 清理可能存在的 Markdown 代码块标记
            content = content.replace("```json", "").replace("```", "").trim();

            JsonNode eventsArray = objectMapper.readTree(content);
            
            if (eventsArray.isArray()) {
                for (JsonNode eventData : eventsArray) {
                    HistoryEvent event = parseSingleEventNode(eventData, team, today);
                    events.add(event);
                }
            } else if (eventsArray.isObject()) {
                // 兼容 AI 偶尔只返回单个对象的情况
                events.add(parseSingleEventNode(eventsArray, team, today));
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            events.add(createFallbackEvent(team, today, "解析 AI 响应失败"));
        }
        return events;
    }

    private HistoryEvent parseSingleEventNode(JsonNode eventData, Team team, LocalDate today) {
        int year = eventData.path("year").asInt(today.getYear());
        String title = eventData.path("title").asText("未知事件");
        String description = eventData.path("description").asText("暂无描述");
        String imageUrl = eventData.path("imageUrl").asText("");
        String typeStr = eventData.path("type").asText("OTHER");

        // 验证图片有效性
        if (!isValidImage(imageUrl)) {
            imageUrl = null;
        }

        HistoryEvent.EventType type;
        try {
            type = HistoryEvent.EventType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = HistoryEvent.EventType.OTHER;
        }

        HistoryEvent event = new HistoryEvent(
                team,
                today.getMonthValue(),
                today.getDayOfMonth(),
                year,
                title,
                description,
                type
        );
        event.setImageUrl(imageUrl);
        return event;
    }

    private boolean isValidImage(String url) {
        if (url == null || url.isEmpty() || !url.startsWith("http")) {
            return false;
        }
        try {
            // 使用 HEAD 请求检查链接是否有效且是图片
            HttpHeaders headers = restTemplate.headForHeaders(url);
            MediaType contentType = headers.getContentType();
            return contentType != null && contentType.toString().startsWith("image");
        } catch (Exception e) {
            // 任何异常都视为无效
            return false;
        }
    }

    private HistoryEvent createFallbackEvent(Team team, LocalDate today, String reason) {
        return new HistoryEvent(
                team,
                today.getMonthValue(),
                today.getDayOfMonth(),
                today.getYear(),
                "AI 服务暂时不可用",
                "无法获取历史事件 (" + reason + ")。请稍后再试。",
                HistoryEvent.EventType.OTHER
        );
    }
}
