package de.crafttogether.pvptoggle.pvplist;

import de.crafttogether.pvptoggle.PvPTogglePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PvPList {

    private final ArrayList<PvPListEntry> pvplistEntries = new ArrayList<>();


    public void add(UUID playerUuid) {
        pvplistEntries.add(new PvPListEntry(playerUuid));
    }

    public void add(UUID playerUuid, boolean state) {
        pvplistEntries.add(new PvPListEntry(playerUuid, state));
    }

    public void add(UUID playerUuid, boolean state, long timestamp) {
        pvplistEntries.add(new PvPListEntry(playerUuid, state, timestamp));
    }

    public long checkTimestamp(UUID playerUuid) {
        // Gibt die Zeit in Millisekunden, die noch übrig sind.

        long timestampNow = System.currentTimeMillis();
        int cooldownMillis = PvPTogglePlugin.getInstance().getConfig().getInt("Settings.Cooldown_Time")*1000;

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                long differenz = timestampNow - pvPListEntry.timestamp();
                long left = cooldownMillis - differenz;

                if (left >= 0) {
                    return left;
                } else {
                    return 0;
                }
            }
        }
        // Wenn, der Spieler nicht gefunden wurde.
        return -1;
    }

    public boolean toggleState(UUID playerUuid) {

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                Player player = Bukkit.getPlayer(playerUuid);
                boolean state = pvPListEntry.state(!pvPListEntry.state());
                if (state)
                    PvPListSQL.updateTimestamp(player);
                PvPListSQL.updateState(player, state);

                return state;
            }
        }
        return false;
    }

    public boolean state(UUID playerUuid, boolean state) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (state)
                    PvPListSQL.updateTimestamp(player);
                PvPListSQL.updateState(player, state);
                pvPListEntry.state(state);
                break;
            }
        }

        return state;
    }

    public boolean state(UUID playerUuid) {
        boolean state = false;

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                state = pvPListEntry.state();
                break;
            }
        }

        return state;
    }

    public long timestamp(UUID playerUuid, long timestamp) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                pvPListEntry.timestamp(timestamp);
                break;
            }
        }

        return timestamp;
    }

    public boolean equalsPlayerUuid(UUID playerUuid) {
        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                return true;
            }
        }
        return false;
    }

    public long timestamp(UUID playerUuid) {

        for (PvPListEntry pvPListEntry : pvplistEntries) {
            if (pvPListEntry.playerUuid().equals(playerUuid)) {
                return pvPListEntry.timestamp();
            }
        }

        return -1;
    }

    public void updateDatabase(UUID playerUuid) {
        PvPListSQL.updateStateFromDataBase(playerUuid);
        PvPListSQL.updateTimestampFromDataBase(playerUuid);
    }
}