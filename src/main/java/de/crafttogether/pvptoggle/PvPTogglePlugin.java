package de.crafttogether.pvptoggle;

import de.crafttogether.pvptoggle.commands.PvpCommand;
import de.crafttogether.pvptoggle.commands.PvpListCommand;
import de.crafttogether.pvptoggle.listener.OnPlayerAttacked;
import de.crafttogether.pvptoggle.listener.OnPlayerJoinLeave;
import de.crafttogether.pvptoggle.listener.OnPluginMessageReceived;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import de.crafttogether.pvptoggle.util.MySQLAdapter.MySQLConfig;
import de.crafttogether.pvptoggle.util.MySQLAdapter.MySQLConnection;
import de.crafttogether.pvptoggle.util.Util;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/*
	Dieses Plugin wurde von Kaai für CT enwtickelt.
 */

public class PvPTogglePlugin extends JavaPlugin {
    private static PvPTogglePlugin plugin;
    private static FileConfiguration config;
    private static MySQLAdapter mySQL;
    private static final ArrayList<UUID> pvpList = new ArrayList<>();

    @Override
    public void onEnable() {

        Arrays.asList(
                        "com.zaxxer.hikari.pool.PoolBase", "com.zaxxer.hikari.pool.HikariPool",
                        "com.zaxxer.hikari.HikariDataSource", "com.zaxxer.hikari.HikariConfig",
                        "com.zaxxer.hikari.util.DriverDataSource")
                .forEach(s -> Logger.getLogger(s).setLevel(Level.OFF));

        plugin = this;

        Objects.requireNonNull(getCommand("pvp")).setExecutor(new PvpCommand());
        Objects.requireNonNull(getCommand("pvplist")).setExecutor(new PvpListCommand());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new OnPlayerAttacked(), this);
        pluginManager.registerEvents(new OnPlayerJoinLeave(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new OnPluginMessageReceived());


        getConfig();
        saveDefaultConfig();
        config = preloadConfig(getConfig());

        setupMySql(config);
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);

        if (mySQL != null)
            mySQL.disconnect();
    }

    private void setupMySql(Configuration config) {

        if (getConfig().getBoolean("Settings.Debug"))
            getLogger().info("[MySQL]: Initialize Adapter...");

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

        mySQL = new MySQLAdapter(myCfg);

        if (getConfig().getBoolean("Settings.Debug"))
            getLogger().info("[MySQL]: Create Tables ...");

        MySQLConnection connection = mySQL.getConnection();
        try {
            String query = "CREATE TABLE IF NOT EXISTS `" + myCfg.getDatabase() + "`.`pvplist` (" +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36) NOT NULL, " +
                    "`playername` VARCHAR(16) NOT NULL, " +
                    "`pvp` BOOLEAN NULL DEFAULT NULL, " +
                    "PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;";

            connection.execute(query);

        } catch (SQLException ex) {
            getLogger().warning("[MySQL]: " + ex.getMessage());
        } catch (Throwable ex) {
            getLogger().warning("[EX]: " + ex.getMessage());
        } finally {
            connection.close();
        }
    }

    private FileConfiguration preloadConfig(FileConfiguration config) {
        String[] paths = {
                "Message.PvP_List",
                "Message.PvP_NotFound",
                "Message.PvP_Usage",
                "Message.PvP_Wrong_Command",
                "Message.PvP_NoPerm",
                "Message.PvP_Nobody"
        };

        for (String path : paths) {
            config.set(path, Util.format(config.getString(path)));
        }
        return config;
    }

    public static FileConfiguration getPreloadConfig() {
        return config;
    }

    public static ArrayList<UUID> getPvpList() {
        return pvpList;
    }

    public static PvPTogglePlugin getInstance() {
        return plugin;
    }

    public static MySQLAdapter getMySQL() {
        return mySQL;
    }
}
