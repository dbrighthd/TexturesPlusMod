package com.dbrighthd.texturesplusmod.datapackutil;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class WeaponsPlusDatapackGenerator {
    public static void generateWeaponsMcfunction() throws IOException {
        LOGGER.info("Generating Weapons+ placement in world...");
        // Path to your JSON file
        String weaponsPath = "weaponsplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            weaponsPath = "weapons";
        }
        Path dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", weaponsPath,"assets","minecraft","items");
        Map<String, List<TexturesPlusItem>> itemMap = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path path : stream) {
                TexturesPlusDatapackGeneralUtil.generateMapEntry(itemMap, path);
            }
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
    public static List<List<TexturesPlusItem>> groupByItemSuffix(List<TexturesPlusItem> items) {
        List<String> materials = List.of("WOODEN", "STONE", "GOLDEN", "IRON", "DIAMOND", "NETHERITE");

        Map<String, List<TexturesPlusItem>> grouped = new LinkedHashMap<>();
        List<TexturesPlusItem> misc = new ArrayList<>();

        for (TexturesPlusItem item : items) {
            String typeUpper = item.rename.toUpperCase();
            boolean matched = false;

            for (String mat : materials) {
                if (typeUpper.startsWith(mat)) {
                    String suffix = item.rename.substring(mat.length()).trim().toLowerCase();
                    grouped.computeIfAbsent(suffix, k -> new ArrayList<>()).add(item);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                misc.add(item);
            }
        }

        // Prepare final grouped list
        List<List<TexturesPlusItem>> result = new ArrayList<>(grouped.values());
        if (!misc.isEmpty()) {
            result.add(misc); // Add misc group at the end
        }

        return result;
    }
    static void createWeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction)
    {
        x--;
        List<List<TexturesPlusItem>> sections = groupByItemSuffix(items);
        for(List<TexturesPlusItem> section : sections)
        {
            if(section.size() > 9 || section.size() == 1)
            {
                x = continueSimpleeaponRow(sb,section, x, y, z, block, direction);
            }
            else
            {
                sb.append("function texturesplus:weapons/placemultweapons" + direction + "start {block:\"" + block + "\", x:" + x + ",y:" + y + ",z:" + z + "}\n");
                int num = 0;
                for(TexturesPlusItem item : section)
                {
                    sb.append(generateWeaponMultCommand(x,y,z,item.rename,block,direction,item.damage, item.itemType,getSpecialBlock(item.itemType, block), num)).append("\n");
                    num++;
                }
                x-=1;
            }
        }
    }
    static int continueSimpleeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction)
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
}
