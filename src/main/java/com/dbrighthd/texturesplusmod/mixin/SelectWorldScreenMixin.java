package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.TexturesPlusWorldGenerator;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.client.screen.ReloadPrompt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {



    @Unique private static final Identifier textures$FOCUSED   = Identifier.of(MODID,"textures/gui/button_focused.png");
    @Unique private static final Identifier textures$UNFOCUSED = Identifier.of(MODID,"textures/gui/button_unfocused.png");
    @Unique private static final Identifier textures$DISABLED  = Identifier.of(MODID,"textures/gui/button_disabled.png");
    @Unique private static final Identifier textures$LOADING   = Identifier.of(MODID,"textures/gui/loading.png");

    @Unique private TexturedButtonWidget tButton;

    protected SelectWorldScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("RETURN"))
    private void texturesPlus$addButton(CallbackInfo ci) {


        int x = 22;
        int y = this.height - 40;

        this.tButton = new TexturedButtonWidget(
                x, y, 22, 22,
                new ButtonTextures(textures$UNFOCUSED, textures$FOCUSED),
                (button) -> onPressed((TexturedButtonWidget) button),
                Text.translatable(MODID + ".open_tooltip")
        ) {
            private int   tick            = 0;
            private int   cooldownTicks   = 0;
            private float deltaAccumulator = 0;

            {
                setTooltip(Tooltip.of(Text.of("Click here to generate a Textures+ test world")));
            }


            @Override
            public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
                deltaAccumulator += delta;
                if (deltaAccumulator * 1000 >= 200) {
                    deltaAccumulator = 0;
                    if (cooldownTicks > 0) cooldownTicks--;
                    else tick++;
                    if (tick > 18) { cooldownTicks = 3; tick = 0; }
                }

                Identifier tex = !this.active
                        ? textures$DISABLED
                        : switch (this.getType()) {
                    case NONE       -> textures$UNFOCUSED;
                    case HOVERED, FOCUSED -> textures$FOCUSED;
                };

                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex,     getX(), getY(), 0, 0,  this.width, this.height, this.width, this.height);
                if (!this.active) {
                    ctx.drawTexture(RenderPipelines.GUI_TEXTURED, textures$LOADING,
                            getX(), getY(), 0, 32 * tick, this.width, this.height,
                            32, 32, 32, 576);
                }
            }
        };

        this.addDrawableChild(tButton);
    }

    @Unique
    private void onPressed(TexturedButtonWidget button) {
        button.setFocused(false);
        button.active = false; // grey-out and show spinner

        TexturesPlusWorldGenerator.generateWorldAsync().thenRun(() -> {
            // Must go back to main thread before touching UI
            MinecraftClient.getInstance().execute(() -> {
                button.active = true;
                MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
            });
        });
    }
}
