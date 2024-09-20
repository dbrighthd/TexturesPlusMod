package com.dbrighthd.texturesplusmod.client;

import com.dbrighthd.texturesplusmod.PackGetterUtil;
import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class TexturesPlusModClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("texturesplusmod");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Fetching textures+ packs...");
        PackGetterUtil.downloadAllPacks().whenComplete(($, e) -> {
            LOGGER.info("Finished fetching textures+ packs!");
            if (e != null) {
                LOGGER.error("There was an error while fetching textures+ packs", e);
            }
        });
    }
}
