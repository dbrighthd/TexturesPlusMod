package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.client.screen.DownloadPacksButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    private TextureManager textureManager;

    // this is a hack so we don't load textures when we're not supposed to lol
    @Inject(method = "<init>", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/screen/TitleScreen;registerTextures(Lnet/minecraft/client/texture/TextureManager;)V"))
    public void textures$registerTextures(RunArgs args, CallbackInfo ci) {
        DownloadPacksButton.registerTextures(textureManager);
    }
}
