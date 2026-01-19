package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.datapackutil.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
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
        File worldDir = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated").toFile();
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
            try{
                PumpkinsPlusDatapackGenerator.generatePumpkinsMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Pumpkins+ section in : ", e);
            }
            try{
                ElytrasPlusDatapackGenerator.generateElytrasMcfunction();
            }
            catch (Exception e) {
                LOGGER.error("Failed to generate Elytras+ section in world: ", e);
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
