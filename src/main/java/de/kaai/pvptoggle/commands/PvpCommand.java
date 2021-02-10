package de.kaai.pvptoggle.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.kaai.pvptoggle.util.MySQLHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import de.kaai.pvptoggle.PvPTogglePlugin;
import de.kaai.pvptoggle.util.Util;

public class PvpCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();
		FileConfiguration config = plugin.getConfig();
		
		if (sender instanceof Player) {
			UUID playerUUID = ((OfflinePlayer) sender).getPlayer().getUniqueId();
			Player player = ((Player) sender).getPlayer();
			
			//pvp
			if (args.length == 0)
			{
				if (!sender.hasPermission("pvptoggle.pvp.toggle")) {
					sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
					return false;
				}
				
				//toggle pvpmode
				if (plugin.getPvplist().contains(playerUUID)) {
					plugin.removePvplist(playerUUID);
					MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
					if(mySQL == null) {
						player.sendMessage(Util.format("Die PvP Liste kann nicht ausgegeben werden!"));
						return false;
					}
					try {
						mySQL.update("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` = '" + playerUUID + "'");
					} catch (SQLException exception) {
						exception.printStackTrace();
					}
					player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_OFF"), player.getName()));
				}
				else {
					plugin.addPvplist(playerUUID);
					MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
					try {
						mySQL.update("UPDATE `pvplist` SET `pvp` = '1' WHERE `pvplist`.`uuid` = '" + playerUUID + "'");
					} catch (SQLException exception) {
						exception.printStackTrace();
					}
					player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_ON"), player.getName()));
				}
			}
			
			else if (args.length == 1)
			{
				Player target = Bukkit.getPlayer(args[0]);
				
				//pvp <Name>
				if (sender.hasPermission("pvptoggle.pvp.other"))
				{
					if (target != null) {
						UUID targetUUID = target.getUniqueId();
						
						if (plugin.getPvplist().contains(targetUUID)) {
							plugin.removePvplist(targetUUID);
							MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
							try {
								mySQL.update("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` = '" + targetUUID + "'");
							} catch (SQLException exception) {
								exception.printStackTrace();
							}
							player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF"), player.getName(), target.getName()));
							if (player.getUniqueId() != target.getUniqueId())
								target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF_Target"), player.getName(), target.getName()));
						}
						else {
							plugin.addPvplist(targetUUID);
							MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
							try {
								mySQL.update("UPDATE `pvplist` SET `pvp` = '1' WHERE `pvplist`.`uuid` = '" + targetUUID + "'");
							} catch (SQLException exception) {
								exception.printStackTrace();
							}
							player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON"), player.getName(), target.getName()));
							if (player.getUniqueId() != target.getUniqueId())
								target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON_Target"), player.getName(), target.getName()));
							}
						}
						else
							player.sendMessage(Util.format(config.getString("Message.PvP_NotFound")));
				} else
					sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
			}
			
			//pvp <Name> <true/false>
			else if (sender.hasPermission("pvptoggle.pvp.other"))
			{
				if (args.length == 2) {
					Player target = Bukkit.getPlayer(args[0]);
					
					if (target != null) {
						if (args[1].equalsIgnoreCase("true")) {
							plugin.addPvplist(target.getUniqueId());
							MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
							try {
								mySQL.update("UPDATE `pvplist` SET `pvp` = '1' WHERE `pvplist`.`uuid` = '" + target.getUniqueId() + "'");
							} catch (SQLException exception) {
								exception.printStackTrace();
							}
							player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON"), player.getName(), target.getName()));
							
							if (target.getUniqueId() != player.getUniqueId())
								target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON_Target"), player.getName(), target.getName()));
						}
						else if (args[1].equalsIgnoreCase("false")) {
							plugin.removePvplist(target.getUniqueId());
							MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
							try {
								mySQL.update("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` = '" + target.getUniqueId() + "'");
							} catch (SQLException exception) {
								exception.printStackTrace();
							}
							player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF"), player.getName(), target.getName()));
							
							if (target.getUniqueId() != player.getUniqueId())
								target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF_Target"), player.getName(), target.getName()));
						}
						else
							player.sendMessage(Util.format("Message.PvP_Usage"));
					}
					else {
						player.sendMessage(Util.format(config.getString("Message.PvP_NotFound")));
					}				
				} else
					sender.sendMessage(Util.format("Message.PvP_Wrong_Command"));
			} else
				sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
		}
			

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		ArrayList<String> newList = new ArrayList<String>();
		
		if (sender.hasPermission("pvptoggle.pvp.other")) {
	    	ArrayList<String> proposals = new ArrayList<String>();
	    	
	    	if (args.length < 3) {
	    		if (args.length <2) {
			    	for (Player current : Bukkit.getOnlinePlayers()) {
			    		proposals.add(current.getName());
					}
		    	}
		    	
				if (args.length == 2) {
					proposals.add("true");
					proposals.add("false");
				}
	    	}
			
	        if (args.length < 1 || args[args.length - 1].equals(""))
	            newList = proposals;
	        else {
	            for (String value : proposals) {
	                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
	                    newList.add(value);
	            }
	        }
		}
		
		return newList;
	}
}
