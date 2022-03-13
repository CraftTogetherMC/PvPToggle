package de.crafttogether.pvptoggle.listener;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
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

import java.util.*;

public class OnPlayerAttacked implements Listener {

    @EventHandler
    public void onPlayerAttacked(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();

        List<String> worlds = PvPTogglePlugin.getPreloadConfig().getStringList("BlockedWorldList");
        if (!worlds.isEmpty()) {
            String currentWorld = damager.getWorld().getName().toLowerCase(Locale.ROOT);
            for (String world : worlds) {
                if (world.toLowerCase(Locale.ROOT).equals(currentWorld)) {
                    return;
                }
            }
        }

        DamageCause cause = e.getCause();

        //  Citizens check
        if (e.getEntity().hasMetadata("NPC"))
            return;

        Configuration config = PvPTogglePlugin.getPreloadConfig();

        switch (cause) {
            case PROJECTILE -> {
                Projectile projectile = (Projectile) e.getDamager();
                projectile.getShooter();
                if (projectile.getType() != EntityType.SNOWBALL && projectile.getType() != EntityType.ENDER_PEARL && projectile.getType() != EntityType.EGG) {
                    if (projectile.getShooter() instanceof Player att && e.getEntity() instanceof Player pl && att != pl) {
                        e.setCancelled(pvplistCheck(pl, att));
                    }
                }
                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (projectile.getShooter() instanceof Player att && e.getEntity() instanceof Tameable pet) {
                        e.setCancelled(petProtectCheck(e.getEntityType(), pet, att));
                    }
                }
            }
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> {
                if (damager instanceof Player att && e.getEntity() instanceof Player pl) {
                    e.setCancelled(pvplistCheck(pl, att));
                }
                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (damager instanceof Player att && e.getEntity() instanceof Tameable pet) {
                        e.setCancelled(petProtectCheck(e.getEntityType(), pet, att));
                    }

                    if (e.getDamager() instanceof Tameable pet && e.getEntity() instanceof Player) {
                        if (pet.getOwner() != null) {
                            Player owner = Bukkit.getPlayer(pet.getOwner().getUniqueId());
                            Player pl = (Player) e.getEntity();
                            HashMap<UUID, Boolean> pvplist = PvPTogglePlugin.pvpList();

                            assert owner != null;
                            if (pvplist.containsKey(owner.getUniqueId()) || pvplist.containsKey(pl.getUniqueId())) {
                                if (!pvplist.get(owner.getUniqueId()) || !pvplist.get(pl.getUniqueId())) {
                                    e.setCancelled(true);
                                }
                            } else e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerAttacked(AreaEffectCloudApplyEvent e) {

        if (e.getEntity().getSource() instanceof Player attacking) {
            for (Entity entity : e.getAffectedEntities()) {
                if (entity instanceof Player player) {
                    if ((!PvPTogglePlugin.pvpList().containsKey(player.getUniqueId()) || !PvPTogglePlugin.pvpList().containsKey(attacking.getUniqueId())) &&
                            player != attacking) {
                        e.setCancelled(true);
                    }
                }
                else if (entity instanceof Tameable pet) {
                    if (pet.getOwner() != null && pet.getOwner().getUniqueId() != attacking.getUniqueId()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerAttacked(PotionSplashEvent e) {

        if (e.getEntity().getShooter() instanceof Player attacking) {
            PotionEffect potion = null;

            for (PotionEffect potionEffect : e.getPotion().getEffects()) {
                if (potionEffect.getType().getName().equals("POISON") ||
                        potionEffect.getType().getName().equals("HARM")) {
                    potion = potionEffect;
                    break;
                }
            }

            if (potion != null) {
                for (Entity entity : e.getAffectedEntities()) {

                    if (entity instanceof Player player) {

                        // Beide/player/attacking sind nicht in der Liste
                        if ((!PvPTogglePlugin.pvpList().containsKey(player.getUniqueId()) || !PvPTogglePlugin.pvpList().containsKey(attacking.getUniqueId())) &&
                                player != attacking) {
                            e.setCancelled(true);
                        }
                    }
                    else if (entity instanceof Tameable pet) {
                        if (pet.getOwner() != null && pet.getOwner().getUniqueId() != attacking.getUniqueId()) {
                            attacking.sendMessage(Util.format(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Pet_Protect"), attacking.getName(), pet.getOwner().getName(), Util.translator(entity.getName())));
                            e.setCancelled(true);
                        }
                    }
                }
            }

        }
    }

    private boolean petProtectCheck(EntityType entityType, Tameable pet, Player attacking) {
        if (pet.getOwner() == null || pet.getOwner().getUniqueId() == attacking.getUniqueId())
            return false;

        Configuration config = PvPTogglePlugin.getPreloadConfig();
        if (entityType == EntityType.WOLF) {
            if (pet.getOwner() != null) {
                Player player = Bukkit.getPlayer(pet.getOwner().getUniqueId());
                if (player != null) {
                    if (pet.getOwner() != attacking) {
                        return pvplistCheck(player, attacking);
                    }
                } else if (pet.getOwner() != attacking) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(pet.getOwner().getUniqueId());
                    // attacking.sendMessage(Util.format(config.getString("Message.PvP_Offline"), attacking.getName(), p.getName(), entityType.toString()));
                    attacking.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), attacking.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
                    return true;
                }
            }
        } else {
            attacking.sendMessage(Util.format(config.getString("Message.PvP_Pet_Protect"), attacking.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
            return true;
        }
        return false;
    }

    private boolean pvplistCheck(Player player, Player attacking) {
        HashMap<UUID, Boolean> pvplist = PvPTogglePlugin.pvpList();

        if (!pvplist.containsKey(player.getUniqueId()) || !pvplist.containsKey(attacking.getUniqueId()))
            return true;

        Configuration config = PvPTogglePlugin.getPreloadConfig();

        if (player == attacking) {
            return false;
        }

        if (!pvplist.get(player.getUniqueId()) && !pvplist.get(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(config.getString("Message.PvP_False_Both"), attacking.getName(), player.getName()));
            return true;
        } else if (pvplist.get(player.getUniqueId()) && !pvplist.get(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(config.getString("Message.PvP_False_Self"), attacking.getName(), player.getName()));
            return true;
        } else if (!pvplist.get(player.getUniqueId()) && pvplist.get(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(config.getString("Message.PvP_False_Other"), attacking.getName(), player.getName()));
            return true;
        }
        return false;
    }


}