package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (item.getType() == Material.EMERALD_BLOCK) {
            player.closeInventory();
            player.sendMessage("§e請使用 /server create <名稱>");
            return;
        }

        if (e.isLeftClick()) {
            manager.teleport(name, player);
        }

        if (e.isRightClick() && e.isShiftClick()) {
            manager.deleteServer(name);
            player.sendMessage("§c已刪除分流: " + name);
            player.closeInventory();
        }
    }
}
