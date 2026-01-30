package com.dbrighthd.texturesplusmod.datapack;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    static <T> Map<String, T> parseKeyValueFromLines(Function<String, T> mapper, Stream<String> lines) {
        Map<String, T> map = new HashMap<>();
        lines.map(String::trim)
                .filter(line -> line.contains(","))
                .forEach(line -> {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        T value = mapper.apply(parts[1].trim());
                        if (value == null) return; // don't populate garbage values
                        map.put(key, value);
                    }
                });
        return map;
    }

    static <T> void addAllElementsToListOfList(List<List<T>> destinationListOfList, List<T> sourceList, int groupSize)
    {
        for (int i = 0; i < sourceList.size(); i += groupSize) {
            destinationListOfList.add(new ArrayList<>(sourceList.subList(i, Math.min(i + groupSize, sourceList.size()))));
        }
    }

    static <T> Map<String, T> parseKeyValueFromLinesOrdered(Function<String, T> mapper, Stream<String> lines) {
        Map<String, T> map = new LinkedHashMap<>();
        lines.map(String::trim)
                .filter(line -> line.contains(","))
                .forEach(line -> {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        T value = mapper.apply(parts[1].trim());
                        if (value == null) return; // don't populate garbage values
                        map.put(key, value);
                    }
                });
        return map;
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

    public static void generateMapEntry(Map<String,List<TexturesPlusItem>> itemMap, Path jsonFile) {
        JsonObject root;
        try (FileReader reader = new FileReader(jsonFile.toFile())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String itemName = jsonFile.getFileName().toString().replace(".json", "");
        List<TexturesPlusItem> allRenameCases = new ArrayList<>();

        if (root.has("model") && root.getAsJsonObject("model").has("cases")) {
            JsonArray cases = root.getAsJsonObject("model").getAsJsonArray("cases");
            for (JsonElement caseElement : cases) {
                getConditions(itemName, allRenameCases, caseElement.getAsJsonObject());
            }
        }
        itemMap.put(itemName, allRenameCases);
    }

    public static void getConditions(String itemType, List<TexturesPlusItem> itemJsonData, JsonObject currCase) {
        JsonElement modelElement = currCase.get("model");
        if (modelElement == null) {
            LOGGER.error("Model wasn't found for an item in {}", itemType);
            return;
        }

        String firstWhen = null;
        JsonElement whenNode = currCase.get("when");
        if (whenNode != null  ) {
            if (whenNode.isJsonArray() && !whenNode.getAsJsonArray().isEmpty()) {
                firstWhen = whenNode.getAsJsonArray().get(0).getAsString();
            } else if (whenNode.isJsonPrimitive()) {
                firstWhen = whenNode.getAsString();
            }
        }

        JsonObject modelObj = modelElement.getAsJsonObject();
        if (modelObj.has("type") && modelObj.get("type").getAsString().contains("model")) {
            String modelPath = modelObj.get("model").getAsString();
            itemJsonData.add(new TexturesPlusItem(new ArrayList<>(), firstWhen, 0, itemType, modelPath));
        } else {
            // Recurse into the nested model structure
            List<TexturesPlusItem> itemsToAdd = getConditionsRecur(new ArrayList<>(), modelObj, itemType, firstWhen, 0);
            if (!itemsToAdd.isEmpty()) {
                itemJsonData.addAll(itemsToAdd);
            }
        }
    }
    public static List<TexturesPlusItem> getConditionsRecur(List<String> enchants, JsonObject node, String itemType, String firstWhen, int damage) {
        List<TexturesPlusItem> foundItems = new ArrayList<>();
        try {
                String type = node.has("type") ? node.get("type").getAsString() : "";

            if (type.contains("model")) {
                foundItems.add(new TexturesPlusItem(new ArrayList<>(enchants), firstWhen, damage, itemType, node.get("model").getAsString()));
            }

            if (node.has("predicate")) {
                String predicate = node.get("predicate").getAsString();
                if (predicate.contains("minecraft:enchantments") && node.has("value")) {
                    for (JsonElement entry : node.getAsJsonArray("value")) {
                        JsonObject entryObj = entry.getAsJsonObject();
                        if (entryObj.has("enchantments")) {
                            enchants.add(entryObj.get("enchantments").getAsString());
                        }
                    }
                }
                if (predicate.contains("minecraft:damage") && node.has("value")) {
                    damage = node.getAsJsonObject("value").getAsJsonObject("damage").getAsJsonObject("min").get("damage").getAsInt();
                }
            }

            if (type.contains("condition")) {
                JsonObject onTrue = node.getAsJsonObject("on_true");
                if (onTrue.has("type") && onTrue.get("type").getAsString().contains("model")) {
                    foundItems.add(new TexturesPlusItem(new ArrayList<>(enchants), firstWhen, damage, itemType, onTrue.get("model").getAsString()));
                } else {
                    if (!node.get("property").getAsString().contains("selected")) {
                        foundItems.addAll(getConditionsRecur(enchants, onTrue, itemType, firstWhen, damage));
                    }
                    foundItems.addAll(getConditionsRecur(enchants, node.getAsJsonObject("on_false"), itemType, firstWhen, damage));
                }
            }

            if (type.contains("range_dispatch")) {
                if (node.get("property").getAsString().contains("damage")) {
                    int maxDamage = getMaxDurability("minecraft:" + itemType);
                    int scale = node.get("scale").getAsInt();
                    int increment = maxDamage / scale;

                    if (node.has("entries") && node.get("entries").isJsonArray()) {
                        for (JsonElement entry : node.getAsJsonArray("entries")) {
                            JsonObject entryObj = entry.getAsJsonObject();
                            // TODO: decipher model file location. There are 2 formats I know of:
                            // TODO: {"type": "minecraft:model", "model": "<model path>"}
                            // TODO: {"type": "minecraft:select", "cases": [<models based on criteria, i.e. in inventory, on ground, etc>]}
                            foundItems.add(new TexturesPlusItem(new ArrayList<>(enchants), firstWhen, (increment * entryObj.get("threshold").getAsInt()) + 1, itemType, entryObj.get("model").toString()));
                        }
                    }
                }
            }

            if (node.has("cases")) {
                JsonArray cases = node.getAsJsonArray("cases");
                String property = node.has("property") ? node.get("property").getAsString() : "";
                boolean isGui = property.contains("display_context");
                boolean isDamage = node.has("component") && node.get("component").getAsString().contains("damage");
                boolean isTrim = property.contains("trim");
                if(isTrim)
                {
                    foundItems.add(getConditionsRecur(enchants, node.getAsJsonObject("fallback"), itemType, firstWhen, 0).getFirst());
                }
                else {
                    for (JsonElement caseElement : cases) {
                        JsonObject caseNode = caseElement.getAsJsonObject();
                        int damageNode = 0;
                        if (isDamage && caseNode.has("when")) {
                            JsonElement when = caseNode.get("when");
                            damageNode = when.isJsonArray() ? when.getAsJsonArray().get(0).getAsInt() : when.getAsInt();
                        }

                        List<TexturesPlusItem> nested = getConditionsRecur(enchants, caseNode.getAsJsonObject("model"), itemType, firstWhen, damageNode);
                        if (isGui) {
                            if (!nested.isEmpty()) foundItems.add(nested.getFirst());
                        }
                    }
                }

            }

            if (foundItems.isEmpty()) {
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

    static String generateCommand(int x, int y, int z, String rename, String block, String direction, String command) {
        return "function texturesplus:place" + command + direction + " {x: \"" + x + "\", y: \"" + y + "\", z: \"" + z + "\",rename: \"" + rename + "\",block: \"" + block + "\"}";
    }
}
