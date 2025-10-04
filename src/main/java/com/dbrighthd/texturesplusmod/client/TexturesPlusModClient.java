package com.dbrighthd.texturesplusmod.client;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import com.dbrighthd.texturesplusmod.client.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.collection.ArrayListDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class TexturesPlusModClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("texturesplusmod");

    private static final ArrayListDeque<Runnable> tasks = new ArrayListDeque<>();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

        if (getConfig().updatePacksOnStartup) {
            LOGGER.info("Fetching textures+ packs...");
            // this is never async because it happens during resource reload
            PackGetterUtil.downloadAllPacks(false).whenComplete(($, e) -> {
                if (e != null) {
                    LOGGER.error("There was an error while fetching textures+ packs", e);
                }
            });
            LOGGER.info("Finished fetching textures+ packs!");
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (RenderSystem.isOnRenderThread()) {
                Runnable task = tasks.peekFirst();
                if (task != null) {
                    System.out.println("Chat there was a task");
                    task.run();
                    tasks.pop();
                }
            }
        });
    }

    public static void queueOnMainThread(Runnable runnable) {
        tasks.addLast(runnable);
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
