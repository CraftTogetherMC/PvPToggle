package de.crafttogether.pvptoggle.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.pvptoggle.PvPTogglePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PvpListCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        FileConfiguration config = PvPTogglePlugin.getInstance().getConfig();

        if (sender instanceof Player player) {
            if (!sender.hasPermission("pvptoggle.pvp.list")) {
                sender.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_NoPerm")));
                return false;
            }

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Subchannel");
            out.writeUTF("Argument");

            // If you don't care about the player
            // Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            // Else, specify them
            Player player2 = Bukkit.getPlayerExact("Example");

            player2.sendPluginMessage(PvPTogglePlugin.getInstance(), "BungeeCord", out.toByteArray());

            if (!PvPTogglePlugin.getPvpList().isEmpty()) {
                for (Player current : Bukkit.getOnlinePlayers()) {
                    if (PvPTogglePlugin.getPvpList().contains(current.getUniqueId())) {
                        player.sendMessage("§c" + current.getName());
                    }
                }
            } else
                player.sendMessage(Objects.requireNonNull(config.getString("Message.PvP_Nobody")));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender arg0, @NotNull Command arg1, @NotNull String arg2, String[] arg3) {
        return new ArrayList<>();
    }
}
