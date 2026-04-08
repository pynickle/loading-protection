package com.loadingprotection.fabric.client;

import com.loadingprotection.LoadingProtectionMod;
import com.loadingprotection.config.LoadingProtectionConfig;
import com.loadingprotection.config.MessageMode;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class LoadingProtectionConfigScreenFabric {
    private LoadingProtectionConfigScreenFabric() {}

    public static Screen create(Screen parent) {
        LoadingProtectionConfig editable = LoadingProtectionConfig.get().copy();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("yacl3.config.loading_protection:config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("yacl3.config.loading_protection:category.server"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("yacl3.config.loading_protection:group.protection"))
                                .options(List.of(
                                        integerOption(
                                                "protectionDuration",
                                                60,
                                                1,
                                                300,
                                                () -> editable.protectionDuration,
                                                value -> editable.protectionDuration = value),
                                        booleanOption(
                                                "endOnMovement",
                                                true,
                                                () -> editable.endOnMovement,
                                                value -> editable.endOnMovement = value),
                                        booleanOption(
                                                "protectMount",
                                                false,
                                                () -> editable.protectMount,
                                                value -> editable.protectMount = value),
                                        booleanOption(
                                                "preventTargeting",
                                                true,
                                                () -> editable.preventTargeting,
                                                value -> editable.preventTargeting = value),
                                        booleanOption(
                                                "preventOutgoingDamage",
                                                true,
                                                () -> editable.preventOutgoingDamage,
                                                value -> editable.preventOutgoingDamage = value)))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("yacl3.config.loading_protection:group.triggers"))
                                .options(List.of(
                                        booleanOption(
                                                "triggerOnJoin",
                                                true,
                                                () -> editable.triggerOnJoin,
                                                value -> editable.triggerOnJoin = value),
                                        booleanOption(
                                                "triggerOnRespawn",
                                                false,
                                                () -> editable.triggerOnRespawn,
                                                value -> editable.triggerOnRespawn = value),
                                        booleanOption(
                                                "triggerOnDimensionChange",
                                                false,
                                                () -> editable.triggerOnDimensionChange,
                                                value -> editable.triggerOnDimensionChange = value)))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("yacl3.config.loading_protection:group.messages"))
                                .options(List.of(
                                        booleanOption(
                                                "showMessages",
                                                true,
                                                () -> editable.showMessages,
                                                value -> editable.showMessages = value),
                                        enumOption(
                                                "messageMode",
                                                MessageMode.BOTH,
                                                () -> editable.messageMode,
                                                value -> editable.messageMode = value)))
                                .build())
                        .build())
                .save(() -> LoadingProtectionConfig.save(editable))
                .build()
                .generateScreen(parent);
    }

    private static Option<Boolean> booleanOption(String key, boolean fallback, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(optionName(key))
                .description(OptionDescription.of(optionDescription(key)))
                .binding(fallback, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> integerOption(
            String key, int fallback, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(optionName(key))
                .description(OptionDescription.of(optionDescription(key)))
                .binding(fallback, getter, setter)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(min, max).step(1))
                .build();
    }

    private static Option<MessageMode> enumOption(
            String key, MessageMode fallback, Supplier<MessageMode> getter, Consumer<MessageMode> setter) {
        return Option.<MessageMode>createBuilder()
                .name(optionName(key))
                .description(OptionDescription.of(optionDescription(key)))
                .binding(fallback, getter, setter)
                .controller(option -> EnumControllerBuilder.create(option)
                        .enumClass(MessageMode.class)
                        .formatValue(value -> Component.translatable(
                                "yacl3.config.loading_protection:enum.message_mode."
                                        + value.name().toLowerCase())))
                .build();
    }

    private static Component optionName(String key) {
        return Component.translatable("yacl3.config." + LoadingProtectionMod.MOD_ID + ":option." + key);
    }

    private static Component optionDescription(String key) {
        return Component.translatable("yacl3.config." + LoadingProtectionMod.MOD_ID + ":option." + key + ".desc");
    }
}
