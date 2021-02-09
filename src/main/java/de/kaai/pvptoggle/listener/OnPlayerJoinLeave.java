package de.kaai.pvptoggle.listener;

import de.kaai.pvptoggle.PvPTogglePlugin;
import de.kaai.pvptoggle.util.MySQLHandler;
import de.kaai.pvptoggle.util.Util;
import jdk.vm.ci.code.site.Call;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.function.Supplier;


public class OnPlayerJoinLeave implements Listener {

	private FileConfiguration config;

	public OnPlayerJoinLeave() {
		config = PvPTogglePlugin.getInstance().getConfig();
	}
	
	@EventHandler
	public void leavePlayer(PlayerQuitEvent e) {
		// Löscht den Spieler aus der PvP Liste
		if(!config.getBoolean("Settings.Keep_PvP_After_Logout"))
			PvPTogglePlugin.getInstance().removePvplist(e.getPlayer().getUniqueId());

			if(config.getBoolean("Settings.MySQL")) {
				MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
				try {
					// Der SQL scheint nicht richtig zu sein
					mySQL.execute("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` =`" + e.getPlayer().getUniqueId() + "`");
				} catch (SQLException exception) {
					exception.printStackTrace();
				}
			}
	}

	@EventHandler
	public void joinPlayer(PlayerJoinEvent e) {


		if(config.getBoolean("Settings.MySQL")) {
			Player user = e.getPlayer();

			MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();

			if(mySQL == null) {
				user.sendMessage(Util.format("Dein PvP Status konnte nicht ermittelt werden!"));
			}

			mySQL.queryAsync("SELECT `uuid` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + user.getUniqueId() + "'", ((result, err) -> {
				if(err != null) {
					err.printStackTrace();
					return;
				}
				try {
					if(!result.next()) { // Ab hier kommt ein Fehler. "Operation not allowed after ResultSet closed". Ich check nicht warum das so ist xd
						mySQL.execute("INSERT INTO `"+ config.getString("MySQL.Database") + "`.`pvplist` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '" + user.getUniqueId() + "', '" + user.getName() + "', '0')");
					}
				} catch (SQLException exception) {
					exception.printStackTrace();
				}
			}));
		}
	}
}