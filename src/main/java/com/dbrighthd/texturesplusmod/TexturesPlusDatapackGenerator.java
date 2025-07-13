package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TexturesPlusDatapackGenerator {
    public static void generatePumpkinsMcfunction() throws IOException {
        // Path to your JSON file
        String pumpkinPath = "pumpkinsplus";
        if(TexturesPlusModClient.getConfig().devMode)
        {
            pumpkinPath = "pumpkins";
        }
        Path jsonFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", pumpkinPath,"assets","minecraft","items","carved_pumpkin.json");

        // Initialize Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile.toFile());

        List<String> references = new ArrayList<>();
        List<String> headwear = new ArrayList<>();
        List<String> animals = new ArrayList<>();
        List<String> cutesy = new ArrayList<>();
        List<String> hats = new ArrayList<>();
        List<String> misc = new ArrayList<>();
        // Navigate to model.cases
        JsonNode cases = root.path("model").path("cases");

        if (cases.isArray()) {
            for (JsonNode caseNode : cases) {
                JsonNode whenNode = caseNode.get("when");
                JsonNode modelPathNode = caseNode.path("model").path("model");

                if (whenNode != null && modelPathNode != null && modelPathNode.isTextual()) {
                    String modelPath = modelPathNode.asText();
                    String firstWhen = null;

                    // Case: "when" is an array
                    if (whenNode.isArray() && whenNode.size() > 0) {
                        firstWhen = whenNode.get(0).asText();
                    }

                    // Case: "when" is a single string
                    else if (whenNode.isTextual()) {
                        firstWhen = whenNode.asText();
                    }

                    if (firstWhen != null) {
                        String lower = modelPath.toLowerCase();
                        if (lower.contains("minecraft:block/pumpkins/references"))
                        {
                            references.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:block/pumpkins/headwear"))
                        {
                            headwear.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:block/pumpkins/animals"))
                        {
                            animals.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:block/pumpkins/cutesy"))
                        {
                            cutesy.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:block/hatsplus"))
                        {
                            hats.add(firstWhen);
                        }
                        else
                        {
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

        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allpumpkins.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    static void createPumpkinRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction)
    {
        for (String name : names)
        {
            if (name.contains("Tree Disguise"))
            {
                continue;
            }
            sb.append(generateCommand(x,y,z,name,block,direction,"pumpkin")).append("\n");
            z +=2;
        }
    }

    public static void generateElytrasMcfunction() throws IOException {
        // Path to your JSON file
        String elytraPath = "elytrasplus";
        if(TexturesPlusModClient.getConfig().devMode)
        {
            elytraPath = "elytras";
        }
        Path jsonFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", elytraPath,"assets","minecraft","items","elytra.json");

        // Initialize Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile.toFile());

        Map<String, Integer> gapSize = new HashMap<>();
        List<String> block = new ArrayList<>();
        List<String> animalwings = new ArrayList<>();
        List<String> color = new ArrayList<>();
        List<String> flag = new ArrayList<>();
        List<String> misc = new ArrayList<>();
        List<String> cape = new ArrayList<>();
        // Navigate to model.cases
        JsonNode cases = root.path("model").path("cases");

        if (cases.isArray()) {
            for (JsonNode caseNode : cases) {
                JsonNode whenNode = caseNode.get("when");
                JsonNode gapNode = caseNode.get("gap");
                JsonNode modelPathNode = caseNode.path("model").path("on_false").path("model");

                if (whenNode != null && modelPathNode != null && modelPathNode.isTextual()) {
                    String modelPath = modelPathNode.asText();
                    String firstWhen = null;

                    // Case: "when" is an array
                    if (whenNode.isArray() && whenNode.size() > 0) {
                        firstWhen = whenNode.get(0).asText();
                    }

                    // Case: "when" is a single string
                    else if (whenNode.isTextual()) {
                        firstWhen = whenNode.asText();
                    }

                    if (firstWhen != null) {
                        String lower = modelPath.toLowerCase();
                        if (lower.contains("minecraft:item/elytras/block_elytras"))
                        {
                            block.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/bug_elytra"))
                        {
                            animalwings.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/parrot_wings"))
                        {
                            animalwings.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/animal_wings"))
                        {
                            animalwings.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/flag_elytras"))
                        {
                            flag.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/misc_elytras/mx_wings"))
                        {
                            flag.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/misc_elytras/brazil_wings"))
                        {
                            flag.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/pride_elytras"))
                        {
                            flag.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/misc_elytras/usa_wings"))
                        {
                            flag.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/modeled_elytras/capes/mojang"))
                        {
                            continue;
                        }
                        else if (lower.contains("minecraft:item/elytras/color_elytra"))
                        {
                            color.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/shulker_elytras"))
                        {
                            color.add(firstWhen);
                        }
                        else if (lower.contains("minecraft:item/elytras/mojang_elytras"))
                        {
                            cape.add(firstWhen.replace(" Elytra",""));
                        }
                        else
                        {
                            misc.add(firstWhen);
                        }
                        if(gapNode != null)
                        {
                            gapSize.put(firstWhen,Integer.parseInt(gapNode.asText()));
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

        //block, 25 -53 -14, red, north,
        createElytraRow(sb, block, 25, -53, -14, "red_concrete", "north", gapSize);
        //wing: 25 -53 -8 orance concrete, north
        createElytraRow(sb, animalwings, 25, -53, -8, "orange_concrete", "north", gapSize);

        createElytraRow(sb, color, 25, -53, -2, "yellow_concrete", "north", gapSize);

        createElytraRow(sb, flag, 25, -53, 2, "lime_concrete", "south", gapSize);

        createElytraRow(sb,  misc, 25, -53, 8, "blue_concrete", "south", gapSize);

        createCapeRow(sb,  cape, 25, -53, 14, "purple_concrete", "south");

        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allelytras.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void createElytraRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction, Map<String, Integer> gapSize)
    {
        for (String name : names)
        {
            if(gapSize.containsKey(name))
            {
                x += gapSize.get(name)-1;
            }
            sb.append(generateCommand(x,y,z,name,block,direction,"elytra")).append("\n");
            if(gapSize.containsKey(name))
            {
                x += gapSize.get(name)-1;
            }
            x +=2;
        }
    }

    static void createCapeRow(StringBuilder sb, List<String> names, int x, int y, int z, String block, String direction)
    {
        for (String name : names)
        {
            sb.append(generateCommand(x,y,z,name,block,direction,"capeleytra")).append("\n");
            x +=2;
        }
    }

    static String generateCommand(int x, int y, int z, String rename, String block, String direction, String command)
    {
        return "function texturesplus:place" + command + direction + " {x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block + "\"}";
    }

}
