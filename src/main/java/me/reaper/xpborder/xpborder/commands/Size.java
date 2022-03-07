package me.reaper.xpborder.xpborder.commands;

import me.reaper.xpborder.xpborder.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class Size implements CommandExecutor {
    private Main main;

    public Size(Main ourMain) {
        main = ourMain;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Gib die Größe ein!");
            return true;
        }
        int size = Integer.parseInt(args[0]);
        sender.sendMessage(ChatColor.RED + "Setting standard Bordersize to " + ChatColor.GREEN + size);
        main.getConfig().set("startSize", size);
        main.getConfig().set("size", size);
        main.saveConfig();
        return true;
    }
}
