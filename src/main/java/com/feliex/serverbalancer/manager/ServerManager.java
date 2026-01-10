package com.feliex.serverbalancer.manager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerManager {

    private final Map<String, WorldSettings> worlds = new HashMap<>();

    public ServerManager() {
        for (World world : Bukkit.getWorlds()) {
            worlds.put(world.getName(), new WorldSettings(world.getName()));
        }
    }

    public Set<String> getServers() {
        return worlds.keySet();
    }

    public void createServer(String name) {
        worlds.put(name, new WorldSettings(name));
        // 這裡可以添加創建世界的邏輯
    }

    public void deleteServer(String name) {
        worlds.remove(name);
    }

    public void teleport(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) player.teleport(world.getSpawnLocation());
    }

    public WorldSettings getWorldSettings(String name) {
        return worlds.get(name);
    }
}
