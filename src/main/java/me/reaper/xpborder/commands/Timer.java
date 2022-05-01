package me.reaper.xpborder.commands;

import me.reaper.xpborder.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class Timer implements CommandExecutor {

    private Main main;

    public Timer(Main ourMain) {
        main = ourMain;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("Gib einen befehl ein: pause, resume, ...");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "pause":
                pause(sender);
                break;
            case "resume":
                resume(sender);
                break;
            default:
                sender.sendMessage("Diesen Befehl gibt's nicht!");
                break;
        }

        return false;
    }

    private void pause(CommandSender sender) {
        if (main.isPaused) {
            sender.sendMessage("§cPlugin ist bereits Pausiert");
        } else {
            sender.sendMessage("§aPlugin ist jetzt pausiert.");
            Bukkit.getWorld("world").setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            Bukkit.getWorld("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            Bukkit.getWorld("world").setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                main.sendWorldBorder(player, Color.GREEN, Double.MAX_VALUE, main.spawnLocation);
                player.setGameMode(GameMode.SPECTATOR);
                main.isPaused = true;
            }

        }
    }

    private void resume(CommandSender sender) {
        if (!main.isPaused) {
            sender.sendMessage("§cPlugin läuft Bereits");
        } else {

            sender.sendMessage("§aPlugin läuft jetzt!");
            Bukkit.getWorld("world").setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            Bukkit.getWorld("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            Bukkit.getWorld("world").setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamemode survival @a");
            main.isPaused = false;
            for (Player player : Bukkit.getOnlinePlayers()) {

                List<String> teleportedPlayers = main.getConfig().getStringList("teleported-players");
                if (teleportedPlayers.contains(player.getUniqueId().toString())) return;
                player.getPlayer().teleport(main.spawnLocation);
                teleportedPlayers.add(player.getUniqueId().toString());
                main.getConfig().set("teleported-players", teleportedPlayers);
                Runnable runnable = () -> main.saveConfig();

            }
        }
    }
}