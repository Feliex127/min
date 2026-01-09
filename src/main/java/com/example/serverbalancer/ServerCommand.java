package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {

    private final ServerManager manager;

    public ServerCommand(ServerManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            ServerGUI.open(player, manager);
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && args.length == 2) {
            manager.createServer(args[1]);
            player.sendMessage("§a已建立分流: " + args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
            manager.deleteServer(args[1]);
            player.sendMessage("§c已刪除分流: " + args[1]);
            return true;
        }

        return true;
    }
}
