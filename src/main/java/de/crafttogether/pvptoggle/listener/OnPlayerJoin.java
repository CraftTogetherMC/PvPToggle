package de.crafttogether.pvptoggle.listener;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.pvplist.PvPListSQL;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;


public class OnPlayerJoin implements Listener {

    @EventHandler
    public void joinPlayer(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();
        if (!plugin.pvplist.equalsPlayerUuid(player.getUniqueId())) {

            MySQLAdapter.MySQLConnection connection = MySQLAdapter.getAdapter().getConnection();
            connection.queryAsync("SELECT `uuid` FROM `%s`.`%s` WHERE `uuid` = '%s'", (err, result) -> {
                if (err != null) {
                    plugin.getLogger().warning("[MySQL]: " + err.getMessage());
                }

                plugin.updateAllProxyCachesCommand(player);

                try {
                    if (!result.next())
                        connection.update("INSERT INTO `%s`.`%s` (`id`, `playername`, `uuid`, `pvpstate`) VALUES (NULL, '%s', '%s', '0')",
                                PvPTogglePlugin.getPreloadConfig().getString("MySQL.Database"), connection.getTablePrefix() + "pvplist",  player.getName(), player.getUniqueId());

                } catch (SQLException ex) {
                    plugin.getLogger().warning(ex.getMessage());
                }
                connection.close();

            }, PvPTogglePlugin.getPreloadConfig().getString("MySQL.Database"), connection.getTablePrefix() + "pvplist", player.getUniqueId());

            plugin.pvplist.add(player.getUniqueId());
        }

        PvPListSQL.updateStateFromDatabase(player.getUniqueId());
        PvPListSQL.updateTimestampFromDatabase(player.getUniqueId());

    }
}