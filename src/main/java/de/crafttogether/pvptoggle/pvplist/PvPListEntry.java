package de.crafttogether.pvptoggle.pvplist;

import java.util.UUID;


class PvPListEntry {

    private final UUID playerUuid;
    private boolean state;
    private long timestamp;

    protected PvPListEntry(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.state = false;
        this.timestamp = 0;
    }

    protected PvPListEntry(UUID playerUuid, boolean state) {
        this.playerUuid = playerUuid;
        this.state = state;
        this.timestamp = 0;
    }

    protected PvPListEntry(UUID playerUuid, boolean state, long timestamp) {
        this.playerUuid = playerUuid;
        this.state = state;
        this.timestamp = timestamp;
    }

    protected boolean state(boolean state) {
        return this.state = state;
    }

    protected boolean state() {
        return state;
    }

    protected long timestamp(long timestamp) {
        return this.timestamp = timestamp;
    }

    protected long timestamp() {
        return timestamp;
    }

    protected UUID playerUuid() {
        return playerUuid;
    }
}
