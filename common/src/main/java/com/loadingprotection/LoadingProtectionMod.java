package com.loadingprotection;

import com.loadingprotection.config.LoadingProtectionConfig;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoadingProtectionMod {
    public static final String MOD_ID = "loading_protection";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private LoadingProtectionMod() {}

    public static void init(Path configDir) {
        LoadingProtectionConfig.init(configDir);
    }
}
