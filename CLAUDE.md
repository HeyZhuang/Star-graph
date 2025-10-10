# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

Star-Graph is a full-stack AI image generation platform with real-time communication capabilities:

### Backend (Spring Boot + Java 17)
- **Location**: `star-graph/`
- **Main Application**: `src/main/java/cn/itcast/StarGraphApp.java`
- **Package Structure**: `cn.itcast.star.graph.core.*`
  - `pojo/` - Data entities (User, SgUserFund, etc.)
  - `config/` - Configuration classes (WebSocket, Redis, MyBatis)
  - `job/` - Scheduled tasks
  - `mapper/` - MyBatis mappers
  - `service/` - Business logic services
  - `controller/` - REST API controllers

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
- Integration with ComfyUI AI service

## Workflow Template System

The project uses **Freemarker templates** to generate ComfyUI workflow JSON. Key concepts:

- **Templates Location**: `star-graph/src/main/resources/templates/`
- **Workflow Generation**: Service layer populates templates with user parameters (prompt, size, steps, etc.)
- **ComfyUI Integration**: Generated workflows are sent via Retrofit HTTP client to ComfyUI API
- **Template Variables**: Common variables include `${prompt}`, `${width}`, `${height}`, `${steps}`, `${seed}`

When modifying AI generation features:
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

## Important Notes
- The project uses Chinese documentation and comments
- WebSocket connections use client ID for user identification
- STOMP protocol enables topic-based message routing (`/topic/*` and `/user/*/topic/*`)
- Auto-reconnection with 5-second delays for WebSocket stability
- **ComfyUI Communication**: Backend maintains persistent WebSocket to ComfyUI for progress updates
- **Scheduled Tasks**: Check `job/` package for background task processors (queue polling, cleanup, etc.)