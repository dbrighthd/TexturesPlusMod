package com.dbrighthd.texturesplusmod.pack;

import com.dbrighthd.texturesplusmod.TexturesPlusMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public record TexturesPlusMetadata(List<String> optionalDependencies, List<String> requiredDependencies) {
    public static final Codec<TexturesPlusMetadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).fieldOf("optional_dependencies").forGetter(TexturesPlusMetadata::optionalDependencies),
                    Codec.list(Codec.STRING).fieldOf("required_dependencies").forGetter(TexturesPlusMetadata::requiredDependencies)
            ).apply(instance, TexturesPlusMetadata::new)
    );

    public static TexturesPlusMetadata fromStream(PackLocationInfo packInfo, InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(reader)).ifError(error -> TexturesPlusMod.LOGGER.error("Couldn't load {} metadata: {}", packInfo.id(), error.message())).result().orElse(null);
        } catch (Exception exception) {
            TexturesPlusMod.LOGGER.error("Couldn't load {} metadata: {}", packInfo.id(), exception.getMessage());
            return null;
        }
    }

    public boolean areRequiredModsPresent() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String modId : requiredDependencies) {
            if (!loader.isModLoaded(modId)) return false;
        }
        return true;
    }

    public boolean areOptionalModsPresent() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String modId : optionalDependencies) {
            if (!loader.isModLoaded(modId)) return false;
        }
        return true;
    }

    public String getMissingRequiredMods() {
        if (areRequiredModsPresent()) return "";
        FabricLoader loader = FabricLoader.getInstance();

        StringBuilder mods = new StringBuilder();
        for (String dep : requiredDependencies) {
            if (!loader.isModLoaded(dep)) {
                if (!mods.isEmpty()) mods.append(", ");
                mods.append(dep);
            }
        }
        return mods.toString();
    }

    public String getMissingOptionalMods() {
        if (areOptionalModsPresent()) return "";
        FabricLoader loader = FabricLoader.getInstance();

        StringBuilder mods = new StringBuilder();
        for (String dep : optionalDependencies) {
            if (!loader.isModLoaded(dep)) {
                if (!mods.isEmpty()) mods.append(", ");
                mods.append(dep);
            }
        }
        return mods.toString();
    }
}
