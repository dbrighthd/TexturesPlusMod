package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.client.screen.ReloadPrompt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Collection;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

@Mixin(PackScreen.class)
public abstract class ResourcePackScreenMixin extends Screen {
    @Unique
    private static final Identifier textures$FOCUSED = Identifier.of(MODID, "button_focused");
    @Unique
    private static final Identifier textures$UNFOCUSED = Identifier.of(MODID, "button_unfocused");
    @Unique
    private static final Identifier textures$DISABLED = Identifier.of(MODID, "button_disabled");
    @Unique
    private static final Identifier textures$LOADING = Identifier.of(MODID, "loading");

    @Unique
    private TexturedButtonWidget tButton;

    @Shadow
    @Final
    private Path file;

    protected ResourcePackScreenMixin(Text title) {
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
            this.tButton = new TexturedButtonWidget(
                    22, (this.height - 40),
                    22, 22,
                    new ButtonTextures(textures$UNFOCUSED, textures$DISABLED, textures$FOCUSED),
                    (button) -> {
                        button.setFocused(false);
                        button.active = false;

                        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
                        Collection<String> previouslyEnabledPacks = resourcePackManager.getEnabledIds();

                        PackGetterUtil.downloadAllPacks(TexturesPlusModClient.getConfig().async).whenComplete(($, err) -> {
                            button.active = true;

                            if (!PackGetterUtil.didAnyUpdate()) {
                                System.out.println("None to update.");
                                return;
                            }

                            if (TexturesPlusModClient.getConfig().async) {
                                TexturesPlusModClient.queueOnMainThread(() -> {
                                    Screen screenToReturnTo = MinecraftClient.getInstance().currentScreen;
                                    MinecraftClient.getInstance().setScreen(new ReloadPrompt((confirmed) -> {
                                        if (!confirmed) return;

                                        if (MinecraftClient.getInstance().currentScreen == screenToReturnTo) this.close();

                                        // keep packs enabled, then rescan
                                        resourcePackManager.setEnabledProfiles(previouslyEnabledPacks);
                                        resourcePackManager.scanPacks();

                                        if (MinecraftClient.getInstance().currentScreen != screenToReturnTo) {
                                            MinecraftClient.getInstance().setScreen(screenToReturnTo);
                                        }
                                    }, false));
                                });
                            }
                        });
                    },
                    Text.translatable(MODID + ".open_tooltip")
            );

            this.tButton.setTooltip(Tooltip.of(Text.of("Click here to update Textures+ resource packs.")));

            this.addDrawableChild(this.tButton);
        }
    }
}
