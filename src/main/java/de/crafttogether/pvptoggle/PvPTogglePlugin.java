package de.crafttogether.pvptoggle;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.pvptoggle.commands.PvpCommand;
import de.crafttogether.pvptoggle.commands.PvpListCommand;
import de.crafttogether.pvptoggle.commands.PvpState;
import de.crafttogether.pvptoggle.listener.OnPlayerAttacked;
import de.crafttogether.pvptoggle.listener.OnPlayerJoin;
import de.crafttogether.pvptoggle.listener.OnPluginMessageReceived;
import de.crafttogether.pvptoggle.pvplist.PvPList;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import de.crafttogether.pvptoggle.util.MySQLAdapter.MySQLConfig;
import de.crafttogether.pvptoggle.util.MySQLAdapter.MySQLConnection;
import de.crafttogether.pvptoggle.util.Util;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/*
	Dieses Plugin wurde von Kaai für CT entwickelt.
 */

public class PvPTogglePlugin extends JavaPlugin {
    private static PvPTogglePlugin plugin;
    private static FileConfiguration config;
    private static MySQLAdapter mySQL;
    public PvPList pvplist = new PvPList();

    @Override
    public void onEnable() {
        Arrays.asList(
                        "com.zaxxer.hikari.pool.PoolBase",
                        "com.zaxxer.hikari.pool.HikariPool",
                        "com.zaxxer.hikari.HikariDataSource",
                        "com.zaxxer.hikari.HikariConfig",
                        "com.zaxxer.hikari.util.DriverDataSource")
                .forEach(s -> Logger.getLogger(s).setLevel(Level.OFF));

        plugin = this;

        getConfig();
        saveDefaultConfig();
        config = preloadConfig(getConfig());

        Objects.requireNonNull(getCommand("pvp")).setExecutor(new PvpCommand());
        Objects.requireNonNull(getCommand("pvplist")).setExecutor(new PvpListCommand());
        Objects.requireNonNull(getCommand("pvpstate")).setExecutor(new PvpState());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new OnPlayerAttacked(), this);
        pluginManager.registerEvents(new OnPlayerJoin(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new OnPluginMessageReceived());

        setupMySql(config);

        setupPvplist();
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
        myCfg.setTablePrefix(config.getString("MySQL.TablePrefix"));

        if ((!myCfg.checkInputs() || myCfg.getDatabase() == null) ||
                (myCfg.getDatabase().equals("yourDatabase") &&
                myCfg.getUsername().equals("yourUsername") &&
                myCfg.getPassword().equals("yourPassword") &&
                myCfg.getTablePrefix().equals("pt_") &&
                myCfg.getPort() == 3306 &&
                myCfg.getHost().equals("127.0.0.1"))) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            mySQL = new MySQLAdapter(myCfg);

            if (getConfig().getBoolean("Settings.Debug"))
                getLogger().info("[MySQL]: Create Tables ...");

            MySQLConnection connection = mySQL.getConnection();
            try {
                String query = "CREATE TABLE IF NOT EXISTS `%s`.`%s` (" +
                        "`id` INT(11) NOT NULL AUTO_INCREMENT, " +
                        "`playername` VARCHAR(16) NOT NULL , " +
                        "`uuid` VARCHAR(36) NOT NULL , " +
                        "`pvpstate` BOOLEAN NULL DEFAULT NULL , " +
                        "`cooldownTimestamp` BIGINT(13) NULL DEFAULT NULL , " +
                        "PRIMARY KEY (`id`), INDEX (`uuid`)) ENGINE = InnoDB;";

                connection.execute(query, myCfg.getDatabase(), myCfg.getTablePrefix() + "pvplist");

            } catch (SQLException e) {
                getLogger().warning("[MySQL]: " + e.getMessage());
            } finally {
                connection.close();
            }

            if (getConfig().getBoolean("Settings.Debug"))
                getLogger().info("[MySQL]: Select uuid, pvpstate, timestamp ...");

        }
    }

    public void updateAllProxyCachesCommand(Player player) {
        if (!PvPTogglePlugin.getPreloadConfig().getBoolean("BungeeCord.Enable"))
            return;

        // update other bungeeCord connections caches
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("pvptoggle");
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF("updateCache");
            msgout.writeShort(123);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());
        player.sendPluginMessage(PvPTogglePlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public void setupPvplist () {
        MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.queryAsync("SELECT `uuid`, `pvpstate`, `cooldownTimestamp` FROM `%s`", (err, result) -> {
            if (err != null)
                getLogger().warning("[MySQL]: " + err.getMessage());
            try {
                while (result.next()) {
                    pvplist.add(UUID.fromString(result.getString("uuid")), result.getBoolean("pvpstate"), result.getLong("cooldownTimestamp"));
                }

            } catch (SQLException e) {
                PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
            } finally {
                connection.close();
            }
        }, connection.getTablePrefix() + "pvplist");
    }

    private FileConfiguration preloadConfig(FileConfiguration config) {
        String[] paths = {
                "Message.PvP_List",
                "Message.PvP_NotFound",
                "Message.PvP_Usage",
                "Message.PvP_Wrong_Command",
                "Message.PvP_NoPerm",
                "Message.PvP_Nobody",
                "Message.PvP_Error"
        };

        for (String path : paths) {
            config.set(path, Util.format(config.getString(path)));
        }
        return config;
    }

    public static FileConfiguration getPreloadConfig() {
        return config;
    }

    public static PvPTogglePlugin getInstance() {
        return plugin;
    }

    public static MySQLAdapter getMySQL() {
        return mySQL;
    }
}
