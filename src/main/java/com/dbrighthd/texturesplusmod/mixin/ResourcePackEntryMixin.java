package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransferableSelectionList.PackEntry.class)
public class ResourcePackEntryMixin {
    @Shadow @Final private PackSelectionModel.Entry pack;

    @Inject(method = "renderContent", at = @At("HEAD"))
    public void render(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
    }

    @Redirect(method = {"renderContent", "handlePackSelection"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z"))
    public boolean redirectIsCompatible(PackCompatibility compatibility) {
        return compatibility.isCompatible() || (isTexturesPlusPack() && TexturesPlusModClient.getConfig().ignoreTexturesPlusMcmeta);
    }

    @Unique
    private boolean isTexturesPlusPack() {
        String desc = this.pack.getDescription().getString().toLowerCase();
        return desc.contains("textures")   &&
               desc.contains("+")          &&
               desc.contains("development");
    }
}
