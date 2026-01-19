package com.dbrighthd.texturesplusmod.client.screen;

import com.google.common.collect.Lists;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

public class ReloadPrompt extends Screen {

    private static final Component MESSAGE = Component.translatable("texturesplusmod.reload.text");
    private MultiLineLabel messageSplit = MultiLineLabel.EMPTY;
    private int buttonEnableTimer;
    private final List<Button> buttons = Lists.newArrayList();

    private final Consumer<Boolean> applier;

    private void onConfirm(boolean confirmed) {
        this.applier.accept(confirmed);
        if (confirmed)  Minecraft.getInstance().reloadResourcePacks();
    }

    public ReloadPrompt(Consumer<Boolean> applier, boolean noPrompt) {
        super(Component.translatable("texturesplusmod.reload"));

        this.applier = applier;

        if (noPrompt) { // basically just bypass this screen
            this.onConfirm(true);
        }
    }

    @Override
    public @NonNull Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultiLineLabel.create(this.font, MESSAGE, this.width - 50);
        int i = Mth.clamp(90 + (this.messageSplit.getLineCount() * 9) + 12, this.height / 6 + 96, this.height - 24);
        this.buttons.clear();
        this.addButtons(i);
        disableButtons((int) (1.5 * 20));
    }

    protected void addButtons(int y) {
        this.addButton(Button.builder(CommonComponents.GUI_PROCEED, button -> this.onConfirm(true)).bounds(this.width / 2 - 155, y, 150, 20).build());
        this.addButton(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onConfirm(false)).bounds(this.width / 2 - 155 + 160, y, 150, 20).build());
    }

    protected void addButton(Button button) {
        this.buttons.add(this.addRenderableWidget(button));
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredString(this.font, this.title, this.width / 2, 70, CommonColors.WHITE);

        this.messageSplit.visitLines(TextAlignment.CENTER, this.width / 2, 90, 9, context.textRenderer());
    }

    public void disableButtons(int ticks) {
        this.buttonEnableTimer = ticks;

        for (Button buttonWidget : this.buttons) {
            buttonWidget.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.buttonEnableTimer == 0) {
            for (Button buttonWidget : this.buttons) {
                buttonWidget.active = true;
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onConfirm(false);
            return true;
        } else {
            return super.keyPressed(input);
        }
    }
}
