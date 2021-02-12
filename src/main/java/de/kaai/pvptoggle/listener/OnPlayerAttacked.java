package de.kaai.pvptoggle.listener;

import de.kaai.pvptoggle.PvPTogglePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;

import de.kaai.pvptoggle.util.Util;

public class OnPlayerAttacked implements Listener {
	
	FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();
	
	@EventHandler
	public void onPlayerAttacked(EntityDamageByEntityEvent e) {
		DamageCause cause = e.getCause();
		Entity damager = e.getDamager();

		//  Prüft ob Entity ein Citizens NPC ist
		if (e.getEntity().hasMetadata("NPC"))
			return;

		switch (cause) {
		case PROJECTILE:
			Projectile projectile = (Projectile) e.getDamager();
			projectile.getShooter();
			
			if(projectile.getType() != EntityType.SNOWBALL && projectile.getType() != EntityType.ENDER_PEARL && projectile.getType() != EntityType.EGG) {
				if(projectile.getShooter() instanceof Player && e.getEntity() instanceof Player) {
					Player pl = (Player) e.getEntity();
					Player att = (Player) projectile.getShooter();
					// Beide sind nicht in der Liste
					if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
						att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
						e.setCancelled(true);
					}
					// att ist nicht in der Liste
					else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
						att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
						e.setCancelled(true);
					}
					// pl ist nicht in der Liste
					else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
						att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
						e.setCancelled(true);
					}
				}
			}
			
			// Wenn ein Spieler ein Tameable angreift
			if(config.getBoolean("Settings.Tamed_Pet_Protect")) {
				if(projectile.getShooter() instanceof Player && e.getEntity() instanceof Tameable) {
					Tameable pet = (Tameable) e.getEntity();
					Player att = (Player) projectile.getShooter();
					if(pet.getOwner() == null || pet.getOwner().getUniqueId() == att.getUniqueId())
						break;
					if(e.getEntityType() == EntityType.WOLF) {
						if(pet.getOwner() != null) {
							// pl = Der Spieler der Angegriefen wird
							Player pl = Bukkit.getPlayer(pet.getOwner().getUniqueId());
							// att = Der Spieler dem Wolf gehört
							if(pl != null) {
								if(pet.getOwner() != att) {
									// Beide sind nicht in der Liste
									if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// att ist nicht in der Liste
									else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// pl ist nicht in der Liste
									else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
								}	
							} else if (pet.getOwner() != att) {
								OfflinePlayer p = Bukkit.getOfflinePlayer(pet.getOwner().getUniqueId());
								att.sendMessage(Util.format(config.getString("Message.PvP_Offline"), att.getName(), p.getName(), e.getEntity().getName()));
								e.setCancelled(true);
							}
						}
					} else {
						att.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), att.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
						e.setCancelled(true);
					}
				}
			}
			
			break;
		
		case ENTITY_ATTACK:
			if(damager instanceof Player && e.getEntity() instanceof Player) {
				Player pl = (Player) e.getEntity();
				Player att = (Player) damager;
				// Beide sind nicht in der Liste
				if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
				// att ist nicht in der Liste
				else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
				// pl ist nicht in der Liste
				else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
			}
			
			// Wenn ein Spieler ein Tameable angreift
			if(config.getBoolean("Settings.Tamed_Pet_Protect")) {
				if(damager instanceof Player && e.getEntity() instanceof Tameable) {
					Tameable pet = (Tameable) e.getEntity();
					Player att = (Player) damager;
					if(pet.getOwner() == null || pet.getOwner().getUniqueId() == att.getUniqueId())
						break;
					if(e.getEntityType() == EntityType.WOLF) {
						if(pet.getOwner() != null) {
							// pl = Der Spieler der Angegriefen wird
							Player pl = Bukkit.getPlayer(pet.getOwner().getUniqueId());
							// att = Der Spieler dem Wolf gehört
							if(pl != null) {
								if(pet.getOwner() != att) {
									// Beide sind nicht in der Liste
									if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// att ist nicht in der Liste
									else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// pl ist nicht in der Liste
									else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
								}	
							} else if (pet.getOwner() != att) {
								OfflinePlayer p = Bukkit.getOfflinePlayer(pet.getOwner().getUniqueId());
								att.sendMessage(Util.format(config.getString("Message.PvP_Offline"), att.getName(), p.getName(), e.getEntity().getName()));
								e.setCancelled(true);
							}
						}
					} else {
						att.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), att.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
						e.setCancelled(true);
					}
				}
				
				// Wenn ein Zähmbares Tier ein Spieler angreift
				if(e.getDamager() instanceof Tameable && e.getEntity() instanceof Player) {
					Tameable pet = (Tameable) e.getDamager();
					if (pet.getOwner() != null) {
						Player owner = Bukkit.getPlayer(pet.getOwner().getUniqueId());
						Player pl = (Player) e.getEntity();
						// Beide, player oder angreifer sind nicht in der Liste
						if (!PvPTogglePlugin.getInstance().getPvpList().contains(owner.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId())) {
							e.setCancelled(true);
						}
					}
				}	
			}
			break;

		case ENTITY_SWEEP_ATTACK:
			if(damager instanceof Player && e.getEntity() instanceof Player) {
				Player pl = (Player) e.getEntity();
				Player att = (Player) damager;
				// Beide sind nicht in der Liste
				if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
				// att ist nicht in der Liste
				else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
				// pl ist nicht in der Liste
				else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
					att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
					e.setCancelled(true);
				}
			}

			// Wenn ein Spieler ein Tameable angreift
			if(config.getBoolean("Settings.Tamed_Pet_Protect")) {
				if(damager instanceof Player && e.getEntity() instanceof Tameable) {
					Tameable pet = (Tameable) e.getEntity();
					Player att = (Player) damager;
					if(pet.getOwner() == null || pet.getOwner().getUniqueId() == att.getUniqueId())
						break;
					if(e.getEntityType() == EntityType.WOLF) {
						if(pet.getOwner() != null) {
							// pl = Der Spieler der Angegriefen wird
							Player pl = Bukkit.getPlayer(pet.getOwner().getUniqueId());
							// att = Der Spieler dem Wolf gehört
							if(pl != null) {
								if(pet.getOwner() != att) {
									// Beide sind nicht in der Liste
									if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// att ist nicht in der Liste
									else if(PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
									// pl ist nicht in der Liste
									else if(!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) {
										att.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), att.getName(), pl.getName()));
										e.setCancelled(true);
									}
								}
							} else if (pet.getOwner() != att) {
								OfflinePlayer p = Bukkit.getOfflinePlayer(pet.getOwner().getUniqueId());
								att.sendMessage(Util.format(config.getString("Message.PvP_Offline"), att.getName(), p.getName(), e.getEntity().getName()));
								e.setCancelled(true);
							}
						}
					} else {
						att.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), att.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
						e.setCancelled(true);
					}
				}

				// Wenn ein Zähmbares Tier ein Spieler angreift
				if(e.getDamager() instanceof Tameable && e.getEntity() instanceof Player) {
					Tameable pet = (Tameable) e.getDamager();
					if (pet.getOwner() != null) {
						Player owner = Bukkit.getPlayer(pet.getOwner().getUniqueId());
						Player pl = (Player) e.getEntity();
						// Beide, player oder angreifer sind nicht in der Liste
						if (!PvPTogglePlugin.getInstance().getPvpList().contains(owner.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId())) {
							e.setCancelled(true);
						}
					}
				}
			}


			break;
		default:
			break;
		}
	}

	@EventHandler
	public void onPlayerAttacked(AreaEffectCloudApplyEvent e) {	
		
		if(e.getEntity().getSource() instanceof Player) {
			Player att = (Player) e.getEntity().getSource();	
			
			for(Entity enti : e.getAffectedEntities()) {
				if(enti instanceof Player) {
					Player pl = (Player) enti;

					// Beide/pl/att sind nicht in der Liste
					if((!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) &&
						pl != att) {
						e.setCancelled(true);
					}
				}
				// Pet Protect
				else if(enti instanceof Tameable) {
					Tameable pet = (Tameable) enti;
					if(pet.getOwner() != null && pet.getOwner().getUniqueId() != att.getUniqueId()) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerAttacked(PotionSplashEvent e) {
		
		if(e.getEntity().getShooter() instanceof Player) {
			Player att = (Player) e.getEntity().getShooter();
			PotionEffect potion = null;
			
			for (PotionEffect potionEffect : e.getPotion().getEffects())
		    {
		        if (potionEffect.getType().getName().equals("POISON") ||
		        	potionEffect.getType().getName().equals("HARM"))
		        {
		        	potion = potionEffect;
		            break;
		        }
		    }
			
			if (potion != null)
		    {
				for(Entity enti : e.getAffectedEntities()) {
					
					
					if(enti instanceof Player) {
						Player pl = (Player) enti;

						// Beide/pl/att sind nicht in der Liste
						if((!PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(att.getUniqueId())) &&
								pl != att) {
							e.setCancelled(true);
						}
					}
					// Pet Protect
					else if(enti instanceof Tameable) {
						Tameable pet = (Tameable) enti;
						if(pet.getOwner() != null && pet.getOwner().getUniqueId() != att.getUniqueId()) {
							att.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), att.getName(), pet.getOwner().getName() , Util.translator(enti.getName())));
							e.setCancelled(true);
						}
					}
				}
		    }

		}	
	}
}