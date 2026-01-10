package com.feliex.serverbalancer.gui;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class ServerGUI implements Listener {

    private static final String TITLE = "§8分流管理";
    private final ServerManager manager;

    public ServerGUI(ServerManager manager) {
        this.manager = manager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        int slot = 0;
        for (String world : manager.getWorlds()) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + world);
            meta.setLore(java.util.List.of("§7左鍵: 傳送", "§e右鍵: 設定"));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        ItemStack create = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = create.getItemMeta();
        meta.setDisplayName("§a➕ 建立世界");
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

        if (item.getType() == Material.EMERALD_BLOCK) {
            p.closeInventory();
            p.sendMessage("§e請輸入 /server create <名稱>");
            return;
        }

        String world = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (e.isLeftClick()) manager.teleport(p, world);
        if (e.isRightClick()) new WorldSettingsGUI(manager, world).open(p);
    }
}
