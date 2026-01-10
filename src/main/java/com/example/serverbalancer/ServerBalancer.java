package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.listener.PlayerWorldListener;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerBalancer extends JavaPlugin {

    private ServerManager manager;
    private ServerGUI gui;

    @Override
    public void onEnable() {
        manager = new ServerManager();
        gui = new ServerGUI(manager);

        getCommand("server").setExecutor(new ServerCommand(gui));
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(
                new PlayerWorldListener(manager), this
        );
    }
}
