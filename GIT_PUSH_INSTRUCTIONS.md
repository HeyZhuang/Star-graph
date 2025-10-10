# Git 推送指令

## 当前状态

✅ Git 仓库已初始化
✅ 远程仓库已配置：https://github.com/HeyZhuang/Star-graph.git
✅ 分支已创建：
  - `master` - 主分支（基础代码）
  - `feature/third-party-ai-api` - 功能分支（通义万相集成）⭐

✅ 代码已提交到本地仓库

## 推送到 GitHub

### 方式一：使用 HTTPS（需要 GitHub 账号密码或 Token）

```bash
# 1. 推送主分支
git push -u origin master

# 2. 推送功能分支
git push -u origin feature/third-party-ai-api
```

### 方式二：使用 SSH（推荐，需要配置 SSH Key）

```bash
# 1. 修改远程仓库地址为 SSH
git remote set-url origin git@github.com:HeyZhuang/Star-graph.git

# 2. 推送主分支
git push -u origin master

# 3. 推送功能分支
git push -u origin feature/third-party-ai-api
```

### SSH Key 配置（如果尚未配置）

```bash
# 1. 生成 SSH Key
ssh-keygen -t ed25519 -C "your_email@example.com"

# 2. 查看公钥
cat ~/.ssh/id_ed25519.pub

# 3. 复制公钥内容，添加到 GitHub
# 访问：https://github.com/settings/keys
# 点击 "New SSH key"，粘贴公钥

# 4. 测试连接
ssh -T git@github.com
```

## 后续操作

### 合并到主分支（完成开发后）

```bash
# 1. 切换到主分支
git checkout master

# 2. 合并功能分支
git merge feature/third-party-ai-api

# 3. 推送合并后的主分支
git push origin master
```

### 创建 Pull Request（推荐）

1. 推送功能分支到 GitHub
2. 访问：https://github.com/HeyZhuang/Star-graph/pulls
3. 点击 "New pull request"
4. 选择 base: `master` ← compare: `feature/third-party-ai-api`
5. 填写 PR 描述，提交审核
6. 审核通过后合并

## 验证推送成功

推送后访问 GitHub 仓库检查：
- https://github.com/HeyZhuang/Star-graph
- https://github.com/HeyZhuang/Star-graph/tree/feature/third-party-ai-api

## 常见问题

### 1. Permission denied (publickey)

**原因**：SSH Key 未配置或未添加到 GitHub

**解决方案**：按照上面的 SSH Key 配置步骤操作，或使用 HTTPS 方式

### 2. Authentication failed (HTTPS)

**原因**：GitHub 已禁用密码认证

**解决方案**：
1. 创建 Personal Access Token：https://github.com/settings/tokens
2. 使用 Token 代替密码进行 push

### 3. fatal: refusing to merge unrelated histories

**原因**：本地和远程仓库历史不一致

**解决方案**：
```bash
git pull origin master --allow-unrelated-histories
git push origin master
```

## 下一步

推送成功后，你可以：

1. **启动项目测试**：
   ```bash
   cd star-graph/
   mvn spring-boot:run
   ```

2. **配置通义万相 API Key**：
   ```bash
   export TONGYI_API_KEY=sk-your-api-key-here
   ```

3. **查看完整文档**：
   - `TONGYI_INTEGRATION.md` - 通义万相集成指南
   - `CLAUDE.md` - 项目开发指南
   - `README.md` - 项目说明

祝开发顺利！🚀
