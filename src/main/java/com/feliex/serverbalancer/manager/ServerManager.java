package com.feliex.serverbalancer.manager;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class ServerManager {

    private final Map<String, WorldSettings> worlds = new HashMap<>();

    public void createWorld(String name) {
        if (Bukkit.getWorld(name) != null) return;
        WorldCreator creator = new WorldCreator(name);
        creator.createWorld();
        worlds.put(name, new WorldSettings());
    }

    public Set<String> getWorlds() {
        return worlds.keySet();
    }

    public WorldSettings getSettings(String world) {
        return worlds.get(world);
    }

    public void teleport(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        WorldSettings ws = worlds.get(worldName);
        if (ws != null) {
            world.setPVP(ws.isPvp());
        }

        player.teleport(world.getSpawnLocation());
    }
}
