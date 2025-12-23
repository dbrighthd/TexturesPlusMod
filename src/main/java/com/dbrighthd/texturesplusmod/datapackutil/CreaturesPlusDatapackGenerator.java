package com.dbrighthd.texturesplusmod.datapackutil;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.text2speech.Narrator.LOGGER;

public class CreaturesPlusDatapackGenerator {
    public static Set<String> parsedEntities;
    public static List<String> disallowedEntitiesBeforeParse;
    public static List<String> disallowedEntitiesAfterParse;
    public static Map<String, int[]> entityMachinePlacements;
    public static Map<String, Integer> entityIncrements;
    public static Map<String, Integer> miscArgs;
    public static Map<String, String> miscStringArgs;


    private static List<String> loadListFromFile(Path filename) {
        try {
            return Files.readAllLines(filename);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static void generateCreaturesMcfunction() throws IOException {
        LOGGER.info("Generating Creatures+ placement in world...");
        // Path to your JSON file
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        entityMachinePlacements = getEntityMachineSize();
        copyMachinesToData();
        disallowedEntitiesBeforeParse = getDisallowedEntitiesBeforeParse();
        disallowedEntitiesAfterParse = getDisallowedEntitiesAfterParse();
        entityIncrements = getEntityIncrements();
        miscArgs = getMiscArgs();
        miscStringArgs = getMiscStringArgs();
        parsedEntities = new HashSet<>();
        List<Path> propertyFiles = new ArrayList<>();
        Path creaturesDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "minecraft", "optifine", "random", "entity");
        Path creaturesCemDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "minecraft", "optifine", "cem");

        Map<String, String> cemTexturePaths = new HashMap<>();
        try (Stream<Path> paths = Files.walk(creaturesDir)) {
            paths.filter(p -> Files.isRegularFile(p) &&
                            p.getFileName().toString().endsWith(".properties"))
                    .forEach(p -> propertyFiles.add(p));
        }
        try (Stream<Path> paths = Files.walk(creaturesCemDir)) {
            paths.filter(p -> Files.isRegularFile(p) &&
                            p.getFileName().toString().endsWith(".jem"))
                    .forEach(p -> processCemTexture(p, cemTexturePaths));
        }
        try (Stream<Path> paths = Files.walk(creaturesCemDir)) {
            paths.filter(p -> Files.isRegularFile(p) &&
                            p.getFileName().toString().endsWith(".properties"))
                    .forEach(p -> propertyFiles.add(p));
        }
        List<TexturesPlusEntity> allEntities = new ArrayList<>();
        for (Path p : propertyFiles) {
            TexturesPlusEntity entityToAdd = getEntityFromPropertyFile(p, cemTexturePaths);
            if (entityToAdd != null) {
                allEntities.add(entityToAdd);
            }
        }
        List<TexturesPlusEntity> entities;


        if (TexturesPlusModClient.getConfig().mergeEntities) {
            entities = mergeLikeEntities(allEntities);
        } else {
            entities = allEntities;
        }
        Comparator<TexturesPlusEntity> byNbtSize =
                Comparator.comparingInt((TexturesPlusEntity e) -> e.nbtList.size())
                        .reversed();
        List<List<TexturesPlusEntity>> westEastEntities;
        if (!TexturesPlusModClient.getConfig().sortAlphabetically) {
            entities.sort(byNbtSize);
            westEastEntities = TexturesPlusDatapackGeneralUtil.splitInHalfAlternating(entities);
        } else {
            entities.sort((a, b) -> a.entityType.compareToIgnoreCase(b.entityType));
            westEastEntities = TexturesPlusDatapackGeneralUtil.splitInHalfMiddle(entities);
        }

        List<TexturesPlusEntity> westEntities = westEastEntities.get(0);
        if (TexturesPlusModClient.getConfig().sortAlphabetically) {
            westEntities = westEntities.reversed();
        }
        List<TexturesPlusEntity> eastEntities = westEastEntities.get(1);

        StringBuilder sb = new StringBuilder();
        createCreatures(sb, westEntities, "west");
        createCreatures(sb, eastEntities, "east");
        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated", "datapacks", "texturesplus", "data", "texturesplus", "function", "allcreatures.mcfunction");
        Path functionPathBlocks = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated", "datapacks", "texturesplus", "data", "texturesplus", "function", "allcreaturesblocks.mcfunction");
        Files.writeString(functionPathBlocks, sb.toString().replace("placeentity", "placeblocks"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void processCemTexture(Path file, Map<String, String> cemMap) {
        try (Reader in = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(in);
            findTextures(root, file.getFileName().toString(), cemMap);
        } catch (IOException ex) {
            // Replace with your logger of choice
            System.err.println("[TextureIndex] Couldn't read " + file + ": " + ex.getMessage());
        }
    }

    private static void findTextures(JsonElement el, String fileName, Map<String, String> cemMap) {
        if (el.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : el.getAsJsonObject().entrySet()) {
                if ("texture".equals(entry.getKey())
                        && entry.getValue().isJsonPrimitive()
                        && entry.getValue().getAsJsonPrimitive().isString()) {

                    if (!fileName.contains("baby") && !fileName.contains("saddle")) {
                        cemMap.put(entry.getValue().getAsString(), fileName);
                    }

                }
                // keep searching children
                findTextures(entry.getValue(), fileName, cemMap);
            }
        } else if (el.isJsonArray()) {
            for (JsonElement child : el.getAsJsonArray()) {
                findTextures(child, fileName, cemMap);
            }
        }
    }

    public static List<TexturesPlusEntity> mergeLikeEntities(List<TexturesPlusEntity> allEntities) {
        List<TexturesPlusEntity> mergedEntities = new ArrayList<>();
        for (TexturesPlusEntity entity : allEntities) {
            int index = indexOfEntity(mergedEntities, entity);
            if (index >= 0) {
                mergeEntityNoRepeat(mergedEntities.get(index), entity);
            } else {
                mergedEntities.add(entity);
            }
        }
        return mergedEntities;
    }

    public static void mergeEntityNoRepeat(TexturesPlusEntity entityToMergeInto, TexturesPlusEntity entityToMergeFrom) {
        for (TexturesPlusEntityNbt entityNbt : entityToMergeFrom.nbtList) {
            boolean isRepeat = false;
            for (TexturesPlusEntityNbt targetNbt : entityToMergeInto.nbtList) {
                if (entityNbt.rename.toLowerCase().equals(targetNbt.rename.toLowerCase()) && !targetNbt.rename.toLowerCase().equals("default")) {
                    isRepeat = true;
                    break;
                }
            }
            if (!isRepeat) {
                entityToMergeInto.nbtList.add(entityNbt);
            }
        }
    }

    public static int indexOfEntity(List<TexturesPlusEntity> entities, TexturesPlusEntity entityToFind) {
        int index = -1;
        for (TexturesPlusEntity entity : entities) {
            index++;
            if (entity.entityType.equals(entityToFind.entityType)) {
                return index;
            }
        }
        return -1;
    }

    public static void createCreatures(StringBuilder sb, List<TexturesPlusEntity> entities, String direction)
    {
        String block = miscStringArgs.get(direction + "_block_1");
        int x = miscArgs.get("x_origin");
        int y = miscArgs.get("y_origin");
        int z = miscArgs.get("z_origin");
        for (TexturesPlusEntity entity : entities) {
            if (Math.abs(x) / 4 >= entities.size() / 3) {
                if (Math.abs(x) / 4 >= entities.size() * 2 / 3) {
                    block = miscStringArgs.get(direction + "_block_3");
                    ;
                } else {
                    block = miscStringArgs.get(direction + "_block_2");
                    ;
                }
            }
            x += miscArgs.get("row_size") * miscArgs.get(direction + "_direction");
            z -= miscArgs.get("row_size");
            int increment = miscArgs.get("default_increment");
            for (Map.Entry<String, Integer> entry : entityIncrements.entrySet()) {
                if (entity.entityType.contains(entry.getKey())) {
                    increment = entry.getValue();
                    break;
                }
            }
            sb.append("function texturesplus:creatures/placenewrow" + direction + " {x:" + x + ",y:" + y + ",z:" + z + ",block:\"" + block + "\"}\n");
            createCreatureRow(sb, entity, x, y, z, increment, block);
        }
    }

    public static void placeNewMachine(StringBuilder sb, int x, int y, int z, String entity) {
        int[] offsets = entityMachinePlacements.get(entity);

        sb.append("execute positioned "
                + (x + offsets[0]) + " "
                + (y + offsets[1]) + " "
                + (z + offsets[2])
                + " run place template texturesplus:machine/" + entity + "\n");
    }

    public static void copyMachinesToData() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }

        Path runDir = MinecraftClient.getInstance().runDirectory.toPath();

        Path source = runDir.resolve(Paths.get(
                "resourcepacks", creaturesPath, "assets", "creatures", "machine"
        ));

        Path target = runDir.resolve(Paths.get(
                "saves", "TexturesPlusGenerated", "datapacks", "texturesplus",
                "data", "texturesplus", "structure", "machine"
        ));

        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(src -> {
                try {
                    Path relative = source.relativize(src);
                    Path dest = target.resolve(relative);

                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException | UncheckedIOException e) {
            System.err.println("Failed to copy machines: " + e.getMessage());
        }
    }
    public static void createCreatureRow(StringBuilder sb, TexturesPlusEntity entity, int x, int y, int z, int increment, String block)
    {
        if (entityMachinePlacements.containsKey(entity.entityType))
        {
            placeNewMachine(sb, x, y, z, entity.entityType);
        }

        for(TexturesPlusEntityNbt nbtData : entity.nbtList)
        {
            z-=increment;
            sb.append(createCreatureCommand(x,y,z,entity.entityType, nbtData.nbt,nbtData.rawnbt,block,increment-2)).append("\n");
        }
    }

    public static String createCreatureCommand(int x, int y, int z, String entity, String nbt, String rawnbt, String block, int gapsize) {
        return "function texturesplus:creatures/placeentity"  + " {gapsize:"+ gapsize +",x:"+x+",y:"+y+",z:"+z+",entity:\"" + entity + "\",nbt:\""+ nbt + "\",block:\"" + block + "\",rawnbt:\"" + rawnbt + "\"}";
    }
    static TexturesPlusEntity getEntityFromPropertyFile(Path propFile, Map<String,String> cemTexturePaths) throws IOException {
        Path resourceDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(),
                "resourcepacks");
        Path relative = resourceDir.relativize(propFile.toAbsolutePath().normalize());
        String cleanPath = relative.toString().replace(".properties", "");
        if(disallowedEntitiesBeforeParse.stream().anyMatch(cleanPath::contains))
        {
            return null;
        }

        Set<String> renamesSet = new LinkedHashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(propFile, StandardCharsets.UTF_8)) {
            reader.lines()
                    .map(String::trim)                // strip leading/trailing whitespace
                    .filter(l -> !l.isEmpty())        // ignore blank lines
                    .filter(l -> l.startsWith("name.")) // keep only “name.X=”
                    .forEach(l -> {
                        int eq = l.indexOf('=');
                        if (eq >= 0 && eq < l.length() - 1) {
                            String value = l.substring(eq + 1).trim();
                            if (!value.isEmpty()) {
                                renamesSet.add(TexturesPlusDatapackGeneralUtil.getFirstRegexMatch(value));
                            }
                        }
                    });
        }
        List<String> renamesList = new ArrayList<>(renamesSet);
        String fileName = propFile.getFileName().toString();
        String entityType = fileName.endsWith(".properties")
                ? fileName.substring(0, fileName.length() - ".properties".length())
                : fileName; // fallback if extension missing
        if(propFile.toString().contains("rabbit"))
        {
            entityType = "rabbit_" + entityType;
        }
        else if(propFile.toString().contains("ocelot"))
        {
            entityType = "ocelot";
        }
        else if(propFile.toString().contains("cat"))
        {
            entityType = "cat_" + entityType;
        }
        else if(propFile.toString().contains("villager"))
        {
            entityType = "villager_" + entityType;
        }
        else if(propFile.toString().contains("llama") && !propFile.toString().contains("trader"))
        {
            entityType = "llama_" + entityType;
        }
        renamesList.removeIf(s -> s.equalsIgnoreCase("default"));
        renamesList.removeIf(s -> s.contains("Slenderman"));
        renamesList.removeIf(s -> s.contains("optijank"));
        renamesList.removeIf(s -> s.contains("hidden_rename"));

        return createEntityFromProp(entityType, renamesList, cemTexturePaths, propFile);
    }
    /*
    THIS IS PAIN

    I could make this less hardcoded if all minecraft entities followed the format that the warm/cold/temperate mobs added in 1.21.5 do, but atm the way they are stored makes me need to do this.
     */
    static TexturesPlusEntity createEntityFromProp(String propName, List<String> renames, Map<String, String> cemTexturePaths, Path propFile) {
        final String nbtSlash = "\\\\\\\\\\\\\\";
        final String rawNbtSlash = "\\";
        String nbtString = "";
        String rawNbtString = "";
        String entityType = propName;

        if (cemTexturePaths.containsKey(propName + ".png")) {
            entityType = cemTexturePaths.get(propName + ".png").replaceAll("\\d+", "").replace(".jem", "");
            if (entityType.contains("baby") || entityType.contains("saddle") || entityType.contains("boat")) return null;
        }

        if (disallowedEntitiesAfterParse.stream().anyMatch(propName::contains) && !propName.contains("axolotl")) {
            return null;
        }

        if (propName.equals("wither") && TexturesPlusModClient.getConfig().mergeEntities) return null;

        if (propName.endsWith("_cow")) {
            entityType = "cow";
            String variant = propName.replace("_cow", "");
            nbtString = formatNbt("variant", variant, nbtSlash);
            rawNbtString = formatNbt("variant", variant, rawNbtSlash);

        }
        if (propName.equals("snow_fox")) {
            entityType = "fox";
            nbtString = formatNbt("Type", "snow", nbtSlash);
            rawNbtString = formatNbt("Type", "snow", rawNbtSlash);

        }
        if (propName.endsWith("panda")) {
            entityType = "panda";
            String gene = propName.replace("_panda", "");
            if(gene.equals("panda"))
            {
                gene = "normal";
            }
            nbtString = "HiddenGene:" + nbtSlash + "\"" + gene + nbtSlash + "\", MainGene:" + nbtSlash + "\"" + gene + nbtSlash + "\"";
            rawNbtString = "HiddenGene:" + rawNbtSlash + "\"" + gene + rawNbtSlash + "\", MainGene:" + rawNbtSlash + "\"" + gene + rawNbtSlash + "\"";
        }
        else if (propName.startsWith("cat_")) {
            entityType = "cat";
            String variant = propName.replace("cat_", "");
            nbtString = formatNbt("variant", variant, nbtSlash);
            rawNbtString = formatNbt("variant", variant, rawNbtSlash);

        } else if (propName.startsWith("villager_")) {
            entityType = "villager";
            String type = propName.replace("villager_", "");
            nbtString = "VillagerData:{type:" + nbtSlash + "\"" + type + nbtSlash + "\"}";
            rawNbtString = "VillagerData:{type:" + rawNbtSlash + "\"" + type + rawNbtSlash + "\"}";

        } else if (propName.startsWith("axolotl")) {
            if(TexturesPlusModClient.getConfig().mergeEntities)
            {
                renames.removeIf(s -> s.contains("tailed"));
            }
            entityType = "axolotl";
            int variant = selectVariant(propName, Map.of("wild", 1, "gold", 2, "cyan", 3, "blue", 4));
            nbtString = rawNbtString = "Variant:" + variant;

        } else if (propName.startsWith("parrot")) {
            entityType = "parrot";
            int variant = selectVariant(propName, Map.of("red_blue", 0, "yellow_blue", 3, "green", 2, "blue", 1, "grey", 4));
            nbtString = rawNbtString = "Variant:" + variant;

        } else if (propName.startsWith("shulker")) {
            entityType = "shulker";
            int color = selectVariant(propName, Map.ofEntries(
                    Map.entry("white", 0),
                    Map.entry("orange", 1),
                    Map.entry("magenta", 2),
                    Map.entry("light_blue", 3),
                    Map.entry("yellow", 4),
                    Map.entry("lime", 5),
                    Map.entry("pink", 6),
                    Map.entry("gray", 7),
                    Map.entry("light_gray", 8),
                    Map.entry("cyan", 9),
                    Map.entry("purple", 10),
                    Map.entry("blue", 11),
                    Map.entry("brown", 12),
                    Map.entry("green", 13),
                    Map.entry("red", 14),
                    Map.entry("black", 15)
            ));
            if(propName.equals("shulker"))
            {
                color = 16;
            }
            nbtString = rawNbtString = "Color:" + color;
        } else if (propName.startsWith("rabbit")) {
            entityType = "rabbit";
            int variant = selectVariant(propName, Map.of("white", 1, "black", 2, "white_splotched", 3, "gold", 4, "salt", 5, "evil", 99));
            nbtString = rawNbtString = "RabbitType:" + variant;

        } else if (propName.startsWith("llama")) {
            entityType = "llama";
            int variant = selectVariant(propName, Map.of("white", 1, "brown", 2, "gray", 3));
            nbtString = rawNbtString = "Variant:" + variant;

        } else if (propName.endsWith("_chicken")) {
            entityType = "chicken";
            String variant = propName.replace("_chicken", "");
            nbtString = formatNbt("variant", variant, nbtSlash);
            rawNbtString = formatNbt("variant", variant, rawNbtSlash);

        } else if (propName.equals("big_sea_turtle")) {
            entityType = "turtle";

        } else if (propName.equals("polarbear")) {
            entityType = "polar_bear";

        } else if (propName.equals("horse_skeleton")) {
            entityType = "skeleton_horse";

        } else if (propName.equals("horse_zombie")) {
            entityType = "zombie_horse";
            nbtString = rawNbtString = "Tame:1";
        } else if (propName.equals("mule")) {
            nbtString = rawNbtString = "Tame:1";
        } else if (propName.equals("donkey")) {
            nbtString = rawNbtString = "Tame:1";
        } else if (propName.startsWith("horse")) {
            entityType = "horse";
            int variant = selectVariant(propName, Map.of("creamy", 1, "chestnut", 2, "darkbrown", 6, "black", 4, "gray", 5, "brown", 3));
            nbtString = rawNbtString = "Tame:1,Variant:" + variant;

        } else if (propName.startsWith("bee_")) {
            if (TexturesPlusModClient.getConfig().mergeEntities) return null;
            entityType = "bee";
            if (propName.contains("angry") && propName.contains("nectar")) {
                nbtString = rawNbtString = "HasNectar:1,AngerTime:1000000";
            } else if (propName.contains("angry")) {
                nbtString = rawNbtString = "AngerTime:1000000";
            } else if (propName.contains("nectar")) {
                nbtString = rawNbtString = "HasNectar:1";
            }

        } else if (propName.endsWith("_pig")) {
            entityType = "pig";
            String variant = propName.replace("_pig", "");
            nbtString = formatNbt("variant", variant, nbtSlash);
            rawNbtString = formatNbt("variant", variant, rawNbtSlash);

        } else if (propName.endsWith("_frog")) {
            entityType = "frog";
            String variant = propName.replace("_frog", "");
            nbtString = formatNbt("variant", variant, nbtSlash);
            rawNbtString = formatNbt("variant", variant, rawNbtSlash);

        } else if (propName.startsWith("puffer_fish")) {
            int puffState = propName.contains("medium") ? 1 : propName.contains("large") ? 2 : 0;
            if (puffState > 0 && TexturesPlusModClient.getConfig().mergeEntities) return null;
            entityType = "pufferfish";
            nbtString = rawNbtString = "PuffState:" + puffState;

        } else if (propName.startsWith("wolf")) {
            entityType = "wolf";
            String noWolf = propName.replace("wolf_", "");
            boolean angry = noWolf.contains("angry");
            boolean tame = noWolf.contains("tame");

            if ((angry || tame) && TexturesPlusModClient.getConfig().mergeEntities) return null;

            noWolf = noWolf.replace("_angry", "").replace("_tame", "");

            if (!noWolf.isEmpty()) {
                nbtString += formatNbt("variant", noWolf, nbtSlash);
                rawNbtString += formatNbt("variant", noWolf, rawNbtSlash);
            }
            if (angry) {
                nbtString += ",AngerTime:100000";
                rawNbtString += ",AngerTime:100000";
            }
            if (tame) {
                nbtString += ",Owner:" + nbtSlash + "\"dbrighthd" + nbtSlash + "\"";
                rawNbtString += ",Owner:" + rawNbtSlash + "\"dbrighthd" + rawNbtSlash + "\"";
            }
        }

        if(!(propFile.toString().contains("cem") && parsedEntities.contains(entityType)))
        {
            renames.addFirst("Default");

        }
        if(!isValidEntityName(entityType))
        {
            LOGGER.error(entityType + " is not a valid entity type, skipping.");
            return null;
        }
        parsedEntities.add(entityType);
        List<TexturesPlusEntityNbt> nbtList = new ArrayList<>();
        for (String rename : renames) {
            String fullNbt = "CustomName:" + nbtSlash + "\"" + rename + nbtSlash + "\"," + nbtString;
            String rawFullNbt = "CustomName:" + rawNbtSlash + "\"" + rename + rawNbtSlash + "\"," + rawNbtString;
            nbtList.add(new TexturesPlusEntityNbt(fullNbt, rawFullNbt, rename));
        }

        return new TexturesPlusEntity(nbtList, entityType);
    }

