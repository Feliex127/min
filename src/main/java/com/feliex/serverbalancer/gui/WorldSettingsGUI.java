package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import com.feliex.serverbalancer.manager.WorldSettings;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class WorldSettingsGUI implements Listener {

    private final ServerManager manager;
    private final String world;

    public WorldSettingsGUI(ServerManager manager, String world) {
        this.manager = manager;
        this.world = world;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, "§8世界設定");
        WorldSettings ws = manager.getSettings(world);

        inv.setItem(3, toggle(Material.DIAMOND_SWORD, "PVP", ws.isPvp()));
        inv.setItem(5, toggle(Material.CHEST, "背包共享", ws.isSharedInventory()));

        Bukkit.getPluginManager().registerEvents(this,
                Bukkit.getPluginManager().getPlugin("ServerBalancer"));

        p.openInventory(inv);
    }

    private ItemStack toggle(Material mat, String name, boolean on) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName("§a" + name + ": " + (on ? "§a啟用" : "§c關閉"));
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§8世界設定")) return;
        e.setCancelled(true);

        WorldSettings ws = manager.getSettings(world);
        if (e.getSlot() == 3) ws.togglePvp();
        if (e.getSlot() == 5) ws.toggleSharedInventory();

        open((Player) e.getWhoClicked());
    }
}
