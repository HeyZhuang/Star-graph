# 通义万相集成指南

本文档说明如何在 Star-Graph 项目中集成和使用阿里云通义万相 API。

## 功能特性

✅ **混合架构**：支持本地 ComfyUI 和云端通义万相灵活切换
✅ **自动降级**：主服务失败时自动切换到备用服务
✅ **实时进度**：WebSocket 推送生成进度和结果
✅ **快速生成**：通义万相平均 2-4 秒完成生图
✅ **无需 GPU**：云端计算，本地无需高性能显卡

## 快速开始

### 1. 获取通义万相 API Key

访问阿里云控制台获取 API Key：
👉 https://dashscope.console.aliyun.com/apiKey

### 2. 配置 API Key

有两种方式配置 API Key：

#### 方式一：环境变量（推荐）
```bash
export TONGYI_API_KEY=sk-your-api-key-here
```

#### 方式二：修改配置文件
编辑 `star-graph/src/main/resources/application.yml`：
```yaml
tongyi:
  api-key: sk-your-api-key-here
```

### 3. 选择服务提供商

在 `application.yml` 中配置：

```yaml
ai:
  provider: tongyi  # comfyui | tongyi | auto
```

**提供商选项**：
- `comfyui` - 使用本地 ComfyUI（需要 GPU）
- `tongyi` - 使用阿里云通义万相（无需 GPU）
- `auto` - 自动选择（优先通义万相，失败回退到 ComfyUI）

### 4. 启动项目

```bash
cd star-graph/
mvn spring-boot:run
```

## 架构说明

### 服务路由流程

```
用户请求
  ↓
Text2ImageController
  ↓
HybridText2ImageServiceImpl (路由层)
  ├─→ provider=comfyui → ComfyUI 服务
  ├─→ provider=tongyi  → 通义万相服务
  └─→ provider=auto    → 智能选择
        ↓
    优先通义万相 → 失败回退 → ComfyUI
```

### 新增文件结构

```
star-graph/src/main/java/cn/itcast/star/graph/
├── tongyi/client/                    # 通义万相客户端
│   ├── api/
│   │   └── TongyiApi.java           # API 接口定义
│   ├── config/
│   │   └── TongyiConfig.java        # Retrofit 配置
│   └── pojo/
│       ├── TongyiText2ImageRequest.java
│       └── TongyiText2ImageResponse.java
└── core/service/
    ├── TongyiImageService.java      # 通义服务接口
    └── impl/
        ├── TongyiImageServiceImpl.java         # 通义服务实现
        └── HybridText2ImageServiceImpl.java    # 混合架构路由
```

## 配置说明

### 完整配置项

```yaml
# AI 服务配置
ai:
  provider: tongyi              # 服务提供商
  fallback:
    enabled: true              # 自动降级开关

# 通义万相配置
tongyi:
  api-key: ${TONGYI_API_KEY}  # API Key
  base-url: https://dashscope.aliyuncs.com/api/v1/  # API 地址
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `TONGYI_API_KEY` | 通义万相 API Key | `your-api-key-here` |

## 使用示例

### 场景一：完全使用通义万相

```yaml
ai:
  provider: tongyi
  fallback:
    enabled: false  # 关闭降级
```

### 场景二：本地开发用 ComfyUI

```yaml
ai:
  provider: comfyui
  fallback:
    enabled: false
```

### 场景三：智能混合（推荐）

```yaml
ai:
  provider: auto
  fallback:
    enabled: true
```

**工作逻辑**：
1. 优先使用通义万相（速度快）
2. 通义万相失败 → 自动切换到本地 ComfyUI
3. ComfyUI 失败 → 返回错误

### 场景四：生产环境高可用

```yaml
ai:
  provider: tongyi
  fallback:
    enabled: true  # 通义失败回退到 ComfyUI
```

## API 对比

| 特性 | ComfyUI | 通义万相 |
|------|---------|----------|
| **速度** | 10-60秒 | 2-4秒 ⚡ |
| **成本** | GPU 硬件成本 | ¥0.03/图 |
| **硬件要求** | RTX 3060+ | 无 ✅ |
| **模型选择** | 自由切换 | 固定模型 |
| **离线使用** | 支持 ✅ | 不支持 |
| **稳定性** | 依赖本地环境 | 云端高可用 ✅ |

## 定价说明

通义万相按图片数量计费：
- **标准版**：¥0.03/图
- **高清版**：¥0.05/图
- **新用户**：免费 500 次额度

详细价格：https://help.aliyun.com/zh/dashscope/developer-reference/tongyi-wanxiang-pricing

## 常见问题

### 1. 如何切换回本地 ComfyUI？

修改配置文件：
```yaml
ai:
  provider: comfyui
```

### 2. 通义万相支持哪些图片尺寸？

- 1024×1024（推荐）
- 720×1280（竖版）
- 1280×720（横版）

### 3. 如何查看 API 调用日志？

启用日志：
```yaml
logging:
  level:
    cn.itcast.star.graph.tongyi: DEBUG
```

### 4. API Key 安全问题？

**推荐做法**：
1. 使用环境变量而非配置文件
2. 不要提交包含 API Key 的配置到 Git
3. 在 `.gitignore` 中添加 `application-local.yml`

### 5. 通义万相生成失败怎么办？

查看错误日志：
```bash
tail -f logs/star-graph.log | grep "通义万相"
```

常见错误：
- `InvalidApiKey` - API Key 无效或过期
- `InsufficientBalance` - 账户余额不足
- `RequestLimitExceeded` - 请求频率超限

## 进阶使用

### 自定义积分计费

修改 `TongyiImageServiceImpl.java`：

```java
// 通义万相按实际生成数量计费
int actualCount = imageUrls.size();
userFundRecordService.pointsDeduct(userId, actualCount);
```

### 添加自定义风格

修改 `TongyiImageServiceImpl.java`：

```java
parameters.setStyle("<anime>");  // 动漫风格
parameters.setStyle("<oil painting>");  // 油画风格
```

可用风格：
- `<auto>` - 自动选择
- `<3d cartoon>` - 3D 卡通
- `<anime>` - 动漫
- `<oil painting>` - 油画
- `<watercolor>` - 水彩
- `<sketch>` - 素描
- `<chinese painting>` - 国画
- `<flat illustration>` - 扁平插画

## 测试

### 单元测试

```bash
mvn test -Dtest=TongyiImageServiceTest
```

### 手动测试

1. 启动后端服务
2. 访问前端：http://localhost:5173
3. 进入文生图页面
4. 输入提示词，点击生成
5. 观察 WebSocket 实时推送结果

## 监控建议

生产环境建议监控以下指标：
- 通义万相 API 调用成功率
- 平均响应时间
- 降级触发次数
- 用户积分消耗情况

## 技术支持

- **通义万相文档**：https://help.aliyun.com/zh/dashscope/
- **项目 Issues**：https://github.com/HeyZhuang/Star-graph/issues
- **邮箱**：13363581668@163.com

## 版本历史

- **v1.1.0** (2025-01-10) - 新增通义万相集成
- **v1.0.0** (2024-10-02) - 初始版本（仅 ComfyUI）

---

🎉 现在你可以享受更快速的 AI 图像生成服务了！
