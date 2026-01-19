package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.client.screen.DownloadPacksButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;

@Mixin(PackSelectionScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Unique
    private DownloadPacksButton tButton;

    @Shadow
    @Final
    private Path packDir;

    protected PackScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void textures$refreshWidgetPositions(CallbackInfo ci) {
        if (this.tButton != null) {
            this.tButton.setPosition(22, (this.height - 40));
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addTexturesButton(CallbackInfo ci) {
        if (this.packDir.equals(this.minecraft.getResourcePackDirectory())) {
            this.tButton = new DownloadPacksButton(this, 22, (this.height - 40), 22, 22);
            this.addRenderableWidget(this.tButton);
        }
    }
}
