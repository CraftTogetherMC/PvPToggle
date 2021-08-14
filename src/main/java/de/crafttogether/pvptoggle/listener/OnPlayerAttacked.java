package de.crafttogether.pvptoggle.listener;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.Util;
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

import java.util.Objects;

public class OnPlayerAttacked implements Listener {

    FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();

    @EventHandler
    public void onPlayerAttacked(EntityDamageByEntityEvent e) {
        DamageCause cause = e.getCause();
        Entity damager = e.getDamager();

        //  Citizens check
        if (e.getEntity().hasMetadata("NPC"))
            return;

        switch (cause) {
            case PROJECTILE -> {
                Projectile projectile = (Projectile) e.getDamager();
                projectile.getShooter();
                if (projectile.getType() != EntityType.SNOWBALL && projectile.getType() != EntityType.ENDER_PEARL && projectile.getType() != EntityType.EGG) {
                    if (projectile.getShooter() instanceof Player att && e.getEntity() instanceof Player pl) {
                        pvpListCheck(e, pl, att);
                    }
                }
                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (projectile.getShooter() instanceof Player att && e.getEntity() instanceof Tameable pet) {
                        petProtectCheck(e, pet, att);
                    }
                }
            }
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> {
                if (damager instanceof Player att && e.getEntity() instanceof Player pl) {
                    pvpListCheck(e, pl, att);
                }
                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (damager instanceof Player att && e.getEntity() instanceof Tameable pet) {
                        petProtectCheck(e, pet, att);
                    }

                    if (e.getDamager() instanceof Tameable pet && e.getEntity() instanceof Player) {
                        if (pet.getOwner() != null) {
                            Player owner = Bukkit.getPlayer(pet.getOwner().getUniqueId());
                            Player pl = (Player) e.getEntity();
                            // Beide, player oder angreifer sind nicht in der Liste
                            assert owner != null;
                            if (!PvPTogglePlugin.getPvpList().contains(owner.getUniqueId()) || !PvPTogglePlugin.getPvpList().contains(pl.getUniqueId())) {
                                e.setCancelled(true);
                            }
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

                    // Beide/player/attacking sind nicht in der Liste
                    if ((!PvPTogglePlugin.getPvpList().contains(player.getUniqueId()) || !PvPTogglePlugin.getPvpList().contains(attacking.getUniqueId())) &&
                            player != attacking) {
                        e.setCancelled(true);
                    }
                }
                // Pet Protect
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
                        if ((!PvPTogglePlugin.getPvpList().contains(player.getUniqueId()) || !PvPTogglePlugin.getPvpList().contains(attacking.getUniqueId())) &&
                                player != attacking) {
                            e.setCancelled(true);
                        }
                    }
                    // Pet Protect
                    else if (entity instanceof Tameable pet) {
                        if (pet.getOwner() != null && pet.getOwner().getUniqueId() != attacking.getUniqueId()) {
                            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Pet_Protect")), attacking.getName(), pet.getOwner().getName(), Util.translator(entity.getName())));
                            e.setCancelled(true);
                        }
                    }
                }
            }

        }
    }

    private void petProtectCheck(EntityDamageByEntityEvent e, Tameable pet, Player attacking) {
        if (pet.getOwner() == null || pet.getOwner().getUniqueId() == attacking.getUniqueId())
            return;
        if (e.getEntityType() == EntityType.WOLF) {
            if (pet.getOwner() != null) {
                Player player = Bukkit.getPlayer(pet.getOwner().getUniqueId());
                if (player != null) {
                    if (pet.getOwner() != attacking) {
                        pvpListCheck(e, player, attacking);
                    }
                } else if (pet.getOwner() != attacking) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(pet.getOwner().getUniqueId());
                    attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Offline")), attacking.getName(), p.getName(), e.getEntity().getName()));
                    e.setCancelled(true);
                }
            }
        } else {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Pet_Protect")), attacking.getName(), pet.getOwner().getName(), Util.translator(pet.getName())));
            e.setCancelled(true);
        }
    }

    private void pvpListCheck(EntityDamageByEntityEvent e, Player player, Player attacking) {
        if (!PvPTogglePlugin.getPvpList().contains(player.getUniqueId()) && !PvPTogglePlugin.getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Both")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        } else if (PvPTogglePlugin.getPvpList().contains(player.getUniqueId()) && !PvPTogglePlugin.getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Self")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        } else if (!PvPTogglePlugin.getPvpList().contains(player.getUniqueId()) && PvPTogglePlugin.getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Other")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        }
    }
}