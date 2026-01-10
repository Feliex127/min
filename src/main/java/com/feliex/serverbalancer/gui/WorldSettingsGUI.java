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
    private final WorldSettings ws;

    public WorldSettingsGUI(ServerManager manager, WorldSettings ws) {
        this.manager = manager;
        this.ws = ws;
    }

    public WorldSettingsGUI(ServerManager manager) {
        this.manager = manager;
        this.ws = null;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack pvpItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName("§aPvP: " + (ws.isPvp() ? "開啟" : "關閉"));
        pvpItem.setItemMeta(pvpMeta);
        inv.setItem(11, pvpItem);

        ItemStack sharedInv = new ItemStack(Material.CHEST);
        ItemMeta chestMeta = sharedInv.getItemMeta();
        chestMeta.setDisplayName("§a背包共享: " + (ws.isSharedInventory() ? "開啟" : "關閉"));
        sharedInv.setItemMeta(chestMeta);
        inv.setItem(15, sharedInv);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || ws == null) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.DIAMOND_SWORD) {
            ws.togglePvp();
            open(p);
        }

        if (item.getType() == Material.CHEST) {
            ws.toggleSharedInventory();
            open(p);
        }
    }
}
