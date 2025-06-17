package com.dbrighthd.texturesplusmod.mixin;
import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.client.screen.ReloadPrompt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import java.nio.file.Path;
import java.util.Collection;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

@Mixin(PackScreen.class)
public abstract class ResourcePackScreenMixin extends Screen {
    @Unique
    private static final Identifier textures$FOCUSED = Identifier.of(MODID, "textures/gui/button_focused.png");
    @Unique
    private static final Identifier textures$UNFOCUSED = Identifier.of(MODID, "textures/gui/button_unfocused.png");
    @Unique
    private static final Identifier textures$DISABLED = Identifier.of(MODID, "textures/gui/button_disabled.png");
    @Unique
    private static final Identifier textures$LOADING = Identifier.of(MODID, "textures/gui/loading.png");

    @Unique private TexturedButtonWidget tButton;

    @Shadow
    @Final
    private Path file;

    protected ResourcePackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "refreshWidgetPositions")
    private void refreshWidgetPositions(CallbackInfo ci) {
        if (this.tButton != null) {
            tButton.setPosition(22, (this.height - 40));
        }
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void addTexturesButton(CallbackInfo ci)
    {
        assert this.client != null;
        if (this.file.equals(this.client.getResourcePackDir())) {
            this.tButton = new TexturedButtonWidget(
                    22, (this.height - 40), 22, 22,
                    new ButtonTextures(textures$UNFOCUSED, textures$FOCUSED),
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
                                        // this code looks weird, but it basically just tells the resource manager "this is what we expect to be enabled"
                                        // the scan call after this then just asks it to make sure that everything we expect to enable actually exists.
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
                    Text.translatable(MODID + ".open_tooltip"))
            {
                private int tick = 0;
                private int cooldownTicks = 0;
                private float deltaAccumulator = 0;

                {
                    setTooltip(Tooltip.of(Text.of(("Click here to update Textures+ resource packs."))));
                }

                //thank you Traben for letting me copy this code
                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    deltaAccumulator += delta;
                    if (deltaAccumulator*1000 >= 200) { // ticks are 4x slower because methinks the animation would look nicer
                        deltaAccumulator = 0;
                        if (cooldownTicks > 0) cooldownTicks--;
                        else tick++;
                        if (tick > 18) {
                            cooldownTicks = 3;
                            tick = 0;
                        }
                    }
                    Identifier identifier = !this.active ? textures$DISABLED : switch (this.getType()) {
                        case NONE -> textures$UNFOCUSED;
                        case HOVERED, FOCUSED -> textures$FOCUSED;
                    };
                    context.drawTexture(RenderLayer::getGuiTextured, identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                    if (!this.active) {
                        context.drawTexture(RenderLayer::getGuiTextured, textures$LOADING, this.getX(), this.getY(), 0,  32*tick, this.width, this.height, 32, 32, 32, 576);
                    }
                }
            };
            this.addDrawableChild(tButton);
        }
    }
}
