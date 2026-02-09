package com.dbrighthd.texturesplusmod.datapack;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;
import static com.dbrighthd.texturesplusmod.datapack.TexturesPlusDatapackGeneralUtil.*;
import static net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil.GSON;

public class ContributorsDatapackGenerator {
    public static void generateContributorsMcfunction() throws IOException {
        LOGGER.info("Generating Weapons+ placement in world...");

        // Path to your JSON file
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        Path dir = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","contributors.json");
        List<TexturesPlusContributor> contributors = load(dir);

        StringBuilder sb = new StringBuilder();
        int x = 2;
        int y = -62;
        int z = 16;
        sb.append(placePart(x, y, z, "start")).append("\n");
        for (int i = 0; i < contributors.size(); i += 2) {
            z-=2;
            if (i + 1 < contributors.size()) {
                sb.append(addTwoContributors(x, y, z, contributors.get(i), contributors.get(i + 1))).append("\n");
            } else {
                sb.append(addOneContributor(x, y, z, contributors.get(i))).append("\n");
            }
        }
        z-=3;
        sb.append(placePart(x, y, z, "end")).append("\n");
        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated", "datapacks", "texturesplus", "data", "texturesplus", "function", "allcontributors.mcfunction");
        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    public static String placePart(int x, int y, int z, String part)
    {
        String command = "function texturesplus:contributors/place_" + part + " {";
        command = addMacro(command, "x", x+"", true);
        command = addMacro(command, "y", y+"", true);
        command = addMacro(command, "z", z+"", false);
        return command + "}";
    }
    public static String addTwoContributors(int x, int y, int z, TexturesPlusContributor contributorOne, TexturesPlusContributor contributorTwo) {
        String command = "function texturesplus:contributors/place_two {";
        command = addMacro(command, "x", x+"", true);
        command = addMacro(command, "y", y+"", true);
        command = addMacro(command, "z", z+"", true);
        command = addMacro(command, "message1_line1", contributorOne.quoteMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line2", contributorOne.quoteMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line3", contributorOne.quoteMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line4", contributorOne.quoteMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_color", contributorOne.quoteColor(), true);
        command = addMacro(command, "contributor1_name", contributorOne.contributorMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line2", contributorOne.contributorMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line3", contributorOne.contributorMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line4", contributorOne.contributorMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_color", contributorOne.contributorColor(), true);
        command = addMacro(command, "contributor1_mcname", contributorOne.minecraftUsername(), true);

        command = addMacro(command, "message2_line1", contributorTwo.quoteMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message2_line2", contributorTwo.quoteMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message2_line3", contributorTwo.quoteMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message2_line4", contributorTwo.quoteMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message2_color", contributorTwo.quoteColor(), true);
        command = addMacro(command, "contributor2_name", contributorTwo.contributorMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution2_line2", contributorTwo.contributorMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution2_line3", contributorTwo.contributorMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution2_line4", contributorTwo.contributorMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution2_color", contributorTwo.contributorColor(), true);
        command = addMacro(command, "contributor2_mcname", contributorTwo.minecraftUsername(), false);
        return command + "}";
    }
    public static String addOneContributor(int x, int y, int z, TexturesPlusContributor contributorOne) {
        String command = "function texturesplus:contributors/place_one {";
        command = addMacro(command, "x", x+"", true);
        command = addMacro(command, "y", y+"", true);
        command = addMacro(command, "z", z+"", true);
        command = addMacro(command, "message1_line1", contributorOne.quoteMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line2", contributorOne.quoteMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line3", contributorOne.quoteMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_line4", contributorOne.quoteMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "message1_color", contributorOne.quoteColor(), true);
        command = addMacro(command, "contributor1_name", contributorOne.contributorMessage().get(0).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line2", contributorOne.contributorMessage().get(1).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line3", contributorOne.contributorMessage().get(2).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_line4", contributorOne.contributorMessage().get(3).replace("\"","\\\\\\\""), true);
        command = addMacro(command, "contribution1_color", contributorOne.contributorColor(), true);
        command = addMacro(command, "contributor1_mcname", contributorOne.minecraftUsername(), false);
        return command + "}";
    }
    public static List<TexturesPlusContributor> load(Path jsonFile) throws IOException {
        Type listType = new TypeToken<List<TexturesPlusContributor>>() {}.getType();

        try (Reader r = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            return GSON.fromJson(r, listType);
        }
    }

}