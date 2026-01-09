import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.World;
package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

public class ServerGUI implements Listener {

    private static final String TITLE = "§8分流管理";
    private final ServerManager manager;

    public ServerGUI(ServerManager manager) {
        this.manager = manager;
    }

    public static void open(Player player, ServerManager manager) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        int slot = 0;
        for (String server : manager.getServers()) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + server);
            meta.setLore(java.util.List.of(
                    "§7左鍵: 傳送",
                    "§cShift+右鍵: 刪除"
            ));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        ItemStack create = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = create.getItemMeta();
        meta.setDisplayName("§a➕ 建立新分流");
        create.setItemMeta(meta);

        inv.setItem(26, create);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.EMERALD_BLOCK) {
            p.closeInventory();
            p.sendMessage("§e請輸入 /server create <名稱>");
            return;
        }

        if (e.isLeftClick()) {
            World world = Bukkit.getWorld(name);
            if (world != null)
                p.teleport(world.getSpawnLocation());
        }

        if (e.isRightClick() && e.isShiftClick()) {
            manager.deleteServer(name);
            p.sendMessage("§c已刪除: " + name);
            p.closeInventory();
        }
    }
}

