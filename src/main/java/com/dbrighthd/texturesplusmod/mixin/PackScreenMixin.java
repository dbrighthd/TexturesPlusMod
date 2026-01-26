package com.dbrighthd.texturesplusmod.mixin;

import com.dbrighthd.texturesplusmod.TexturesPlusMod;
import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.client.screen.AsyncTexturesPlusButton;
import com.dbrighthd.texturesplusmod.client.screen.ReloadPrompt;
import com.dbrighthd.texturesplusmod.pack.PackDownloader;
import com.dbrighthd.texturesplusmod.pack.PackMetadataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import oshi.util.tuples.Pair;

@Mixin(PackSelectionScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Unique
    private AbstractButton tButton;

    @Shadow
    @Final
    private Path packDir;

    @Shadow
    @Final
    private PackSelectionModel model;

    protected PackScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void textures$reload(CallbackInfo ci) {
        TexturesPlusModClient.LOGGER.info("Checking all packs for t+ meta...");
        PackMetadataManager manager = TexturesPlusModClient.getMetadataManager();
        this.model.getUnselected().map(PackSelectionModel.Entry::getId).forEach(manager::processPack);
        this.model.getSelected().map(PackSelectionModel.Entry::getId).forEach(manager::processPack);
    }

    @Inject(method = "repositionElements", at = @At("RETURN"))
    private void textures$refreshWidgetPositions(CallbackInfo ci) {
        if (this.tButton != null) {
            this.tButton.setPosition(22, (this.height - 40));
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void textures$addTexturesButton(CallbackInfo ci) {
        if (this.packDir.equals(this.minecraft.getResourcePackDirectory())) {
            PackRepository resourcePackManager = Minecraft.getInstance().getResourcePackRepository();
            AtomicReference<Collection<String>> previouslyEnabledPacks = new AtomicReference<>();
            AtomicReference<Screen> previousScreen = new AtomicReference<>();
            AtomicBoolean anyUpdated = new AtomicBoolean();
            this.tButton = new AsyncTexturesPlusButton<>(
                    22, (this.height - 40), 22, 22,
                    Component.translatable("texturesplusmod.open_tooltip"), Component.translatable("texturesplusmod.open_tooltip"),
                    () -> {
                        previousScreen.set(Minecraft.getInstance().screen);
                        previouslyEnabledPacks.set(resourcePackManager.getSelectedIds());
                        boolean async = TexturesPlusModClient.getConfig().async;
                        CompletableFuture<Pair<String, Boolean>> elytras = PackDownloader.downloadResourcePack("elytras", !async);
                        CompletableFuture<Pair<String, Boolean>> pumpkins = PackDownloader.downloadResourcePack("pumpkins", !async);
                        CompletableFuture<Pair<String, Boolean>> weapons = PackDownloader.downloadResourcePack("weapons", !async);
                        CompletableFuture<Pair<String, Boolean>> creatures = PackDownloader.downloadResourcePack("creatures", !async);
                        elytras = elytras.whenComplete((p,t) -> {if (p.getB()) anyUpdated.set(true);});
                        pumpkins = pumpkins.whenComplete((p,t) -> {if (p.getB()) anyUpdated.set(true);});
                        weapons = weapons.whenComplete((p,t) -> {if (p.getB()) anyUpdated.set(true);});
                        creatures = creatures.whenComplete((p,t) -> {if (p.getB()) anyUpdated.set(true);});
                        return CompletableFuture.allOf(elytras, pumpkins, weapons, creatures);
                    }, ($) -> {
                        if (!anyUpdated.get()) {
                            TexturesPlusMod.LOGGER.info("None to update.");
                            return;
                        }

                        if (TexturesPlusModClient.getConfig().async) {
                            Minecraft.getInstance().execute(() -> {
                                Screen screenToReturnTo = Minecraft.getInstance().screen;
                                Minecraft.getInstance().setScreen(new ReloadPrompt((confirmed) -> {
                                    if (!confirmed) return;

                                    if (Minecraft.getInstance().screen == screenToReturnTo) previousScreen.get().onClose();

                                    // keep packs enabled, then rescan
                                    resourcePackManager.setSelected(previouslyEnabledPacks.get());
                                    resourcePackManager.reload();

                                    if (Minecraft.getInstance().screen != screenToReturnTo) {
                                        Minecraft.getInstance().setScreen(screenToReturnTo);
                                    }
                                }, false));
                            });
                        }
                    }
            );
            tButton.setTooltip(Tooltip.create(Component.nullToEmpty("Click here to update Textures+ resource packs.")));
            this.addRenderableWidget(this.tButton);
        }
    }
}
