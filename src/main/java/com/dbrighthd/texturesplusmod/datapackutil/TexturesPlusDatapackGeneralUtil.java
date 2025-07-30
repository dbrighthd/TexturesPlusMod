package com.dbrighthd.texturesplusmod.datapackutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

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


    static String generateCommand(int x, int y, int z, String rename, String block, String direction, String command)
    {
        return "function texturesplus:place" + command + direction + " {x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block + "\"}";
    }
}
