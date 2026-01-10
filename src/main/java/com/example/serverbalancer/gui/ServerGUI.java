package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ServerGUI implements Listener {

    private static final String TITLE = "§8Server Manager";
    private final ServerManager manager;

    public ServerGUI(ServerManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        int slot = 0;
        for (String server : manager.getServers()) {
            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName("§a" + server);
            meta.setLore(List.of(
                    "§7Left Click → Teleport",
                    "§cShift + Right Click → Delete"
            ));

            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String serverName = item.getItemMeta()
                .getDisplayName()
                .replace("§a", "");

        if (e.isLeftClick()) {
            manager.teleport(player, serverName);
            player.closeInventory();
        }

        if (e.isRightClick() && e.isShiftClick()) {
            manager.deleteServer(serverName);
            player.sendMessage("§cDeleted server: " + serverName);
            player.closeInventory();
        }
    }
}
