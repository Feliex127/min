package com.feliex.serverbalancer.manager;

public class WorldSettings {
    private boolean pvp = false;
    private boolean sharedInventory = false;

    public boolean isPvp() {
        return pvp;
    }

    public void togglePvp() {
        pvp = !pvp;
    }

    public boolean isSharedInventory() {
        return sharedInventory;
    }

    public void toggleSharedInventory() {
        sharedInventory = !sharedInventory;
    }
}
