package de.crafttogether.pvptoggle.pvplist;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PvPListSQL {

    public static void sendPvplistFromDatabaseToPvPList(@NotNull Player player, List<String> playerList) {
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
                player.sendMessage(Objects.requireNonNull(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_List")));

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
                        player.sendMessage(Objects.requireNonNull(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody")));
                    }
                } else {
                    player.sendMessage(Objects.requireNonNull(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody")));
                }
            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            }
            connection.close();
        }, connection.getTablePrefix() + "pvplist");
    }

    public static void updateTimestampFromDataBase(UUID playerUuid) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.queryAsync("SELECT `cooldownTimestamp` FROM `%s` WHERE `uuid` = '%s'", (err, result) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            try {
                while (result.next()) {
                    long timestamp = result.getLong("cooldownTimestamp");
                    plugin.pvplist.timestamp(playerUuid, timestamp);
                }

            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            } finally {
                connection.close();
            }
        }, connection.getTablePrefix() + "pvplist", playerUuid);
    }

    public static void updateStateFromDataBase(UUID playerUuid) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.queryAsync("SELECT `pvpstate` FROM `%s` WHERE `uuid` = '%s'", (err, result) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            try {
                while (result.next()) {
                    boolean state = result.getBoolean("pvpstate");
                    plugin.pvplist.state(playerUuid, state);
                }

            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            } finally {
                connection.close();
            }
        }, connection.getTablePrefix() + "pvplist", playerUuid);
    }

    public static void updateState(Player player, boolean state) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        if (state) {
            if (plugin.getConfig().getBoolean("Settings.Debug"))
                plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 1 (enabled) ...");

            MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
            connection.updateAsync("UPDATE %s SET `pvpstate` = '1' WHERE `uuid` = '%s'", (err, affectedRows) -> {
                if (err != null)
                    plugin.getLogger().warning("[MySQL]: " + err.getMessage());
                plugin.updateAllProxyCachesCommand(player);
                connection.close();
            }, connection.getTablePrefix() + "pvplist", player.getUniqueId());
        } else {
            if (plugin.getConfig().getBoolean("Settings.Debug"))
                plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 0 (disabled) ...");

            MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();

            connection.updateAsync("UPDATE %s SET `pvpstate` = '0' WHERE `uuid` = '%s'", (err, affectedRows) -> {
                if (err != null)
                    plugin.getLogger().warning("[MySQL]: " + err.getMessage());
                if (PvPTogglePlugin.getPreloadConfig().getBoolean("BungeeCord.Enable"))
                    plugin.updateAllProxyCachesCommand(player);
                connection.close();
            }, connection.getTablePrefix() + "pvplist", player.getUniqueId());
        }
    }

    public static void updateTimestamp(Player player) {
        updateTimestamp(player, System.currentTimeMillis());
    }

    public static void updateTimestamp(Player player, long timestamp) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        if (!plugin.getConfig().getBoolean("Settings.Cooldown"))
            return;

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.updateAsync("UPDATE %s SET `cooldownTimestamp` = '%s' WHERE `uuid` = '%s'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            plugin.updateAllProxyCachesCommand(player);
            connection.close();
        }, connection.getTablePrefix() + "pvplist", timestamp, player.getUniqueId());
    }
}
