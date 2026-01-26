package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.TexturesPlusWorldGenerator;
import com.dbrighthd.texturesplusmod.client.screen.AsyncTexturesPlusButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    @Shadow @Final protected Screen lastScreen;

    protected SelectWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addButton(CallbackInfo ci) {
        int x = 22;
        int y = this.height - 40;

        AsyncTexturesPlusButton<Void> tButton = new AsyncTexturesPlusButton<>(
                x, y, 22, 22,
                Component.translatable("texturesplusmod.open_tooltip"), Component.translatable("texturesplusmod.open_tooltip"),
                TexturesPlusWorldGenerator::generateWorldAsync,
                ($) -> Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreen(new SelectWorldScreen(this.lastScreen));
                })
        );

        tButton.setTooltip(Tooltip.create(Component.nullToEmpty("Click here to generate a Textures+ test world")));

        this.addRenderableWidget(tButton);
    }
}
