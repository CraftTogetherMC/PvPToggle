package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.pvplist.PvPListSQL;
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

public class PvpCommand implements TabExecutor {

    PvPTogglePlugin plugin = PvPTogglePlugin.getInstance();
    FileConfiguration config = PvPTogglePlugin.getPreloadConfig();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!plugin.pvplist.equalsPlayerUuid(player.getUniqueId())) {
            PvPListSQL.updatePvplist();
            player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Error")));
            return false;
        }

        switch (args.length) {
            case 0 -> {
                if (!player.hasPermission("pvptoggle.pvp.toggle")) {
                    player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
                    return false;
                }
                pvp(player);
            }
            case 1, 2 -> {
                if (!player.hasPermission("pvptoggle.pvp.other")) {
                    player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
                    return false;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (args.length == 1) {
                    pvp(target, player);
                } else {
                    if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                        pvp(target, player, Boolean.parseBoolean(args[1]));
                    } else
                        player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Wrong_Command")));
                }
            }
            default -> player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Usage")));
        }

        return false;
    }

    private boolean pvp(Player target) {
        boolean state = plugin.pvplist.state(target.getUniqueId());

        if (state && config.getBoolean("Settings.Cooldown")) {
            long left = plugin.pvplist.checkTimestamp(target.getUniqueId());

            if (left > 0) {
                target.sendMessage(Util.format(Objects.requireNonNull(plugin.getConfig().getString("Message.PvP_Cooldown")), target.getName(), left));
            } else if (left == -1) {
                target.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Error")));
            } else {
                PvPListSQL.updateDatabaseState(target, plugin.pvplist.state(target.getUniqueId(), false));
                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_OFF")), target.getName()));
            }
        } else {
            PvPListSQL.updateDatabaseState(target, plugin.pvplist.state(target.getUniqueId(), !state));
            if (!state) {
                plugin.pvplist.timestamp(target.getUniqueId(), System.currentTimeMillis());
                PvPListSQL.updateDatabaseTimestamp(target);
                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_ON")), target.getName()));
            }
            else
                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_OFF")), target.getName()));
        }

        return state;
    }

    private boolean pvp(Player target, Player player) {
        if (isTargetNotAlright(target, player)) return false;

        return pvpChange(target, player, !plugin.pvplist.state(target.getUniqueId()));
    }

    private boolean pvp(Player target, Player player, boolean state) {
        if (isTargetNotAlright(target, player)) return state;

        return pvpChange(target, player, state);
    }

    private boolean pvpChange(Player target, Player player, boolean state) {
        if (plugin.pvplist.state(target.getUniqueId(), state)) {
            plugin.pvplist.timestamp(target.getUniqueId(), System.currentTimeMillis());
            if (player != target) {
                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON_Target")), player.getName(), target.getName()));
            }
            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_ON")), player.getName(), target.getName()));
        } else {
            if (player != target) {
                target.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF_Target")), player.getName(), target.getName()));
            }
            player.sendMessage(Util.format(Objects.requireNonNull(config.getString("Message.PvP_Toggle_Other_OFF")), player.getName(), target.getName()));
        }

        return state;
    }

    private boolean isTargetNotAlright(Player target, Player sender) {
        if (target == null) {
            sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NotFound")));
            return true;
        }
        if (!plugin.pvplist.equalsPlayerUuid(target.getUniqueId())) {
            PvPListSQL.updatePvplist();
            sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Error")));
            return true;
        }
        return false;
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
