package com.dbrighthd.texturesplusmod.client.screen;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import org.jspecify.annotations.NonNull;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

public class DownloadPacksButton extends AbstractButton {
    public static final Identifier FOCUSED = Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/button_focused.png");
    public static final Identifier UNFOCUSED = Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/button_unfocused.png");
    public static final Identifier DISABLED = Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/button_disabled.png");
    public static final Identifier LOADING = Identifier.fromNamespaceAndPath(MODID, "textures/gui/sprites/loading.png");

    public static final Identifier FOCUSED_RELATIVE = Identifier.fromNamespaceAndPath(MODID, "button_focused");
    public static final Identifier UNFOCUSED_RELATIVE = Identifier.fromNamespaceAndPath(MODID, "button_unfocused");
    public static final Identifier DISABLED_RELATIVE = Identifier.fromNamespaceAndPath(MODID, "button_disabled");

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerForNextReload(FOCUSED);
        textureManager.registerForNextReload(UNFOCUSED);
        textureManager.registerForNextReload(DISABLED);
        textureManager.registerForNextReload(LOADING);
    }

    private final Screen screen;

    private int tick = 0;
    private int cooldownTicks = 0;
    private float deltaAccumulator = 0;

    public DownloadPacksButton(Screen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("texturesplusmod.open_tooltip"));
        this.screen = screen;
        this.setTooltip(Tooltip.create(net.minecraft.network.chat.Component.nullToEmpty("Click here to update Textures+ resource packs.")));
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        this.setFocused(false);
        this.active = false;

        PackRepository resourcePackManager = Minecraft.getInstance().getResourcePackRepository();
        Collection<String> previouslyEnabledPacks = resourcePackManager.getSelectedIds();

        PackGetterUtil.downloadAllPacks(TexturesPlusModClient.getConfig().async).whenComplete(($, err) -> {
            this.active = true;

            if (!PackGetterUtil.didAnyUpdate()) {
                System.out.println("None to update.");
                return;
            }

            if (TexturesPlusModClient.getConfig().async) {
                TexturesPlusModClient.queueOnMainThread(() -> {
                    Screen screenToReturnTo = Minecraft.getInstance().screen;
                    Minecraft.getInstance().setScreen(new ReloadPrompt((confirmed) -> {
                        if (!confirmed) return;

                        if (Minecraft.getInstance().screen == screenToReturnTo) screen.onClose();

                        // keep packs enabled, then rescan
                        resourcePackManager.setSelected(previouslyEnabledPacks);
                        resourcePackManager.reload();

                        if (Minecraft.getInstance().screen != screenToReturnTo) {
                            Minecraft.getInstance().setScreen(screenToReturnTo);
                        }
                    }, false));
                });
            }
        });
    }

    @Override
    public void renderContents(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
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
        Identifier identifier = !this.active ? DISABLED : this.isHoveredOrFocused() ? FOCUSED : UNFOCUSED;
        context.blit(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        if (!this.active) {
            context.blit(RenderPipelines.GUI_TEXTURED, LOADING, this.getX(), this.getY(), 0,  32*tick, this.width, this.height, 32, 32, 32, 576);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.USAGE, Component.translatable("texturesplusmod.open_tooltip"));
    }
}
