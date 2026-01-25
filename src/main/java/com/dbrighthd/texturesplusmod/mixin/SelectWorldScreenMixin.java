package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.TexturesPlusWorldGenerator;
import com.dbrighthd.texturesplusmod.client.screen.DownloadPacksButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MOD_ID;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    protected SelectWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addButton(CallbackInfo ci) {
        int x = 22;
        int y = this.height - 40;

        ImageButton tButton = new ImageButton(
                x, y, 22, 22,
                new WidgetSprites(DownloadPacksButton.UNFOCUSED_RELATIVE, DownloadPacksButton.DISABLED_RELATIVE, DownloadPacksButton.FOCUSED_RELATIVE),
                (button) -> onPressed((ImageButton) button),
                Component.translatable(MOD_ID + ".open_tooltip")
        );

        tButton.setTooltip(Tooltip.create(Component.nullToEmpty("Click here to generate a Textures+ test world")));

        this.addRenderableWidget(tButton);
    }

    @Unique
    private void onPressed(ImageButton button) {
        button.setFocused(false);
        button.active = false;

        TexturesPlusWorldGenerator.generateWorldAsync().thenRun(() ->
            Minecraft.getInstance().execute(() -> {
                button.active = true;
                Minecraft.getInstance().setScreen(new SelectWorldScreen(this));
            })
        );
    }
}
