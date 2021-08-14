package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PvpCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();
        FileConfiguration config = PvPTogglePlugin.getPreloadConfig();

        if (sender instanceof Player player) {
            UUID playerUUID = player.getUniqueId();

            if (args.length == 0) {
                if (!sender.hasPermission("pvptoggle.pvp.toggle")) {
                    sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
                    return false;
                }
                if (!PvPTogglePlugin.getPvpList().contains(playerUUID)) {
                    add(plugin, player);

                    player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_ON")), player.getName()));
                } else {
                    remove(plugin, player);

                    player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_OFF")), player.getName()));
                }
            } else if (args.length == 1) {
                if (sender.hasPermission("pvptoggle.pvp.other")) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        UUID targetUUID = target.getUniqueId();

                        if (!PvPTogglePlugin.getPvpList().contains(targetUUID)) {
                            add(plugin, target);

                            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON")), player.getName(), target.getName()));

                            if (player.getUniqueId() != target.getUniqueId())
                                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON_Target")), player.getName(), target.getName()));

                        } else {
                            remove(plugin, target);
                            if (player.getUniqueId() != target.getUniqueId())
                                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF_Target")), player.getName(), target.getName()));

                            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF")), player.getName(), target.getName()));

                        }
                    } else
                        player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NotFound")));
                } else
                    sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
            } else if (sender.hasPermission("pvptoggle.pvp.other")) {
                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if (target != null) {
                        if (args[1].equalsIgnoreCase("true")) {
                            add(plugin, target);

                            if (target.getUniqueId() != player.getUniqueId())
                                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON_Target")), player.getName(), target.getName()));

                            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON")), player.getName(), target.getName()));
                        } else if (args[1].equalsIgnoreCase("false")) {
                            remove(plugin, target);

                            if (target.getUniqueId() != player.getUniqueId())
                                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF_Target")), player.getName(), target.getName()));

                            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF")), player.getName(), target.getName()));
                        } else
                            player.sendMessage(Util.format("Message.PvP_Usage"));
                    } else {
                        player.sendMessage(Util.format(config.getString("Message.PvP_NotFound")));
                    }
                } else
                    sender.sendMessage(Util.format("Message.PvP_Wrong_Command"));
            } else
                sender.sendMessage(Util.format(config.getString("Message.PvP_NoPerm")));
        }

        return false;
    }

    private void remove(PvPTogglePlugin plugin, Player player) {
        PvPTogglePlugin.getPvpList().remove(player.getUniqueId());

        if (plugin.getConfig().getBoolean("Settings.Debug"))
            plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 0 (disabled) ...");

        PvPTogglePlugin.getMySQL().getConnection().updateAsync("UPDATE `?`.`?` SET `pvp` = '0' WHERE `?`.`uuid` = '?'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
        }, PvPTogglePlugin.getMySQL().getConfig().getDatabase(), "pvplist", player.getUniqueId()).close();
    }

    private void add(PvPTogglePlugin plugin, Player player) {
        PvPTogglePlugin.getPvpList().add(player.getUniqueId());

        if (plugin.getConfig().getBoolean("Settings.Debug"))
            plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 1 (enabled) ...");

        PvPTogglePlugin.getMySQL().getConnection().updateAsync("UPDATE `?`.`?` SET `pvp` = '1' WHERE `?`.`uuid` = '?'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
        }, PvPTogglePlugin.getMySQL().getConfig().getDatabase(), "pvplist", player.getUniqueId()).close();
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        ArrayList<String> newList = new ArrayList<>();

        if (sender.hasPermission("pvptoggle.pvp.other")) {
            ArrayList<String> proposals = new ArrayList<>();

            if (args.length < 3) {
                if (args.length < 2) {
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
