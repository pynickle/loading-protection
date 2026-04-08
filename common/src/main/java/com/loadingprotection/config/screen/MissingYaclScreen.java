package com.loadingprotection.config.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class MissingYaclScreen extends Screen {
    private final Screen parent;

    public MissingYaclScreen(Screen parent) {
        super(Component.translatable("screen.loading_protection.missing_yacl.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> minecraft.setScreen(parent))
                .bounds(this.width / 2 - 75, this.height - 28, 150, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
