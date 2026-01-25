package com.dbrighthd.texturesplusmod.datapack;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class PumpkinsPlusDatapackGenerator {
    public static void generatePumpkinsMcfunction() throws IOException {
        LOGGER.info("Generating Pumpkins+ placement in world...");
        // Path to your JSON file
        String pumpkinPath = "pumpkinsplus";
        if(TexturesPlusModClient.getConfig().devMode)
        {
            pumpkinPath = "pumpkins";
        }
        Path jsonFile = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", pumpkinPath,"assets","minecraft","items","carved_pumpkin.json");

        JsonObject root;
        try (FileReader reader = new FileReader(jsonFile.toFile())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        List<String> references = new ArrayList<>();
        List<String> headwear = new ArrayList<>();
        List<String> animals = new ArrayList<>();
        List<String> cutesy = new ArrayList<>();
        List<String> hats = new ArrayList<>();
        List<String> misc = new ArrayList<>();
        // Navigate to model.cases
        if (root.has("model") && root.getAsJsonObject("model").has("cases")) {
            JsonArray cases = root.getAsJsonObject("model").getAsJsonArray("cases");

            for (JsonElement element : cases) {
                JsonObject caseNode = element.getAsJsonObject();
                JsonElement whenNode = caseNode.get("when");

                String modelPath = null;

                // Handle model navigation with fallback
                if (caseNode.has("model")) {
                    JsonObject modelObj = caseNode.getAsJsonObject("model");

                    // Try direct model first
                    if (modelObj.has("model")) {
                        modelPath = modelObj.get("model").getAsString();
                    }
                    // Fallback logic
                    else if (modelObj.has("fallback")) {
                        JsonObject fallbackObj = modelObj.getAsJsonObject("fallback");
                        if (fallbackObj.has("model")) {
                            modelPath = fallbackObj.get("model").getAsString();
                        }
                    }
                }

                if (whenNode != null && modelPath != null) {
                    String firstWhen = null;

                    // Case: "when" is an array
                    if (whenNode.isJsonArray() && whenNode.getAsJsonArray().size() > 0) {
                        firstWhen = whenNode.getAsJsonArray().get(0).getAsString();
                    }
                    // Case: "when" is a single string
                    else if (whenNode.isJsonPrimitive()) {
                        firstWhen = whenNode.getAsString();
                    }

                    if (firstWhen != null) {
                        String lower = modelPath.toLowerCase();
                        if (lower.contains("minecraft:block/pumpkins/references")) {
                            references.add(firstWhen);
                        } else if (lower.contains("minecraft:block/pumpkins/headwear")) {
                            headwear.add(firstWhen);
                        } else if (lower.contains("minecraft:block/pumpkins/animals")) {
                            animals.add(firstWhen);
                        } else if (lower.contains("minecraft:block/pumpkins/cutesy")) {
                            cutesy.add(firstWhen);
                        } else if (lower.contains("minecraft:block/hatsplus")) {
                            hats.add(firstWhen);
                        } else {
                            misc.add(firstWhen);
                        }
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        //references: 14 -53 25, red concrete, east
        createPumpkinRow(sb, references, 14, -53, 25, "red_concrete", "east");
        //headwear: 8 -53 25 orance concrete, east
        createPumpkinRow(sb, headwear, 8, -53, 25, "orange_concrete", "east");
        //animals: 2 -53 25 yellow concrete, east
        createPumpkinRow(sb, animals, 2, -53, 25, "yellow_concrete", "east");

        //cutesy: -2 -53 25, lime concrete, west
        createPumpkinRow(sb, cutesy, -2, -53, 25, "lime_concrete", "west");
        //misc, -8 -53 25, blue_concrete, west
        createPumpkinRow(sb, misc, -8, -53, 25, "blue_concrete", "west");
        //hats, -14 -53 25, purple_concrete, west
        createPumpkinRow(sb, hats, -14, -53, 25, "purple_concrete", "west");

        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allpumpkins.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @SuppressWarnings("SameParameterValue")
    static void createPumpkinRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction)
    {
        for (String name : names)
        {
            if (name.contains("Tree Disguise"))
            {
                continue;
            }
            sb.append(TexturesPlusDatapackGeneralUtil.generateCommand(x,y,z,name,block,direction,"pumpkin")).append("\n");
            z +=2;
        }
    }
}
