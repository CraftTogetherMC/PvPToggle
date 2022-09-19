package de.crafttogether.pvptoggle.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.pvplist.PvPListSQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PvpListCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Plugin plugin = PvPTogglePlugin.getInstance();
        FileConfiguration config = plugin.getConfig();

        if (sender instanceof Player player) {
            if (!player.hasPermission("pvptoggle.pvp.list")) {
                player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
                return false;
            }

            if (config.getBoolean("BungeeCord.Enable")) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("PlayerList");
                out.writeUTF("ALL");
                player.sendPluginMessage(PvPTogglePlugin.getInstance(), "BungeeCord", out.toByteArray());
            } else {

                ArrayList<String> names = new ArrayList<>();

                for (Player current : Bukkit.getOnlinePlayers()) {
                    names.add(current.getName());
                }

                PvPListSQL.sendMessagePvplist(player, names);
            }

        }
        return false;
    }



    @Override
    public List<String> onTabComplete(@NotNull CommandSender arg0, @NotNull Command arg1, @NotNull String arg2, String[] arg3) {
        return new ArrayList<>();
    }

}
