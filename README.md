# ServerBalancer - Minecraft Paper 分流插件

一個用於 Minecraft Paper 1.21.8 的伺服器分流插件，支持通過 `/server` 指令連接其他分流。

## 功能特點

- ✅ 支持多個伺服器群組（lobby、survival、creative 等）
- ✅ 多種分流策略（輪詢、隨機）
- ✅ BungeeCord 兼容
- ✅ 完整的指令系統
- ✅ 可配置的 MOTD 和玩家限制
- ✅ 自動重新連接功能

## 安裝方法

### 手動安裝
1. 下載最新版本的 `ServerBalancer.jar`
2. 放入 Paper 伺服器的 `plugins/` 資料夾
3. 重啟伺服器
4. 根據需要修改 `plugins/ServerBalancer/config.yml`

### 使用 GitHub Actions
1. Fork 此專案
2. 在 Actions 頁面啟用工作流程
3. 每次推送代碼或創建 Release 時會自動構建
4. 從 Artifacts 下載構建好的 JAR 檔案

## 配置說明

編輯 `config.yml`：

```yaml
servers:
  lobby:
    balancing: ROUND_ROBIN
    servers:
      - lobby1
      - lobby2
      - lobby3
  
  survival:
    balancing: ROUND_ROBIN
    servers:
      - survival1
      - survival2

properties:
  motd: "§a分流伺服器系統"
  max-players: 100