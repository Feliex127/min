package com.feliex.serverbalancer.manager;

public class WorldSettings {
    private boolean pvpEnabled = false;
    private boolean sharedInventory = false;

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isSharedInventory() {
        return sharedInventory;
    }

    public void setSharedInventory(boolean sharedInventory) {
        this.sharedInventory = sharedInventory;
    }
}
