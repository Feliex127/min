package com.feliex.serverbalancer.manager;

import org.bukkit.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class ServerManager {

    private final Plugin plugin;
    private final Set<String> servers = new HashSet<>();

    public ServerManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void createServer(String name) {
        if (servers.contains(name)) return;

        WorldCreator creator = new WorldCreator(name);
        Bukkit.createWorld(creator);
        servers.add(name);
    }

    public void deleteServer(String name) {
        if (!servers.contains(name)) return;

        World world = Bukkit.getWorld(name);
        if (world != null) Bukkit.unloadWorld(world, false);

        deleteFolder(world.getWorldFolder());
        servers.remove(name);
    }

    private void deleteFolder(File file) {
        if (file.isDirectory())
            for (File f : file.listFiles()) deleteFolder(f);
        file.delete();
    }

    public Set<String> getServers() {
        return servers;
    }
}
