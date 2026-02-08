package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.datapack.*;
import java.io.*;
import java.nio.file.*;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TexturesPlusWorldGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TexturesPlusWorldGenerator.class);

    public static CompletableFuture<Void> generateWorldAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                generateWorld();
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Failed to generate world", e);
            }
        });
    }

    public static void generateWorld() throws IOException, InterruptedException {
        LOGGER.info("Generating Textures+ test world...");

        File worldDir = new File(
                Minecraft.getInstance().gameDirectory,
                "saves/TexturesPlusGenerated"
        );

        File LevelDatCurr = new File(worldDir, "level.dat");
        File levelDatOld = new File(worldDir, "level.dat_old");

        File backupDir = new File(
                Minecraft.getInstance().gameDirectory,
                "texturesplus_backups"
        );
        backupDir.mkdirs();

        File backupLevelDat = new File(backupDir, "TexturesPlusGenerated_level.dat.bak");
        File backupLevelDatOld = new File(backupDir, "TexturesPlusGenerated_level.dat_old.bak");

        boolean restoreLastPos = TexturesPlusModClient.getConfig().lastPos;

        // Backup old world dat, for storing last position.
        if (restoreLastPos && LevelDatCurr.exists()) {
            FileUtils.copyFile(LevelDatCurr, backupLevelDat);
            LOGGER.info("Backed up level.dat");

            if (levelDatOld.exists()) {
                FileUtils.copyFile(levelDatOld, backupLevelDatOld);
            }
        }

        if (worldDir.exists()) {
            FileUtils.deleteDirectory(worldDir);
        }
        Path tempZip = Files.createTempFile("template_world", ".zip");
        try (InputStream in = TexturesPlusMod.class.getResourceAsStream("/assets/texturesplusmod/template_world/template_world.zip")) {
            if (in == null) {
                throw new IOException("template_world.zip not found in mod resources!");
            }
            Files.copy(in, tempZip, StandardCopyOption.REPLACE_EXISTING);
            unzipWorldFileToSaves(tempZip, "TexturesPlusGenerated");  // unzip into the saves directory
            if (restoreLastPos && backupLevelDat.exists()) {
                FileUtils.copyFile(backupLevelDat, LevelDatCurr);
                LOGGER.info("Restored level.dat");

                if (backupLevelDatOld.exists()) {
                    FileUtils.copyFile(backupLevelDatOld, levelDatOld);
                }
            }
            Path levelDat = worldDir.toPath().resolve("level.dat");
            if (Files.exists(levelDat)) {
                try {
                    CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
                    CompoundTag data = root.getCompound("Data").orElseGet(() -> {
                        CompoundTag created = new CompoundTag();
                        root.put("Data", created);
                        return created;
                    });
                    data.put("LastPlayed", LongTag.valueOf(System.currentTimeMillis()));
                    NbtIo.writeCompressed(root, levelDat);
                    LOGGER.info("Set LastPlayed in level.dat to {}", Instant.ofEpochMilli(System.currentTimeMillis()));
                } catch (IOException e) {
                    LOGGER.error("Failed to bump LastPlayed", e);
                }
            }

            {
                LOGGER.info("Generating Pumpkins+ placement in world...");
                ItemCategory REFERENCES = new ItemCategory(Blocks.RED_CONCRETE, new BlockPos(14, -53, 25), Direction.EAST);
                ItemCategory HEADWEAR = new ItemCategory(Blocks.ORANGE_CONCRETE, new BlockPos(8, -53, 25), Direction.EAST);
                ItemCategory ANIMALS = new ItemCategory(Blocks.YELLOW_CONCRETE, new BlockPos(2, -53, 25), Direction.EAST);
                ItemCategory CUTESY = new ItemCategory(Blocks.LIME_CONCRETE, new BlockPos(-2, -53, 25), Direction.WEST);
                ItemCategory MISC = new ItemCategory(Blocks.BLUE_CONCRETE, new BlockPos(-8, -53, 25), Direction.WEST);
                ItemCategory HATS = new ItemCategory(Blocks.PURPLE_CONCRETE, new BlockPos(-14, -53, 25), Direction.WEST);
                ItemCategory EXCLUDED = new ItemCategory(Blocks.REDSTONE_BLOCK, new BlockPos(-20, -53, 25), Direction.WEST, true);
                ItemBasedDatapackGenerator pumpkins = new ItemBasedDatapackGenerator(
                        Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", TexturesPlusModClient.getConfig().devMode ? "pumpkins" : "pumpkinsplus", "assets", "minecraft", "items", "carved_pumpkin.json"),
                        new MapCategorySelector(List.of(
                                Pair.of(List.of("minecraft:block/pumpkins/misc/you_saw_nothing/"), EXCLUDED), // note: the "EXCLUDED" category has excluded set to true, so the generator will skip it
                                Pair.of(List.of("minecraft:block/pumpkins/references"), REFERENCES), // note: repeated pairs might be okay, but I don't know
                                Pair.of(List.of("minecraft:block/pumpkins/headwear"), HEADWEAR), // note: you can omit the namespace, if you do it will only check the path
                                Pair.of(List.of("minecraft:block/pumpkins/animals"), ANIMALS), // note: if the namespace isn't omitted, it will only match if the namespace is an exact match
                                Pair.of(List.of("minecraft:block/pumpkins/cutesy"), CUTESY), // note: these AREN'T regex
                                Pair.of(List.of("minecraft:block/hatsplus"), HATS), // note: technically these are serializable, which is great because it means in the future you could make these categories defined in the pack
                                Pair.of(List.of(""), MISC) // note: an empty string will always match, so will an empty list, so this has to be last. These keys are checked in order from first to last.
                        )),
                        (sb, category, models) -> {
                            if (category.excluded()) return;
                            int zOffset = 0;
                            for (String model : models) {
                                sb.append(TexturesPlusDatapackGeneralUtil.generateCommand(
                                        category.position().getX(), category.position().getY(), category.position().getZ() + zOffset,
                                        model, BuiltInRegistries.BLOCK.getKey(category.block()).getPath(), category.direction().getName(), "pumpkin"
                                )).append("\n");
                                zOffset += 2;
                            }
                        }
                );
                pumpkins.generateCommands(false).ifSuccess((s) -> {
                    try {
                        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated", "datapacks", "texturesplus", "data", "texturesplus", "function", "allpumpkins.mcfunction");
                        Files.writeString(functionPath, s, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write Pumpkins+ section: ", e);
                    }
                }).ifError((e) -> {
                    LOGGER.error("Failed to generate Pumpkins+ section: {}", e.message());
                });
            }
            {
                LOGGER.info("Generating Elytras+ placement in world...");
                ItemCategory BLOCKS = new ItemCategory(Blocks.RED_CONCRETE, new BlockPos(25, -53, -14), Direction.NORTH);
                ItemCategory ANIMAL_WINGS = new ItemCategory(Blocks.ORANGE_CONCRETE, new BlockPos(25, -53, -8), Direction.NORTH);
                ItemCategory COLOR = new ItemCategory(Blocks.YELLOW_CONCRETE, new BlockPos(25, -53, -2), Direction.NORTH);
                ItemCategory FLAG = new ItemCategory(Blocks.LIME_CONCRETE, new BlockPos(25, -53, 2), Direction.SOUTH);
                ItemCategory MISC = new ItemCategory(Blocks.BLUE_CONCRETE, new BlockPos(25, -53, 8), Direction.SOUTH);
                ItemCategory CAPE = new ItemCategory(Blocks.PURPLE_CONCRETE, new BlockPos(25, -53, 14), Direction.SOUTH);
                ItemCategory EXCLUDED = new ItemCategory(Blocks.REDSTONE_BLOCK, new BlockPos(25, -53, -20), Direction.NORTH, true);
                ItemBasedDatapackGenerator elytras = new ItemBasedDatapackGenerator(
                        Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", TexturesPlusModClient.getConfig().devMode ? "elytras" : "elytrasplus", "assets", "minecraft", "items", "elytra.json"),
                        new MapCategorySelector(List.of(
                                Pair.of(List.of("minecraft:item/elytras/modeled_elytras/capes/mojang"), EXCLUDED),
                                Pair.of(List.of("minecraft:item/elytras/block_elytras"), BLOCKS),
                                Pair.of(List.of(
                                        "minecraft:item/elytras/bug_elytra",
                                        "minecraft:item/elytras/parrot_wings",
                                        "minecraft:item/elytras/animal_wings"
                                ), ANIMAL_WINGS),
                                Pair.of(List.of(
                                        "minecraft:item/elytras/flag_elytras",
                                        "minecraft:item/elytras/misc_elytras/mx_wings",
                                        "minecraft:item/elytras/misc_elytras/brazil_wings",
                                        "minecraft:item/elytras/pride_elytras",
                                        "minecraft:item/elytras/misc_elytras/usa_wings"
                                ), FLAG),
                                Pair.of(List.of(
                                        "minecraft:item/elytras/color_elytra",
                                        "minecraft:item/elytras/shulker_elytras"
                                ), COLOR),
                                Pair.of(List.of("minecraft:item/elytras/mojang_elytras"), CAPE),
                                Pair.of(List.of(), MISC)
                        )),
                        (sb, category, models) -> {
                            if (category.excluded()) return;
                            int xOffset = 0;
                            for (String model : models) {
                                boolean cape = category == CAPE; // whatever
                                String processed = cape ? model.replace(" Elytra", "") : model;

                                sb.append(TexturesPlusDatapackGeneralUtil.generateCommand(
                                        category.position().getX() + xOffset, category.position().getY(), category.position().getZ(),
                                        processed, BuiltInRegistries.BLOCK.getKey(category.block()).getPath(), category.direction().getName(), (cape ? "capeleytra" : "elytra") + (TexturesPlusModClient.getConfig().showElytraArmorStands ? "" : "noarmor")
                                )).append("\n");
                                xOffset += 2;
                            }
                        }
                );

                elytras.generateCommands(true).ifSuccess((s) -> {
                    try {
                        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated", "datapacks", "texturesplus", "data", "texturesplus", "function", "allelytras.mcfunction");
                        Files.writeString(functionPath, s, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write Elytras+ section: ", e);
                    }
                }).ifError((e) -> {
                    LOGGER.error("Failed to generate Elytras+ section: {}", e.message());
                });
            }
            try{
                WeaponsPlusDatapackGenerator.generateWeaponsMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Weapons+ section in world: ", e);
            }
            try{
                CreaturesPlusDatapackGenerator.generateCreaturesMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Creatures+ section in world: ", e);
            }
            try{
                ContributorsDatapackGenerator.generateContributorsMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Contributors section in world: ", e);
            }
            LOGGER.info("Finished Generating World!");
        }



    }

    @SuppressWarnings("SameParameterValue")
    private static void unzipWorldFileToSaves(Path filePathToUnzip, String worldName) {
        File savesDir = new File(Minecraft.getInstance().gameDirectory, "saves");
        File targetDir = new File(savesDir, worldName);

        try (ZipFile zip = new ZipFile(filePathToUnzip.toFile())) {
            if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
                throw new IOException("Failed to create world save directory: " + targetDir);
            }

            for (ZipEntry entry : Collections.list(zip.entries())) {
                @SuppressWarnings("JvmTaintAnalysis")
                File outFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!outFile.isDirectory() && !outFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + outFile);
                    }
                } else {
                    File parent = outFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create parent directory: " + parent);
                    }

                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to unzip world file to saves directory", e);
        }
    }
}
