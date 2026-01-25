package com.dbrighthd.texturesplusmod.client;

import com.dbrighthd.texturesplusmod.pack.PackDownloader;
import com.dbrighthd.texturesplusmod.TexturesPlusMod;
import com.dbrighthd.texturesplusmod.client.config.ModConfig;
import com.dbrighthd.texturesplusmod.pack.PackMetadataManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ArrayListDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class TexturesPlusModClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(TexturesPlusMod.MOD_ID + "-client");

    private static final ArrayListDeque<Runnable> TASKS = new ArrayListDeque<>();
    private static PackMetadataManager metadataManager;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

        metadataManager = new PackMetadataManager(Minecraft.getInstance().getResourcePackRepository());

        if (getConfig().updatePacksOnStartup) {
            LOGGER.info("Fetching textures+ packs...");
            // this is never async because it happens during resource reload
            PackDownloader.downloadAllPacks(false).whenComplete(($, e) -> {
                if (e != null) {
                    LOGGER.error("There was an error while fetching textures+ packs", e);
                }
            });
            LOGGER.info("Finished fetching textures+ packs!");
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (RenderSystem.isOnRenderThread()) {
                Runnable task = TASKS.peekFirst();
                if (task != null) {
                    task.run();
                    TASKS.pop();
                }
            }
        });
    }

    public static void queueOnMainThread(Runnable runnable) {
        TASKS.addLast(runnable);
    }

    public static PackMetadataManager getMetadataManager() {
        return metadataManager;
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
