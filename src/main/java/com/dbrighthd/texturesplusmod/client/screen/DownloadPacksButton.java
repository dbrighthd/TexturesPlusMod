package com.dbrighthd.texturesplusmod.client.screen;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

public class DownloadPacksButton extends PressableWidget {
    public static final Identifier FOCUSED = Identifier.of(MODID, "textures/gui/sprites/button_focused.png");
    public static final Identifier UNFOCUSED = Identifier.of(MODID, "textures/gui/sprites/button_unfocused.png");
    public static final Identifier DISABLED = Identifier.of(MODID, "textures/gui/sprites/button_disabled.png");
    public static final Identifier LOADING = Identifier.of(MODID, "textures/gui/sprites/loading.png");

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerTexture(FOCUSED);
        textureManager.registerTexture(UNFOCUSED);
        textureManager.registerTexture(DISABLED);
        textureManager.registerTexture(LOADING);
    }

    private final Screen screen;

    private int tick = 0;
    private int cooldownTicks = 0;
    private float deltaAccumulator = 0;

    public DownloadPacksButton(Screen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Text.translatable("texturesplusmod.open_tooltip"));
        this.screen = screen;
        this.setTooltip(Tooltip.of(net.minecraft.text.Text.of("Click here to update Textures+ resource packs.")));
    }

    @Override
    public void onPress(AbstractInput input) {
        this.setFocused(false);
        this.active = false;

        ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
        Collection<String> previouslyEnabledPacks = resourcePackManager.getEnabledIds();

        PackGetterUtil.downloadAllPacks(TexturesPlusModClient.getConfig().async).whenComplete(($, err) -> {
            this.active = true;

            if (!PackGetterUtil.didAnyUpdate()) {
                System.out.println("None to update.");
                return;
            }

            if (TexturesPlusModClient.getConfig().async) {
                TexturesPlusModClient.queueOnMainThread(() -> {
                    Screen screenToReturnTo = MinecraftClient.getInstance().currentScreen;
                    MinecraftClient.getInstance().setScreen(new ReloadPrompt((confirmed) -> {
                        if (!confirmed) return;

                        if (MinecraftClient.getInstance().currentScreen == screenToReturnTo) screen.close();

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
    }

    @Override
    public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
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
        Identifier identifier = !this.active ? DISABLED : switch (this.getType()) {
            case NONE -> UNFOCUSED;
            case HOVERED, FOCUSED -> FOCUSED;
        };
        context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        if (!this.active) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, LOADING, this.getX(), this.getY(), 0,  32*tick, this.width, this.height, 32, 32, 32, 576);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.USAGE, Text.translatable("texturesplusmod.open_tooltip"));
    }
}
