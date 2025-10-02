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
mvn spring-boot:run      # Run development server
mvn clean package       # Build JAR file
mvn test                 # Run tests
```

### Frontend (Node.js/npm)
```bash
cd star-graph-ui完整前端/star-graph-ui/
npm install              # Install dependencies
npm run dev             # Start development server
npm run build-prod      # Build for production
npm run preview         # Preview production build
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
- Spring Boot 3.2.8 with Java 17
- MyBatis-Plus for database operations
- Redis for caching and session management
- WebSocket with STOMP protocol
- MySQL database

**Frontend**:
- Vue 3 + TypeScript
- Vite build tool
- Element Plus UI components
- Pinia for state management
- @stomp/stompjs for WebSocket communication

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

## Database
- **MySQL** with MyBatis-Plus ORM
- **Redis** for caching and real-time data
- **Connection Pool**: Configured via Spring Boot

## Important Notes
- The project uses Chinese documentation and comments
- WebSocket connections use client ID for user identification
- STOMP protocol enables topic-based message routing (`/topic/*` and `/user/*/topic/*`)
- Auto-reconnection with 5-second delays for WebSocket stability