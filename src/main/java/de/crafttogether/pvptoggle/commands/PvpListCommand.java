package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PvpListCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!sender.hasPermission("pvptoggle.pvp.list")) {
                sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
                return false;
            }
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

			/* TODO: MYSQL
			MySQLHandler mySQL = PvPTogglePlugin.getInstance().getMySQLHandler();
			if(mySQL == null) {
				player.sendMessage(Util.format("Dein PvP Status konnte nicht geändern werden"));
				return false;
			}
			mySQL.queryAsync(("SELECT `playername` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `pvp` = 1"), ((result, thrown) -> {

				if(thrown == null) {
					try {
						if (result.next()) {
							do {
								player.sendMessage("§c" + result.getString("playername"));
							} while (!result.next());
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
			 */
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        return new ArrayList<>();
    }
}
