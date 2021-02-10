package de.kaai.pvptoggle.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.kaai.pvptoggle.PvPTogglePlugin;
import de.kaai.pvptoggle.util.MySQLHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.kaai.pvptoggle.util.Util;

public class PvpListCommand implements TabExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (!sender.hasPermission("pvptoggle.pvp.list")) {
				sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
				return false;
			}
			//Gib eine Liste mit Spieler die in der Pvp Liste auf true sind
			player.sendMessage(Util.format(config.getString("Message.PvP_List")));
			/*
			if(!PvPTogglePlugin.getInstance().getPvplist().isEmpty()) {
				for (Player current : Bukkit.getOnlinePlayers()) {
					if(PvPTogglePlugin.getInstance().getPvplist().contains(current.getUniqueId()) ) {
						player.sendMessage("§c" + current.getName());
					}
				}
			} else
				player.sendMessage(Util.format(config.getString("Message.PvP_Nobody")));
		}*/
			MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
			if(mySQL == null) {
				player.sendMessage(Util.format("Fehler!"));
			}
			mySQL.queryAsync(("SELECT `playername` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` FROM `pvp` = 1"), ((result, thrown) -> {

				if(thrown == null) {
					try {
						if (result.next()) {
							while (result.next()) {
								player.sendMessage("§c" + result.getString("playername"));
							}
						} else {
							player.sendMessage(Util.format(config.getString("Message.PvP_Nobody")));
						}
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}

				// TODO: Close ResultSet(?)
				try {
					result.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}));
		}
		return false;
	}
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		ArrayList<String> newList = new ArrayList<String>();
		return newList;
	}
}
