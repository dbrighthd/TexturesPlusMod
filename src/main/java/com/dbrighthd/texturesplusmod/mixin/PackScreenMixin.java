package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.client.screen.DownloadPacksButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Unique
    private DownloadPacksButton tButton;

    @Shadow
    @Final
    private Path file;

    protected PackScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "refreshWidgetPositions", at = @At("RETURN"))
    private void textures$refreshWidgetPositions(CallbackInfo ci) {
        if (this.tButton != null) {
            this.tButton.setPosition(22, (this.height - 40));
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addTexturesButton(CallbackInfo ci) {
        assert this.client != null;

        if (this.file.equals(this.client.getResourcePackDir())) {
            this.tButton = new DownloadPacksButton(this, 22, (this.height - 40), 22, 22);
            this.addDrawableChild(this.tButton);
        }
    }
}
