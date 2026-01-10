package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import com.feliex.serverbalancer.manager.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ServerGUI implements Listener {

    private static final String TITLE = "§8分流管理";
    private final ServerManager manager;

    public ServerGUI(ServerManager manager) {
        this.manager = manager;
    }

    public ServerManager getManager() {
        return manager;
    }

    /**
     * 打開 GUI
     */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        int slot = 0;
        for (String serverName : manager.getServers()) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + serverName);

                WorldSettings ws = manager.getWorldSettings(serverName);
                String pvp = ws.isPvp() ? "§a已啟用" : "§c已禁用";
                String shared = ws.isSharedInventory() ? "§a已啟用" : "§c已禁用";

                meta.setLore(List.of(
                        "§7左鍵: 傳送",
                        "§7右鍵: 世界設定",
                        "§7PvP: " + pvp,
                        "§7共享背包: " + shared
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        // 創建新世界按鈕
        ItemStack create = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = create.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a➕ 創建新世界");
            meta.setLore(List.of("§7點擊後請使用指令 /server create <名稱>"));
            create.setItemMeta(meta);
        }
        inv.setItem(26, create);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.EMERALD_BLOCK) {
            player.closeInventory();
            player.sendMessage("§e請輸入 /server create <世界名稱> 以創建新世界");
            return;
        }

        WorldSettings ws = manager.getWorldSettings(name);
        if (ws == null) return;

        if (e.isLeftClick()) {
            // 左鍵: 傳送
            World world = Bukkit.getWorld(name);
            if (world != null) {
                player.teleport(world.getSpawnLocation());
                player.sendMessage("§a已傳送至 " + name);
            }
        }

        if (e.isRightClick()) {
            // 右鍵: 打開世界設定 GUI
            WorldSettingsGUI settingsGUI = new WorldSettingsGUI(manager, ws);
            settingsGUI.open(player);
        }
    }
}
