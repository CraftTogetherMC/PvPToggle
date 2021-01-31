package de.kaai.pvptoggle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.kaai.pvptoggle.Main;


public class LeavePlayer implements Listener {
	
	@EventHandler
	public void leavePlayer(PlayerQuitEvent e) {
		//L�scht den Spieler aus der PvP Liste
		Main.removePvplist(e.getPlayer().getUniqueId());
	}

}
