package com.dbrighthd.texturesplusmod.datapackutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class TexturesPlusDatapackGeneralUtil {

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


    public static String getFirstRegexMatch(String pattern) {

        String lower = pattern.toLowerCase(Locale.ROOT);
        if (lower.startsWith("regex:")    || lower.startsWith("iregex:")
                || lower.startsWith("pattern:")  || lower.startsWith("ipattern:")) {
            pattern = pattern.substring(pattern.indexOf(':') + 1);
        }

        String raw = firstBranch(pattern.trim(), 0, pattern.length());

        return raw.replaceAll("\\\\([.\\[*+?()|{}^$\\\\])", "$1") // un-escape literals like "\."
                .trim();
    }

    /*More in depth regex checking*/
    private static String firstBranch(String s, int lo, int hi) {
        StringBuilder out = new StringBuilder();
        int i = lo;

        while (i < hi) {
            char c = s.charAt(i);
            if (c == '|') break;
            if (c == '(') {
                int depth = 1, j = i + 1;
                while (j < hi && depth > 0) {
                    char cj = s.charAt(j);
                    if (cj == '(') depth++;
                    else if (cj == ')') depth--;
                    j++;
                }
                if (depth == 0) {

                    out.append(firstBranch(s, i + 1, j - 1));
                    i = j;
                    continue;
                } else {
                    break;
                }
            }

            if (c == '.' || c == '*' || c == '?' || c == '+'
                    || c == '^' || c == '$') {
                i++;
                continue;
            }

            if (c == '\\' && i + 1 < hi) {
                out.append(s.charAt(i + 1));
                i += 2;
                continue;
            }

            out.append(c);
            i++;
        }
        return out.toString();
    }
    public static void generateMapEntry(Map<String,List<TexturesPlusItem>> itemMap, Path jsonFile)
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(jsonFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String itemName = jsonFile.getFileName().toString().replace(".json","");
        JsonNode cases = root.path("model").path("cases");
        List<TexturesPlusItem> allRenameCases = new ArrayList<>();
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
            LOGGER.error("Model wasn't found for an item in {}", itemType);
            return;
        }
        String firstWhen = null;
        String modelPath;
        JsonNode whenNode = currCase.get("when");
        if (whenNode.isArray() && !whenNode.isEmpty()) {
            firstWhen = whenNode.get(0).asText();
        }
        else if (whenNode.isTextual()) {
            firstWhen = whenNode.asText();
        }
        if(node.get("type").asText().contains("model"))
        {
            modelPath = node.get("model").asText();
            itemJsonData.add(new TexturesPlusItem(new ArrayList<>(), firstWhen, 0, itemType, modelPath));
        }
        else
        {
            if(currCase.has("model"))
            {
                currCase = currCase.get("model");
            }

            List<TexturesPlusItem> itemsToAdd = getConditionsRecur(new ArrayList<>(), currCase, itemType, firstWhen, 0);
            if(!itemsToAdd.isEmpty())
            {
                itemJsonData.addAll(itemsToAdd);
            }
        }

    }
    public static List<TexturesPlusItem> getConditionsRecur(List<String> enchants, JsonNode node, String itemType, String firstWhen, int damage)
    {
        List<TexturesPlusItem> foundItems = new ArrayList<>();
        try {
            if (node.has("type") && node.get("type").asText().contains("model")) {
                String modelPath = node.get("model").asText();
                foundItems.add(new TexturesPlusItem(new ArrayList<>(), firstWhen, 0, itemType, modelPath));
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
                LOGGER.error("Failed to find model in item {}", firstWhen);
            }
            return foundItems;
        } catch (Exception e) {
            LOGGER.error("Failed to fully parse item", e);
            return foundItems;
        }

    }
    public static int getMaxDurability(String itemId) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        return new ItemStack(item).getMaxDamage();
    }


    static String generateCommand(int x, int y, int z, String rename, String block, String direction, String command)
    {
        return "function texturesplus:place" + command + direction + " {x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block + "\"}";
    }
}
