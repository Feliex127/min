package com.feliex.serverbalancer.manager;

public class WorldSettings {
    private final String name;
    private boolean pvp = true;
    private boolean sharedInventory = false;

    public WorldSettings(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isPvp() {
        return pvp;
    }

    public boolean isSharedInventory() {
        return sharedInventory;
    }

    public void togglePvp() {
        pvp = !pvp;
    }

    public void toggleSharedInventory() {
        sharedInventory = !sharedInventory;
    }
}
