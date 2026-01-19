package com.dbrighthd.texturesplusmod.datapackutil;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import net.minecraft.client.Minecraft;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class ElytrasPlusDatapackGenerator {
    public static void generateElytrasMcfunction() throws IOException {
        LOGGER.info("Generating Elytras+ placement in world...");
        // Path to your JSON file
        String elytraPath = "elytrasplus";
        if(TexturesPlusModClient.getConfig().devMode)
        {
            elytraPath = "elytras";
        }
        Path jsonFile = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", elytraPath,"assets","minecraft","items","elytra.json");

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
                    if (whenNode.isArray() && !whenNode.isEmpty()) {
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

        String append = "";
        if(!TexturesPlusModClient.getConfig().elytraArmorStands)
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
