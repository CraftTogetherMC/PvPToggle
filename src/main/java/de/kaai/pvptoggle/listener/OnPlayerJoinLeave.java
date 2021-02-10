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

import java.sql.ResultSet;
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
		if(!config.getBoolean("Settings.Keep_PvP_After_Logout")) {
			PvPTogglePlugin.getInstance().removePvplist(e.getPlayer().getUniqueId());

			MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
			try {
				mySQL.update("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` = '" + e.getPlayer().getUniqueId() + "'");
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		}
	}

	@EventHandler
	public void joinPlayer(PlayerJoinEvent e) {

		Player user = e.getPlayer();

		MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();

		if(mySQL == null) {
			PvPTogglePlugin.getInstance().getLogger().info("Der PvP Status von " + user.getName() + " konnte nicht ermittelt werden!");
			return;
		}

		/*
		try {
			ResultSet result = mySQL.query("SELECT `uuid` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + user.getUniqueId() + "'");//, ((result, thrown) -> {
			System.out.println(("Result closed: " + (result.isClosed() ? "Ja" : "Nein")));
			if(result.next() ) {
				PvPTogglePlugin.getInstance().getLogger().info("Bin schon drin! UUID = " + result.getString("uuid"));
			} else {
				PvPTogglePlugin.getInstance().getLogger().info("Ich Fehle! Moment.. Ich trag mich ein..");
				mySQL.update("INSERT INTO `"+ config.getString("MySQL.Database") + "`.`pvplist` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '" + user.getUniqueId() + "', '" + user.getName() + "', '0')");
				PvPTogglePlugin.getInstance().getLogger().info("Hab mich eingetragen.");
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		*/
		mySQL.queryAsync(("SELECT `uuid` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + user.getUniqueId() + "'"), ((result, thrown) -> {

			if(thrown == null) {
				try {
					if(result.next() ) {
						PvPTogglePlugin.getInstance().getLogger().info("Bin schon drin!");
					} else {
						mySQL.update("INSERT INTO `"+ config.getString("MySQL.Database") + "`.`pvplist` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '" + user.getUniqueId() + "', '" + user.getName() + "', '0')");
					}
				} catch (SQLException exception) {
					exception.printStackTrace();
				}
			} else {
				thrown.printStackTrace();
			}

			// TODO: Close ResultSet(?)
			try {
				result.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}));
	}
}