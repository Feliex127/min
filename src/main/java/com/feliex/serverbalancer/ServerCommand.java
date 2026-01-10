package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {

    private final ServerGUI gui;

    public ServerCommand(ServerGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        gui.open(player); // 只傳 player
        return true;
    }
}
