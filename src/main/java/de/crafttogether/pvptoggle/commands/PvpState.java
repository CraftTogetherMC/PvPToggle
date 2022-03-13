package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.pvplist.PvPList;
import de.crafttogether.pvptoggle.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PvpState implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("pvptoggle.pvp.state")) {
                player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_NoPerm"));
                return false;
            }

            PvPList pvplist = PvPTogglePlugin.getInstance().pvplist;

            if (pvplist.equalsPlayerUuid(player.getUniqueId())) {
                Configuration config = PvPTogglePlugin.getPreloadConfig();
                if (pvplist.state(player.getUniqueId()))
                    sender.sendMessage(Util.format(config.getString("Message.PvP_State_ON"), player.getName()));
                else
                    sender.sendMessage(Util.format(config.getString("Message.PvP_State_OFF"), player.getName()));
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
