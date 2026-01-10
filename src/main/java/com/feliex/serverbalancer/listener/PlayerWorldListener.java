package com.feliex.serverbalancer.listener;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerWorldListener implements Listener {

    private final ServerManager manager;

    public PlayerWorldListener(ServerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        String worldName = e.getTo().getWorld().getName();

        // 使用 manager.teleport 套用設定
        manager.teleport(player, worldName);
    }
}
