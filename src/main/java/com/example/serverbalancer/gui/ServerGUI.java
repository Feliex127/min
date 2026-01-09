package com.feliex.serverbalancer.gui; // <-- 改成這個

import com.feliex.serverbalancer.manager.ServerManager; // <-- 統一包名
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

import java.util.ArrayList;
import java.util.List;

public class ServerGUI implements Listener {

    private final Player player;
    private Inventory inv;

    public ServerGUI(Player player) {
        this.player = player;
        createInventory();
    }

    private void createInventory() {
        inv = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Server GUI");

        ItemStack create = new ItemStack(Material.GREEN_WOOL);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.AQUA + "Create Server");
        createMeta.setLore(List.of(ChatColor.GRAY + "Click to create a new server"));
        create.setItemMeta(createMeta);
        inv.setItem(0, create);

        ItemStack delete = new ItemStack(Material.RED_WOOL);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Delete Server");
        deleteMeta.setLore(List.of(ChatColor.GRAY + "Click to delete a server"));
        delete.setItemMeta(deleteMeta);
        inv.setItem(1, delete);

        ItemStack tp = new ItemStack(Material.BLUE_WOOL);
        ItemMeta tpMeta = tp.getItemMeta();
        tpMeta.setDisplayName(ChatColor.BLUE + "Teleport to Server");
        tpMeta.setLore(List.of(ChatColor.GRAY + "Click to teleport"));
        tp.setItemMeta(tpMeta);
        inv.setItem(2, tp);
    }

    public void open() {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GREEN + "Server GUI")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        if (name.equals(ChatColor.AQUA + "Create Server")) {
            ServerManager.createServer(player);
        } else if (name.equals(ChatColor.RED + "Delete Server")) {
            ServerManager.deleteServer(player);
        } else if (name.equals(ChatColor.BLUE + "Teleport to Server")) {
            ServerManager.teleportServer(player);
        }
    }
}
