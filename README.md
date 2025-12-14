# 球队历史上的今天 (Team History On This Day)

这是一个前后端分离的 Web 应用，用于展示球队（目前以曼联为例）在历史上的今天发生的重大事件。

## 🛠 技术栈

- **后端**: Java, Spring Boot 3, MyBatis, H2 Database
- **前端**: Vue 3, Vite
- **其他**: REST API, AI 模拟接口

## ✨ 功能

1. **历史上的今天**: 自动获取当前日期的历史事件。
2. **AI 挖掘**: 点击按钮模拟调用大模型，生成并保存新的历史事件（模拟功能）。
3. **美观 UI**: 响应式卡片设计，曼联主题色。
4. **数据持久化**: 使用 H2 内存数据库（应用重启后数据重置，可配置为文件存储）。

## 🚀 快速开始

### 后端启动

1. 进入 `backend` 目录:
   ```bash
   cd backend
   ```
2. 运行 Spring Boot 应用:
   ```bash
   mvn spring-boot:run
   ```
   或者打包运行:
   ```bash
   mvn package -DskipTests
   java -jar target/team-history-0.0.1-SNAPSHOT.jar
   ```
   后端服务将运行在 `http://localhost:8080`。

### 前端启动

1. 进入 `frontend` 目录:
   ```bash
   cd frontend
   ```
2. 安装依赖:
   ```bash
   npm install
   ```
3. 启动开发服务器:
   ```bash
   npm run dev
   ```
   前端服务通常运行在 `http://localhost:5173`。

## 📝 API 接口

- `GET /api/events/today`: 获取今天的历史事件。
- `POST /api/events/fetch-ai?teamId={id}`: 模拟 AI 挖掘并保存新事件。

## ⚠️ 注意事项

- **H2 数据库**: 这是一个内存数据库，方便测试。如果你想保留数据，请修改 `application.properties` 中的 `spring.datasource.url`。
- **MyBatis**: 项目已从 JPA 迁移到 MyBatis，使用 XML 配置 SQL。
- **AI 功能**: 目前是模拟实现，每次点击会生成随机年份的数据。

---
Created by Trae AI
