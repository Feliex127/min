package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {

    private final ServerManager manager;
    private final ServerBalancer plugin;

    public ServerCommand(ServerManager manager, ServerBalancer plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        ServerGUI.open(player, manager);
        return true;
    }
}
