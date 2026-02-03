package com.dbrighthd.texturesplusmod.datapack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;

public class MapCategorySelector implements ItemCategorySelector {

    private final List<Pair<List<String>, ItemCategory>> patternMap = new ArrayList<>();

    public MapCategorySelector(List<Pair<List<String>, ItemCategory>> categories) {
        patternMap.addAll(categories);
    }

    @Override
    public SequencedCollection<ItemCategory> categories() {
        return patternMap.stream().map(Pair::getSecond).toList();
    }

    @Override
    public Optional<ItemCategory> select(Identifier model) {
        for (Pair<List<String>, ItemCategory> pair : patternMap) {
            List<String> patterns = pair.getFirst();
            if (patterns.isEmpty()) {
                return Optional.of(pair.getSecond());
            }
            for (String pattern : patterns) {
                if (pattern.isBlank()) {
                    return Optional.of(pair.getSecond());
                } else {
                    Identifier identifier = Identifier.tryParse(pattern);
                    if (identifier == null) { // tryParse returns null if a pattern isn't an identifier
                        if (model.getPath().toLowerCase().contains(pattern)) {
                            return Optional.of(pair.getSecond());
                        }
                    } else if (model.getNamespace().equalsIgnoreCase(identifier.getNamespace()) && model.getPath().toLowerCase().contains(identifier.getPath())) {
                        return Optional.of(pair.getSecond());
                    }
                }
            }
        }
        return Optional.empty();
    }
}
