package com.dbrighthd.texturesplusmod.client.screen;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.Alignment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class ReloadPrompt extends Screen {

    public static final Logger LOGGER = LoggerFactory.getLogger("texturesplusmod");

    private static final int TITLE_BOTTOM_MARGIN = 20;
    private static final Text MESSAGE = Text.translatable("texturesplusmod.reload.text");
    private MultilineText messageSplit = MultilineText.EMPTY;
    private int buttonEnableTimer;
    private final List<ButtonWidget> buttons = Lists.newArrayList();

    private final Consumer<Boolean> applier;

    private void onConfirm(boolean confirmed) {
        this.applier.accept(confirmed);
        if (confirmed)  MinecraftClient.getInstance().reloadResources();
    }

    public ReloadPrompt(Consumer<Boolean> applier, boolean noPrompt) {
        super(Text.translatable("texturesplusmod.reload"));

        this.applier = applier;

        if (noPrompt) { // basically just bypass this screen
            this.onConfirm(true);
        }
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), MESSAGE);
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultilineText.create(this.textRenderer, MESSAGE, this.width - 50);
        int i = MathHelper.clamp(90 + (this.messageSplit.getLineCount() * 9) + 12, this.height / 6 + 96, this.height - 24);
        this.buttons.clear();
        this.addButtons(i);
    }

    protected void addButtons(int y) {
        this.addButton(ButtonWidget.builder(ScreenTexts.PROCEED, button -> this.onConfirm(true)).dimensions(this.width / 2 - 155, y, 150, 20).build());
        this.addButton(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.onConfirm(false)).dimensions(this.width / 2 - 155 + 160, y, 150, 20).build());
    }

    protected void addButton(ButtonWidget button) {
        this.buttons.add(this.addDrawableChild(button));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 70, Colors.WHITE);

        this.messageSplit.draw(Alignment.CENTER, this.width / 2, 90, 9, context.getTextConsumer());
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return MathHelper.clamp(i - TITLE_BOTTOM_MARGIN - 9, 10, 80);
    }

    private int getMessageY() {
        return this.getTitleY() + TITLE_BOTTOM_MARGIN;
    }

    private int getMessagesHeight() {
        return this.messageSplit.getLineCount() * 9;
    }

    public void disableButtons(int ticks) {
        this.buttonEnableTimer = ticks;

        for (ButtonWidget buttonWidget : this.buttons) {
            buttonWidget.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.buttonEnableTimer == 0) {
            for (ButtonWidget buttonWidget : this.buttons) {
                buttonWidget.active = true;
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onConfirm(false);
            return true;
        } else {
            return super.keyPressed(input);
        }
    }
}
