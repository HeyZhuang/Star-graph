# Star-Graph AI 图像生成平台

一个基于Spring Boot和Vue 3的全栈AI图像生成平台，支持文生图、图生图、画质提升、姿势控制等多种AI图像生成功能。

## 功能特点

### 🎨 多种生成模式
- **文生图（Text-to-Image）**: 根据文字描述生成图像
- **图生图（Image-to-Image）**: 基于输入图像生成新图像
- **画质提升（Upscale）**: 2倍/4倍图像放大增强
- **姿势控制（Pose Control）**: 基于姿势骨架生成图像

### ⚡ 实时通信
- WebSocket + STOMP 协议实现实时进度推送
- 支持队列状态、生成进度、结果推送
- 双WebSocket架构（前端↔后端↔ComfyUI）

### 🔧 技术栈
**后端技术:**
- Spring Boot 3.2.8 + Java 17
- MyBatis-Plus 数据库操作
- Redis 缓存和会话管理
- WebSocket + STOMP 实时通信
- MySQL 数据存储
- Freemarker 模板引擎

**前端技术:**
- Vue 3 + TypeScript + Composition API
- Vite 构建工具
- Element Plus UI组件库
- Pinia 状态管理
- @stomp/stompjs WebSocket客户端

## 项目结构

```
Star-graph/
├── star-graph/                           # 后端项目
│   ├── src/main/java/cn/itcast/
│   │   ├── StarGraphApp.java             # 主启动类
│   │   └── star/graph/core/
│   │       ├── controller/               # REST控制器
│   │       ├── service/                  # 业务服务层
│   │       ├── mapper/                   # 数据访问层
│   │       ├── pojo/                     # 实体类
│   │       ├── dto/                      # 数据传输对象
│   │       └── config/                   # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml               # 应用配置
│   │   └── templates/                    # Freemarker模板
│   └── pom.xml                          # Maven配置
├── star-graph-ui完整前端/star-graph-ui/    # 前端项目
│   ├── src/
│   │   ├── views/                        # 页面组件
│   │   │   ├── t2i/                     # 文生图
│   │   │   ├── i2i/                     # 图生图
│   │   │   ├── upscale/                 # 画质提升
│   │   │   └── pose/                    # 姿势控制
│   │   ├── components/                   # 公共组件
│   │   ├── api/                         # API调用
│   │   └── router/                      # 路由配置
│   ├── package.json                     # 依赖配置
│   └── vite.config.ts                   # Vite配置
├── docker-compose-env.yml               # Docker环境配置
└── CLAUDE.md                           # 开发指南
```

## 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Docker (可选)

### 后端启动
```bash
cd star-graph/
mvn clean compile
mvn spring-boot:run
```

### 前端启动
```bash
cd star-graph-ui完整前端/star-graph-ui/
npm install
npm run dev
```

### Docker环境
```bash
# 启动MySQL、Redis、ComfyUI服务
docker-compose -f docker-compose-env.yml up -d
```

## API接口

### 图像生成接口
- `POST /api/authed/1.0/t2i/propmt` - 文生图
- `POST /api/authed/1.0/i2i/generate` - 图生图
- `POST /api/authed/1.0/upscale/enhance` - 画质提升
- `POST /api/authed/1.0/pose/generate` - 姿势控制

### WebSocket通信
- 连接地址: `ws://localhost:8080/ws`
- 支持主题订阅: `/topic/messages`, `/user/{clientId}/topic/messages`

## 配置说明

### 后端配置 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/star-graph
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
server:
  port: 8080
```

### 前端配置 (.env.development)
```env
VITE_PREFIX_BASE_API = '/dev-api'
VITE_PROXY_URL = http://localhost:8080
VITE_WS_HOST_URL = ws://localhost:8080/ws
VITE_APP_PORT = 5173
```

## 开发指南

查看 [CLAUDE.md](./CLAUDE.md) 获取详细的开发指南和架构说明。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

- 邮箱: 13363581668@163.com
- GitHub: [@heycckz](https://github.com/heycckz)