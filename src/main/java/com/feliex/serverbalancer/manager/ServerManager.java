package com.feliex.serverbalancer.manager;

import java.util.HashMap;
import java.util.Map;

public class ServerManager {

    private final Map<String, WorldSettings> worlds = new HashMap<>();

    public void addWorld(String name, WorldSettings ws) {
        worlds.put(name, ws);
    }

    public void removeWorld(String name) {
        worlds.remove(name);
    }

    public Iterable<String> getServers() {
        return worlds.keySet();
    }

    public WorldSettings getWorldSettings(String name) {
        return worlds.get(name);
    }
}
