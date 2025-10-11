# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

Star-Graph is a full-stack AI image generation platform with real-time communication capabilities:

### Backend (Spring Boot + Java 11)
- **Location**: `star-graph/`
- **Main Application**: `src/main/java/cn/itcast/StarGraphApp.java`
- **Package Structure**: `cn.itcast.star.graph.core.*`
  - `pojo/` - Data entities (User, SgUserFund, etc.)
  - `config/` - Configuration classes (WebSocket, Redis, MyBatis)
  - `job/` - Scheduled tasks
  - `mapper/` - MyBatis mappers
  - `service/` - Business logic services
  - `controller/` - REST API controllers
- **Test Structure**: `src/test/java/cn/itcast/` - Test files follow naming pattern `*Test.java`

### Frontend (Vue 3 + TypeScript + Vite)
- **Location**: `star-graph-ui完整前端/star-graph-ui/`
- **Framework**: Vue 3 with Composition API
- **UI Library**: Element Plus
- **State Management**: Pinia
- **Real-time Communication**: STOMP over WebSocket

## Development Commands

### Backend (Maven)
```bash
cd star-graph/
mvn clean compile         # Compile the project
mvn spring-boot:run      # Run development server (port 8080)
mvn clean package       # Build JAR file
mvn test                 # Run all tests
mvn test -Dtest=ClassName  # Run specific test class
```

### Frontend (Node.js/npm)
```bash
cd star-graph-ui完整前端/star-graph-ui/
npm install              # Install dependencies
npm run dev             # Start development server (default port 5173)
npm run build-prod      # Build for production
npm run preview         # Preview production build
```

### Docker Environment
```bash
cd star-graph/
docker-compose -f docker-compose-env.yml up -d    # Start MySQL, Redis, ComfyUI
docker-compose -f docker-compose-env.yml down     # Stop services
docker-compose -f docker-compose-env.yml logs -f  # View logs
```

## Architecture Overview

### Real-time Communication System
The project implements a dual WebSocket architecture:
1. **Frontend ↔ Backend**: STOMP over WebSocket for user communication
2. **Backend ↔ ComfyUI**: WebSocket connection to AI image generation service

**Message Types**:
- `imageResult` - Generated image URLs
- `progress` - Generation progress updates
- `index` - Queue position updates
- `start` - Task execution start
- `execution_error` - Error notifications

### Tech Stack
**Backend**:
- Spring Boot 3.2.8 with Java 11
- MyBatis-Plus 3.5.7 for database operations
- Redis for caching and session management
- WebSocket with STOMP protocol
- MySQL 8.0 database
- Freemarker for workflow template generation
- Retrofit 2.11.0 for HTTP client (ComfyUI API)
- Hutool 5.8.28 utilities
- Redisson 3.30.0 for distributed locks
- Alibaba DashScope SDK 2.12.0 for Tongyi Wanxiang API

**Frontend**:
- Vue 3.4.x + TypeScript 5.x
- Vite 5.x build tool
- Element Plus 2.7.8 UI components
- Pinia 2.2.1 for state management
- @stomp/stompjs 7.0.0 for WebSocket communication

### Configuration
- **Environment**: Uses `.env.development` and `.env.production` files
- **Proxy Setup**: Vite dev server proxies `/dev-api` to `localhost:8080`
- **WebSocket URL**: `ws://localhost:8080/ws`
- **Docker**: ComfyUI, MySQL, and Redis services in `docker-compose-env.yml`

### Key Features
- Real-time AI image generation with progress tracking
- User queue management system
- WebSocket-based live updates
- User fund tracking and transaction records
- **Hybrid AI Architecture**: Support for both local ComfyUI and cloud-based Tongyi Wanxiang API
- **Automatic Fallback**: Intelligent service switching when primary provider fails

## AI Service Architecture

### Hybrid AI Provider System
The platform supports multiple AI image generation backends:

**Service Providers** (configured in `application.yml`):
- `comfyui` - Local ComfyUI (requires GPU)
- `tongyi` - Alibaba Tongyi Wanxiang API (cloud-based)
- `auto` - Automatic selection with fallback (priority: Tongyi → ComfyUI)

**Configuration**:
```yaml
ai:
  provider: tongyi  # comfyui | tongyi | auto
  fallback:
    enabled: true   # Auto-fallback on failure

tongyi:
  api-key: ${TONGYI_API_KEY:your-api-key-here}
```

**Service Routing**:
- `HybridText2ImageServiceImpl` acts as the router
- Routes requests based on `ai.provider` configuration
- Implements automatic fallback when enabled
- See `TONGYI_INTEGRATION.md` for detailed setup

### Workflow Template System (ComfyUI)
The project uses **Freemarker templates** to generate ComfyUI workflow JSON:

