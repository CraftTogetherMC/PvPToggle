package de.crafttogether.pvptoggle.pvplist;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class PvPList {

    private final ArrayList<PvPListEntry> pvplistEntries = new ArrayList<>();


    public void add(UUID playerUuid) {
        pvplistEntries.add(new PvPListEntry(playerUuid));
    }

    public void add(UUID playerUuid, boolean state) {
        pvplistEntries.add(new PvPListEntry(playerUuid, state));
    }

    public void add(UUID playerUuid, boolean state, long timestamp) {
        pvplistEntries.add(new PvPListEntry(playerUuid, state, timestamp));
    }

    public long checkTimestamp(UUID playerUuid) {
        // Gibt die Zeit in Millisekunden, die noch übrig sind.

        long timestampNow = System.currentTimeMillis();
        int cooldownMillis = PvPTogglePlugin.getInstance().getConfig().getInt("Settings.Cooldown_Time")*1000;

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                long differenz = timestampNow - pvPListEntry.timestamp();
                long left = cooldownMillis - differenz;

                if (left >= 0) {
                    return left;
                } else {
                    return 0;
                }
            }
        }
        // Wenn, der Spieler nicht gefunden wurde.
        return -1;
    }

    public void updateTimestamp(Player player) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        if (!plugin.getConfig().getBoolean("Settings.Cooldown"))
            return;

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.updateAsync("UPDATE %s SET `cooldownTimestamp` = '%s' WHERE `uuid` = '%s'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            plugin.updateAllProxyCachesCommand(player);
            connection.close();
        }, connection.getTablePrefix() + "pvplist", System.currentTimeMillis(), player.getUniqueId());

    }

    public void updateTimestampFromDataBase(Player player) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.queryAsync("SELECT `cooldownTimestamp` FROM `%s` WHERE `uuid` = '%s'", (err, result) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            try {
                while (result.next()) {
                    long timestamp = result.getLong("cooldownTimestamp");
                    plugin.pvplist.timestamp(player.getUniqueId(), timestamp);
                }

            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            } finally {
                connection.close();
            }
        }, connection.getTablePrefix() + "pvplist", player.getUniqueId());
    }

    public void updateState(Player player, boolean state) {
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

    public boolean toggleState(UUID playerUuid) {

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                Player player = Bukkit.getPlayer(playerUuid);
                boolean state = pvPListEntry.state(!pvPListEntry.state());
                if (state)
                    updateTimestamp(player);
                updateState(player, state);

                return state;
            }
        }
        return false;
    }

    public boolean state(UUID playerUuid, boolean state) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (state)
                    updateTimestamp(player);
                updateState(player, state);
                pvPListEntry.state(state);
                break;
            }
        }

        return state;
    }

    public boolean state(UUID playerUuid) {
        boolean state = false;

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                state = pvPListEntry.state();
                break;
            }
        }

        return state;
    }

    public void timestamp(UUID playerUuid, long timestamp) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                pvPListEntry.timestamp(timestamp);
                break;
            }
        }
    }

    public boolean equalsPlayerUuid(UUID playerUuid) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                return true;
            }
        }
        return false;
    }

}