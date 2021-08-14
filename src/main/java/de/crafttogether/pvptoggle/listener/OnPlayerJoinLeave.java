package de.crafttogether.pvptoggle.listener;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Logger;


public class OnPlayerJoinLeave implements Listener {

    private final FileConfiguration config;

    public OnPlayerJoinLeave() {
        config = PvPTogglePlugin.getPreloadConfig();
    }

    @EventHandler
    public void leavePlayer(PlayerQuitEvent e) throws Throwable {

    }

    @EventHandler
    public void joinPlayer(PlayerJoinEvent e) throws Throwable {
        Player player = e.getPlayer();

        MySQLAdapter.MySQLConnection mySqlConnection = MySQLAdapter.getAdapter().getConnection();


        mySqlConnection.queryAsync("SELECT `uuid` FROM `?`.`?` WHERE `uuid` = '?'", (err, result) -> {
            if (err != null) {
                PvPTogglePlugin.getInstance().getLogger().warning("[MySQL]: " + err.getMessage());
            }

            try {
                if (!result.next())
                    mySqlConnection.update("INSERT INTO `?`.`?` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '?', '?', '0')",
                            config.getString("MySQL.Database"), "pvplist", player.getUniqueId(), player.getName());

            } catch (Throwable ex) {
                PvPTogglePlugin.getInstance().getLogger().warning(ex.getMessage());
            }


        }, PvPTogglePlugin.getPreloadConfig(), "pvplist", player.getUniqueId()).close();
    }
}