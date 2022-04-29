package me.reaper.xpborder;

import me.reaper.xpborder.commands.Size;
import me.reaper.xpborder.commands.Timer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Main extends JavaPlugin implements Listener {

    public boolean isPaused = true;

    public int sec = 0;

    public double bisherigeGroesse = 0;


    public void sendWorldBorder(Player player, Color color, double size, Location centerLocation) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.world = ((CraftWorld) centerLocation.getWorld()).getHandle();
        worldBorder.c(centerLocation.getBlockX() + 0.5, centerLocation.getBlockZ() + 0.5);

        worldBorder.a(size);

        worldBorder.b(0);
        worldBorder.c(0);

        ((CraftPlayer) player).getHandle().b.a(new ClientboundInitializeBorderPacket(worldBorder));
    }

    public Location spawnLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isPaused) player.setGameMode(GameMode.SPECTATOR);
        if (!isPaused) player.setGameMode(GameMode.SURVIVAL);
        if (isPaused) return;
        if (!isPaused) ;
        List<String> teleportedPlayers = getConfig().getStringList("teleported-players");
        if (teleportedPlayers.contains(player.getUniqueId().toString())) return;
        event.getPlayer().teleport(spawnLocation);
        teleportedPlayers.add(player.getUniqueId().toString());
        getConfig().set("teleported-players", teleportedPlayers);
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> saveConfig()
        );
    }

    public void resetSpawnLocation() {
        spawnLocation = Bukkit.getWorld("world").getHighestBlockAt(0, 0).getRelative(0, 1, 0).getLocation().add(0.5, 0, 0.5);
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::resetSpawnLocation, 1);
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("borderreset").setExecutor(this);
        getCommand("timer").setExecutor(new Timer(this));
        getCommand("size").setExecutor(new Size(this));
        int startSize = getConfig().getInt("startSize");
        if (startSize == 0) getConfig().set("startSize", 1);

        sec = getConfig().getInt("seconds", 0);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            getConfig().set("seconds", sec);
            saveConfig();
        }, 60 * 20, 60 * 20);

        Bukkit.getWorld("world").setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        Bukkit.getWorld("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        Bukkit.getWorld("world").setGameRule(GameRule.RANDOM_TICK_SPEED, 0);


        Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + getTime() + ChatColor.GREEN + getTotalXPLevels() + " Total Level"));
            }

            if (isPaused) return;
            sec++;

            int size = getConfig().getInt("startSize");
            size += getTotalXPLevels();
            size = Math.max(size, getConfig().getInt("size", 0));

            if (getConfig().getInt("size") < size) {
                getConfig().set("size", size);
                Bukkit.getScheduler().runTaskAsynchronously(this,
                        () -> saveConfig()
                );
            } else {
                size = getConfig().getInt("size");
            }

            Color color = Color.BLUE;

            if (bisherigeGroesse != size) {
                color = Color.GREEN;
                bisherigeGroesse = size;
                getLogger().info("WorldBorder size changed");

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText(ChatColor.GREEN + "WorldBorder expanding"));
                }

            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                sendWorldBorder(player, Color.GREEN, size, spawnLocation);
            }

        }, 20, 20);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendWorldBorder(player, Color.GREEN, Double.MAX_VALUE, spawnLocation);
            player.setGameMode(GameMode.SPECTATOR);

        }
    }

    private String getTime() {
        int days = sec / 86400;
        int hours = (sec % 86400) / 3600;
        int minutes = ((sec % 86400) % 3600) / 60;
        int seconds = ((sec % 86400) % 3600) % 60;


        return String.format("%d T  %d H  %d Min  %d Sec    ", days, hours, minutes, seconds);

        // "0 days 0:0:12"




        /*
        numberOfDays = input / 86400;
        numberOfHours = (input % 86400 ) / 3600 ;
        numberOfMinutes = ((input % 86400 ) % 3600 ) / 60
        numberOfSeconds = ((input % 86400 ) % 3600 ) % 60  ;
         */
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {

        if (isPaused) return;

        resetSpawnLocation();

        int maxCoords = getConfig().getInt("size", 0);
        System.out.println(maxCoords);
        if (event.getRespawnLocation().getBlockX() > maxCoords / 2
                || event.getRespawnLocation().getBlockX() < -maxCoords / 2
                || event.getRespawnLocation().getBlockZ() > maxCoords / 2
                || event.getRespawnLocation().getBlockZ() < -maxCoords / 2) {
            event.setRespawnLocation(spawnLocation);
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int startSize = getConfig().getInt("startSize");
        getConfig().set("size", startSize);
        getConfig().set("teleported-players", new ArrayList<String>());
        getConfig().set("startSize", new ArrayList<String>());
        getConfig().set("startSize", startSize);


        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> saveConfig()
        );
        resetSpawnLocation();
        sec = 0;
        Bukkit.getOnlinePlayers().forEach(new Consumer<Player>() {
            @Override
            public void accept(Player player) {
                player.setLevel(0);
                player.setExp(0);
                if (!isPaused) {
                    player.teleport(spawnLocation);
                }
            }
        });
        return true;

    }

    public static int getTotalXPLevels() {
        int totalLevels = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            totalLevels += player.getLevel();
        }
        return totalLevels;
    }
}

