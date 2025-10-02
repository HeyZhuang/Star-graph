# Star-Graph API接口文档

## 目录

1. [接口概述](#1-接口概述)
2. [认证授权](#2-认证授权)
3. [通用说明](#3-通用说明)
4. [用户接口](#4-用户接口)
5. [图像生成接口](#5-图像生成接口)
6. [视频生成接口](#6-视频生成接口)
7. [任务管理接口](#7-任务管理接口)
8. [WebSocket接口](#8-websocket接口)
9. [错误码说明](#9-错误码说明)
10. [接口示例](#10-接口示例)

## 1. 接口概述

### 1.1 基础信息

```yaml
基础URL: https://api.star-graph.com
API版本: v1
协议: HTTPS
数据格式: JSON
字符编码: UTF-8
```

### 1.2 环境说明

| 环境 | 域名 | 用途 |
|------|------|------|
| 开发环境 | dev-api.star-graph.com | 开发测试 |
| 测试环境 | test-api.star-graph.com | 集成测试 |
| 生产环境 | api.star-graph.com | 正式服务 |

### 1.3 接口分类

```yaml
接口分类:
  认证类: /api/v1/auth/*
  用户类: /api/v1/users/*
  生成类: /api/v1/generate/*
  任务类: /api/v1/tasks/*
  资源类: /api/v1/resources/*
  管理类: /api/v1/admin/*
```

## 2. 认证授权

### 2.1 登录接口

**接口地址**: `POST /api/v1/auth/login`

**请求参数**:
```json
{
  "username": "string",  // 用户名
  "password": "string"   // 密码（MD5加密）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1001,
      "username": "user123",
      "nickname": "用户123",
      "avatar": "https://cdn.star-graph.com/avatar/default.png",
      "role": "USER"
    }
  }
}
```

### 2.2 刷新Token

**接口地址**: `POST /api/v1/auth/refresh`

**请求头**:
```
Authorization: Bearer {refreshToken}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

### 2.3 登出接口

**接口地址**: `POST /api/v1/auth/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

## 3. 通用说明

### 3.1 请求头

所有需要认证的接口都需要在请求头中携带Token：

```
Authorization: Bearer {token}
Content-Type: application/json
Accept: application/json
X-Request-ID: {uuid}  // 可选，用于追踪请求
```

### 3.2 通用响应格式

```typescript
interface ApiResponse<T> {
  code: number;        // 状态码
  message: string;     // 消息提示
  data?: T;           // 响应数据
  timestamp: number;   // 时间戳
  requestId: string;   // 请求ID
}
```

### 3.3 分页参数

```typescript
interface PageRequest {
  page: number;      // 页码，从1开始
  pageSize: number;  // 每页大小，默认20
  sort?: string;     // 排序字段
  order?: 'asc' | 'desc';  // 排序方向
}

interface PageResponse<T> {
  content: T[];      // 数据列表
  totalElements: number;  // 总数量
  totalPages: number;     // 总页数
  page: number;          // 当前页码
  pageSize: number;      // 每页大小
}
```

## 4. 用户接口

### 4.1 获取用户信息

**接口地址**: `GET /api/v1/users/profile`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "user123",
    "nickname": "用户123",
    "email": "user123@example.com",
    "phone": "13800138000",
    "avatar": "https://cdn.star-graph.com/avatar/1001.png",
    "balance": 1000,
    "vipLevel": 1,
    "vipExpireTime": "2024-12-31T23:59:59",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-10-02T12:00:00"
  }
}
```

### 4.2 更新用户信息

**接口地址**: `PUT /api/v1/users/profile`

**请求参数**:
```json
{
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatar": "base64图片数据或URL"
}
```

### 4.3 获取积分余额

**接口地址**: `GET /api/v1/users/balance`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "balance": 1000,
    "freezed": 50,
    "available": 950,
    "todayUsed": 100,
    "monthUsed": 500
  }
}
```

### 4.4 获取积分记录

**接口地址**: `GET /api/v1/users/fund-records`

**请求参数**:
```
GET /api/v1/users/fund-records?page=1&pageSize=20&type=DEDUCT
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 10001,
        "type": "DEDUCT",
        "amount": -10,
        "balance": 990,
        "reason": "文生图生成",
        "taskId": "task_123456",
        "createTime": "2024-10-02T10:30:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "page": 1,
    "pageSize": 20
  }
}
```

## 5. 图像生成接口

### 5.1 文生图接口

**接口地址**: `POST /api/v1/generate/text2img`

**请求参数**:
```json
{
  "prompt": "一只可爱的猫咪在花园里玩耍",
  "negativePrompt": "低质量,模糊,变形",
  "modelName": "stable-diffusion-xl-base-1.0",
  "width": 1024,
  "height": 1024,
  "steps": 30,
  "cfgScale": 7.5,
  "seed": -1,
  "sampler": "euler",
  "scheduler": "normal",
  "denoise": 1.0,
  "batchSize": 1,
  "clientId": "optional_client_id"
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| prompt | string | 是 | 正向提示词 | - |
| negativePrompt | string | 否 | 负向提示词 | "" |
| modelName | string | 否 | 模型名称 | "stable-diffusion-xl-base-1.0" |
| width | integer | 否 | 图片宽度 | 1024 |
| height | integer | 否 | 图片高度 | 1024 |
| steps | integer | 否 | 采样步数 | 30 |
| cfgScale | number | 否 | 提示词权重 | 7.5 |
| seed | long | 否 | 随机种子 | -1 |
| sampler | string | 否 | 采样器 | "euler" |
| scheduler | string | 否 | 调度器 | "normal" |
| denoise | number | 否 | 降噪强度 | 1.0 |
| batchSize | integer | 否 | 批量数量 | 1 |

**响应示例**:
```json
{
  "code": 200,
  "message": "任务创建成功",
  "data": {
    "taskId": "t2i_20241002_123456",
    "queuePosition": 3,
    "estimatedTime": 30,
    "cost": 10
  }
}
```

### 5.2 图生图接口

**接口地址**: `POST /api/v1/generate/img2img`

**请求参数**:
```json
{
  "prompt": "将图片转换为油画风格",
  "negativePrompt": "低质量",
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "imageUrl": "https://example.com/image.jpg",
  "strength": 0.7,
  "modelName": "stable-diffusion-v1-5",
  "width": 512,
  "height": 512,
  "steps": 20,
  "cfgScale": 7,
  "seed": -1
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageBase64 | string | 二选一 | Base64编码的图片 |
| imageUrl | string | 二选一 | 图片URL |
| strength | number | 否 | 变化强度(0-1) |

### 5.3 画质提升接口

**接口地址**: `POST /api/v1/generate/upscale`

**请求参数**:
```json
{
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "imageUrl": "https://example.com/image.jpg",
  "scaleFactor": 2,
  "model": "RealESRGAN"
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scaleFactor | integer | 否 | 放大倍数(2或4) |
| model | string | 否 | 提升模型 |

### 5.4 姿势控制接口

**接口地址**: `POST /api/v1/generate/pose`

**请求参数**:
```json
{
  "prompt": "一个穿着红色裙子的女孩",
  "poseImageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "poseImageUrl": "https://example.com/pose.jpg",
  "controlType": "openpose",
  "controlStrength": 1.0,
  "modelName": "stable-diffusion-v1-5",
  "width": 512,
  "height": 768
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| controlType | string | 否 | 控制类型(openpose/canny/depth) |
| controlStrength | number | 否 | 控制强度(0-2) |

## 6. 视频生成接口

### 6.1 文生视频接口

**接口地址**: `POST /api/v1/generate/text2video`

**请求参数**:
```json
{
  "prompt": "一只猫在草地上奔跑",
  "negativePrompt": "低质量,模糊",
  "modelName": "stable-video-diffusion",
  "width": 512,
  "height": 512,
  "duration": 4,
  "fps": 8,
  "motionBucketId": 127,
  "cfgScale": 7.5,
  "steps": 25,
  "seed": -1
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| duration | integer | 否 | 视频时长(秒) | 4 |
| fps | integer | 否 | 帧率 | 8 |
| motionBucketId | integer | 否 | 运动强度(1-255) | 127 |

**响应示例**:
```json
{
  "code": 200,
  "message": "文生视频任务已提交",
  "data": {
    "taskId": "t2v_20241002_123456",
    "queuePosition": 2,
    "estimatedTime": 120,
    "duration": 4,
    "fps": 8,
    "totalFrames": 32,
    "cost": 30
  }
}
```

### 6.2 图生视频接口

**接口地址**: `POST /api/v1/generate/img2video`

**请求参数**:
```json
{
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "imageUrl": "https://example.com/image.jpg",
  "prompt": "让画面动起来",
  "modelName": "stable-video-diffusion-img2vid",
  "duration": 4,
  "fps": 8,
  "motionBucketId": 127,
  "augmentationLevel": 0.0,
  "cfgScale": 2.5,
  "steps": 25,
  "seed": -1,
  "videoLoop": false
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 | 默认值 |
|------|------|------|------|--------|
| augmentationLevel | number | 否 | 增强级别(0-1) | 0.0 |
| videoLoop | boolean | 否 | 是否循环 | false |

## 7. 任务管理接口

### 7.1 获取任务列表

**接口地址**: `GET /api/v1/tasks`

**请求参数**:
```
GET /api/v1/tasks?page=1&pageSize=20&status=COMPLETED&type=TEXT2IMG
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 任务状态(PENDING/PROCESSING/COMPLETED/FAILED) |
| type | string | 否 | 任务类型(TEXT2IMG/IMG2IMG/TEXT2VIDEO等) |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "t2i_20241002_123456",
        "type": "TEXT2IMG",
        "status": "COMPLETED",
        "progress": 100,
        "prompt": "一只可爱的猫咪",
        "results": [
          "https://cdn.star-graph.com/output/image1.png"
        ],
        "cost": 10,
        "createTime": "2024-10-02T10:00:00",
        "completeTime": "2024-10-02T10:00:30"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "page": 1,
    "pageSize": 20
  }
}
```

### 7.2 获取任务详情

**接口地址**: `GET /api/v1/tasks/{taskId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "t2i_20241002_123456",
    "type": "TEXT2IMG",
    "status": "COMPLETED",
    "progress": 100,
    "prompt": "一只可爱的猫咪在花园里玩耍",
    "negativePrompt": "低质量,模糊",
    "parameters": {
      "modelName": "stable-diffusion-xl-base-1.0",
      "width": 1024,
      "height": 1024,
      "steps": 30,
      "cfgScale": 7.5,
      "seed": 123456789
    },
    "results": [
      {
        "url": "https://cdn.star-graph.com/output/image1.png",
        "thumbnailUrl": "https://cdn.star-graph.com/output/image1_thumb.png",
        "size": 2048576,
        "width": 1024,
        "height": 1024,
        "format": "PNG"
      }
    ],
    "cost": 10,
    "queueTime": 5,
    "processTime": 25,
    "createTime": "2024-10-02T10:00:00",
    "startTime": "2024-10-02T10:00:05",
    "completeTime": "2024-10-02T10:00:30"
  }
}
```

### 7.3 取消任务

**接口地址**: `DELETE /api/v1/tasks/{taskId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "任务已取消",
  "data": {
    "taskId": "t2i_20241002_123456",
    "refund": 10
  }
}
```

### 7.4 重试任务

**接口地址**: `POST /api/v1/tasks/{taskId}/retry`

**响应示例**:
```json
{
  "code": 200,
  "message": "任务重试成功",
  "data": {
    "newTaskId": "t2i_20241002_789012",
    "queuePosition": 5
  }
}
```

## 8. WebSocket接口

### 8.1 连接建立

**连接地址**: `wss://api.star-graph.com/ws`

**连接参数**:
```javascript
const socket = new SockJS('https://api.star-graph.com/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
});
```

### 8.2 订阅主题

**订阅格式**:
```javascript
// 订阅个人消息
stompClient.subscribe('/user/queue/reply', function(message) {
  const data = JSON.parse(message.body);
  console.log('Personal message:', data);
});

// 订阅任务进度
stompClient.subscribe('/user/topic/progress', function(message) {
  const progress = JSON.parse(message.body);
  console.log('Progress:', progress);
});

// 订阅任务结果
stompClient.subscribe('/user/topic/result', function(message) {
  const result = JSON.parse(message.body);
  console.log('Result:', result);
});
```

### 8.3 消息格式

**进度消息**:
```json
{
  "type": "progress",
  "taskId": "t2i_20241002_123456",
  "progress": 50,
  "currentStep": 15,
  "totalSteps": 30,
  "message": "正在生成图像..."
}
```

**结果消息**:
```json
{
  "type": "result",
  "taskId": "t2i_20241002_123456",
  "status": "COMPLETED",
  "results": [
    {
      "url": "https://cdn.star-graph.com/output/image1.png",
      "thumbnailUrl": "https://cdn.star-graph.com/output/image1_thumb.png"
    }
  ]
}
```

**错误消息**:
```json
{
  "type": "error",
  "taskId": "t2i_20241002_123456",
  "code": "E001",
  "message": "生成失败：模型加载错误"
}
```

### 8.4 发送消息

```javascript
// 发送生成请求
stompClient.send('/app/generate', {}, JSON.stringify({
  type: 'TEXT2IMG',
  prompt: '一只可爱的猫咪'
}));

// 查询任务状态
stompClient.send('/app/task/status', {}, JSON.stringify({
  taskId: 't2i_20241002_123456'
}));
```

## 9. 错误码说明

### 9.1 系统错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数 |
| 401 | 未授权 | 重新登录获取Token |
| 403 | 权限不足 | 检查用户权限 |
| 404 | 资源不存在 | 检查请求路径 |
| 429 | 请求过于频繁 | 降低请求频率 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 502 | 网关错误 | 稍后重试 |
| 503 | 服务不可用 | 稍后重试 |

### 9.2 业务错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| E001 | 模型加载失败 | 重试或更换模型 |
| E002 | 余额不足 | 充值积分 |
| E003 | 任务队列已满 | 稍后提交 |
| E004 | 图片格式不支持 | 使用JPG/PNG格式 |
| E005 | 文件大小超限 | 压缩文件至10MB以内 |
| E006 | 敏感内容检测 | 修改提示词 |
| E007 | GPU资源不足 | 等待资源释放 |
| E008 | 任务超时 | 重新提交任务 |

## 10. 接口示例

### 10.1 完整的文生图流程

```javascript
// 1. 登录获取Token
const loginResponse = await fetch('https://api.star-graph.com/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'user123',
    password: md5('password123')
  })
});

const { token } = await loginResponse.json();

// 2. 创建生成任务
const generateResponse = await fetch('https://api.star-graph.com/api/v1/generate/text2img', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    prompt: '一只可爱的猫咪在花园里玩耍',
    width: 1024,
    height: 1024,
    steps: 30
  })
});

const { taskId } = await generateResponse.json();

// 3. 建立WebSocket连接监听进度
const socket = new SockJS('https://api.star-graph.com/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': `Bearer ${token}`
}, function(frame) {
  // 订阅进度更新
  stompClient.subscribe('/user/topic/progress', function(message) {
    const progress = JSON.parse(message.body);
    if (progress.taskId === taskId) {
      console.log(`Progress: ${progress.progress}%`);
    }
  });
  
  // 订阅结果
  stompClient.subscribe('/user/topic/result', function(message) {
    const result = JSON.parse(message.body);
    if (result.taskId === taskId) {
      console.log('Generated images:', result.results);
      // 显示生成的图片
      result.results.forEach(img => {
        displayImage(img.url);
      });
    }
  });
});

// 4. 轮询任务状态（备选方案）
const pollTaskStatus = async (taskId) => {
  const response = await fetch(`https://api.star-graph.com/api/v1/tasks/${taskId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const task = await response.json();
  
  if (task.status === 'COMPLETED') {
    console.log('Task completed:', task.results);
  } else if (task.status === 'FAILED') {
    console.error('Task failed:', task.error);
  } else {
    // 继续轮询
    setTimeout(() => pollTaskStatus(taskId), 2000);
  }
};
```

### 10.2 批量生成示例

```javascript
// 批量生成不同尺寸的图片
async function batchGenerate(prompt, sizes) {
  const tasks = [];
  
  for (const size of sizes) {
    const response = await fetch('https://api.star-graph.com/api/v1/generate/text2img', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        prompt: prompt,
        width: size.width,
        height: size.height
      })
    });
    
    const task = await response.json();
    tasks.push(task);
  }
  
  return tasks;
}

// 使用示例
const sizes = [
  { width: 512, height: 512 },
  { width: 768, height: 768 },
  { width: 1024, height: 1024 }
];

const tasks = await batchGenerate('美丽的风景画', sizes);
console.log('Created tasks:', tasks);
```

### 10.3 错误处理示例

```javascript
class ApiClient {
  constructor(token) {
    this.token = token;
    this.baseUrl = 'https://api.star-graph.com/api/v1';
  }
  
  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      ...options,
      headers: {
        'Authorization': `Bearer ${this.token}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    };
    
    try {
      const response = await fetch(url, config);
      const data = await response.json();
      
      if (data.code !== 200) {
        throw new ApiError(data.code, data.message);
      }
      
      return data.data;
    } catch (error) {
      // 错误处理
      if (error instanceof ApiError) {
        switch (error.code) {
          case 401:
            // Token过期，刷新Token
            await this.refreshToken();
            // 重试请求
            return this.request(endpoint, options);
            
          case 429:
            // 请求过于频繁，延迟重试
            await sleep(5000);
            return this.request(endpoint, options);
            
          case 'E002':
            // 余额不足
            alert('积分余额不足，请充值');
            break;
            
          default:
            console.error('API Error:', error);
        }
      }
      
      throw error;
    }
  }
}
```

## 总结

Star-Graph API提供了完整的AI内容生成服务接口，包括：

1. **RESTful API**: 标准的HTTP接口，支持各种生成任务
2. **WebSocket**: 实时推送，提供进度更新和结果通知
3. **认证授权**: JWT Token机制，安全可靠
4. **错误处理**: 完善的错误码体系，便于问题定位
5. **批量操作**: 支持批量生成，提高效率

通过这些接口，开发者可以轻松集成Star-Graph的AI生成能力到自己的应用中。