- **Templates Location**: `star-graph/src/main/resources/templates/` (e.g., `t2i.ftlh`)
- **Workflow Generation**: Service layer populates templates with user parameters (prompt, size, steps, etc.)
- **ComfyUI Integration**: Generated workflows are sent via Retrofit HTTP client to ComfyUI API
- **Template Variables**: Common variables include `${prompt}`, `${width}`, `${height}`, `${steps}`, `${seed}`

When modifying ComfyUI generation features:
1. Update the Freemarker template in `templates/`
2. Update the service method that populates template variables
3. Test the generated workflow JSON with ComfyUI API

## Database
- **MySQL** with MyBatis-Plus ORM
- **Redis** for caching, task queues, and real-time data
- **Connection Pool**: Configured via Spring Boot (HikariCP)
- **Key Redis Patterns**:
  - Task queues: `queue:*`
  - User sessions: `session:*`
  - Distributed locks: via Redisson

## Git Workflow

**Branches**:
- `main` - Production-ready code
- `master` - Legacy main branch
- `feature/*` - Feature development branches (e.g., `feature/third-party-ai-api`)

**Current Working Branch**: `feature/third-party-ai-api` (Tongyi Wanxiang integration)

When creating pull requests, the main branch to target is not explicitly set, so check with the team or use `main` as the default.

## Environment Configuration

### Backend Environment Variables
Configure sensitive data via environment variables (recommended) or `application-local.yml` (ignored by git):

```bash
export TONGYI_API_KEY=sk-your-api-key-here
```

**Important**: Never commit `application-local.yml`, `application-dev.yml`, or files with API keys to git. These are already in `.gitignore`.

### Frontend Environment Files
- `.env.development` - Development environment (proxies to `localhost:8080`)
- `.env.production` - Production environment (configure actual server URLs)

**Development** (localhost):
```env
VITE_PROXY_URL = http://localhost:8080
VITE_WS_HOST_URL = ws://localhost:8080/ws
```

**Production** (example):
```env
VITE_WS_HOST_URL = ws://your-server:8082/ws
```

## Important Notes
- The project uses Chinese documentation and comments
- WebSocket connections use client ID for user identification
- STOMP protocol enables topic-based message routing (`/topic/*` and `/user/*/topic/*`)
- Auto-reconnection with 5-second delays for WebSocket stability
- **ComfyUI Communication**: Backend maintains persistent WebSocket to ComfyUI for progress updates
- **Scheduled Tasks**: Check `job/` package for background task processors (queue polling, cleanup, etc.)
- **Environment Variables**: Set `TONGYI_API_KEY` for Tongyi Wanxiang integration (see `TONGYI_INTEGRATION.md`)
- **Security**: Database passwords and API keys should be configured via environment variables in production

## Service Layer Architecture

The service layer follows a hybrid pattern with service interfaces and multiple implementations:

**Key Services**:
- `TongyiImageService` - Alibaba Tongyi Wanxiang integration
- `Text2ImageService` - Base interface for text-to-image generation
- `HybridText2ImageServiceImpl` - Router for ComfyUI/Tongyi selection
- `Image2ImageService` - Image-to-image transformation
- `UpscaleService` - Image quality enhancement
- `PoseService` - Pose-guided generation
- `WsNoticeService` - WebSocket notification service
- `FreemarkerService` - Template rendering for ComfyUI workflows

**Service Package Structure**:
```
core/service/
├── TongyiImageService.java          # Interface
├── impl/
│   ├── TongyiImageServiceImpl.java  # Tongyi implementation
│   └── HybridText2ImageServiceImpl.java  # Service router
```

When adding new AI generation features:
1. Create service interface in `core/service/`
2. Implement in `core/service/impl/`
3. Consider supporting both local (ComfyUI) and cloud (Tongyi) providers
4. Use WebSocket (`WsNoticeService`) for real-time progress updates

## API Endpoints

**Image Generation APIs** (all under `/api/authed/1.0/`):
- `POST /t2i/propmt` - Text-to-image generation
- `POST /i2i/generate` - Image-to-image transformation
- `POST /upscale/enhance` - Image quality enhancement (2x/4x upscaling)
- `POST /pose/generate` - Pose-guided image generation
- `POST /t2v/generate` - Text-to-video generation
- `POST /i2v/generate` - Image-to-video generation

**User APIs**:
- User authentication and fund management endpoints

## Testing

**Test Execution**:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserTest
mvn test -Dtest=FreemarkerTest
mvn test -Dtest=ComfyuiApiTest
```

**Test Structure**:
- Test files are located in `star-graph/src/test/java/cn/itcast/`
- Follow naming convention: `*Test.java` (e.g., `UserTest.java`, `FreemarkerTest.java`)
- Tests typically use Spring Boot test annotations (`@SpringBootTest`)

**Key Test Files**:
- `FreemarkerTest.java` - Template generation tests
- `ComfyuiApiTest.java` - ComfyUI API integration tests
- `UserTest.java` - User service tests
- `UserFundTest.java` - Fund transaction tests