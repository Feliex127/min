package com.feliex.serverbalancer.listener;

import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerWorldListener implements Listener {

    private final ServerManager manager;

    public PlayerWorldListener(ServerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        manager.teleport(e.getPlayer(), e.getPlayer().getWorld().getName());
    }
}
