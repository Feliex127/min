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

        // 註冊指令
        this.getCommand("server").setExecutor(new ServerCommand(gui));

        // 註冊事件
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(new WorldSettingsGUI(manager), this);
    }

    public ServerManager getManager() {
        return manager;
    }

    public ServerGUI getGui() {
        return gui;
    }
}
