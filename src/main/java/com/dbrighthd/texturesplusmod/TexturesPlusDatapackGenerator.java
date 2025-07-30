package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TexturesPlusDatapackGenerator {
    public static Set<String> parsedEntities;
    public static final List<String> DISALLOWED_ENTITIES_BEFORE_PARSE = List.of("respackopts","overlay", "_charging", "_shooting", "equipment","armor","rope","harness","saddle","spark","evoker_fangs","profession","collar");
    public static final List<String> DISALLOWED_ENTITIES_AFTER_PARSE = List.of("armadillo_scute","harness","emerald","stone","gold","diamond","horse_markings","spider_eyes","fox_sleep","iron_golem_crackiness","sheep_wool","enderman_eyes","boat","saddle","baby");
    public static final Map<String, Integer> ENTITY_INCREMENTS = Map.of(
            "ghast", 11,
            "camel", 4,
            "sniffer", 4
    );
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

    public static void generateCreaturesMcfunction() throws IOException {
        LOGGER.info("Generating Creatures+ placement in world...");
        // Path to your JSON file
        String creaturesPath = "creaturesplus";
        if (TexturesPlusModClient.getConfig().devMode) {
            creaturesPath = "creatures";
        }
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
        if (!TexturesPlusModClient.getConfig().sortAlphabetically)
        {
            entities.sort(byNbtSize);
            westEastEntities = splitInHalfAlternating(entities);
        }
        else
        {
            entities.sort((a, b) -> a.entityType.compareToIgnoreCase(b.entityType));
            westEastEntities = splitInHalfMiddle(entities);
        }

        List<TexturesPlusEntity> westEntities = westEastEntities.get(0);
        if(TexturesPlusModClient.getConfig().sortAlphabetically)
        {
            westEntities = westEntities.reversed();
        }
        List<TexturesPlusEntity> eastEntities = westEastEntities.get(1);

        StringBuilder sb = new StringBuilder();
        createWestCreatures(sb,westEntities);
        createEastCreatures(sb,eastEntities);
        Path functionPath = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allcreatures.mcfunction");
        Path functionPathBlocks = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "saves", "TexturesPlusGenerated","datapacks","texturesplus","data","texturesplus","function","allcreaturesblocks.mcfunction");
        Files.writeString(functionPathBlocks, sb.toString().replace("placeentity","placeblocks"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Files.writeString(functionPath, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void processCemTexture(Path file, Map<String,String> cemMap) {
        try (Reader in = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(in);
            findTextures(root, file.getFileName().toString(), cemMap);
        } catch (IOException ex) {
            // Replace with your logger of choice
            System.err.println("[TextureIndex] Couldn't read " + file + ": " + ex.getMessage());
        }
    }
    private static void findTextures(JsonElement el, String fileName, Map<String,String> cemMap) {
        if (el.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : el.getAsJsonObject().entrySet()) {
                if ("texture".equals(entry.getKey())
                        && entry.getValue().isJsonPrimitive()
                        && entry.getValue().getAsJsonPrimitive().isString()) {

                    if(!fileName.contains("baby") && !fileName.contains("saddle"))
                    {
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
    public static List<TexturesPlusEntity> mergeLikeEntities(List<TexturesPlusEntity> allEntities)
    {
        List<TexturesPlusEntity> mergedEntities = new ArrayList<>();
        for (TexturesPlusEntity entity : allEntities)
        {
            int index = indexOfEntity(mergedEntities,entity);
            if(index >= 0)
            {
                mergeEntityNoRepeat(mergedEntities.get(index), entity);
            }
            else
            {
                mergedEntities.add(entity);
            }
        }
        return mergedEntities;
    }
    public static void mergeEntityNoRepeat(TexturesPlusEntity entityToMergeInto, TexturesPlusEntity entityToMergeFrom)
    {
        for (TexturesPlusEntityNbt entityNbt : entityToMergeFrom.nbtList)
        {
            boolean isRepeat = false;
            for(TexturesPlusEntityNbt targetNbt : entityToMergeInto.nbtList)
            {
                if (entityNbt.rename.toLowerCase().equals(targetNbt.rename.toLowerCase()) && !targetNbt.rename.toLowerCase().equals("default"))
                {
                    isRepeat = true;
                    break;
                }
            }
            if(!isRepeat)
            {
                entityToMergeInto.nbtList.add(entityNbt);
            }
        }
    }
    public static int indexOfEntity(List<TexturesPlusEntity> entities, TexturesPlusEntity entityToFind)
    {
        int index = -1;
        for(TexturesPlusEntity entity : entities)
        {
            index++;
            if(entity.entityType.equals(entityToFind.entityType))
            {
                return index;
            }
        }
        return -1;
    }
    public static void createWestCreatures(StringBuilder sb, List<TexturesPlusEntity> entities)
    {
        String block = "lime_concrete";
        int x = 0;
        int y = -53;
        int z = -31;
        for(TexturesPlusEntity entity : entities)
        {
            if(Math.abs(x) / 4 >= entities.size()/3)
            {
                if(Math.abs(x) / 4 >= entities.size() * 2/3)
                {
                    block = "purple_concrete";
                }
                else
                {
                    block = "blue_concrete";
                }
            }
            x-=4;
            z-=4;
            sb.append("function texturesplus:creatures/placenewrowwest {x:" + x + ",y:" + y + ",z:" + z + ",block:\"" + block + "\"}\n");
            createCreatureRow(sb,entity,x,y,z,block);
        }
    }
    public static void createEastCreatures(StringBuilder sb, List<TexturesPlusEntity> entities)
    {
        String block = "yellow_concrete";
        int x = 0;
        int y = -53;
        int z = -31;
        for(TexturesPlusEntity entity : entities)
        {
            if(Math.abs(x) / 4 >= entities.size()/3)
            {
                if(Math.abs(x) / 4 >= entities.size() * 2/3)
                {
                    block = "red_concrete";
                }
                else
                {
                    block = "orange_concrete";
                }
            }
            x+=4;
            z-=4;
            sb.append("function texturesplus:creatures/placenewroweast {x:" + x + ",y:" + y + ",z:" + z + ",block:\"" + block + "\"}\n");
            createCreatureRow(sb,entity,x,y,z,block);
        }
    }
    public static void createCreatureRow(StringBuilder sb, TexturesPlusEntity entity, int x, int y, int z, String block)
    {
        int increment = 3;
        for (Map.Entry<String, Integer> entry : ENTITY_INCREMENTS.entrySet()) {
            if (entity.entityType.contains(entry.getKey())) {
                increment = entry.getValue();
                break;
            }
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

    public static <T> List<List<T>> splitInHalfAlternating(List<T> items) {
        List<T> evenIdx = new ArrayList<>();
        List<T> oddIdx  = new ArrayList<>();

        // Distribute items according to their position
        for (int i = 0; i < items.size(); i++) {
            if ((i & 1) == 0) {           // even index: 0, 2, 4, …
                evenIdx.add(items.get(i));
            } else {                      // odd index: 1, 3, 5, …
                oddIdx.add(items.get(i));
            }
        }

        return List.of(evenIdx, oddIdx);  // [evenIndices, oddIndices]
    }
    public static <T> List<List<T>> splitInHalfMiddle(List<T> input) {
        int size = input.size();
        int mid = size / 2;

        List<T> firstHalf = new ArrayList<>(input.subList(0, mid));
        List<T> secondHalf = new ArrayList<>(input.subList(mid, size));

        List<List<T>> result = new ArrayList<>();
        result.add(firstHalf);
        result.add(secondHalf);

        return result;
    }
    static TexturesPlusEntity getEntityFromPropertyFile(Path propFile,Map<String,String> cemTexturePaths) throws IOException {
        Path resourceDir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(),
                "resourcepacks");
        Path relative = resourceDir.relativize(propFile.toAbsolutePath().normalize());
        String cleanPath = relative.toString().replace(".properties", "");
        if(DISALLOWED_ENTITIES_BEFORE_PARSE.stream().anyMatch(cleanPath::contains))
        {
            return null;
        }

        Set<String> renamesSet = new HashSet<>();

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
                                renamesSet.add(getFirstRegexMatch(value));
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

        if (DISALLOWED_ENTITIES_AFTER_PARSE.stream().anyMatch(propName::contains) && !propName.contains("axolotl")) {
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

        } else if (propName.startsWith("horse")) {
            entityType = "horse";
            int variant = selectVariant(propName, Map.of("creamy", 1, "chestnut", 2, "darkbrown", 6, "black", 4, "gray", 5, "brown", 3));
            nbtString = rawNbtString = "Variant:" + variant;

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
    public static String getFirstRegexMatch(String pattern) {
        // Strip known prefixes
        if (pattern.startsWith("regex:") || pattern.startsWith("iregex:")) {
            pattern = pattern.substring(pattern.indexOf(':') + 1)
                    .replace(".*","")
                    .replace(".","");
        } else if (pattern.startsWith("pattern:") || pattern.startsWith("ipattern:")) {
            pattern = pattern.substring(pattern.indexOf(':') + 1)
                    .replace("*", "")
                    .replace("?", "");
        } else {
            return pattern;
        }

        // Remove leading/trailing wildcards, but keep structure
        pattern = pattern.replaceAll("^\\.*", "").replaceAll("\\.*$", "");

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            if (c == '(') {
                int depth = 1;
                int start = i + 1;
                int end = start;
                while (end < pattern.length() && depth > 0) {
                    if (pattern.charAt(end) == '(') depth++;
                    else if (pattern.charAt(end) == ')') depth--;
                    end++;
                }
                if (depth == 0) {
                    String group = pattern.substring(start, end - 1);
                    String[] options = group.split("\\|");
                    result.append(options[0].trim());  // take first option
                    i = end;
                    continue;
                } else {
                    break;  // unbalanced
                }
            } else if (c != '.' && c != '*') {
                result.append(c);
            }
            i++;
        }

        return result.toString();
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
        String modelPath;
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
            List<String> falseEnchants = enchants;
            if (node.has("type") && node.get("type").asText().contains("model")) {
                String modelPath = null;
                modelPath = node.get("model").asText();
                foundItems.add(new TexturesPlusItem(new ArrayList<String>(), firstWhen, 0, itemType, modelPath));
            }
            if (node.has("predicate") && node.get("predicate").asText().contains("minecraft:enchantments")) {
                for (JsonNode entry : node.path("value")) {
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
                        foundItems.addAll(getConditionsRecur(falseEnchants, node.get("on_true"), itemType, firstWhen, damage));
                    }
                    foundItems.addAll(getConditionsRecur(falseEnchants, node.get("on_false"), itemType, firstWhen, damage));
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
                            foundItems.add(new TexturesPlusItem(enchants, firstWhen, increment * entry.get("threshold").asInt()+1, itemType, entry.get("model").asText()));
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

    public static class TexturesPlusEntity {
        public final List<TexturesPlusEntityNbt> nbtList;
        public final String entityType;

        public TexturesPlusEntity(List<TexturesPlusEntityNbt> nbtList, String entityType) {
            this.nbtList = nbtList;
            this.entityType = entityType;
        }
    }
    public static class TexturesPlusEntityNbt {
        public final String nbt;
        public final String rawnbt;
        public final String rename;

        public TexturesPlusEntityNbt(String nbt, String rawnbt, String rename) {
            this.nbt = nbt;
            this.rawnbt = rawnbt;
            this.rename = rename;
        }
    }
}
