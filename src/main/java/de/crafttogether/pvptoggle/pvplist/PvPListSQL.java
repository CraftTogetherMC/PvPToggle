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
}
