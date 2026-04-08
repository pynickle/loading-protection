package com.loadingprotection.fabric;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.ProtectionTrigger;
import com.loadingprotection.protection.LoadingProtectionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class LoadingProtectionFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LoadingProtectionMod.init(FabricLoader.getInstance().getConfigDir());

        ServerPlayerEvents.JOIN.register((player) -> LoadingProtectionManager.startProtection(player, ProtectionTrigger.JOIN));
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> LoadingProtectionManager.startProtection(newPlayer, ProtectionTrigger.RESPAWN));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(
                (player, origin, destination) -> LoadingProtectionManager.startProtection(player, ProtectionTrigger.DIMENSION_CHANGE));
        ServerPlayerEvents.LEAVE.register(LoadingProtectionManager::clearPlayer);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> LoadingProtectionManager.shouldAllowDamage(entity, source));
        ServerTickEvents.END_SERVER_TICK.register(LoadingProtectionManager::tick);
    }
}
