package de.crafttogether.pvptoggle.util;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String format(String message, String playerName) {
        String a = message.replace("<player>", playerName);
        return ChatColor.translateAlternateColorCodes('&', a);
    }

    public static String format(String message, String playerName, String targetName) {
        String a = message.replace("<player>", playerName);
        String b = a.replace("<target>", targetName);
        return ChatColor.translateAlternateColorCodes('&', b);
    }

    public static String format(String message, String playerName, String targetName, String petName) {
        String a = message.replace("<player>", playerName);
        String b = a.replace("<target>", targetName);
        String c = b.replace("<pet>", petName);
        return ChatColor.translateAlternateColorCodes('&', c);
    }

    public static String translator(String entityName) {
        return switch (entityName) {
            case "Cat" -> "Katze";
            case "Donkey" -> "Esel";
            case "Horse" -> "Pferd";
            case "Llama" -> "Lama";
            case "Mule" -> "Maultier";
            case "Parrot" -> "Papagei";
            case "Zombie Horse" -> "Zombie Pferd";
            case "Skeleton Horse" -> "Skelett Pferd";
            default -> entityName;
        };
    }

    public static void sendPvplistFromDatabaseToPlayer(@NotNull Player player, List<String> playerList) {
        Plugin plugin = PvPTogglePlugin.getInstance();

        if (plugin.getConfig().getBoolean("Settings.Debug"))
            plugin.getLogger().info("[MySQL]: Select all playernames where pvpstate = 1 ...");

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();

        connection.queryAsync("SELECT `playername` FROM `%s` WHERE `pvpstate` = '1'", (err, result) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());

            try {
                List<String> pvplist = new ArrayList<>();
                while (result.next()) {
                    pvplist.add(result.getString("playername"));
                }
                player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_List"));

                List<String> finalPvpList = new ArrayList<>();

                if (!pvplist.isEmpty()) {
                    for (String pvplistPlayer : pvplist) {
                        for (String onlinePlayer : playerList) {
                            if (pvplistPlayer.equals(onlinePlayer))
                                finalPvpList.add(pvplistPlayer);
                        }
                    }
                    if (!finalPvpList.isEmpty()) {
                        for (String s : finalPvpList) {
                            player.sendMessage("§8-§c " + s);
                        }
                    } else {
                        player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody"));
                    }
                } else {
                    player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody"));
                }
            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            }
            connection.close();
        }, connection.getTablePrefix() + "pvplist");
    }
}
