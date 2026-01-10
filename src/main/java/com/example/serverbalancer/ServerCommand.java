package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {

    private final ServerGUI gui;

    public ServerCommand(ServerGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        gui.open(p);
        return true;
    }
}
