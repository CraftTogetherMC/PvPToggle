package de.kaai.pvptoggle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.kaai.pvptoggle.commands.PvpCommand;
import de.kaai.pvptoggle.commands.PvpListCommand;
import de.kaai.pvptoggle.listener.OnPlayerJoinLeave;
import de.kaai.pvptoggle.listener.OnPlayerAttacked;
import de.kaai.pvptoggle.util.MySQLHandler;

/*
	Dieses Plugin wurde von Kaai für CT enwtickelt.
 */

public class PvPTogglePlugin extends JavaPlugin{ // siehst du hier auch den fehler? <---- ro  t puuuuuuuh
	private static PvPTogglePlugin plugin;

	private static MySQLHandler mySQL;

	FileConfiguration config;

	ArrayList<UUID> pvplist = new ArrayList<>();
	public static List<String> blockedWorlds;

	public ArrayList<UUID> getPvplist() {
		return pvplist;
	}
	public void addPvplist(UUID playerUUID) {
		if(config == null)
			return;
		pvplist.add(playerUUID);
		// mySQL.updateAsync("", (result, err) -> {

		//});
	}
	public void removePvplist(UUID playerUUID) {
		if(config == null)
			return;
		pvplist.remove(playerUUID);
	}

	String host;
	int port;
	String database;
	String username;
	String password;

	public void onEnable() {
		plugin = this;

		getCommand("pvp").setExecutor(new PvpCommand());
		getCommand("pvplist").setExecutor(new PvpListCommand());
		
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new OnPlayerAttacked(), this);	
		pluginManager.registerEvents(new OnPlayerJoinLeave(), this);
		
		if(getConfig() != null)
			saveDefaultConfig();

		config = PvPTogglePlugin.getInstance().getConfig();

		if(config.getBoolean("Settings.MySQL")) {
			// MySQL Daten wird aus der Config geholt
			host = config.getString("MySQL.Host");
			port = config.getInt("MySQL.Port");
			database = config.getString("MySQL.Database");
			username = config.getString("MySQL.Username");
			password = config.getString("MySQL.Password");
			mySQL = new MySQLHandler(host, port, database, username, password);

			try {
				String query =
				"CREATE TABLE IF NOT EXISTS `minecraft`.`pvplist` ( `id` INT(11) NOT NULL AUTO_INCREMENT , `uuid` VARCHAR(36) NOT NULL , `playername` VARCHAR(16) NOT NULL , `pvp` BOOLEAN NULL DEFAULT NULL , PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;";
				mySQL.update(query);
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	public void onDisable() {
		FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();
		if(mySQL != null)
			mySQL.disconnect();
	}

	public static PvPTogglePlugin getInstance() {
		return plugin;
	}

	public MySQLHandler getMySQLHandler() {
		return mySQL;
	}
}
