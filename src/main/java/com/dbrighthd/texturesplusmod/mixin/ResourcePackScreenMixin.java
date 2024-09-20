package com.dbrighthd.texturesplusmod.mixin;
import com.dbrighthd.texturesplusmod.PackGetterUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
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

    @Shadow
    @Final
    private Path file;

    protected ResourcePackScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void addTexturesButton(CallbackInfo ci)
    {
        assert this.client != null;
        if (this.file.equals(this.client.getResourcePackDir())) {
            this.addDrawableChild(
                new TexturedButtonWidget(
                    22, (this.height - 40), 22, 22,
                    new ButtonTextures(textures$UNFOCUSED, textures$FOCUSED),
                    (button) -> {
                        button.active = false;
                        PackGetterUtil.downloadAllPacks().whenComplete(($, err) -> button.active = true);
                    },
                    Text.translatable(MODID + ".open_tooltip"))
                {
                    private int tick = 0;
                    private float deltaAccumulator = 0;

                    {
                        setTooltip(Tooltip.of(Text.of(("Click here to update Textures+ resource packs."))));
                    }

                    //thank you Traben for letting me copy this code
                    @Override
                    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                        deltaAccumulator += delta;
                        if (deltaAccumulator*1000 >= 50) {
                            deltaAccumulator = 0;
                            tick++;
                        }
                        Identifier identifier = !this.active ? textures$DISABLED : this.isSelected() ? textures$FOCUSED : textures$UNFOCUSED;
                        context.drawTexture(identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                        if (!this.active) {
                            context.drawTexture(textures$LOADING, this.getX(), this.getY(), this.width, this.height, 0,  32*(tick % 18), 32, 32, 32, 576);
                        }
                    }
                });
        }
    }
}
