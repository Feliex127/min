package com.example.serverbalancer.gui;

import com.example.serverbalancer.manager.ServerManager;
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

    // 建立 GUI
    private void createInventory() {
        inv = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Server GUI");

        // 創建按鈕 - Create Server
        ItemStack create = new ItemStack(Material.GREEN_WOOL);
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.AQUA + "Create Server");
        List<String> createLore = new ArrayList<>();
        createLore.add(ChatColor.GRAY + "Click to create a new server");
        createMeta.setLore(createLore);
        create.setItemMeta(createMeta);
        inv.setItem(0, create);

        // 創建按鈕 - Delete Server
        ItemStack delete = new ItemStack(Material.RED_WOOL);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + "Delete Server");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add(ChatColor.GRAY + "Click to delete a server");
        deleteMeta.setLore(deleteLore);
        delete.setItemMeta(deleteMeta);
        inv.setItem(1, delete);

        // 創建按鈕 - Teleport
        ItemStack tp = new ItemStack(Material.BLUE_WOOL);
        ItemMeta tpMeta = tp.getItemMeta();
        tpMeta.setDisplayName(ChatColor.BLUE + "Teleport to Server");
        List<String> tpLore = new ArrayList<>();
        tpLore.add(ChatColor.GRAY + "Click to teleport");
        tpMeta.setLore(tpLore);
        tp.setItemMeta(tpMeta);
        inv.setItem(2, tp);

        // 這裡可以繼續添加更多按鈕
    }

    // 開啟 GUI
    public void open() {
        player.openInventory(inv);
    }

    // 點擊事件
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Server GUI")) {
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
}
