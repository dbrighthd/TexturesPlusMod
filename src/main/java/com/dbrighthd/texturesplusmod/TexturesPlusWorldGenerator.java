package com.dbrighthd.texturesplusmod;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtSizeTracker;
import org.apache.commons.io.FileUtils;

import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TexturesPlusWorldGenerator {

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
        File worldDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated").toFile();
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

            Path levelDat = worldDir.toPath().resolve("level.dat");
            if (Files.exists(levelDat)) {
                try {
                    NbtCompound root = NbtIo.readCompressed(levelDat, NbtSizeTracker.ofUnlimitedBytes());
                    NbtCompound data = root.getCompound("Data").orElseGet(() -> {
                        NbtCompound created = new NbtCompound();
                        root.put("Data", created);
                        return created;
                    });
                    data.put("LastPlayed", NbtLong.of(System.currentTimeMillis()));
                    NbtIo.writeCompressed(root, levelDat);
                    LOGGER.info("Set LastPlayed in level.dat to {}", Instant.ofEpochMilli(System.currentTimeMillis()));
                } catch (IOException e) {
                    LOGGER.error("Failed to bump LastPlayed", e);
                }
            }
            try{
                TexturesPlusDatapackGenerator.generatePumpkinsMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Pumpkins+ section in : ", e);
            }
            try{
                TexturesPlusDatapackGenerator.generateElytrasMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Elytras+ section in world: ", e);
            }
            try{
                TexturesPlusDatapackGenerator.generateWeaponsMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Weapons+ section in world: ", e);
            }
            try{
                TexturesPlusDatapackGenerator.generateCreaturesMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Creatures+ section in world: ", e);
            }

            LOGGER.info("Finished Generating World!");
        }



    }
    private static void unzipWorldFileToSaves(Path filePathToUnzip, String worldName) {
        File savesDir = new File(MinecraftClient.getInstance().runDirectory, "saves");
        File targetDir = new File(savesDir, worldName);

        try (ZipFile zip = new ZipFile(filePathToUnzip.toFile())) {
            if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
                throw new IOException("Failed to create world save directory: " + targetDir);
            }

            for (ZipEntry entry : Collections.list(zip.entries())) {
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
