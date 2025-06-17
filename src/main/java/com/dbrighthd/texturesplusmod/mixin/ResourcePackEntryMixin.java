package com.dbrighthd.texturesplusmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackListWidget.ResourcePackEntry.class)
public class ResourcePackEntryMixin {
    @Shadow @Final private ResourcePackOrganizer.Pack pack;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
    }

    @Redirect(method = {"render", "enable"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackCompatibility;isCompatible()Z"))
    public boolean redirectIsCompatible(ResourcePackCompatibility compatibility) {
        return compatibility.isCompatible() || isTexturesPlusPack();
    }

    @Unique
    private boolean isTexturesPlusPack() {
        String desc = this.pack.getDescription().getString().toLowerCase();
        return desc.contains("textures")   &&
               desc.contains("+")          &&
               desc.contains("development");
    }
}
