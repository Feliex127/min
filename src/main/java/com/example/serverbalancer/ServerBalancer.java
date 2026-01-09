package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerBalancer extends JavaPlugin {

    private static ServerBalancer instance;
    private ServerManager serverManager;

    @Override
    public void onEnable() {
        instance = this;
        serverManager = new ServerManager(this);

        getCommand("server").setExecutor(new ServerCommand(serverManager));
        getServer().getPluginManager().registerEvents(new ServerGUI(serverManager), this);
    }

    public static ServerBalancer get() {
        return instance;
    }
}