    private static String formatNbt(String key, String value, String slash) {
        return key + ":" + slash + "\"" + value + slash + "\"";
    }

    public static int selectVariant(String propName, Map<String, Integer> variantMap) {
        // Sort entries by key length descending
        return variantMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                .filter(entry -> propName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0);
    }
    public static boolean isValidEntityName(String entityName) {
        Identifier id = Identifier.tryParse(entityName);
        if (id == null) return false;

        return Registries.ENTITY_TYPE.containsId(id);
    }
    public static List<String> getDisallowedEntitiesBeforeParse() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        List<String> output = new ArrayList<>();
        try {
            Path txtFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","disallowed_entities_before_parse.txt");
            output = loadListFromFile(txtFile);
        } catch (Exception e) {
            System.err.println("Could not read entity_increments.txt: " + e.getMessage());
        }
        return output;
    }

    public static List<String> getDisallowedEntitiesAfterParse() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        List<String> output = new ArrayList<>();
        try {
            Path txtFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","disallowed_entities_after_parse.txt");
            output = loadListFromFile(txtFile);
        } catch (Exception e) {
            System.err.println("Could not read entity_increments.txt: " + e.getMessage());
        }
        return output;
    }
    public static List<String> getEntitiesWithMachines() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        List<String> output = new ArrayList<>();
        try {
            Path machineFolder = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","machine");
            output = Files.list(machineFolder)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".nbt"))
                    .map(name -> name.substring(0, name.length() - 4)) // strip ".nbt"
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Could not find any nbt files" + e.getMessage());
        }
        return output;
    }
    public static Map<String, Integer> getEntityIncrements() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        Path txtFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","entity_increments.txt");

        Map<String, Integer> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(txtFile)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && line.contains(","))
                    .forEach(line -> {
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            try {
                                int value = Integer.parseInt(parts[1].trim());
                                map.put(key, value);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid number for key: " + key);
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Could not read entity_increments.txt: " + e.getMessage());
        }
        return map;
    }

    public static Map<String, Integer> getMiscArgs() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        Path txtFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","init_args.txt");

        Map<String, Integer> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(txtFile)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && line.contains(","))
                    .forEach(line -> {
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            try {
                                int value = Integer.parseInt(parts[1].trim());
                                map.put(key, value);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid number for key: " + key);
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Could not read misc_args.txt: " + e.getMessage());
        }
        return map;
    }

    public static Map<String, String> getMiscStringArgs() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
        Path txtFile = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "resourcepacks", creaturesPath, "assets", "creatures","init_string_args.txt");

        Map<String, String> map = new HashMap<>();
        try (Stream<String> lines = Files.lines(txtFile)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && line.contains(","))
                    .forEach(line -> {
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            try {
                                String value = parts[1].trim();
                                map.put(key, value);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid number for key: " + key);
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Could not read misc_args.txt: " + e.getMessage());
        }
        return map;
    }

    public static Map<String, int[]> getEntityMachineSize() {
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }

        Path txtFile = Paths.get(
                MinecraftClient.getInstance().runDirectory.getPath(),
                "resourcepacks",
                creaturesPath,
                "assets",
                "creatures",
                "machine",
                "machine_pos.txt"
        );

        Map<String, int[]> map = new HashMap<>();

        try (Stream<String> lines = Files.lines(txtFile)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && line.contains(","))
                    .forEach(line -> {
                        String[] parts = line.split(",");
                        if (parts.length >= 2) {
                            String key = parts[0].trim();

                            int[] values = new int[parts.length - 1];
                            boolean valid = true;

                            for (int i = 1; i < parts.length; i++) {
                                try {
                                    values[i - 1] = Integer.parseInt(parts[i].trim());
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid number for key: " + key +
                                            " at index " + (i - 1) +
                                            " value: " + parts[i]);
                                    valid = false;
                                    break;
                                }
                            }

                            if (valid) {
                                map.put(key, values);
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Could not read machine_pos.txt: " + e.getMessage());
        }

        return map;
    }
}
