package de.kaai.pvptoggle;

import de.kaai.pvptoggle.commands.PvpCommand;
import de.kaai.pvptoggle.commands.PvpListCommand;
import de.kaai.pvptoggle.listener.OnPlayerAttacked;
import de.kaai.pvptoggle.listener.OnPlayerJoinLeave;
import de.kaai.pvptoggle.util.MySQLAdapter;
import de.kaai.pvptoggle.util.MySQLAdapter.MySQLConfig;
import de.kaai.pvptoggle.util.MySQLAdapter.MySQLConnection;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/*
	Dieses Plugin wurde von Kaai für CT enwtickelt.
 */

public class PvPTogglePlugin extends JavaPlugin{
	private static PvPTogglePlugin plugin;
	private static MySQLAdapter MySQL;

	ArrayList<UUID> pvpList = new ArrayList<>();

	public void onEnable() {

		Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.OFF);

		plugin = this;

		getCommand("pvp").setExecutor(new PvpCommand());
		getCommand("pvplist").setExecutor(new PvpListCommand());

		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new OnPlayerAttacked(), this);
		pluginManager.registerEvents(new OnPlayerJoinLeave(), this);

		if(getConfig() != null)
			saveDefaultConfig();

		FileConfiguration config = getConfig();

		if (getConfig().getBoolean("Settings.Debug"))
			getLogger().info("[MySQL]: Initialize Adapter...");

		// Setup MySQLConfig
		MySQLConfig myCfg = new MySQLConfig();
		myCfg.setHost(config.getString("MySQL.Host"));
		myCfg.setPort(config.getInt("MySQL.Port"));
		myCfg.setUsername(config.getString("MySQL.Username"));
		myCfg.setPassword(config.getString("MySQL.Password"));
		myCfg.setDatabase(config.getString("MySQL.Database"));

		if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
			getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
			getServer().getPluginManager().disablePlugin(this);
		}

		// Initialize MySQLAdapter
		MySQL = new MySQLAdapter(myCfg);

		if (getConfig().getBoolean("Settings.Debug"))
			getLogger().info("[MySQL]: Create Tables ...");

		// Create Tables
		try {
			MySQLConnection conn = MySQL.getConnection();

			String query = "CREATE TABLE IF NOT EXISTS `minecraft`.`pvplist` (" +
				"`id` INT(11) NOT NULL AUTO_INCREMENT, " +
				"`uuid` VARCHAR(36) NOT NULL, " +
				"`playername` VARCHAR(16) NOT NULL, " +
				"`pvp` BOOLEAN NULL DEFAULT NULL, " +
			"PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;";

			conn.execute(query);
		}

		catch (SQLException ex) {
			getLogger().warning("[MySQL]: " + ex.getMessage());
		}

		catch (Throwable ex) {
			getLogger().warning("[EX]: " + ex.getMessage());
		}
	}

	public void onDisable() {
		if(MySQL != null)
			MySQL.disconnect();
	}

	public ArrayList<UUID> getPvpList() {
		return pvpList;
	}

	public void addPvplist(UUID playerUUID) {
		pvpList.add(playerUUID);
	}
	public void removePvplist(UUID playerUUID) {
		pvpList.remove(playerUUID);
	}

	public static PvPTogglePlugin getInstance() {
		return plugin;
	}
	public static MySQLAdapter getMySQL() {
		return MySQL;
	}
}
