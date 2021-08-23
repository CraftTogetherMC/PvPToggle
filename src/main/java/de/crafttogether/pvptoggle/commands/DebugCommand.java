package de.crafttogether.pvptoggle.commands;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
    // THIS COMMAND WILL BE REMOVED
public class DebugCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {

            HashMap<UUID, Boolean> map = PvPTogglePlugin.pvpList();

            player.sendMessage("");
            player.sendMessage("§cDEBUG INFOMATIONEN");
            player.sendMessage("§c<-------------------------------->");
            player.sendMessage("PvpListCache:");
            if (!PvPTogglePlugin.pvpList().isEmpty()) {
                for (Map.Entry<UUID, Boolean> entry : map.entrySet()) {
                    UUID uuid = entry.getKey();
                    Boolean pvpstate = entry.getValue();
                    player.sendMessage("§1----------------------------------");
                    player.sendMessage("Spieler: " + (Bukkit.getPlayer(uuid) == null ? "Nicht Online" : Bukkit.getPlayer(uuid).getName()));
                    player.sendMessage("UUID: " + uuid);
                    player.sendMessage("PvpState: " + pvpstate);
                }
            } else {
                player.sendMessage("Leer");
            }
            player.sendMessage("§c<-------------------------------->");
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
