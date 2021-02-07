package de.kaai.pvptoggle.listener;

import de.kaai.pvptoggle.PvPTogglePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;


public class LeavePlayer implements Listener {
	
	@EventHandler
	public void leavePlayer(PlayerQuitEvent e) {
		// Löscht den Spieler aus der PvP Liste
		PvPTogglePlugin.removePvplist(e.getPlayer().getUniqueId());
	}
}