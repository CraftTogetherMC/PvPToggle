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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OnPlayerAttacked implements Listener {

    FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();

    @EventHandler
    public void onPlayerAttacked(@NotNull EntityDamageByEntityEvent e) {
        DamageCause cause = e.getCause();
        Entity damager = e.getDamager();

        //  Citizens check
        if (e.getEntity().hasMetadata("NPC"))
            return;

        switch (cause) {
            case PROJECTILE:
                Projectile projectile = (Projectile) e.getDamager();
                projectile.getShooter();

                if (projectile.getType() != EntityType.SNOWBALL && projectile.getType() != EntityType.ENDER_PEARL && projectile.getType() != EntityType.EGG) {
                    if (projectile.getShooter() instanceof Player && e.getEntity() instanceof Player) {
                        Player pl = (Player) e.getEntity();
                        Player att = (Player) projectile.getShooter();
                        pvpListCheck(e, pl, att);
                    }
                }

                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (projectile.getShooter() instanceof Player && e.getEntity() instanceof Tameable) {
                        Tameable pet = (Tameable) e.getEntity();
                        Player att = (Player) projectile.getShooter();
                        petProtectCheck(e, pet, att);
                    }
                }
                break;

            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                if (damager instanceof Player && e.getEntity() instanceof Player) {
                    Player pl = (Player) e.getEntity();
                    Player att = (Player) damager;
                    pvpListCheck(e, pl, att);
                }

                if (config.getBoolean("Settings.Tamed_Pet_Protect")) {
                    if (damager instanceof Player && e.getEntity() instanceof Tameable) {
                        Tameable pet = (Tameable) e.getEntity();
                        Player att = (Player) damager;
                        petProtectCheck(e, pet, att);
                    }

                    if (e.getDamager() instanceof Tameable && e.getEntity() instanceof Player) {
                        Tameable pet = (Tameable) e.getDamager();
                        if (pet.getOwner() != null) {
                            Player owner = Bukkit.getPlayer(pet.getOwner().getUniqueId());
                            Player pl = (Player) e.getEntity();
                            // Beide, player oder angreifer sind nicht in der Liste
                            assert owner != null;
                            if (!PvPTogglePlugin.getInstance().getPvpList().contains(owner.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(pl.getUniqueId())) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }

                break;
        }
    }

    @EventHandler
    public void onPlayerAttacked(@NotNull AreaEffectCloudApplyEvent e) {

        if (e.getEntity().getSource() instanceof Player) {
            Player attacking = (Player) e.getEntity().getSource();

            for (Entity entity : e.getAffectedEntities()) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;

                    // Beide/player/attacking sind nicht in der Liste
                    if ((!PvPTogglePlugin.getInstance().getPvpList().contains(player.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(attacking.getUniqueId())) &&
                            player != attacking) {
                        e.setCancelled(true);
                    }
                }
                // Pet Protect
                else if (entity instanceof Tameable) {
                    Tameable pet = (Tameable) entity;
                    if (pet.getOwner() != null && pet.getOwner().getUniqueId() != attacking.getUniqueId()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerAttacked(@NotNull PotionSplashEvent e) {

        if (e.getEntity().getShooter() instanceof Player) {
            Player attacking = (Player) e.getEntity().getShooter();
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


                    if (entity instanceof Player) {
                        Player player = (Player) entity;

                        // Beide/player/attacking sind nicht in der Liste
                        if ((!PvPTogglePlugin.getInstance().getPvpList().contains(player.getUniqueId()) || !PvPTogglePlugin.getInstance().getPvpList().contains(attacking.getUniqueId())) &&
                                player != attacking) {
                            e.setCancelled(true);
                        }
                    }
                    // Pet Protect
                    else if (entity instanceof Tameable) {
                        Tameable pet = (Tameable) entity;
                        if (pet.getOwner() != null && pet.getOwner().getUniqueId() != attacking.getUniqueId()) {
                            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Pet_Protect")), attacking.getName(), pet.getOwner().getName(), Util.translator(entity.getName())));
                            e.setCancelled(true);
                        }
                    }
                }
            }

        }
    }

    private void petProtectCheck(EntityDamageByEntityEvent e, @NotNull Tameable pet, Player attacking) {
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

    private void pvpListCheck(EntityDamageByEntityEvent e, @NotNull Player player, Player attacking) {
        if (!PvPTogglePlugin.getInstance().getPvpList().contains(player.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Both")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        } else if (PvPTogglePlugin.getInstance().getPvpList().contains(player.getUniqueId()) && !PvPTogglePlugin.getInstance().getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Self")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        } else if (!PvPTogglePlugin.getInstance().getPvpList().contains(player.getUniqueId()) && PvPTogglePlugin.getInstance().getPvpList().contains(attacking.getUniqueId())) {
            attacking.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_False_Other")), attacking.getName(), player.getName()));
            e.setCancelled(true);
        }
    }
}