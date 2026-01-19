package com.dbrighthd.texturesplusmod.util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PackUtil {
    private static final Map<TexturesPlusPackType, List<String>> REQUIRED_MODS = Map.of(
            TexturesPlusPackType.CREATURES, List.of("entity_model_features", "entity_texture_features"),
            TexturesPlusPackType.ELYTRAS, List.of("entity_model_features", "entity_texture_features", "animatica"),
            TexturesPlusPackType.WEAPONS, List.of(),
            TexturesPlusPackType.PUMPKINS, List.of()
    );
    private static final Map<TexturesPlusPackType, List<String>> OPTIONAL_MODS = Map.of(
            TexturesPlusPackType.CREATURES, List.of("entity_sound_features", "animatica"),
            TexturesPlusPackType.ELYTRAS, List.of(),
            TexturesPlusPackType.WEAPONS, List.of("variants-cit", "dbrighthd_regex_mod"),
            TexturesPlusPackType.PUMPKINS, List.of()
    );

    private static final List<String> KNOWN_PACK_IDS = List.of(
            "elytrasplus",
            "creaturesplus",
            "weaponsplus",
            "pumpkinsplus"
    );

    public static String sanitizePackId(String packId) {
        return packId.toLowerCase(Locale.ENGLISH).strip().replaceAll("file/", "");
    }

    public static boolean isTexturesPlusPack(String packId) {
        String sanitized = sanitizePackId(packId);
        return sanitized.matches("\\S*plus") && KNOWN_PACK_IDS.contains(sanitized);
    }

    public static String getMissingRequiredMods(String packId) {
        if (!isTexturesPlusPack(packId)) return "Error: not a textures+ pack";
        TexturesPlusPackType type = getPackType(packId);
        if (areRequiredModsPresentForPackType(type) && areOptionalModsPresentForPackType(type)) return "None! (this is a bug)";

        FabricLoader loader = FabricLoader.getInstance();

        StringBuilder mods = new StringBuilder();
        REQUIRED_MODS.get(type).forEach(s -> {
            if (!loader.isModLoaded(s)) {
                if (!mods.isEmpty()) mods.append(", ");
                mods.append(s);
            }
        });
        return mods.toString();
    }

    public static String getMissingOptionalMods(String packId) {
        if (!isTexturesPlusPack(packId)) return "Error: not a textures+ pack";
        TexturesPlusPackType type = getPackType(packId);
        if (areRequiredModsPresentForPackType(type) && areOptionalModsPresentForPackType(type)) return "None! (this is a bug)";

        FabricLoader loader = FabricLoader.getInstance();

        StringBuilder mods = new StringBuilder();
        OPTIONAL_MODS.get(type).forEach(s -> {
            if (!loader.isModLoaded(s)) {
                if (!mods.isEmpty()) mods.append(", ");
                mods.append(s);
            }
        });
        return mods.toString();
    }

    public static boolean areRequiredModsPresentForPackType(TexturesPlusPackType type) {
        FabricLoader loader = FabricLoader.getInstance();
        return REQUIRED_MODS.get(type).stream().allMatch(loader::isModLoaded);
    }

    public static boolean areOptionalModsPresentForPackType(TexturesPlusPackType type) {
        FabricLoader loader = FabricLoader.getInstance();
        return OPTIONAL_MODS.get(type).stream().allMatch(loader::isModLoaded);
    }

    public static @NotNull TexturesPlusPackType getPackType(String packId) throws IllegalArgumentException {
        String sanitized = sanitizePackId(packId);
        if (!isTexturesPlusPack(sanitized)) throw new IllegalArgumentException("\"" + sanitized + "\" is not a valid textures plus pack id.");
        String type = sanitized.replaceAll("plus", "").toUpperCase(Locale.ENGLISH);
        return TexturesPlusPackType.valueOf(type);
    }
}
