package com.dbrighthd.texturesplusmod.datapack;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;
import static com.dbrighthd.texturesplusmod.datapack.TexturesPlusDatapackGeneralUtil.*;

public class WeaponsPlusDatapackGenerator {
    public static Map<String, String> specialBlocks;
    public static Map<String, String> armorGroupings;
    public static List<String> materials;
    public static void generateWeaponsMcfunction() throws IOException {
        LOGGER.info("Generating Weapons+ placement in world...");
        specialBlocks = getSpecialBlocks();
        armorGroupings = getArmorGroupings();

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
        List<TexturesPlusItem> armors = new ArrayList<>();

        for (Map.Entry<String, List<TexturesPlusItem>> entry : itemMap.entrySet()) {

            String key = entry.getKey();
            List<TexturesPlusItem> renames = entry.getValue();

            if(key.contains("boots") || key.contains("leggings") || key.contains("chestplate") || key.contains("helmet"))
            {
                armors.addAll(renames);
            }
            else if(key.contains("shield") || key.contains("totem"))
            {
                shieldtotems.addAll(renames);
            }
            else if(key.contains("hoe"))
            {
                hoes.addAll(renames);
            }
            else if(key.contains("pickaxe"))
            {
                hoes.addAll(renames);
            }
            else if(key.contains("shovel"))
            {
                hoes.addAll(renames);
            }
            else if(key.contains("axe"))
            {
                axes.addAll(renames);
            }
            else if(key.contains("spear"))
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
        createWeaponRow(sb,misc,-25,-53,14,"red_concrete","south", regexGroupPatternsMisc);
        //shieldtotems -25 -53 8, orange_concrete, south
        createWeaponRow(sb,shieldtotems,-25,-53,8,"orange_concrete","south", regexGroupPatternsShieldTotem);
        //hoes -25 -53 2, yellow_concrete, south
        createWeaponRow(sb,hoes,-25,-53,2,"yellow_concrete","south", regexGroupPatternsHoe);
        //axes -25 -53 -2, lime_concrete, north
        createWeaponRow(sb,axes,-25,-53,-2,"lime_concrete","north", regexGroupPatternsAxe);
        //swords -25 -53 -8, blue_concrete, north
        createWeaponRow(sb,swords,-25,-53,-8,"blue_concrete","north", regexGroupPatternsSword);
        //armors -25 -53 -14, purple_concrete, north
        createArmorsRow(sb,armors,-25,-53,-14,"purple_concrete","north");


        Path functionPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allweapons.mcfunction");
        Path functionSettingsPath = Paths.get(Minecraft.getInstance().gameDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","armorsettings.mcfunction");

        if(TexturesPlusModClient.getConfig().cullArmor)
        {
            Files.writeString(functionSettingsPath, "return 1", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    public static Map<String, List<TexturesPlusItem>> getArmorFallbacks(List<TexturesPlusItem> allArmors) {
        String weaponsPath = "weaponsplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            weaponsPath = "weapons";
        }

        Path txtFile = Paths.get(
                Minecraft.getInstance().gameDirectory.getPath(),
                "resourcepacks",
                weaponsPath,
                "assets",
                "minecraft",
                "items",
                "grouping",
                "armor_defaults.txt"
        );

        // Build a quick lookup: rename() -> item (O(1) lookups while parsing)
        Map<String, TexturesPlusItem> byRename = new HashMap<>();
        for (TexturesPlusItem item : allArmors) {
            if (item == null) continue;
            String name = item.rename();
            if (name == null) continue;

            // If duplicates exist, keep the first one encountered
            byRename.putIfAbsent(name, item);
        }

        Map<String, List<TexturesPlusItem>> result = new LinkedHashMap<>();

        if (!Files.exists(txtFile)) {
            // File not found => return empty map
            return result;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(txtFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Couldn't read => return empty map (or log it if you have a logger)
            e.printStackTrace();
            return result;
        }

        for (String rawLine : lines) {
            if (rawLine == null) continue;

            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // Optional: ignore comments
            if (line.startsWith("#") || line.startsWith("//")) continue;

            String[] parts = line.split(",");
            if (parts.length < 5) {
                // Need: key + 4 armor names
                continue;
            }

            String keyRegex = parts[0].trim();
            if (keyRegex.isEmpty()) continue;

            List<TexturesPlusItem> fallbackList = new ArrayList<>(4);
            for (int i = 1; i <= 4; i++) {
                String armorName = parts[i].trim();
                TexturesPlusItem match = byRename.get(armorName); // may be null if not found
                fallbackList.add(match);
            }

            result.put(keyRegex, fallbackList);
        }

        return result;
    }
    public static Map<String, String> getSpecialBlocks() {
        String weaponsPath = TexturesPlusModClient.getConfig().devMode ? "weapons" : "weaponsplus";

        Path txtFile = Paths.get(
                Minecraft.getInstance().gameDirectory.getPath(),
                "resourcepacks",
                weaponsPath,
                "assets",
                "minecraft",
                "items",
                "grouping",
                "materials_blocks.txt"
        );

        try (Stream<String> lines = Files.lines(txtFile)) {
            return parseKeyValueFromLines(s -> s, lines);
        } catch (IOException e) {
            System.err.println("Could not read materials_blocks.txt: " + e.getMessage());
            return new HashMap<>();
        }
    }
    public static Map<String, String> getArmorGroupings() {
        String weaponsPath = TexturesPlusModClient.getConfig().devMode ? "weapons" : "weaponsplus";

        Path txtFile = Paths.get(
                Minecraft.getInstance().gameDirectory.getPath(),
                "resourcepacks",
                weaponsPath,
                "assets",
                "minecraft",
                "items",
                "grouping",
                "group_armor.txt"
        );

        try (Stream<String> lines = Files.lines(txtFile)) {
            return parseKeyValueFromLinesOrdered(s -> s, lines);
        } catch (IOException e) {
            System.err.println("Could not read group_armor.txt: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static List<List<TexturesPlusItem>> groupByItemSuffix(List<TexturesPlusItem> items, List<Pattern> regexGoupPatterns) {

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
            addAllElementsToListOfList(result, group, 9);
        }

        // Split misc groups into chunks of max size 9 too
        for (Map.Entry<String, List<TexturesPlusItem>> entry : miscGrouped.entrySet()) {
            List<TexturesPlusItem> group = entry.getValue();
            addAllElementsToListOfList(result, group, 9);
        }
        for (List<TexturesPlusItem> group : result) {
            group.sort((a,b) -> compareByMaterialOrder(a, b, materials));
        }
        return result;
    }

    public static String getArmorSlot(TexturesPlusItem item)
    {
        if (item.itemType().contains("chestplate"))
        {
            return "chest";
        }
        else if (item.itemType().contains("helmet"))
        {
            return "head";
        }
        else if (item.itemType().contains("boots"))
        {
            return "feet";
        }
        else if (item.itemType().contains("leggings"))
        {
            return "legs";
        }
        return "none";
    }
    public static boolean isValidArmorSet(List<TexturesPlusItem> armors)
    {
        if(armors.size() > 4)
        {
            return false;
        }
        int chestplatecount = 0, helmetcount = 0, bootcount = 0, leggingcount = 0;
        for (TexturesPlusItem armor : armors)
        {
            String armorType = getArmorSlot(armor);
            if (armorType.equals("chest"))
            {
                chestplatecount++;
            }
            if (armorType.equals("feet"))
            {
                bootcount++;
            }
            if (armorType.equals("head"))
            {
                helmetcount++;
            }
            if (armorType.equals("legs"))
            {
                leggingcount++;
            }
        }
        return chestplatecount == 1 && helmetcount == 1 && bootcount == 1 && leggingcount == 1;
    }
    public static TexturesPlusItem getArmorTypeInSet(String armor_type, List<TexturesPlusItem> armorset)
    {
        for (TexturesPlusItem armor : armorset)
        {
            if (getArmorSlot(armor).equals(armor_type))
            {
                return armor;
            }
        }
        return null;
    }
    public static String removeArmorType(String rename)
    {
        return rename.replaceAll("(( ?)chestplate)|(( ?)helmet)|(( ?)boots)|(( ?)leggings)","");
    }
    public static List<List<TexturesPlusItem>> groupByArmorSuffix(List<TexturesPlusItem> items, Map<String,List<TexturesPlusItem>> armorFallbacks) {

        Map<String, List<TexturesPlusItem>> grouped = new LinkedHashMap<>();
        List<TexturesPlusItem> misc = new ArrayList<>();


        Map<String, List<TexturesPlusItem>> miscGrouped = new LinkedHashMap<>();

        for (TexturesPlusItem item : items) {
            miscGrouped.computeIfAbsent("exact:" + removeArmorType(item.rename().toLowerCase()), k -> new ArrayList<>()).add(item);
        }

        List<List<TexturesPlusItem>> result = new ArrayList<>();


        // Split misc groups into chunks of max size 4 too
        for (Map.Entry<String, List<TexturesPlusItem>> entry : miscGrouped.entrySet()) {
            List<TexturesPlusItem> group = entry.getValue();
            fillArmorSetWithFallbacks(group,armorFallbacks);
            if(isValidArmorSet(group))
            {
                result.add(group);
            }
            else
            {
                if(group.size() % 4 == 0)
                {

                    List<List<TexturesPlusItem>> resultTest = new ArrayList<>();
                    addAllElementsToListOfList(resultTest, group, 4);
                    for(List<TexturesPlusItem> armorSet : resultTest)
                    {
                        if(isValidArmorSet(armorSet))
                        {
                            result.add(armorSet);
                        }
                        else
                        {
                            addAllElementsToListOfList(result, armorSet, 1);
                        }
                    }
                }
                else
                {
                    addAllElementsToListOfList(result, group, 1);
                }

            }
        }
        for (List<TexturesPlusItem> group : result) {
            group.sort((a,b) -> compareByMaterialOrder(a, b, materials));
        }
        return result;
    }
    public static List<List<List<TexturesPlusItem>>> groupArmorSets(List<List<TexturesPlusItem>> armorSets) {

        List<List<List<TexturesPlusItem>>> grouped = new ArrayList<>();


        List<List<TexturesPlusItem>> remaining = new ArrayList<>(armorSets);

        List<Map.Entry<Pattern, String>> rules = new ArrayList<>();
        for (Map.Entry<String, String> e : armorGroupings.entrySet()) {
            rules.add(Map.entry(Pattern.compile(e.getKey()), e.getValue()));
        }

        for (Map.Entry<Pattern, String> rule : rules) {
            Pattern pattern = rule.getKey();
            String slot = rule.getValue();

            List<List<TexturesPlusItem>> group = new ArrayList<>();

            for (Iterator<List<TexturesPlusItem>> it = remaining.iterator(); it.hasNext();) {
                List<TexturesPlusItem> armorSet = it.next();

                TexturesPlusItem itemInSlot = getArmorTypeInSet(slot, armorSet);
                if (itemInSlot == null) continue;

                String rename = itemInSlot.rename();
                if (rename == null) continue;

                if (pattern.matcher(rename).matches()) {
                    group.add(armorSet);
                    it.remove();
                }
            }
            if (!group.isEmpty()) {
                grouped.add(group);
            }

            if (remaining.isEmpty()) break;
        }

        for (List<TexturesPlusItem> leftover : remaining) {
            grouped.add(new ArrayList<>(List.of(leftover)));
        }
        for (List<List<TexturesPlusItem>> toSortArmorSetAlphabetical : grouped)
        {
            toSortArmorSetAlphabetical.sort((a,b) -> a.getFirst().rename().compareToIgnoreCase(b.getFirst().rename()));
            toSortArmorSetAlphabetical.sort(Comparator.comparingInt(p -> countSpaces(p.getFirst().rename())));
        }
        for (List<List<TexturesPlusItem>> toSortArmorSetByMaterial : grouped)
        {
            toSortArmorSetByMaterial.sort((a,b) -> compareByMaterialOrder(a.getFirst(), b.getFirst(), materials));
        }
        return grouped;
    }

    private static int countSpaces(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == ' ') count++;
        }
        return count;
    }

    public static void fillArmorSetWithFallbacks(List<TexturesPlusItem> armorSet, Map<String,List<TexturesPlusItem>> armorFallbacks)
    {
        int chestplatecount = 0, helmetcount = 0, bootcount = 0, leggingcount = 0;
        for (TexturesPlusItem armor : armorSet)
        {
            String armorType = getArmorSlot(armor);
            if (armorType.equals("chest"))
            {
                chestplatecount++;
            }
            if (armorType.equals("feet"))
            {
                bootcount++;
            }
            if (armorType.equals("head"))
            {
                helmetcount++;
            }
            if (armorType.equals("legs"))
            {
                leggingcount++;
            }
        }
        if(bootcount == 1 && leggingcount == 1 && helmetcount == 1 && chestplatecount == 1)
        {
            return;
        }
        if(bootcount > 1 || leggingcount > 1 || helmetcount > 1 || chestplatecount > 1)
        {
            return;
        }
        List<List<TexturesPlusItem>> MatchingArmorFallbacks = new ArrayList<>();
        for(String pattern : armorFallbacks.keySet())
        {
            if (armorSet.getFirst().rename().toLowerCase().matches(pattern.toLowerCase()))
            {
                MatchingArmorFallbacks.add(armorFallbacks.get(pattern));
            }
        }
        if(MatchingArmorFallbacks.isEmpty())
        {
            return;
        }
        List<TexturesPlusItem> MatchingArmorFallback = MatchingArmorFallbacks.getFirst();
        if(bootcount == 0)
        {
            armorSet.add(getArmorTypeInSet("feet",MatchingArmorFallback));
        }
        if(leggingcount == 0)
        {
            armorSet.add(getArmorTypeInSet("legs",MatchingArmorFallback));
        }
        if(chestplatecount == 0)
        {
            armorSet.add(getArmorTypeInSet("chest",MatchingArmorFallback));
        }
        if(helmetcount == 0)
        {
            armorSet.add(getArmorTypeInSet("head",MatchingArmorFallback));
        }
    }
    public static int compareByMaterialOrder(TexturesPlusItem a, TexturesPlusItem b, List<String> materials) {
        int indexA = getMaterialIndex(a.itemType(), materials);
        int indexB = getMaterialIndex(b.itemType(), materials);
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
    static void createWeaponRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction, List<Pattern> regexGoupPatterns) {
        x--;
        List<List<TexturesPlusItem>> sections = groupByItemSuffix(items, regexGoupPatterns);
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
    static void createArmorsRow(StringBuilder sb, List<TexturesPlusItem> items, int x, int y, int z, String block, String direction) {
        x--;
        int defaultXOffset = 2;
        Map<String,List<TexturesPlusItem>> armorFallbacks = getArmorFallbacks(items);
        List<List<TexturesPlusItem>> sections = groupByArmorSuffix(items, armorFallbacks);
        List<List<List<TexturesPlusItem>>> sortedSections = groupArmorSets(sections);
        for(List<List<TexturesPlusItem>> sortedSection : sortedSections)
        {
            if(sortedSection.size() == 1)
            {
                if(sortedSection.getFirst().size() == 1)
                {
                    sb.append(generateArmorCommand(x,y,z,sortedSection.getFirst().getFirst(),block,getSpecialBlock(sortedSection.getFirst().getFirst().itemType(),block), defaultXOffset, 0) + "\n");
                }
                else
                {
                    sb.append(generateArmorSetCommand(x,y,z,sortedSection.getFirst(),block, defaultXOffset, 0)+"\n");
                }
                x -= defaultXOffset;
            }
            else
            {
                int zOffsetIncrement = 3;
                int zOffset = 0;
                int index = 0;
                for(List<TexturesPlusItem> armorSet : sortedSection)
                {
                    if(index == 0)
                    {
                        if(armorSet.size() == 1)
                        {
                            sb.append(generateArmorCommand(x,y,z,armorSet.getFirst(),block,getSpecialBlock(armorSet.getFirst().itemType(),block), 1, 0) + "\n");
                        }
                        else
                        {
                            sb.append(generateArmorSetCommand(x,y,z + zOffset,armorSet,block, 1, 0)+"\n");
                        }

                    }
                    else {
                        if(armorSet.size() == 1)
                        {
                            sb.append(generateArmorCommand(x,y,z + zOffset,armorSet.getFirst(),block,getSpecialBlock(armorSet.getFirst().itemType(),block), 0, zOffsetIncrement-2) + "\n");
                        }
                        else
                        {
                            sb.append(generateArmorSetCommand(x,y,z + zOffset,armorSet,block, 0, zOffsetIncrement-2)+"\n");
                        }

                    }
                    index++;
                    zOffset -= zOffsetIncrement;
                }
                x -= defaultXOffset;
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
        if (specialBlocks == null)
        {
            return block;
        }
        for(String key : specialBlocks.keySet())
        {
            if (itemName.matches(key))
            {
                if(specialBlocks.get(key).equals("none"))
                {
                    return block;
                }
                else
                {
                    return specialBlocks.get(key);
                }
            }
        }
        return block;
    }

    static String generateArmorCommand(int x, int y, int z, TexturesPlusItem armor, String block, String specialBlock, int xOffset, int zOffset)
    {
        String command = "function texturesplus:weapons/place1armornorth {";
        command = addMacro(command, "x_offset", xOffset+"", true);
        command = addMacro(command, "z_offset", zOffset+"", true);
        command = addMacro(command, "x", x+"", true);
        command = addMacro(command, "y", y+"", true);
        command = addMacro(command, "z", z+"", true);
        command = addMacro(command, "block", block, true);
        command = addMacro(command, "special_block", specialBlock, true);
        command = addMacro(command, "item", armor.itemType(), true);
        command = addMacro(command, "slot", getArmorSlot(armor), true);
        command = addMacro(command, "damage", armor.damage()+"", true);
        command = addMacro(command, "rename", armor.rename(), false);
        return command + "}";
    }

    static String generateArmorSetCommand(int x, int y, int z, List<TexturesPlusItem> armorSet, String block, int xOffset, int zOffset)
    {
        TexturesPlusItem helmet = getArmorTypeInSet("head",armorSet);
        TexturesPlusItem chestplate = getArmorTypeInSet("chest",armorSet);
        TexturesPlusItem leggings = getArmorTypeInSet("legs",armorSet);
        TexturesPlusItem boots = getArmorTypeInSet("feet",armorSet);
        String command = "function texturesplus:weapons/placefullarmornorth {";
        command = addMacro(command, "x_offset", xOffset+"", true);
        command = addMacro(command, "z_offset", zOffset+"", true);
        command = addMacro(command, "x", x+"", true);
        command = addMacro(command, "y", y+"", true);
        command = addMacro(command, "z", z+"", true);
        command = addMacro(command, "block", block, true);
        command = addMacro(command, "special_block_boots", getSpecialBlock(boots.itemType(), block), true);
        command = addMacro(command, "item_boots", boots.itemType(), true);
        command = addMacro(command, "rename_boots", boots.rename(), true);
        command = addMacro(command, "damage_boots", boots.damage()+"", true);
        command = addMacro(command, "special_block_leggings", getSpecialBlock(leggings.itemType(), block), true);
        command = addMacro(command, "item_leggings", leggings.itemType(), true);
        command = addMacro(command, "rename_leggings", leggings.rename(), true);
        command = addMacro(command, "damage_leggings", leggings.damage()+"", true);
        command = addMacro(command, "special_block_chestplate", getSpecialBlock(chestplate.itemType(), block), true);
        command = addMacro(command, "item_chestplate", chestplate.itemType(), true);
        command = addMacro(command, "rename_chestplate", chestplate.rename(), true);
        command = addMacro(command, "damage_chestplate", chestplate.damage()+"", true);
        command = addMacro(command, "special_block_helmet", getSpecialBlock(helmet.itemType(), block), true);
        command = addMacro(command, "item_helmet", helmet.itemType(), true);
        command = addMacro(command, "rename_helmet", helmet.rename(), true);
        command = addMacro(command, "damage_helmet", helmet.damage()+"", false);
        return  command + "}";
    }

    static String addMacro(String command, String macro_name, String macro_input, boolean comma)
    {
        String result = command + macro_name + ":\"" + macro_input + "\"";
        if(comma)
            result = result + ",";
        return result;
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
