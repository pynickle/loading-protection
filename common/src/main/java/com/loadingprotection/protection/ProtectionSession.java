package com.loadingprotection.protection;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public final class ProtectionSession {
    private final UUID playerId;
    private final Vec3 startPosition;
    private final Set<Integer> announcedSeconds = new HashSet<>();
    private int ticksRemaining;

    public ProtectionSession(UUID playerId, Vec3 startPosition, int ticksRemaining) {
        this.playerId = playerId;
        this.startPosition = startPosition;
        this.ticksRemaining = ticksRemaining;
    }

    public UUID playerId() {
        return playerId;
    }

    public Vec3 startPosition() {
        return startPosition;
    }

    public int ticksRemaining() {
        return ticksRemaining;
    }

    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    public boolean markAnnounced(int secondsRemaining) {
        return announcedSeconds.add(secondsRemaining);
    }
}
