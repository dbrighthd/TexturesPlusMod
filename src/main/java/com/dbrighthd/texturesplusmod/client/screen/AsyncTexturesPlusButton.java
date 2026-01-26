package com.dbrighthd.texturesplusmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MOD_ID;

public class AsyncTexturesPlusButton<T> extends AbstractButton {
    public static final Identifier FOCUSED = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/button_focused.png");
    public static final Identifier UNFOCUSED = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/button_unfocused.png");
    public static final Identifier DISABLED = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/button_disabled.png");
    public static final Identifier LOADING = Identifier.fromNamespaceAndPath(MOD_ID, "textures/gui/sprites/loading.png");

    public static final Identifier FOCUSED_RELATIVE = Identifier.fromNamespaceAndPath(MOD_ID, "button_focused");
    public static final Identifier UNFOCUSED_RELATIVE = Identifier.fromNamespaceAndPath(MOD_ID, "button_unfocused");
    public static final Identifier DISABLED_RELATIVE = Identifier.fromNamespaceAndPath(MOD_ID, "button_disabled");

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerForNextReload(FOCUSED);
        textureManager.registerForNextReload(UNFOCUSED);
        textureManager.registerForNextReload(DISABLED);
        textureManager.registerForNextReload(LOADING);
    }

    private int tick = 0;
    private int cooldownTicks = 0;
    private float deltaAccumulator = 0;

    private final Component narration;
    private final Supplier<CompletableFuture<T>> onPress;
    private final Consumer<T> whenComplete;

    public AsyncTexturesPlusButton(int x, int y, int width, int height, Component inactiveTooltip, Component narration, Supplier<CompletableFuture<T>> onPress, Consumer<T> whenComplete) {
        super(x, y, width, height, inactiveTooltip);
        this.narration = narration;
        this.onPress = onPress;
        this.whenComplete = whenComplete;
    }

    @Override
    public void onPress(@NonNull InputWithModifiers input) {
        this.setFocused(false);
        this.active = false;
        this.onPress.get().whenComplete((t, err) -> {
            this.active = true;
            whenComplete.accept(t);
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
        builder.add(NarratedElementType.USAGE, narration);
    }
}
