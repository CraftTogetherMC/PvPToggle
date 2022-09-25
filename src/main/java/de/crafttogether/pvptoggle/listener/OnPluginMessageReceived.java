package de.crafttogether.pvptoggle.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.crafttogether.pvptoggle.PvPTogglePlugin;
import de.crafttogether.pvptoggle.commands.PvpListCommand;
import de.crafttogether.pvptoggle.pvplist.PvPListSQL;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class OnPluginMessageReceived implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        PvpListCommand pvpListCommand = PvPTogglePlugin.pvpListCommand();

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("PlayerList") && pvpListCommand.waiting()) { // from PvpListCommand
            pvpListCommand.waiting(false);
            String server = in.readUTF();
            if (server.equals("ALL")) {
                String[] playerList = in.readUTF().split(", ");

                PvPListSQL.sendMessagePvplist(player, Arrays.stream(playerList).toList());
            }
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

                PvPListSQL.updatePvplist();
            }
        }
    }

}
