package com.example.serverbalancer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBalancer extends JavaPlugin implements Listener, CommandExecutor, TabCompleter, PluginMessageListener {
    
    // 主要數據結構
    private Map<String, ServerGroup> serverGroups;
    private Map<String, ServerInfo> serverStatus;
    private Map<UUID, Long> playerCooldowns;
    private Properties serverProperties;
    private boolean debugMode;
    
    // 配置路徑
    private File configFile;
    private File messagesFile;
    
    // 訊息管理器
    private MessageManager messageManager;
    
    @Override
    public void onEnable() {
        // 初始化數據結構
        serverGroups = new HashMap<>();
        serverStatus = new ConcurrentHashMap<>();
        playerCooldowns = new ConcurrentHashMap<>();
        serverProperties = new Properties();
        
        // 初始化檔案路徑
        configFile = new File(getDataFolder(), "config.yml");
        messagesFile = new File(getDataFolder(), "messages.yml");
        
        // 初始化訊息管理器
        messageManager = new MessageManager(this);
        
        // 確保資料夾存在
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // 載入配置檔案
        saveDefaultConfig();
        loadConfiguration();
        
        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(this, this);
        
        // 註冊指令
        getCommand("server").setExecutor(this);
        getCommand("server").setTabCompleter(this);
        getCommand("serverbalancer").setExecutor(this);
        getCommand("serverbalancer").setTabCompleter(this);
        
        // 註冊 BungeeCord 通道
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        
        // 啟動伺服器狀態檢查任務
        startServerStatusChecker();
        
        // 啟動冷卻清除任務
        startCooldownCleaner();
        
        // 載入歡迎訊息
        loadWelcomeMessage();
        
        getLogger().info("==========================================");
        getLogger().info("ServerBalancer 插件已成功啟用!");
        getLogger().info("版本: " + getDescription().getVersion());
        getLogger().info("作者: " + getDescription().getAuthors());
        getLogger().info("已載入 " + serverGroups.size() + " 個伺服器群組");
        getLogger().info("==========================================");
    }
    
    @Override
    public void onDisable() {
        // 儲存配置
        saveConfiguration();
        
        // 取消註冊通道
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        
        getLogger().info("ServerBalancer 插件已停用!");
    }
    
    /**
     * 載入所有配置檔案
     */
    private void loadConfiguration() {
        // 載入主配置
        reloadConfig();
        
        // 載入調試模式
        debugMode = getConfig().getBoolean("properties.debug", false);
        
        // 清除舊的群組
        serverGroups.clear();
        serverStatus.clear();
        
        // 載入伺服器群組設定
        if (getConfig().isConfigurationSection("servers")) {
            for (String groupName : getConfig().getConfigurationSection("servers").getKeys(false)) {
                ServerGroup group = new ServerGroup(groupName);
                
                List<String> servers = getConfig().getStringList("servers." + groupName + ".servers");
                String balancingMethod = getConfig().getString("servers." + groupName + ".balancing", "ROUND_ROBIN");
                int priority = getConfig().getInt("servers." + groupName + ".priority", 1);
                boolean enabled = getConfig().getBoolean("servers." + groupName + ".enabled", true);
                
                group.setServers(servers);
                group.setBalancingMethod(balancingMethod);
                group.setPriority(priority);
                group.setEnabled(enabled);
                
                serverGroups.put(groupName.toLowerCase(), group);
                
                // 初始化伺服器狀態
                for (String server : servers) {
                    serverStatus.put(server.toLowerCase(), new ServerInfo(server));
                }
                
                if (debugMode) {
                    getLogger().info("已載入伺服器群組: " + groupName + 
                                   " (伺服器數量: " + servers.size() + 
                                   ", 策略: " + balancingMethod + 
                                   ", 優先級: " + priority + ")");
                }
            }
        }
        
        // 載入伺服器屬性
        if (getConfig().isConfigurationSection("properties")) {
            for (String key : getConfig().getConfigurationSection("properties").getKeys(false)) {
                serverProperties.setProperty(key, getConfig().getString("properties." + key));
            }
        }
        
        // 載入訊息檔案
        messageManager.loadMessages();
        
        if (debugMode) {
            getLogger().info("配置檔案載入完成!");
        }
    }
    
    /**
     * 儲存配置檔案
     */
    private void saveConfiguration() {
        try {
            getConfig().save(configFile);
            if (debugMode) {
                getLogger().info("配置檔案已儲存!");
            }
        } catch (IOException e) {
            getLogger().warning("儲存配置檔案時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 載入歡迎訊息
     */
    private void loadWelcomeMessage() {
        String welcomeMessage = getConfig().getString("properties.welcome-message", 
                                                     "§6[§eServerBalancer§6] §a輸入 §e/server §a查看可用伺服器列表");
        if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
            getConfig().set("properties.welcome-message", welcomeMessage);
        }
    }
    
    /**
     * 啟動伺服器狀態檢查任務
     */
    private void startServerStatusChecker() {
        int checkInterval = getConfig().getInt("advanced.check-interval", 100);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                checkServerStatus();
            }
        }.runTaskTimer(this, 0L, checkInterval);
        
        if (debugMode) {
            getLogger().info("伺服器狀態檢查任務已啟動，間隔: " + checkInterval + " ticks");
        }
    }
    
    /**
     * 啟動冷卻清除任務
     */
    private void startCooldownCleaner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long cooldownTime = getConfig().getInt("advanced.cooldown", 3) * 1000L;
                
                playerCooldowns.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue() > cooldownTime
                );
            }
        }.runTaskTimerAsynchronously(this, 0L, 100L); // 每5秒檢查一次
        
        if (debugMode) {
            getLogger().info("冷卻清除任務已啟動");
        }
    }
    
    /**
     * 檢查伺服器狀態（透過 BungeeCord）
     */
    private void checkServerStatus() {
        if (!getConfig().getBoolean("advanced.enable-bungeecord", true)) {
            return;
        }
        
        for (String serverName : serverStatus.keySet()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ServerIP");
            out.writeUTF(serverName);
            
            // 在非同步執行緒發送請求
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player != null) {
                        player.sendPluginMessage(ServerBalancer.this, "BungeeCord", out.toByteArray());
                        break; // 只需要一個玩家發送請求
                    }
                }
            });
        }
    }
    
    /**
     * 處理玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 發送歡迎訊息
        String welcomeMessage = getConfig().getString("properties.welcome-message");
        if (welcomeMessage != null && !welcomeMessage.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', welcomeMessage));
                }
            }, 40L); // 2秒後顯示
        }
        
        // 檢查更新（僅管理員）
        if (player.hasPermission("serverbalancer.admin") && 
            getConfig().getBoolean("update-check.enabled", true)) {
            checkForUpdates(player);
        }
    }
    
    /**
     * 處理伺服器列表 Ping 事件
     */
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String motd = getConfig().getString("properties.motd", "§a分流伺服器系統\n§7使用 /server 查看可用分流");
        int maxPlayers = getConfig().getInt("properties.max-players", 100);
        
        event.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
        event.setMaxPlayers(maxPlayers);
        
        // 可以根據在線玩家數量動態調整
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        event.setNumPlayers(onlinePlayers);
    }
    
    /**
     * 處理插件訊息（來自 BungeeCord）
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        
        if (subchannel.equals("ServerIP")) {
            String serverName = in.readUTF();
            String ip = in.readUTF();
            int port = in.readUnsignedShort();
            
            // 更新伺服器狀態
            ServerInfo info = serverStatus.get(serverName.toLowerCase());
            if (info != null) {
                info.setOnline(true);
                info.setLastSeen(System.currentTimeMillis());
                info.setAddress(ip + ":" + port);
                
                if (debugMode) {
                    getLogger().info("伺服器 " + serverName + " 狀態: 在線 (" + info.getAddress() + ")");
                }
            }
        }
    }
    
    /**
     * 處理指令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("server")) {
            return handleServerCommand(sender, args);
        } else if (cmd.getName().equalsIgnoreCase("serverbalancer")) {
            return handleAdminCommand(sender, args);
        }
        return false;
    }
    
    /**
     * 處理 /server 指令
     */
    private boolean handleServerCommand(CommandSender sender, String[] args) {
        // 檢查是否為玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // 檢查權限
        if (!player.hasPermission("serverbalancer.use")) {
            player.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        // 檢查冷卻
        if (!checkCooldown(player)) {
            long cooldown = getConfig().getInt("advanced.cooldown", 3);
            player.sendMessage(messageManager.getMessage("cooldown").replace("%time%", String.valueOf(cooldown)));
            return true;
        }
        
        // 沒有參數：顯示伺服器列表
        if (args.length == 0) {
            sendServerList(player, 1);
            return true;
        }
        
        String argument = args[0].toLowerCase();
        
        // 顯示列表
        if (argument.equals("list")) {
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }
            sendServerList(player, page);
            return true;
        }
        
        // 顯示幫助
        if (argument.equals("help")) {
            sendHelp(player);
            return true;
        }
        
        // 顯示狀態
        if (argument.equals("status")) {
            if (!player.hasPermission("serverbalancer.admin")) {
                player.sendMessage(messageManager.getMessage("no-permission"));
                return true;
            }
            sendServerStatus(player);
            return true;
        }
        
        // 連接到伺服器
        connectToServer(player, argument);
        return true;
    }
    
    /**
     * 處理管理指令
     */
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("serverbalancer.reload")) {
                    sender.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                loadConfiguration();
                sender.sendMessage(messageManager.getMessage("reloaded"));
                return true;
                
            case "version":
                sender.sendMessage("§6ServerBalancer §7版本: §e" + getDescription().getVersion());
                sender.sendMessage("§7作者: §a" + String.join(", ", getDescription().getAuthors()));
                sender.sendMessage("§7支援的 Minecraft 版本: §a1.21.x");
                return true;
                
            case "debug":
                if (!sender.hasPermission("serverbalancer.admin")) {
                    sender.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                debugMode = !debugMode;
                sender.sendMessage("§7除錯模式: " + (debugMode ? "§a啟用" : "§c停用"));
                if (debugMode) {
                    sender.sendMessage("§7已載入群組: " + serverGroups.size());
                    sender.sendMessage("§7已追蹤伺服器: " + serverStatus.size());
                }
                return true;
                
            case "status":
                if (!sender.hasPermission("serverbalancer.admin")) {
                    sender.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                sendDetailedStatus(sender);
                return true;
                
            case "help":
            default:
                sendAdminHelp(sender);
                return true;
        }
    }
    
    /**
     * 檢查冷卻時間
     */
    private boolean checkCooldown(Player player) {
        if (player.hasPermission("serverbalancer.bypass")) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownTime = getConfig().getInt("advanced.cooldown", 3) * 1000L;
        UUID playerId = player.getUniqueId();
        
        if (playerCooldowns.containsKey(playerId)) {
            long lastUsed = playerCooldowns.get(playerId);
            if (currentTime - lastUsed < cooldownTime) {
                return false;
            }
        }
        
        playerCooldowns.put(playerId, currentTime);
        return true;
    }
    
    /**
     * 發送伺服器列表
     */
    private void sendServerList(Player player, int page) {
        List<ServerGroup> sortedGroups = new ArrayList<>(serverGroups.values());
        sortedGroups.sort(Comparator.comparingInt(ServerGroup::getPriority).reversed());
        
        int groupsPerPage = 6;
        int totalPages = (int) Math.ceil((double) sortedGroups.size() / groupsPerPage);
        
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        int start = (page - 1) * groupsPerPage;
        int end = Math.min(start + groupsPerPage, sortedGroups.size());
        
        // 發送標題
        player.sendMessage(messageManager.getMessage("list-header"));
        player.sendMessage("§7頁數: §e" + page + "§7/§e" + totalPages);
        player.sendMessage("");
        
        // 發送群組列表
        for (int i = start; i < end; i++) {
            ServerGroup group = sortedGroups.get(i);
            if (!group.isEnabled()) continue;
            
            String balancingText = group.getBalancingMethod().equals("ROUND_ROBIN") ? 
                messageManager.getMessage("balancing-round-robin") : 
                messageManager.getMessage("balancing-random");
            
            player.sendMessage(messageManager.getMessage("list-group")
                .replace("%group%", group.getName())
                .replace("%count%", String.valueOf(group.getServers().size()))
                .replace("%balancing%", balancingText));
            
            // 顯示伺服器狀態
            for (String server : group.getServers()) {
                ServerInfo info = serverStatus.get(server.toLowerCase());
                String status = (info != null && info.isOnline()) ? "§a●" : "§c●";
                player.sendMessage(messageManager.getMessage("list-server")
                    .replace("%server%", server)
                    .replace("%status%", status));
            }
            player.sendMessage("");
        }
        
        // 發送頁尾
        player.sendMessage(messageManager.getMessage("list-footer"));
        if (totalPages > 1) {
            player.sendMessage("§7使用 §e/server list <頁數> §7查看更多");
        }
    }
    
    /**
     * 發送幫助訊息
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6=== ServerBalancer 幫助 ===");
        player.sendMessage("§e/server §7- 顯示伺服器列表");
        player.sendMessage("§e/server list [頁數] §7- 顯示詳細列表");
        player.sendMessage("§e/server <名稱> §7- 連接至伺服器");
        player.sendMessage("§e/server help §7- 顯示此幫助");
        
        if (player.hasPermission("serverbalancer.admin")) {
            player.sendMessage("§e/server status §7- 查看伺服器狀態");
        }
    }
    
    /**
     * 發送管理員幫助
     */
    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§6=== ServerBalancer 管理指令 ===");
        sender.sendMessage("§e/serverbalancer reload §7- 重新載入設定");
        sender.sendMessage("§e/serverbalancer version §7- 顯示版本資訊");
        sender.sendMessage("§e/serverbalancer status §7- 詳細狀態資訊");
        sender.sendMessage("§e/serverbalancer debug §7- 切換除錯模式");
        sender.sendMessage("§e/serverbalancer help §7- 顯示此幫助");
    }
    
    /**
     * 發送伺服器狀態
     */
    private void sendServerStatus(Player player) {
        player.sendMessage("§6=== 伺服器狀態 ===");
        
        for (ServerGroup group : serverGroups.values()) {
            int onlineCount = 0;
            for (String server : group.getServers()) {
                ServerInfo info = serverStatus.get(server.toLowerCase());
                if (info != null && info.isOnline()) {
                    onlineCount++;
                }
            }
            
            String status = group.isEnabled() ? "§a啟用" : "§c停用";
            player.sendMessage("§e" + group.getName() + " §7- " + 
                             status + " §7| §f" + onlineCount + "§7/§f" + group.getServers().size() + " 在線");
        }
    }
    
    /**
     * 發送詳細狀態
     */
    private void sendDetailedStatus(CommandSender sender) {
        sender.sendMessage("§6=== 詳細伺服器狀態 ===");
        sender.sendMessage("§7總群組數: §e" + serverGroups.size());
        sender.sendMessage("§7追蹤伺服器: §e" + serverStatus.size());
        
        int totalOnline = 0;
        for (ServerInfo info : serverStatus.values()) {
            if (info.isOnline()) totalOnline++;
        }
        sender.sendMessage("§7在線伺服器: §a" + totalOnline + "§7/§e" + serverStatus.size());
        
        sender.sendMessage("");
        sender.sendMessage("§6群組列表:");
        
        for (ServerGroup group : serverGroups.values()) {
            sender.sendMessage("§e" + group.getName() + ":");
            sender.sendMessage("  §7策略: §f" + group.getBalancingMethod());
            sender.sendMessage("  §7優先級: §f" + group.getPriority());
            sender.sendMessage("  §7狀態: " + (group.isEnabled() ? "§a啟用" : "§c停用"));
            
            for (String server : group.getServers()) {
                ServerInfo info = serverStatus.get(server.toLowerCase());
                String status = (info != null && info.isOnline()) ? "§a在線" : "§c離線";
                String lastSeen = (info != null && info.getLastSeen() > 0) ? 
                    formatTimeAgo(info.getLastSeen()) : "未知";
                sender.sendMessage("  §7- " + server + ": " + status + " §7(最後檢查: " + lastSeen + ")");
            }
            sender.sendMessage("");
        }
    }
    
    /**
     * 連接到伺服器
     */
    private void connectToServer(Player player, String target) {
        // 檢查是否是伺服器群組
        if (serverGroups.containsKey(target)) {
            ServerGroup group = serverGroups.get(target);
            
            if (!group.isEnabled()) {
                player.sendMessage("§c此伺服器群組目前停用中!");
                return;
            }
            
            String selectedServer = group.selectServer();
            
            if (selectedServer == null) {
                player.sendMessage(messageManager.getMessage("not-found"));
                return;
            }
            
            // 檢查伺服器狀態
            ServerInfo info = serverStatus.get(selectedServer.toLowerCase());
            if (info != null && !info.isOnline() && 
                !player.hasPermission("serverbalancer.bypass")) {
                player.sendMessage("§c伺服器 " + selectedServer + " 目前離線!");
                return;
            }
            
            player.sendMessage(messageManager.getMessage("connecting").replace("%server%", selectedServer));
            connectPlayerToServer(player, selectedServer);
            return;
        }
        
        // 檢查是否是直接伺服器名稱
        for (ServerGroup group : serverGroups.values()) {
            if (group.getServers().contains(target)) {
                
                // 檢查伺服器狀態
                ServerInfo info = serverStatus.get(target.toLowerCase());
                if (info != null && !info.isOnline() && 
                    !player.hasPermission("serverbalancer.bypass")) {
                    player.sendMessage("§c伺服器 " + target + " 目前離線!");
                    return;
                }
                
                player.sendMessage(messageManager.getMessage("connecting").replace("%server%", target));
                connectPlayerToServer(player, target);
                return;
            }
        }
        
        player.sendMessage(messageManager.getMessage("not-found"));
    }
    
    /**
     * 連接玩家到伺服器
     */
    private void connectPlayerToServer(Player player, String serverName) {
        if (!getConfig().getBoolean("advanced.enable-bungeecord", true)) {
            player.sendMessage("§cBungeeCord 支援未啟用!");
            return;
        }
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        
        // 設定重試機制
        int maxRetries = getConfig().getInt("advanced.max-retries", 3);
        int retryDelay = getConfig().getInt("advanced.retry-delay", 2) * 20; // 轉換為 ticks
        
        if (getConfig().getBoolean("advanced.auto-retry", true)) {
            new BukkitRunnable() {
                private int attempts = 0;
                
                @Override
                public void run() {
                    attempts++;
                    
                    if (!player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    
                    if (attempts >= maxRetries) {
                        player.sendMessage(messageManager.getMessage("failed"));
                        this.cancel();
                        return;
                    }
                    
                    // 重新發送連接請求
                    player.sendPluginMessage(ServerBalancer.this, "BungeeCord", out.toByteArray());
                    player.sendMessage("§e嘗試重新連接... (" + attempts + "/" + maxRetries + ")");
                }
            }.runTaskTimer(this, retryDelay, retryDelay);
        }
        
        // 超時檢查
        int timeout = getConfig().getInt("advanced.connect-timeout", 5) * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendMessage("§c連接超時! 請稍後再試。");
                }
            }
        }.runTaskLater(this, timeout);
    }
    
    /**
     * 檢查更新
     */
    private void checkForUpdates(Player player) {
        // 這裡可以實現更新檢查邏輯
        // 例如: 從 GitHub API 檢查最新版本
        if (debugMode) {
            getLogger().info("為玩家 " + player.getName() + " 檢查更新...");
        }
    }
    
    /**
     * 格式化時間間隔
     */
    private String formatTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        
        if (seconds < 60) {
            return seconds + "秒前";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分鐘前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小時前";
        } else {
            return (seconds / 86400) + "天前";
        }
    }
    
    /**
     * 指令自動補全
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (cmd.getName().equalsIgnoreCase("server")) {
            if (args.length == 1) {
                // 基本指令
                completions.add("list");
                completions.add("help");
                
                // 管理員指令
                if (sender.hasPermission("serverbalancer.admin")) {
                    completions.add("status");
                }
                
                // 伺服器群組
                for (String groupName : serverGroups.keySet()) {
                    if (groupName.startsWith(args[0].toLowerCase())) {
                        completions.add(groupName);
                    }
                }
                
                // 伺服器名稱
                for (ServerGroup group : serverGroups.values()) {
                    for (String server : group.getServers()) {
                        if (server.toLowerCase().startsWith(args[0].toLowerCase())) {
                            completions.add(server);
                        }
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
                // 分頁建議
                int totalGroups = serverGroups.size();
                int groupsPerPage = 6;
                int totalPages = (int) Math.ceil((double) totalGroups / groupsPerPage);
                
                for (int i = 1; i <= totalPages; i++) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("serverbalancer")) {
            if (args.length == 1) {
                String[] adminCommands = {"reload", "version", "status", "debug", "help"};
                for (String command : adminCommands) {
                    if (command.startsWith(args[0].toLowerCase())) {
                        completions.add(command);
                    }
                }
            }
        }
        
        return completions;
    }
    
    // ========================= 內部類別 =========================
    
    /**
     * 伺服器群組類別
     */
    private static class ServerGroup {
        private final String name;
        private List<String> servers;
        private String balancingMethod;
        private int priority;
        private boolean enabled;
        private int currentIndex = 0;
        private final Random random = new Random();
        
        public ServerGroup(String name) {
            this.name = name;
            this.servers = new ArrayList<>();
            this.balancingMethod = "ROUND_ROBIN";
            this.priority = 1;
            this.enabled = true;
        }
        
        public String getName() {
            return name;
        }
        
        public List<String> getServers() {
            return servers;
        }
        
        public void setServers(List<String> servers) {
            this.servers = servers;
        }
        
        public String getBalancingMethod() {
            return balancingMethod;
        }
        
        public void setBalancingMethod(String method) {
            this.balancingMethod = method;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String selectServer() {
            if (servers.isEmpty() || !enabled) {
                return null;
            }
            
            switch (balancingMethod.toUpperCase()) {
                case "RANDOM":
                    return servers.get(random.nextInt(servers.size()));
                    
                case "LEAST_CONNECTED":
                    // 這裡可以實現基於玩家數量的選擇
                    return servers.get(currentIndex);
                    
                case "ROUND_ROBIN":
                default:
                    String server = servers.get(currentIndex);
                    currentIndex = (currentIndex + 1) % servers.size();
                    return server;
            }
        }
    }
    
    /**
     * 伺服器資訊類別
     */
    private static class ServerInfo {
        private final String name;
        private boolean online;
        private long lastSeen;
        private String address;
        private int playerCount;
        
        public ServerInfo(String name) {
            this.name = name;
            this.online = false;
            this.lastSeen = 0;
            this.address = "未知";
            this.playerCount = 0;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isOnline() {
            return online;
        }
        
        public void setOnline(boolean online) {
            this.online = online;
        }
        
        public long getLastSeen() {
            return lastSeen;
        }
        
        public void setLastSeen(long lastSeen) {
            this.lastSeen = lastSeen;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public int getPlayerCount() {
            return playerCount;
        }
        
        public void setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
        }
    }
    
    /**
     * 訊息管理器類別
     */
    private static class MessageManager {
        private final ServerBalancer plugin;
        private final Map<String, String> messages;
        
        public MessageManager(ServerBalancer plugin) {
            this.plugin = plugin;
            this.messages = new HashMap<>();
        }
        
        public void loadMessages() {
            messages.clear();
            
            // 載入預設訊息
            messages.put("player-only", "§c只有玩家可以使用此指令！");
            messages.put("no-permission", "§c你沒有權限使用此指令！");
            messages.put("reloaded", "§a設定已重新載入！");
            messages.put("connecting", "§a正在連接至 §e%server%§a...");
            messages.put("connected", "§a已成功連接至 §e%server%");
            messages.put("failed", "§c連接失敗！請稍後再試或聯繫管理員。");
            messages.put("not-found", "§c找不到指定的伺服器！使用 §f/server list §c查看可用伺服器。");
            messages.put("cooldown", "§c請等待 %time% 秒後再使用此指令！");
            
            messages.put("list-header", "§6§l=== 可用伺服器列表 ===");
            messages.put("list-footer", "§6使用指令: §e/server <伺服器名稱> §6連接至分流");
            messages.put("list-group", "§e%group% §7- §f%count%個分流 §7(%balancing%)");
            messages.put("list-server", "  %status% §7- §f%server%");
            
            messages.put("balancing-round-robin", "§a輪詢分配");
            messages.put("balancing-random", "§a隨機分配");
            
            // 嘗試從配置文件載入自定義訊息
            if (plugin.getConfig().isConfigurationSection("messages")) {
                for (String key : plugin.getConfig().getConfigurationSection("messages").getKeys(false)) {
                    String message = plugin.getConfig().getString("messages." + key);
                    if (message != null) {
                        messages.put(key, ChatColor.translateAlternateColorCodes('&', message));
                    }
                }
            }
        }
        
        public String getMessage(String key) {
            return messages.getOrDefault(key, "§c找不到訊息: " + key);
        }
        
        public String getMessage(String key, String... replacements) {
            String message = getMessage(key);
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace("%" + replacements[i] + "%", replacements[i + 1]);
                }
            }
            return message;
        }
    }
}