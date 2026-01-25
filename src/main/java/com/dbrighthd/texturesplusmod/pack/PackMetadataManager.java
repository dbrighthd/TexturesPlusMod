package com.dbrighthd.texturesplusmod.pack;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PackMetadataManager {
    private final Map<String, TexturesPlusMetadata> metadataCache = new HashMap<>();
    private final PackRepository repository;

    public PackMetadataManager(PackRepository repository) {
        this.repository = repository;
    }

    public void processPack(String packId) {
        Pack pack = repository.getPack(packId);
        if (pack != null) {
            try (PackResources resources = pack.open()) {
                IoSupplier<InputStream> metadataAsset = resources.getRootResource("texturesplus.mcmeta");
                if (metadataAsset != null) {
                    TexturesPlusMetadata metadata = TexturesPlusMetadata.fromStream(pack.location(), metadataAsset.get());
                    if (metadata != null) {
                        metadataCache.put(packId, metadata);
                        return;
                    }
                }
            } catch (IOException ex) {
                TexturesPlusModClient.LOGGER.error("Failed to get t+ metadata from pack {} (see log for details)", packId);
            }
        } else TexturesPlusModClient.LOGGER.warn("Pack {} is null", packId);
        metadataCache.remove(packId);
    }

    public @Nullable TexturesPlusMetadata getMetadataForPack(String packId) {
        return metadataCache.get(packId);
    }
}
