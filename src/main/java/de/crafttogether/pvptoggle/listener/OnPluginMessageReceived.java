package de.crafttogether.pvptoggle.listener;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.util.MySQLAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnPluginMessageReceived implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("PlayerList")) { // from PvpListCommand
            String server = in.readUTF();
            String[] playerList = in.readUTF().split(", ");

            Plugin plugin = PvPTogglePlugin.getInstance();

            if (plugin.getConfig().getBoolean("Settings.Debug"))
                plugin.getLogger().info("[MySQL]: Select all playernames where pvpstate = 1 ...");

            MySQLAdapter.MySQLConnection connection = PvPTogglePlugin.getMySQL().getConnection();

            connection.queryAsync("SELECT `playername` FROM `%s` WHERE `pvpstate` = '1'", (err, result) -> {
                if (err != null)
                    plugin.getLogger().warning("[MySQL]: " + err.getMessage());

                try {
                    List<String> pvplist = new ArrayList<>();
                    while (result.next()) {
                        pvplist.add(result.getString("playername"));
                    }
                    player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_List"));

                    List<String> finalPvpList = new ArrayList<>();

                    if (!pvplist.isEmpty()) {
                        for (String pvplistPlayer : pvplist) {
                            for (String onlinePlayer : playerList) {
                                if (pvplistPlayer.equals(onlinePlayer))
                                    finalPvpList.add(pvplistPlayer);
                            }
                        }
                        if (!finalPvpList.isEmpty()) {
                            for (String s : finalPvpList) {
                                player.sendMessage("§8-§c " + s);
                            }
                        } else {
                            player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody"));
                        }
                    } else {
                        player.sendMessage(PvPTogglePlugin.getPreloadConfig().getString("Message.PvP_Nobody"));
                    }
                } catch (SQLException e) {
                    PvPTogglePlugin.getInstance().getLogger().warning(e.getMessage());
                }
                connection.close();
            }, connection.getTablePrefix() + "pvplist");
        }
        else if (subchannel.equals("pvptoggle")) {
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            String command = null;
            try {
                command = msgin.readUTF();
                short uselessNummber = msgin.readShort();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (command != null && command.equals("updateCache")) {
                Plugin plugin = PvPTogglePlugin.getInstance();

                if (plugin.getConfig().getBoolean("Settings.Debug"))
                    plugin.getLogger().info("[MySQL]: Select uuid, pvpstate ...");

                PvPTogglePlugin.getInstance().updatePvplist();
            }
        }
    }

}
