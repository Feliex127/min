package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import com.feliex.serverbalancer.manager.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WorldSettingsGUI implements Listener {

    private static final String TITLE = "§8世界設定";
    private final ServerManager manager;
    private final String worldName;

    public WorldSettingsGUI(ServerManager manager, String worldName) {
        this.manager = manager;
        this.worldName = worldName;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);
        WorldSettings ws = manager.getSettings(worldName);

        ItemStack pvpItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName("§aPvP: " + (ws.isPvp() ? "§c開啟" : "§a關閉"));
        pvpItem.setItemMeta(pvpMeta);
        inv.setItem(0, pvpItem);

        ItemStack invItem = new ItemStack(Material.CHEST);
        ItemMeta invMeta = invItem.getItemMeta();
        invMeta.setDisplayName("§a共享背包: " + (ws.isSharedInventory() ? "§c開啟" : "§a關閉"));
        invItem.setItemMeta(invMeta);
        inv.setItem(1, invItem);

        player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("ServerBalancer"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        WorldSettings ws = manager.getSettings(worldName);

        if (item.getType() == Material.DIAMOND_SWORD) {
            ws.togglePvp();
        }

        if (item.getType() == Material.CHEST) {
            ws.toggleSharedInventory();
        }

        p.closeInventory();
        open(p);
    }
}
