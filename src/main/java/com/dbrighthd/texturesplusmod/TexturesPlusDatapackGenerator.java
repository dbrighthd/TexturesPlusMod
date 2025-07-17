package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TexturesPlusDatapackGenerator {
    public static void generatePumpkinsMcfunction() throws IOException {
        LOGGER.info("Generating Pumpkins+ placement in world...");
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
                if(modelPathNode == null || !modelPathNode.isTextual())
                {
                    modelPathNode = caseNode.path("model").path("fallback").path("model");
                }
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
        LOGGER.info("Generating Elytras+ placement in world...");
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

        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allelytras.mcfunction");

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
            sb.append(generateCommand(x,y,z,name,block,direction,"elytra" + arg)).append("\n");
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
            sb.append(generateCommand(x,y,z,name,block,direction,"capeleytra"+ arg)).append("\n");
            x +=2;
        }
    }

    public static void generateWeaponsMcfunction() throws IOException {
        LOGGER.info("Generating Weapons+ placement in world...");
        // Path to your JSON file
        String weaponsPath = "weaponsplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            weaponsPath = "weapons";
        }
        Path dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", weaponsPath,"assets","minecraft","items");
        Map<String,List<TexturesPlusItem>> itemMap = new HashMap<String,List<TexturesPlusItem>>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path path : stream) {
                generateMapEntry(itemMap, path);
            }
        }
        List<TexturesPlusItem> misc = new ArrayList<TexturesPlusItem>();
        List<TexturesPlusItem> shieldtotems = new ArrayList<TexturesPlusItem>();
        List<TexturesPlusItem> hoes = new ArrayList<TexturesPlusItem>();
        List<TexturesPlusItem> axes = new ArrayList<TexturesPlusItem>();
        List<TexturesPlusItem> swords = new ArrayList<TexturesPlusItem>();

        for (Map.Entry<String, List<TexturesPlusItem>> entry : itemMap.entrySet()) {

            String key = entry.getKey();
            List<TexturesPlusItem> renames = entry.getValue();

            if(key.contains("shield") || key.contains("totem"))
            {
                shieldtotems.addAll(renames);
            }
            else if(key.contains("hoe"))
            {
                hoes.addAll(renames);
            }
            else if(key.contains("pickaxe"))
            {
                misc.addAll(renames);
            }
            else if(key.contains("axe"))
            {
                axes.addAll(renames);
            }
            else if(key.contains("sword"))
            {
                swords.addAll(renames);
            }
            else
            {
                misc.addAll(renames);
            }
        }

        StringBuilder sb = new StringBuilder();
        //misc -25 -53 14, red_concrete, south
        createWeaponRow(sb,misc,-25,-53,14,"red_concrete","south");
        //shieldtotems -25 -53 8, orange_concrete, south
        createWeaponRow(sb,shieldtotems,-25,-53,8,"orange_concrete","south");
        //hoes -25 -53 2, yellow_concrete, south
        createWeaponRow(sb,hoes,-25,-53,2,"yellow_concrete","south");
        //axes -25 -53 -2, lime_concrete, north
        createWeaponRow(sb,axes,-25,-53,-2,"lime_concrete","north");
        //swords -25 -53 -8, blue_concrete, north
        createWeaponRow(sb,swords,-25,-53,-8,"blue_concrete","north");

        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allweapons.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void createWeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction)
    {
        for (TexturesPlusItem item : items)
        {
            if(item.enchantments.isEmpty())
            {
                sb.append(generateWeaponCommand(x,y,z,item.rename,block,direction,item.damage, item.itemType,getSpecialBlock(item.itemType, block))).append("\n");
            }
            else
            {
                sb.append(generateEnchantedWeaponCommand(x,y,z,item.rename,block,direction,item.damage, item.enchantments.getFirst(), item.itemType,getSpecialBlock(item.itemType, block))).append("\n");
            }
            x -=1;
        }
    }

    public static void generateMapEntry(Map<String,List<TexturesPlusItem>> itemMap, Path jsonFile)
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(jsonFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String itemName = jsonFile.getFileName().toString().replace(".json","");
        JsonNode cases = root.path("model").path("cases");
        List<TexturesPlusItem> allRenameCases = new ArrayList<TexturesPlusItem>();
        if (cases.isArray()) {
            for (JsonNode caseNode : cases) {
                getConditions(itemName, allRenameCases,caseNode);
            }
        }
        itemMap.put(itemName, allRenameCases);


    }

    public static void getConditions(String itemType, List<TexturesPlusItem> itemJsonData, JsonNode currCase)
    {
        JsonNode node = currCase.get("model");
        if(node == null)
        {
            LOGGER.error("Model wasnt found for an item in " + itemType);
            return;
        }
        String firstWhen = null;
        String modelPath = null;
        JsonNode whenNode = currCase.get("when");
        if (whenNode.isArray() && whenNode.size() > 0) {
            firstWhen = whenNode.get(0).asText();
        }
        else if (whenNode.isTextual()) {
            firstWhen = whenNode.asText();
        }
        if(node.get("type").asText().contains("model"))
        {
            modelPath = node.get("model").asText();
            itemJsonData.add(new TexturesPlusItem(new ArrayList<String>(), firstWhen, 0, itemType, modelPath));
            return;
        }
        else
        {
            if(currCase.has("model"))
            {
                currCase = currCase.get("model");
            }

            List<TexturesPlusItem> itemsToAdd = getConditionsRecur(new ArrayList<String>(), currCase, itemType, firstWhen, 0);
            if(!itemsToAdd.isEmpty())
            {
                itemJsonData.addAll(itemsToAdd);
            }
        }

    }
    public static List<TexturesPlusItem> getConditionsRecur(List<String> enchants, JsonNode node, String itemType, String firstWhen, int damage)
    {
        List<TexturesPlusItem> foundItems = new ArrayList<TexturesPlusItem>();
        try {

            if (node.has("type") && node.get("type").asText().contains("model")) {
                String modelPath = null;
                modelPath = node.get("model").asText();
                foundItems.add(new TexturesPlusItem(new ArrayList<String>(), firstWhen, 0, itemType, modelPath));
            }
            if (node.has("predicate") && node.get("predicate").asText().contains("minecraft:enchantments")) {
                for (JsonNode entry : node.path("value")) {        // iterate the array
                    String id = entry.path("enchantments").asText(null);
                    if (id != null) enchants.add(id);
                }
            }
            if (node.has("predicate") && node.get("predicate").asText().contains("minecraft:damage")) {
                damage = node.get("value").get("damage").get("min").asInt();
            }
            if (node.get("type") != null && node.get("type").asText().contains("condition")) {
                if (node.get("on_true").get("type").asText().contains("model")) {
                    foundItems.add(new TexturesPlusItem(enchants, firstWhen, damage, itemType, node.get("on_true").get("model").asText()));
                } else {
                    if(!node.get("property").asText().contains("selected"))
                    {
                        foundItems.addAll(getConditionsRecur(enchants, node.get("on_true"), itemType, firstWhen, damage));
                    }
                    foundItems.addAll(getConditionsRecur(enchants, node.get("on_false"), itemType, firstWhen, damage));
                }
            }
            if (node.get("type") != null && node.get("type").asText().contains("range_dispatch")) {
                if (node.get("property").asText().contains("damage")) {
                    int maxDamage = getMaxDurability("minecraft:" +itemType);
                    int scale = node.get("scale").asInt();
                    int increment = maxDamage/scale;

                    if(node.get("entries").isArray())
                    {
                        for (JsonNode entry : node.get("entries"))
                        {
                            foundItems.add(new TexturesPlusItem(enchants, firstWhen, increment * entry.get("threshold").asInt(), itemType, entry.get("model").asText()));
                        }
                    }

                }
            }
            if (node.has("cases")) {
                JsonNode cases = node.get("cases");
                boolean isGui = node.get("property").asText().contains("display_context");
                boolean isDamage = node.has("component") && node.get("component").asText().contains("damage");
                if (node.get("cases").isArray()) {
                    for (JsonNode caseNode : cases) {
                        int damageNode = 0;
                        if(isDamage)
                        {
                            JsonNode whenNode = caseNode.get("when");
                            if(whenNode.isArray())
                            {
                                damageNode = whenNode.get(0).asInt();
                            }
                            else
                            {
                                damageNode = whenNode.asInt();
                            }
                        }
                        if(isGui)
                        {
                            foundItems.add(getConditionsRecur(enchants, caseNode.get("model"), itemType, firstWhen, damageNode).getFirst());

                        }
                        else
                        {
                            foundItems.addAll(getConditionsRecur(enchants, caseNode.get("model"), itemType, firstWhen, damageNode));
                        }
                    }
                }
            }
            if(foundItems.isEmpty())
            {
                LOGGER.error("Failed to find model in item " + firstWhen);
            }
            return foundItems;
        } catch (Exception e) {
            LOGGER.error("Failed to fully parse item", e);
            return foundItems;
        }

    }
    public static int getMaxDurability(String itemId) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        return new ItemStack(item).getMaxDamage();
    }

    static String getSpecialBlock(String itemName, String block)
    {
        if(itemName.contains("wooden"))
        {
            return "oak_planks";
        }
        if(itemName.contains("diamond"))
        {
            return "diamond_block";
        }
        if(itemName.contains("iron"))
        {
            return "iron_block";
        }
        if(itemName.contains("golden"))
        {
            return "gold_block";
        }
        if(itemName.contains("stone"))
        {
            if(itemName.contains("redstone"))
            {
                return block;
            }
            return "stone";
        }
        if(itemName.contains("netherite"))
        {
            return "netherite_block";
        }
        else
        {
            return block;
        }
    }
    static String generateWeaponCommand(int x, int y, int z, String rename, String block, String direction, int damage, String item, String specialBlock)
    {
        return "function texturesplus:weapons/place1weapon" + direction + " {item:\""+ item + "\",x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block +"\",special_block:\""+specialBlock + "\",damage:"+ damage +"}";
    }
    static String generateEnchantedWeaponCommand(int x, int y, int z, String rename, String block, String direction, int damage, String enchant, String item, String specialBlock)
    {
        return "function texturesplus:weapons/place1weaponenchant" + direction + " {item:\""+ item + "\",x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block+"\",special_block:\""+specialBlock + "\",damage:"+ damage +",enchantment:\""+enchant.replace("minecraft:","")+"\"}";
    }

    static String generateCommand(int x, int y, int z, String rename, String block, String direction, String command)
    {
        return "function texturesplus:place" + command + direction + " {x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block + "\"}";
    }

    public static class TexturesPlusItem {
        public final String itemType;
        public final List<String> enchantments;
        public final String rename;
        public final int damage;
        public final String model;

        public TexturesPlusItem(List<String> enchantments, String rename, int damage, String itemType, String model) {
            this.enchantments = Collections.unmodifiableList(
                    new ArrayList<>(Objects.requireNonNullElse(enchantments, List.of()))
            );
            this.rename = rename;
            this.damage = damage;
            this.itemType = itemType;
            this.model = model;
        }
    }
}
