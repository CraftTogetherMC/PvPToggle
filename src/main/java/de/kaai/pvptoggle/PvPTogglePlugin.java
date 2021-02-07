package de.kaai.pvptoggle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
	}
	
	public void onDisable() {
		
	}

	public static PvPTogglePlugin getInstance() {
		return plugin;
	}
}
