package com.loadingprotection.neoforge;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.ProtectionTrigger;
import com.loadingprotection.neoforge.client.LoadingProtectionNeoForgeClient;
import com.loadingprotection.protection.LoadingProtectionManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.ModContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(LoadingProtectionMod.MOD_ID)
public final class LoadingProtectionNeoForge {
    public LoadingProtectionNeoForge(ModContainer container) {
        LoadingProtectionMod.init(FMLPaths.CONFIGDIR.get());

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            LoadingProtectionNeoForgeClient.register(container);
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        LoadingProtectionManager.startProtection((net.minecraft.server.level.ServerPlayer) event.getEntity(), ProtectionTrigger.JOIN);
    }

    private void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        LoadingProtectionManager.startProtection((net.minecraft.server.level.ServerPlayer) event.getEntity(), ProtectionTrigger.RESPAWN);
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        LoadingProtectionManager.startProtection(
                (net.minecraft.server.level.ServerPlayer) event.getEntity(), ProtectionTrigger.DIMENSION_CHANGE);
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LoadingProtectionManager.clearPlayer((net.minecraft.server.level.ServerPlayer) event.getEntity());
    }

    private void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!LoadingProtectionManager.shouldAllowDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        LoadingProtectionManager.tick(event.getServer());
    }
}
