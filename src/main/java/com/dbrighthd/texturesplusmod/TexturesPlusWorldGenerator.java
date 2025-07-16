package com.dbrighthd.texturesplusmod;

import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TexturesPlusWorldGenerator {
    public static void generateWorld() throws IOException {
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
            TexturesPlusDatapackGenerator.generatePumpkinsMcfunction();
            TexturesPlusDatapackGenerator.generateElytrasMcfunction();
            TexturesPlusDatapackGenerator.generateWeaponsMcfunction();
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
