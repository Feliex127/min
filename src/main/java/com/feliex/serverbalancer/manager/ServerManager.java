package com.feliex.serverbalancer.manager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerManager {

    private final Map<String, WorldSettings> worlds = new HashMap<>();

    public void createWorld(String name) {
        World world = Bukkit.createWorld(new WorldCreator(name));
        worlds.put(name, new WorldSettings());
    }

    public Set<String> getServers() {
        return worlds.keySet();
    }

    public WorldSettings getSettings(String worldName) {
        return worlds.get(worldName);
    }

    public void teleport(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            player.teleport(world.getSpawnLocation());
        }
    }

    public void deleteServer(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            worlds.remove(worldName);
        }
    }
}
