package com.feliex.serverbalancer;

import com.feliex.serverbalancer.gui.ServerGUI;
import com.feliex.serverbalancer.manager.ServerManager;
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
        if (!(sender instanceof Player player)) return true;

        ServerManager manager = gui.getManager(); // 取得 ServerManager

        if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
            String worldName = args[1];

            if (manager.getServers().contains(worldName)) {
                player.sendMessage("§c世界已存在！");
                return true;
            }

            manager.createWorld(worldName);
            player.sendMessage("§a已創建世界: " + worldName);
            gui.open(player); // 重新打開 GUI
            return true;
        }

        gui.open(player); // 沒有帶參數就打開 GUI
        return true;
    }
}
