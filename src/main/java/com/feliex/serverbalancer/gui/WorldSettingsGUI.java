package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class WorldSettingsGUI {

    private static final String TITLE = "§8世界設定";

    public static void open(Player player, String worldName, WorldSettings ws) {
        Inventory inv = Bukkit.createInventory(null, 9, TITLE);

        ItemStack pvpItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = pvpItem.getItemMeta();
        meta.setDisplayName("§aPvP: " + (ws.isPvp() ? "§a開啟" : "§c關閉"));
        pvpItem.setItemMeta(meta);

        ItemStack invItem = new ItemStack(Material.CHEST);
        meta = invItem.getItemMeta();
        meta.setDisplayName("§a背包共享: " + (ws.isSharedInventory() ? "§a開啟" : "§c關閉"));
        invItem.setItemMeta(meta);

        inv.setItem(0, pvpItem);
        inv.setItem(1, invItem);

        player.openInventory(inv);
    }
}
