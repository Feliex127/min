package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerBalancer extends JavaPlugin {

    private ServerManager manager;
    private ServerGUI gui;

    @Override
    public void onEnable() {
        manager = new ServerManager();
        gui = new ServerGUI(manager);

        getServer().getPluginManager().registerEvents(gui, this);
        getCommand("server").setExecutor(new ServerCommand(gui));
    }

    public ServerManager getManager() {
        return manager;
    }
}
