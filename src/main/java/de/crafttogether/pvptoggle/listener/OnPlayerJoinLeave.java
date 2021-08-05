package de.crafttogether.pvptoggle.listener;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class OnPlayerJoinLeave implements Listener {

    private final FileConfiguration config;

    public OnPlayerJoinLeave() {
        config = PvPTogglePlugin.getInstance().getConfig();
    }

    @EventHandler
    public void leavePlayer(PlayerQuitEvent e) {
        if (!config.getBoolean("Settings.Keep_PvP_After_Logout")) {
            PvPTogglePlugin.getInstance().removePvplist(e.getPlayer().getUniqueId());

			/* TODO: MySQL
			mySQL.update("UPDATE `pvplist` SET `pvp` = '0' WHERE `pvplist`.`uuid` = '" + e.getPlayer().getUniqueId() + "'");
			*/
        }
    }

    @EventHandler
    public void joinPlayer(PlayerJoinEvent e) {
        Player player = e.getPlayer();

		/* TODO: MySQL
		try {
			ResultSet result = mySQL.query("SELECT `uuid` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + player.getUniqueId() + "'");//, ((result, thrown) -> {
			System.out.println(("Result closed: " + (result.isClosed() ? "Ja" : "Nein")));
			if(result.next() ) {
				PvPTogglePlugin.getInstance().getLogger().info("Bin schon drin! UUID = " + result.getString("uuid"));
			} else {
				PvPTogglePlugin.getInstance().getLogger().info("Ich Fehle! Moment.. Ich trag mich ein..");
				mySQL.update("INSERT INTO `"+ config.getString("MySQL.Database") + "`.`pvplist` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '" + user.getUniqueId() + "', '" + user.getName() + "', '0')");
				PvPTogglePlugin.getInstance().getLogger().info("Hab mich eingetragen.");
			}
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		*/

		/* TODO: MySQL
		mySQL.queryAsync(("SELECT `uuid` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + player.getUniqueId() + "'"), ((result, thrown) -> {

			if(thrown == null) {
				try {
					if(result.next() ) {
						PvPTogglePlugin.getInstance().getLogger().info("Bin schon drin!");
						// Bungeecord Sync.. bzw ich versuchs xd
						mySQL.queryAsync(("SELECT `pvp` FROM `" + config.getString("MySQL.Database") + "`.`pvplist` WHERE `uuid` = '" + player.getUniqueId() + "'"), (result2, thrown2) -> {
							try {
								if (result2.getInt("pvp") == 1) {
									PvPTogglePlugin.getInstance().addPvplist(player.getUniqueId());

								}
								else {
									PvPTogglePlugin.getInstance().removePvplist(player.getUniqueId());
								}
							} catch (SQLException exception) {
								exception.printStackTrace();
							}
						});

					} else {
						mySQL.update("INSERT INTO `"+ config.getString("MySQL.Database") + "`.`pvplist` (`id`, `uuid`, `playername`, `pvp`) VALUES (NULL, '" + player.getUniqueId() + "', '" + player.getName() + "', '0')");
					}
				} catch (SQLException exception) {
					exception.printStackTrace();
				}
			} else {
				thrown.printStackTrace();
			}

			try {
				result.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}));
		*/
    }
}