package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerBalancer extends JavaPlugin {

    private ServerManager serverManager;
    private ServerGUI serverGUI;

    @Override
    public void onEnable() {
        serverManager = new ServerManager();
        serverGUI = new ServerGUI(serverManager);

        getServer().getPluginManager().registerEvents(serverGUI, this);
        getCommand("server").setExecutor(new ServerCommand(serverGUI));

        getLogger().info("ServerBalancer enabled");
    }
}
