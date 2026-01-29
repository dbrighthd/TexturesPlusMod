package com.dbrighthd.texturesplusmod.datapack;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class ElytrasPlusDatapackGenerator {
    public static void generateElytrasMcfunction() throws IOException {
        LOGGER.info("Generating Elytras+ placement in world...");
        String elytraPath = "elytrasplus";
        if(TexturesPlusModClient.getConfig().devMode)
        {
            elytraPath = "elytras";
        }
        Path jsonFile = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", elytraPath,"assets","minecraft","items","elytra.json");

        JsonObject root;
        try (FileReader reader = new FileReader(jsonFile.toFile())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        Map<String, Integer> gapSize = new HashMap<>();
        List<String> block = new ArrayList<>();
        List<String> animalwings = new ArrayList<>();
        List<String> color = new ArrayList<>();
        List<String> flag = new ArrayList<>();
        List<String> misc = new ArrayList<>();
        List<String> cape = new ArrayList<>();
        // Navigate to model.cases

        if (root.has("model") && root.getAsJsonObject("model").has("cases")) {
            JsonArray cases = root.getAsJsonObject("model").getAsJsonArray("cases");
            for (JsonElement element : cases) {
                JsonObject caseNode = element.getAsJsonObject();
                JsonElement whenNode = caseNode.get("when");
                JsonElement gapNode = caseNode.get("gap");

                String modelPath = null;
                if (caseNode.has("model")) {
                    JsonObject modelObj = caseNode.getAsJsonObject("model");
                    if (modelObj.has("on_false")) {
                        JsonObject onFalse = modelObj.getAsJsonObject("on_false");
                        if (onFalse.has("model")) {
                            modelPath = onFalse.get("model").getAsString();
                        }
                    }
                }

                if (whenNode != null && modelPath != null) {
                    String firstWhen = null;

                    // Case: "when" is an array
                    if (whenNode.isJsonArray() && !whenNode.getAsJsonArray().isEmpty()) {
                        firstWhen = whenNode.getAsJsonArray().get(0).getAsString();
                    }
                    // Case: "when" is a single string
                    else if (whenNode.isJsonPrimitive()) {
                        firstWhen = whenNode.getAsString();
                    }

                    if (firstWhen != null) {
                        String lower = modelPath.toLowerCase();

                        // Categorization Logic
                        if (lower.contains("minecraft:item/elytras/block_elytras")) {
                            block.add(firstWhen);
                        } else if (lower.contains("minecraft:item/elytras/bug_elytra") ||
                                lower.contains("minecraft:item/elytras/parrot_wings") ||
                                lower.contains("minecraft:item/elytras/animal_wings")) {
                            animalwings.add(firstWhen);
                        } else if (lower.contains("minecraft:item/elytras/flag_elytras") ||
                                lower.contains("minecraft:item/elytras/misc_elytras/mx_wings") ||
                                lower.contains("minecraft:item/elytras/misc_elytras/brazil_wings") ||
                                lower.contains("minecraft:item/elytras/pride_elytras") ||
                                lower.contains("minecraft:item/elytras/misc_elytras/usa_wings")) {
                            flag.add(firstWhen);
                        } else if (lower.contains("minecraft:item/elytras/modeled_elytras/capes/mojang")) {
                            continue;
                        } else if (lower.contains("minecraft:item/elytras/color_elytra") ||
                                lower.contains("minecraft:item/elytras/shulker_elytras")) {
                            color.add(firstWhen);
                        } else if (lower.contains("minecraft:item/elytras/mojang_elytras")) {
                            cape.add(firstWhen.replace(" Elytra", ""));
                        } else {
                            misc.add(firstWhen);
                        }

                        if (gapNode != null) {
                            gapSize.put(firstWhen, gapNode.getAsInt());
                        }
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        Collections.sort(block);
        Collections.sort(animalwings);
        Collections.sort(color);
        Collections.sort(flag);
        Collections.sort(misc);
        Collections.sort(cape);

        String append = "";
        if(!TexturesPlusModClient.getConfig().showElytraArmorStands)
        {
            append = "noarmor";
        }
        //block, 25 -53 -14, red, north,
        createElytraRow(sb, block, 25, -53, -14, "red_concrete", "north", gapSize, append);
        //wing: 25 -53 -8 orance concrete, north
        createElytraRow(sb, animalwings, 25, -53, -8, "orange_concrete", "north", gapSize, append);

        createElytraRow(sb, color, 25, -53, -2, "yellow_concrete", "north", gapSize, append);

        createElytraRow(sb, flag, 25, -53, 2, "lime_concrete", "south", gapSize, append);

        createElytraRow(sb,  misc, 25, -53, 8, "blue_concrete", "south", gapSize, append);

        createCapeRow(sb,  cape, 25, -53, 14, "purple_concrete", "south", append);

        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allelytras.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void createElytraRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction, Map<String, Integer> gapSize, String arg)
    {
        for (String name : names)
        {
            if(gapSize.containsKey(name))
            {
                x += gapSize.get(name)-1;
            }
            sb.append(TexturesPlusDatapackGeneralUtil.generateCommand(x,y,z,name,block,direction,"elytra" + arg)).append("\n");
            if(gapSize.containsKey(name))
            {
                x += gapSize.get(name)-1;
            }
            x +=2;
        }
    }

    static void createCapeRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction, String arg)
    {
        for (String name : names)
        {
            sb.append(TexturesPlusDatapackGeneralUtil.generateCommand(x,y,z,name,block,direction,"capeleytra"+ arg)).append("\n");
            x +=2;
        }
    }
}
