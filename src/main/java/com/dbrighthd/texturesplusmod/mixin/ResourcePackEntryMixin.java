package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.util.PackUtil;
import com.dbrighthd.texturesplusmod.util.TexturesPlusPackType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(TransferableSelectionList.PackEntry.class)
@SuppressWarnings("DataFlowIssue")
public class ResourcePackEntryMixin {
    @Shadow @Final private PackSelectionModel.Entry pack;

    @Unique private static final Component SUBOPTIMAL_MESSAGE = Component.translatable("texturesplusmod.suboptimal");
    @Unique private static final Component MODS_ARE_MISSING_MESSAGE = Component.translatable("texturesplusmod.some_mods_are_missing").withStyle(ChatFormatting.GRAY);
    @Unique private static final Component REQUIRED_MODS_ARE_MISSING_MESSAGE = Component.translatable("texturesplusmod.some_required_mods_are_missing").withStyle(ChatFormatting.GRAY);

    @Inject(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z", ordinal = 0))
    public void renderYellowBackground(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (PackUtil.isTexturesPlusPack(pack.getId())) {
            TexturesPlusPackType type = PackUtil.getPackType(pack.getId());
            int x = ((TransferableSelectionList.PackEntry) (Object) this).getContentX() - 1;
            int y = ((TransferableSelectionList.PackEntry) (Object) this).getContentY() - 1;
            int w = ((TransferableSelectionList.PackEntry) (Object) this).getContentRight() + 1;
            int h = ((TransferableSelectionList.PackEntry) (Object) this).getContentBottom() + 1;
            if (!PackUtil.areRequiredModsPresentForPackType(type)) {
                context.fill(x, y, w, h, 0xFF770000);
            } else if (!PackUtil.areOptionalModsPresentForPackType(type)) {
                context.fill(x, y, w, h, 0xFF716000);
            }
        }
    }

    @Inject(method = "renderContent", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z", ordinal = 1))
    public void renderCompatibilityMessage(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (PackUtil.isTexturesPlusPack(pack.getId())) {
            TexturesPlusPackType type = PackUtil.getPackType(pack.getId());
            if (!PackUtil.areRequiredModsPresentForPackType(type)) {
                ((TransferableSelectionList.PackEntry) (Object) this).nameWidget.setMessage(TransferableSelectionList.INCOMPATIBLE_TITLE);
                ((TransferableSelectionList.PackEntry) (Object) this).descriptionWidget.setMessage(REQUIRED_MODS_ARE_MISSING_MESSAGE);
            } else if (!PackUtil.areOptionalModsPresentForPackType(type)) {
                ((TransferableSelectionList.PackEntry) (Object) this).nameWidget.setMessage(SUBOPTIMAL_MESSAGE);
                ((TransferableSelectionList.PackEntry) (Object) this).descriptionWidget.setMessage(MODS_ARE_MISSING_MESSAGE);
            }
        }
    }

    @Inject(method = "renderContent", at = @At(value = "TAIL"))
    public void renderModListTooltip(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (hovered) {
            if (PackUtil.isTexturesPlusPack(pack.getId())) {
                TexturesPlusPackType type = PackUtil.getPackType(pack.getId());
                if (!PackUtil.areRequiredModsPresentForPackType(type) || !PackUtil.areOptionalModsPresentForPackType(type)) {
                    Font font = Minecraft.getInstance().font;
                    String missingRequired = PackUtil.getMissingRequiredMods(pack.getId());
                    String missingOptional = PackUtil.getMissingOptionalMods(pack.getId());
                    List<FormattedCharSequence> lines = new ArrayList<>();

                    if (!missingRequired.isBlank()) {
                        List<FormattedCharSequence> requiredLines = font.split(FormattedText.of("Missing (Required) Mods: " + missingRequired), ((TransferableSelectionList.PackEntry) (Object) this).getContentWidth());
                        lines.addAll(requiredLines);
                    }
                    if (!missingOptional.isBlank()) {
                        List<FormattedCharSequence> optionalLines = font.split(FormattedText.of("Missing (Optional) Mods: " + missingOptional), ((TransferableSelectionList.PackEntry) (Object) this).getContentWidth());
                        lines.addAll(optionalLines);
                    }
                    context.setTooltipForNextFrame(font, lines, new BelowOrAboveWidgetTooltipPositioner(((TransferableSelectionList.PackEntry) (Object) this).getRectangle()), mouseX, mouseY, true);
                }
            }
        }
    }

    @Redirect(method = {"renderContent", "handlePackSelection"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackCompatibility;isCompatible()Z"))
    public boolean redirectIsCompatible(PackCompatibility compatibility) {
        return compatibility.isCompatible() || (PackUtil.isTexturesPlusPack(pack.getId()) && TexturesPlusModClient.getConfig().ignoreTexturesPlusMcmeta);
    }
}
