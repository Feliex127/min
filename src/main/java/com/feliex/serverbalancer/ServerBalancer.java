package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.gui.WorldSettingsGUI;
import com.feliex.serverbalancer.listener.PlayerWorldListener;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerBalancer extends JavaPlugin {

    private ServerManager manager;

    @Override
    public void onEnable() {
        manager = new ServerManager();
        getServer().getPluginManager().registerEvents(new PlayerWorldListener(manager), this);
        getServer().getPluginManager().registerEvents(new ServerGUI(manager), this);
        getCommand("server").setExecutor(new ServerCommand(manager, this));
    }

    public ServerManager getManager() {
        return manager;
    }
}
