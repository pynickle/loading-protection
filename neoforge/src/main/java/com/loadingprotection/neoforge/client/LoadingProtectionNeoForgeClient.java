package com.loadingprotection.neoforge.client;

import com.loadingprotection.config.screen.MissingYaclScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class LoadingProtectionNeoForgeClient {
    private LoadingProtectionNeoForgeClient() {}

    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) ->
                ModList.get().isLoaded("yet_another_config_lib_v3")
                        ? LoadingProtectionConfigScreenNeoForge.create(parent)
                        : new MissingYaclScreen(parent));
    }
}
