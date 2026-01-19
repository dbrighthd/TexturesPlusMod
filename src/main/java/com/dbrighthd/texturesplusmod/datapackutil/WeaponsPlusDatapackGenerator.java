package com.dbrighthd.texturesplusmod.datapackutil;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class WeaponsPlusDatapackGenerator {
    public static void generateWeaponsMcfunction() throws IOException {
        LOGGER.info("Generating Weapons+ placement in world...");

        // Path to your JSON file
        String weaponsPath = "weaponsplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            weaponsPath = "weapons";
        }
        Path dir = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", weaponsPath, "assets", "minecraft", "items");
        Path groupsPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", weaponsPath, "assets", "minecraft", "items", "grouping");
        Path materialsPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "resourcepacks", weaponsPath, "assets", "minecraft", "items", "grouping", "materials.txt");

        Map<String, List<TexturesPlusItem>> itemMap = new LinkedHashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path path : stream) {
                TexturesPlusDatapackGeneralUtil.generateMapEntry(itemMap, path);
            }
        }
        List<Pattern> regexGroupPatternsSword = new ArrayList<>();
        List<Pattern> regexGroupPatternsAxe = new ArrayList<>();
        List<Pattern> regexGroupPatternsHoe = new ArrayList<>();
        List<Pattern> regexGroupPatternsShieldTotem = new ArrayList<>();
        List<Pattern> regexGroupPatternsMisc = new ArrayList<>();
        List<String> materials = new ArrayList<>();
        try {
            regexGroupPatternsSword = Files.readAllLines(groupsPath.resolve("group_sword.txt")).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
            regexGroupPatternsAxe = Files.readAllLines(groupsPath.resolve("group_axe.txt")).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
            regexGroupPatternsHoe = Files.readAllLines(groupsPath.resolve("group_hoe.txt")).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
            regexGroupPatternsShieldTotem = Files.readAllLines(groupsPath.resolve("group_shieldtotem.txt")).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
            regexGroupPatternsMisc = Files.readAllLines(groupsPath.resolve("group_misc.txt")).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                    .toList();
        } catch (Exception e)
        {
            System.err.println("Could not read groups: " + e.getMessage());
        }
        try {
            materials = Files.readAllLines(materialsPath).stream().toList();
        } catch (Exception e)
        {
            System.err.println("Could not read materials.txt: " + e.getMessage());
        }
        List<TexturesPlusItem> misc = new ArrayList<>();
        List<TexturesPlusItem> shieldtotems = new ArrayList<>();
        List<TexturesPlusItem> hoes = new ArrayList<>();
        List<TexturesPlusItem> axes = new ArrayList<>();
        List<TexturesPlusItem> swords = new ArrayList<>();

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
        createWeaponRow(sb,misc,-25,-53,14,"red_concrete","south", regexGroupPatternsMisc, materials);
        //shieldtotems -25 -53 8, orange_concrete, south
        createWeaponRow(sb,shieldtotems,-25,-53,8,"orange_concrete","south", regexGroupPatternsShieldTotem, materials);
        //hoes -25 -53 2, yellow_concrete, south
        createWeaponRow(sb,hoes,-25,-53,2,"yellow_concrete","south", regexGroupPatternsHoe, materials);
        //axes -25 -53 -2, lime_concrete, north
        createWeaponRow(sb,axes,-25,-53,-2,"lime_concrete","north", regexGroupPatternsAxe, materials);
        //swords -25 -53 -8, blue_concrete, north
        createWeaponRow(sb,swords,-25,-53,-8,"blue_concrete","north", regexGroupPatternsSword, materials);

        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allweapons.mcfunction");

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static List<List<TexturesPlusItem>> groupByItemSuffix(List<TexturesPlusItem> items, List<Pattern> regexGoupPatterns, List<String> materials) {

        Map<String, List<TexturesPlusItem>> grouped = new LinkedHashMap<>();
        List<TexturesPlusItem> misc = new ArrayList<>();

        // 1. Group by material
        for (TexturesPlusItem item : items) {
            String typeUpper = item.rename().toUpperCase();
            boolean matched = false;

            for (String mat : materials) {
                if (typeUpper.startsWith(mat)) {
                    String suffix = item.rename().substring(mat.length()).trim().toLowerCase();
                    grouped.computeIfAbsent("material:" + suffix, k -> new ArrayList<>()).add(item);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                misc.add(item);
            }
        }

        // 2. Group misc items that are Lightsabers by their descriptor
        Map<String, List<TexturesPlusItem>> miscGrouped = new LinkedHashMap<>();

        // 3. Group by regex match from file
        Set<TexturesPlusItem> regexMatched = new LinkedHashSet<>();
        for (Pattern pattern : regexGoupPatterns) {
            for (TexturesPlusItem item : misc) {
                if (regexMatched.contains(item)) continue;
                if (pattern.matcher(item.rename()).matches()) {
                    String key = "regex:" + pattern.pattern();
                    miscGrouped.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                    regexMatched.add(item);
                }
            }
        }

        // 4. Group exact name matches for unmatched
        for (TexturesPlusItem item : misc) {
            if (!regexMatched.contains(item)) {
                miscGrouped.computeIfAbsent("exact:" + item.rename().toLowerCase(), k -> new ArrayList<>()).add(item);
            }
        }

        // Build final grouped list
        List<List<TexturesPlusItem>> result = new ArrayList<>();

        // Split non-misc (material) groups into chunks of max size 9
        for (Map.Entry<String, List<TexturesPlusItem>> entry : grouped.entrySet()) {
            List<TexturesPlusItem> group = entry.getValue();
            for (int i = 0; i < group.size(); i += 9) {
                result.add(new ArrayList<>(group.subList(i, Math.min(i + 9, group.size()))));
            }
        }

        // Split misc groups into chunks of max size 9 too
        for (Map.Entry<String, List<TexturesPlusItem>> entry : miscGrouped.entrySet()) {
            List<TexturesPlusItem> group = entry.getValue();
            for (int i = 0; i < group.size(); i += 9) {
                result.add(new ArrayList<>(group.subList(i, Math.min(i + 9, group.size()))));
            }
        }
        for (List<TexturesPlusItem> group : result) {
            group.sort((a,b) -> compareByMaterialOrder(a, b, materials));
        }
        return result;
    }
    public static int compareByMaterialOrder(TexturesPlusItem a, TexturesPlusItem b, List<String> materials) {
        int indexA = getMaterialIndex(a.rename(), materials);
        int indexB = getMaterialIndex(b.rename(), materials);
        return Integer.compare(indexA, indexB);
    }

    public static int getMaterialIndex(String rename, List<String> materials) {
        for (int i = 0; i < materials.size(); i++) {
            if (rename.toLowerCase().contains(materials.get(i).toLowerCase())) {
                return i;
            }
        }
        return Integer.MAX_VALUE; // Items without a material go to the end
    }
    static void createWeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction, List<Pattern> regexGoupPatterns, List<String> materials) {
        x--;
        List<List<TexturesPlusItem>> sections = groupByItemSuffix(items, regexGoupPatterns, materials);
        for(List<TexturesPlusItem> section : sections)
        {
            if(section.size() > 9 || section.size() == 1)
            {
                x = continueSimpleWeaponRow(sb,section, x, y, z, block, direction);
            }
            else
            {
                if(section.getFirst().enchantments().isEmpty())
                {
                    sb.append("function texturesplus:weapons/placemultweapons").append(direction).append("start {block:\"").append(block).append("\", x:").append(x).append(",y:").append(y).append(",z:").append(z).append("}\n");
                    int num = 0;
                    for(TexturesPlusItem item : section)
                    {
                        sb.append(generateWeaponMultCommand(x,y,z, item.rename(),block,direction, item.damage(), item.itemType(),getSpecialBlock(item.itemType(), block), num)).append("\n");
                        num++;
                    }
                }
                else
                {
                    sb.append("function texturesplus:weapons/placemultweapons").append(direction).append("startenchant {block:\"").append(block).append("\", x:").append(x).append(",y:").append(y).append(",z:").append(z).append(",enchantment:").append(section.getFirst().enchantments().getFirst().replace("minecraft:", "")).append("}\n");
                    int num = 0;
                    for(TexturesPlusItem item : section)
                    {
                        sb.append(generateWeaponMultCommandEnchant(x,y,z, item.rename(),block,direction, item.damage(), item.enchantments().getFirst(), item.itemType(),getSpecialBlock(item.itemType(), block), num)).append("\n");
                        num++;
                    }
                }
                x-=1;
            }
        }
    }
    static int continueSimpleWeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction)
    {
        for (TexturesPlusItem item : items)
        {
            if(item.enchantments().isEmpty())
            {
                sb.append(generateWeaponCommand(x,y,z, item.rename(),block,direction, item.damage(), item.itemType(),getSpecialBlock(item.itemType(), block))).append("\n");
            }
            else
            {
                sb.append(generateEnchantedWeaponCommand(x,y,z, item.rename(),block,direction, item.damage(), item.enchantments().getFirst(), item.itemType(),getSpecialBlock(item.itemType(), block))).append("\n");
            }
            x -=1;
        }
        return x;
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
    static String generateWeaponMultCommand(int x, int y, int z, String rename, String block, String direction, int damage, String item, String specialBlock, int num)
    {
        return "function texturesplus:weapons/placemultweapons" + direction + "num {item:\""+ item + "\",x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",num: " + num + ",rename: \"" + rename + "\",block: \"" + block +"\",special_block:\""+specialBlock + "\",damage:"+ damage +"}";
    }
    static String generateWeaponMultCommandEnchant(int x, int y, int z, String rename, String block, String direction, int damage, String enchant, String item, String specialBlock, int num)
    {
        return "function texturesplus:weapons/placemultweapons" + direction + "numenchant {item:\""+ item + "\",x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",num: " + num + ",rename: \"" + rename + "\",block: \"" + block +"\",special_block:\""+specialBlock + "\",damage:"+ damage +",enchantment:\""+enchant.replace("minecraft:","")+"\"}";
    }
}
