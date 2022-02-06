package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
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
            if (!PvPTogglePlugin.pvpList().containsKey(playerUUID)) {
                PvPTogglePlugin.getInstance().updatePvplist();
                player.sendMessage(config.getString("Message.PvP_Error"));
                return false;
            }

            if (args.length == 0) {
                if (!sender.hasPermission("pvptoggle.pvp.toggle")) {
                    sender.sendMessage(config.getString("Message.PvP_NoPerm"));
                    return false;
                }
                if (!PvPTogglePlugin.pvpList().get(playerUUID)) {
                    add(plugin, player);

                    player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_ON"), player.getName()));
                } else {
                    remove(plugin, player);

                    player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_OFF"), player.getName()));
                }
            } else if (args.length == 1) {
                if (!sender.hasPermission("pvptoggle.pvp.toggle")) {
                    sender.sendMessage(config.getString("Message.PvP_NoPerm"));
                    return false;
                }
                if (sender.hasPermission("pvptoggle.pvp.other")) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        UUID targetUUID = target.getUniqueId();

                        if (!PvPTogglePlugin.pvpList().get(targetUUID)) {
                            add(plugin, target);
                            player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON"), player.getName(), target.getName()));
                            if (player.getUniqueId() != target.getUniqueId())
                                target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON_Target"), player.getName(), target.getName()));

                        } else {
                            remove(plugin, target);
                            if (player.getUniqueId() != target.getUniqueId())
                                target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF_Target"), player.getName(), target.getName()));

                            player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF"), player.getName(), target.getName()));
                        }
                    } else
                        player.sendMessage(config.getString("Message.PvP_NotFound"));
                } else
                    sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
            } else if (sender.hasPermission("pvptoggle.pvp.other")) {
                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if (target == null) {
                        player.sendMessage(Util.format(config.getString("Message.PvP_NotFound")));
                        return false;
                    }
                    if (args[1].equalsIgnoreCase("true")) {
                        if (!PvPTogglePlugin.pvpList().get(target.getUniqueId())) {
                            add(plugin, target);
                            if (target.getUniqueId() != player.getUniqueId())
                                target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON_Target"), player.getName(), target.getName()));
                        }
                        player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_ON"), player.getName(), target.getName()));
                    } else if (args[1].equalsIgnoreCase("false")) {
                        if (PvPTogglePlugin.pvpList().get(target.getUniqueId())) {
                            remove(plugin, target);
                            if (target.getUniqueId() != player.getUniqueId())
                                target.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF_Target"), player.getName(), target.getName()));
                        }
                        player.sendMessage(Util.format(config.getString("Message.PvP_Toggle_Other_OFF"), player.getName(), target.getName()));
                    } else
                        player.sendMessage(config.getString("Message.PvP_Usage"));
                } else
                    sender.sendMessage(config.getString("Message.PvP_Wrong_Command"));
            } else
                sender.sendMessage(config.getString("Message.PvP_NoPerm"));
        }

        return false;
    }

    private void remove(PvPTogglePlugin plugin, Player player) {
        PvPTogglePlugin.pvpList(player.getUniqueId(), false);

        if (plugin.getConfig().getBoolean("Settings.Debug"))
            plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 0 (disabled) ...");

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();

        connection.updateAsync("UPDATE %s SET `pvpstate` = '0' WHERE `uuid` = '%s'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            if (PvPTogglePlugin.getPreloadConfig().getBoolean("BungeeCord.Enable"))
                plugin.updateAllProxyCachesCommand(player);
            connection.close();
        }, connection.getTablePrefix() + "pvplist", player.getUniqueId());

    }

    private void add(PvPTogglePlugin plugin, Player player) {
        PvPTogglePlugin.pvpList(player.getUniqueId(), true);

        if (plugin.getConfig().getBoolean("Settings.Debug"))
            plugin.getLogger().info("[MySQL]: Updating pvp-state of player '" + player.getName() + "' to 1 (enabled) ...");

        MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();
        connection.updateAsync("UPDATE %s SET `pvpstate` = '1' WHERE `uuid` = '%s'", (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL]: " + err.getMessage());
            if (PvPTogglePlugin.getPreloadConfig().getBoolean("BungeeCord.Enable"))
                plugin.updateAllProxyCachesCommand(player);
            connection.close();
        }, connection.getTablePrefix() + "pvplist", player.getUniqueId());

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
