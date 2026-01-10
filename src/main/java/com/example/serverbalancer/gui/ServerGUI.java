package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ServerGUI {

    private final ServerManager manager;

    public ServerGUI(ServerManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Server Manager");

        int slot = 0;
        for (String server : manager.getServers()) {
            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§a" + server);
            meta.setLore(List.of(
                    "§7Click to teleport",
                    "§8Server: " + server
            ));

            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    public void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        String name = item.getItemMeta().getDisplayName();
        if (name == null) return;

        String serverName = name.replace("§a", "");
        manager.teleport(player, serverName);
    }
}
