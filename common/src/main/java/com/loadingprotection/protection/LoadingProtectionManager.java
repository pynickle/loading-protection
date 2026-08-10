package com.loadingprotection.protection;

import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.config.MessageMode;
import com.loadingprotection.config.ProtectionTrigger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;

public final class LoadingProtectionManager {
    private static final Set<Integer> MESSAGE_MILESTONES = Set.of(10, 5, 3, 2, 1);
    private static final double MOVEMENT_THRESHOLD_SQR = 0.0001D;
    private static final double TARGET_CLEAR_RADIUS = 96.0D;
    private static final Map<UUID, ProtectionSession> ACTIVE_SESSIONS = new HashMap<>();

    private LoadingProtectionManager() {}

    public static void startProtection(ServerPlayer player, ProtectionTrigger trigger) {
        LoadingProtectionConfig config = LoadingProtectionConfig.get();
        if (!config.isTriggerEnabled(trigger)) {
            return;
        }

        int durationTicks = config.protectionDuration * 20;
        if (durationTicks <= 0) {
            return;
        }

        ACTIVE_SESSIONS.put(player.getUUID(), new ProtectionSession(player.getUUID(), player.position(), durationTicks));
        sendStartMessage(player, config.protectionDuration, trigger);
    }

    public static void stopProtection(ServerPlayer player, ProtectionEndReason reason) {
        ProtectionSession removed = ACTIVE_SESSIONS.remove(player.getUUID());
        if (removed != null && reason != ProtectionEndReason.DISCONNECT && reason != ProtectionEndReason.REPLACED) {
            sendEndMessage(player, reason);
        }
    }

    public static void clearPlayer(ServerPlayer player) {
        stopProtection(player, ProtectionEndReason.DISCONNECT);
    }

    public static boolean isProtected(ServerPlayer player) {
        return ACTIVE_SESSIONS.containsKey(player.getUUID());
    }

    public static void tick(MinecraftServer server) {
        if (ACTIVE_SESSIONS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, ProtectionSession>> iterator = ACTIVE_SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ProtectionSession> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }

            ProtectionSession session = entry.getValue();
            LoadingProtectionConfig config = LoadingProtectionConfig.get();

            if (config.endOnMovement
                    && player.position().distanceToSqr(session.startPosition()) > MOVEMENT_THRESHOLD_SQR) {
                iterator.remove();
                sendEndMessage(player, ProtectionEndReason.MOVEMENT);
                continue;
            }

            if (config.preventTargeting) {
                clearMobTargets(player, config.protectMount ? player.getVehicle() : null);
            }

            int ticksRemaining = session.ticksRemaining() - 1;
            session.setTicksRemaining(ticksRemaining);

            int secondsRemaining = (ticksRemaining + 19) / 20;
            if (ticksRemaining <= 0) {
                iterator.remove();
                sendEndMessage(player, ProtectionEndReason.TIMER);
                continue;
            }

            if (config.showMessages && MESSAGE_MILESTONES.contains(secondsRemaining) && session.markAnnounced(secondsRemaining)) {
                sendMessage(
                        player,
                        Component.translatable("message.loading_protection.remaining", secondsRemaining),
                        config.messageMode);
            }
        }
    }

    public static boolean shouldAllowDamage(LivingEntity victim, DamageSource source) {
        LoadingProtectionConfig config = LoadingProtectionConfig.get();
        Entity attacker = resolveAttacker(source);

        if (victim instanceof ServerPlayer player && isProtected(player)) {
            return false;
        }

        if (config.protectMount && isProtectedMount(victim)) {
            return false;
        }

        if (config.preventOutgoingDamage && attacker instanceof ServerPlayer player && isProtected(player)) {
            return false;
        }

        return true;
    }

    private static Entity resolveAttacker(DamageSource source) {
        return source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
    }

    private static boolean isProtectedMount(Entity entity) {
        for (UUID playerId : ACTIVE_SESSIONS.keySet()) {
            ServerPlayer player = findPlayer(entity, playerId);
            if (player != null && player.getVehicle() == entity) {
                return true;
            }
        }

        return false;
    }

    private static ServerPlayer findPlayer(Entity referenceEntity, UUID playerId) {
        MinecraftServer server = referenceEntity.level().getServer();
        return server == null ? null : server.getPlayerList().getPlayer(playerId);
    }

    private static void clearMobTargets(ServerPlayer player, Entity protectedMount) {
        ServerLevel level = player.level();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(TARGET_CLEAR_RADIUS))) {
            LivingEntity target = mob.getTarget();
            if (target == player || (protectedMount instanceof LivingEntity livingMount && target == livingMount)) {
                mob.setTarget(null);
                mob.setLastHurtByMob(null);
                if (mob instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
            }
        }
    }

    private static void sendStartMessage(ServerPlayer player, int durationSeconds, ProtectionTrigger trigger) {
        LoadingProtectionConfig config = LoadingProtectionConfig.get();
        if (!config.showMessages) {
            return;
        }

        sendMessage(
                player,
                Component.translatable(
                        "message.loading_protection.started",
                        durationSeconds,
                        Component.translatable(triggerTranslationKey(trigger))),
                config.messageMode);
    }

    private static void sendEndMessage(ServerPlayer player, ProtectionEndReason reason) {
        LoadingProtectionConfig config = LoadingProtectionConfig.get();
        if (!config.showMessages) {
            return;
        }

        Component message = switch (reason) {
            case MOVEMENT -> Component.translatable("message.loading_protection.ended_movement");
            case TIMER -> Component.translatable("message.loading_protection.ended_timer");
            default -> null;
        };

        if (message != null) {
            sendMessage(player, message, config.messageMode);
        }
    }

    private static String triggerTranslationKey(ProtectionTrigger trigger) {
        return switch (trigger) {
            case JOIN -> "message.loading_protection.trigger.join";
            case RESPAWN -> "message.loading_protection.trigger.respawn";
            case DIMENSION_CHANGE -> "message.loading_protection.trigger.dimension_change";
        };
    }

    private static void sendMessage(ServerPlayer player, Component message, MessageMode mode) {
        switch (mode) {
            case CHAT -> player.sendSystemMessage(message);
            case ACTION_BAR -> player.connection.send(new ClientboundSetActionBarTextPacket(message));
            case BOTH -> {
                player.sendSystemMessage(message);
                player.connection.send(new ClientboundSetActionBarTextPacket(message));
            }
        }
    }
}
