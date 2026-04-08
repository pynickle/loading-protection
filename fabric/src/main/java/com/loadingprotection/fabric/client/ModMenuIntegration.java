package com.loadingprotection.fabric.client;

import com.loadingprotection.config.screen.MissingYaclScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")
                ? LoadingProtectionConfigScreenFabric.create(parent)
                : new MissingYaclScreen(parent);
    }
}
