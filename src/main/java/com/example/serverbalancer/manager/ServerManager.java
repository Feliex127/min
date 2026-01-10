package com.feliex.serverbalancer.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ServerManager {

    private final Set<String> servers = new HashSet<>();

    public void createServer(String name) {
        servers.add(name);
    }

    public void deleteServer(String name) {
        servers.remove(name);
    }

    public Set<String> getServers() {
        return servers;
    }

    public void teleport(Player player, String serverName) {
        World world = Bukkit.getWorld(serverName);

        if (world == null) {
            player.sendMessage("§cServer not found: " + serverName);
            return;
        }

        Location spawn = world.getSpawnLocation();
        player.teleport(spawn);
        player.sendMessage("§aTeleported to " + serverName);
    }
}
