package de.kaai.pvptoggle.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.kaai.pvptoggle.Main;
import de.kaai.pvptoggle.util.Util;

public class PvpListCommand implements TabExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		FileConfiguration config = Main.getPlugin().getConfig();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!sender.hasPermission("pvptoggle.pvp.list")) {
				sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
				return false;
			}
			//Gib eine Liste mit Spieler die in der Pvp Liste auf true sind
			player.sendMessage(Util.format(config.getString("Message.PvP_List")));
			if(!Main.getPvplist().isEmpty()) {
				for (Player current : Bukkit.getOnlinePlayers()) {
					if(Main.getPvplist().contains(current.getUniqueId()) ) {
						player.sendMessage("§c" + current.getName());
					}
				}
			} else
				player.sendMessage(Util.format(config.getString("Message.PvP_Nobody")));
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		ArrayList<String> newList = new ArrayList<String>();
		return newList;
	}

}
