package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.TexturesPlusWorldGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.MODID;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    @Unique private static final Identifier textures$FOCUSED   = Identifier.of(MODID, "button_focused");
    @Unique private static final Identifier textures$UNFOCUSED = Identifier.of(MODID, "button_unfocused");
    @Unique private static final Identifier textures$DISABLED  = Identifier.of(MODID, "button_disabled");
    @Unique private static final Identifier textures$LOADING   = Identifier.of(MODID, "loading");

    protected SelectWorldScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addButton(CallbackInfo ci) {
        int x = 22;
        int y = this.height - 40;

        TexturedButtonWidget tButton = new TexturedButtonWidget(
                x, y, 22, 22,
                new ButtonTextures(textures$UNFOCUSED, textures$DISABLED, textures$FOCUSED),
                (button) -> onPressed((TexturedButtonWidget) button),
                Text.translatable(MODID + ".open_tooltip")
        );

        tButton.setTooltip(Tooltip.of(Text.of("Click here to generate a Textures+ test world")));

        this.addDrawableChild(tButton);
    }

    @Unique
    private void onPressed(TexturedButtonWidget button) {
        button.setFocused(false);
        button.active = false;

        TexturesPlusWorldGenerator.generateWorldAsync().thenRun(() -> {
            MinecraftClient.getInstance().execute(() -> {
                button.active = true;
                MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
            });
        });
    }
}
