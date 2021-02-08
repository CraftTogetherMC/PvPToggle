package de.kaai.pvptoggle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.kaai.pvptoggle.util.MySQLHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.kaai.pvptoggle.commands.PvpCommand;
import de.kaai.pvptoggle.commands.PvpListCommand;
import de.kaai.pvptoggle.listener.LeavePlayer;
import de.kaai.pvptoggle.listener.OnPlayerAttacked;

/*
	Dieses Plugin wurde von Kaai für CT enwtickelt.
 */

public class PvPTogglePlugin extends JavaPlugin{
	private static PvPTogglePlugin plugin;
	
	static ArrayList<UUID> pvplist = new ArrayList<>();
	public static List<String> blockedWorlds;

	public static ArrayList<UUID> getPvplist() {
		return pvplist;
	}
	public static void addPvplist(UUID playerUUID) {
		pvplist.add(playerUUID);
	}
	public static void removePvplist(UUID playerUUID) {
		pvplist.remove(playerUUID);
	}

	String host = "";
	String port = "";
	String database = "";
	String username = "";
	String password = "";

	public void onEnable() {
		plugin = this;
		
		getCommand("pvp").setExecutor(new PvpCommand());
		getCommand("pvplist").setExecutor(new PvpListCommand());
		
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new OnPlayerAttacked(), this);	
		pluginManager.registerEvents(new LeavePlayer(), this);
		
		if(getConfig() != null) {
			saveDefaultConfig();
		}

		FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();

		if(Boolean.parseBoolean(config.getString("Settings.MySQL"))) {
			// MySQL Daten wird aus der Config geholt
			host = config.getString("MySQL.Host");
			port = config.getString("MySQL.Port");
			database = config.getString("MySQL.Database");
			username = config.getString("MySQL.Username");
			password = config.getString("MySQL.Password");

			MySQLHandler MY_SQL = new MySQLHandler(
					PvPTogglePlugin.plugin.host,
					PvPTogglePlugin.plugin.port,
					PvPTogglePlugin.plugin.database,
					PvPTogglePlugin.plugin.username,
					PvPTogglePlugin.plugin.password);

			MY_SQL.update("CREATE TABLE IF NOT EXISTS pvplist( `id` INT(255) NOT NULL AUTO_INCREMENT , `uuid` VARCHAR(255) NOT NULL , `playername` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
		}
	}
	
	public void onDisable() {
		MySQLHandler.close();
	}

	public static PvPTogglePlugin getInstance() {
		return plugin;
	}
}
